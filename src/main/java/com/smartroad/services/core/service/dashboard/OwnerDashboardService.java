package com.smartroad.services.core.service.dashboard;

import com.smartroad.services.common.enums.contracts.ContractStatusEnum;
import com.smartroad.services.common.enums.purchase.PurchaseOrderStatusEnum;
import com.smartroad.services.core.dto.dashboard.OwnerDashboardResponseDTO;
import com.smartroad.services.domain.repository.ContractRepository;
import com.smartroad.services.domain.repository.ProjectRepository;
import com.smartroad.services.domain.repository.PurchaseOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Service layer for the executive owner dashboard.
 * Aggregates portfolio, contract, financial, and deadline metrics for an
 * organization directly from persisted project, contract, and procurement records.
 *
 * @author Vishal
 * @version 1.0
 */
@Service
public class OwnerDashboardService {

    private static final int UPCOMING_WINDOW_DAYS = 30;

    private static final List<PurchaseOrderStatusEnum> COMMITTED_ORDER_STATUSES =
            List.of(PurchaseOrderStatusEnum.ORDERED,
                    PurchaseOrderStatusEnum.PARTIALLY_RECEIVED,
                    PurchaseOrderStatusEnum.RECEIVED);

    private static final List<PurchaseOrderStatusEnum> AWAITING_DELIVERY_STATUSES =
            List.of(PurchaseOrderStatusEnum.ORDERED,
                    PurchaseOrderStatusEnum.PARTIALLY_RECEIVED);

    private final ProjectRepository projectRepository;
    private final ContractRepository contractRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    /**
     * Constructs the service with the repositories it aggregates over.
     *
     * @param projectRepository the project repository
     * @param contractRepository the contract repository
     * @param purchaseOrderRepository the purchase order repository
     */
    public OwnerDashboardService(ProjectRepository projectRepository,
                                 ContractRepository contractRepository,
                                 PurchaseOrderRepository purchaseOrderRepository) {
        this.projectRepository = projectRepository;
        this.contractRepository = contractRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    /**
     * Builds the complete owner dashboard for an organization.
     *
     * @param organizationId the organization UUID to report on
     * @return the aggregated {@link OwnerDashboardResponseDTO}
     */
    @Transactional(readOnly = true)
    public OwnerDashboardResponseDTO getOwnerDashboard(UUID organizationId) {
        Date today = new Date();
        Date windowEnd = Date.from(Instant.now().plus(UPCOMING_WINDOW_DAYS, ChronoUnit.DAYS));

        return new OwnerDashboardResponseDTO(
                buildPortfolioSummary(organizationId),
                buildContractSummary(organizationId),
                buildFinancialSummary(organizationId),
                buildDeadlineSummary(organizationId, today, windowEnd)
        );
    }

    /**
     * Aggregates project counts and average delivery progress.
     *
     * @param organizationId the organization UUID
     * @return the portfolio section of the dashboard
     */
    private OwnerDashboardResponseDTO.PortfolioSummary buildPortfolioSummary(UUID organizationId) {
        Double averageProgress = projectRepository.averageProgressByOrganization(organizationId);

        return new OwnerDashboardResponseDTO.PortfolioSummary(
                projectRepository.countProjectsByOrganization(organizationId),
                projectRepository.countActiveProjectsByOrganization(organizationId),
                projectRepository.countOnHoldProjectsByOrganization(organizationId),
                projectRepository.countCompletedProjectsByOrganization(organizationId),
                averageProgress == null ? 0d : averageProgress
        );
    }

    /**
     * Aggregates contract counts and contracted business value.
     *
     * @param organizationId the organization UUID
     * @return the contract section of the dashboard
     */
    private OwnerDashboardResponseDTO.ContractSummary buildContractSummary(UUID organizationId) {
        return new OwnerDashboardResponseDTO.ContractSummary(
                contractRepository.countByOrganization(organizationId),
                contractRepository.countByOrganizationAndStatus(organizationId, ContractStatusEnum.ACTIVE),
                zeroIfNull(contractRepository.sumContractValueByOrganization(organizationId)),
                zeroIfNull(contractRepository.sumContractValueByOrganizationAndStatus(organizationId, ContractStatusEnum.ACTIVE))
        );
    }

    /**
     * Aggregates approved budget, incurred cost, and committed procurement spend.
     * Budget variance is the approved budget minus the cost incurred so far, so a
     * negative figure means the organization has spent beyond its approved budget.
     *
     * @param organizationId the organization UUID
     * @return the financial section of the dashboard
     */
    private OwnerDashboardResponseDTO.FinancialSummary buildFinancialSummary(UUID organizationId) {
        BigDecimal totalBudget = zeroIfNull(projectRepository.sumBudgetByOrganization(organizationId));
        BigDecimal totalActualCost = zeroIfNull(projectRepository.sumActualCostByOrganization(organizationId));

        return new OwnerDashboardResponseDTO.FinancialSummary(
                totalBudget,
                totalActualCost,
                totalBudget.subtract(totalActualCost),
                projectRepository.countOverBudgetProjectsByOrganization(organizationId),
                zeroIfNull(purchaseOrderRepository.sumAmountByOrganizationAndStatuses(organizationId, COMMITTED_ORDER_STATUSES)),
                zeroIfNull(purchaseOrderRepository.sumAmountByOrganizationAndStatuses(organizationId, AWAITING_DELIVERY_STATUSES))
        );
    }

    /**
     * Defaults an absent aggregate to zero, since SQL sums return null rather than
     * zero when no row matches the filter.
     *
     * @param value the aggregate value returned by the repository, possibly null
     * @return the value itself, or {@link BigDecimal#ZERO} when it is null
     */
    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * Aggregates overdue and upcoming project, contract, and delivery commitments.
     *
     * @param organizationId the organization UUID
     * @param today the reference date for overdue comparisons
     * @param windowEnd the end of the upcoming-commitment window
     * @return the deadline section of the dashboard
     */
    private OwnerDashboardResponseDTO.DeadlineSummary buildDeadlineSummary(UUID organizationId,
                                                                          Date today,
                                                                          Date windowEnd) {
        return new OwnerDashboardResponseDTO.DeadlineSummary(
                projectRepository.countOverdueProjectsByOrganization(organizationId, today),
                projectRepository.countProjectsDueBetween(organizationId, today, windowEnd),
                contractRepository.countExpiringBetween(organizationId, ContractStatusEnum.ACTIVE, today, windowEnd),
                purchaseOrderRepository.countOverdueDeliveriesByOrganization(organizationId, AWAITING_DELIVERY_STATUSES, today)
        );
    }
}
