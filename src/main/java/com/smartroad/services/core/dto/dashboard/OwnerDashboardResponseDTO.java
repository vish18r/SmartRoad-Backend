package com.smartroad.services.core.dto.dashboard;

import java.math.BigDecimal;

/**
 * Executive owner dashboard response containing aggregated portfolio, contract,
 * financial, and deadline metrics for a single organization.
 * Every value is derived from persisted records; no figure is estimated or sampled.
 *
 * @author Vishal
 * @version 1.0
 */
public record OwnerDashboardResponseDTO(
        PortfolioSummary portfolio,
        ContractSummary contracts,
        FinancialSummary financials,
        DeadlineSummary deadlines
) {

    /**
     * Project portfolio counts and overall delivery progress.
     */
    public record PortfolioSummary(
            long totalProjects,
            long activeProjects,
            long onHoldProjects,
            long completedProjects,
            double averageProgress
    ) {}

    /**
     * Contract counts and contracted business value.
     */
    public record ContractSummary(
            long totalContracts,
            long activeContracts,
            BigDecimal totalContractValue,
            BigDecimal activeContractValue
    ) {}

    /**
     * Budget, incurred cost, and committed procurement spend.
     */
    public record FinancialSummary(
            BigDecimal totalBudget,
            BigDecimal totalActualCost,
            BigDecimal budgetVariance,
            long overBudgetProjects,
            BigDecimal committedProcurement,
            BigDecimal procurementAwaitingDelivery
    ) {}

    /**
     * Overdue and upcoming delivery commitments.
     */
    public record DeadlineSummary(
            long overdueProjects,
            long projectsDueIn30Days,
            long contractsExpiringIn30Days,
            long deliveriesOverdue
    ) {}
}
