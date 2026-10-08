package com.smartroad.services.core.service.payroll;

import com.smartroad.services.common.enums.payroll.PayrollStatusEnum;
import com.smartroad.services.common.enums.workers.WageTypeEnum;
import com.smartroad.services.common.enums.workers.WorkerStatusEnum;
import com.smartroad.services.common.exception.ApplicationLayer;
import com.smartroad.services.common.exception.ErrorCodeMapping;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.payroll.PayrollAllowanceDTO;
import com.smartroad.services.core.dto.payroll.PayrollCalculateRequestDTO;
import com.smartroad.services.core.dto.payroll.PayrollComputationDTO;
import com.smartroad.services.core.dto.payroll.PayrollDeductionDTO;
import com.smartroad.services.core.dto.payroll.PayrollPageResponseDTO;
import com.smartroad.services.core.dto.payroll.PayrollRequestDTO;
import com.smartroad.services.core.dto.payroll.PayrollResponseDTO;
import com.smartroad.services.core.dto.payroll.PayrollSummaryResponseDTO;
import com.smartroad.services.core.dto.material.PaginationMetaDTO;
import com.smartroad.services.core.mapper.payroll.PayrollMapper;
import com.smartroad.services.core.service.organization.OrganizationService;
import com.smartroad.services.core.service.workers.WorkerService;
import com.smartroad.services.domain.entity.payroll.PayrollAllowanceEntity;
import com.smartroad.services.domain.entity.payroll.PayrollDeductionEntity;
import com.smartroad.services.domain.entity.payroll.PayrollEntity;
import com.smartroad.services.domain.entity.project.ProjectEntity;
import com.smartroad.services.domain.entity.worker.WorkerEntity;
import com.smartroad.services.domain.repository.ProjectRepository;
import com.smartroad.services.domain.repository.payroll.PayrollAllowanceRepository;
import com.smartroad.services.domain.repository.payroll.PayrollDeductionRepository;
import com.smartroad.services.domain.repository.payroll.PayrollRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for payroll business logic: calculation previews, persistence and the
 * DRAFT &rarr; PROCESSED &rarr; APPROVED &rarr; PAID status workflow. Every monetary and
 * attendance-derived figure is produced by {@link PayrollCalculationService}; this service never
 * accepts a total, status or worker identity from the caller without validating it first.
 *
 * @author Vishal
 * @version 1.0
 */
@Service
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final PayrollAllowanceRepository allowanceRepository;
    private final PayrollDeductionRepository deductionRepository;
    private final ProjectRepository projectRepository;
    private final WorkerService workerService;
    private final OrganizationService organizationService;
    private final PayrollCalculationService calculationService;
    private final PayrollMapper payrollMapper;

    /**
     * Constructs the service with required dependencies.
     *
     * @param payrollRepository the payroll repository
     * @param allowanceRepository the payroll allowance repository
     * @param deductionRepository the payroll deduction repository
     * @param projectRepository the project repository, used to resolve a worker's site name
     * @param workerService the worker service, reused for worker lookup and authorization
     * @param organizationService the organization service
     * @param calculationService the centralized payroll calculation engine
     * @param payrollMapper the payroll response assembler
     */
    public PayrollService(PayrollRepository payrollRepository, PayrollAllowanceRepository allowanceRepository,
                          PayrollDeductionRepository deductionRepository, ProjectRepository projectRepository,
                          WorkerService workerService, OrganizationService organizationService,
                          PayrollCalculationService calculationService, PayrollMapper payrollMapper) {
        this.payrollRepository = payrollRepository;
        this.allowanceRepository = allowanceRepository;
        this.deductionRepository = deductionRepository;
        this.projectRepository = projectRepository;
        this.workerService = workerService;
        this.organizationService = organizationService;
        this.calculationService = calculationService;
        this.payrollMapper = payrollMapper;
    }

    /**
     * Calculates a payroll preview for a worker and period without persisting anything.
     *
     * @param userId the UUID of the requesting user
     * @param request the calculation request
     * @return the computed, unsaved {@link PayrollResponseDTO}
     * @throws SmartRoadException if the worker is unknown, inactive, unauthorized, the period is
     *                            invalid, or the worker's wage configuration is incomplete
     */
    @Transactional(readOnly = true)
    public PayrollResponseDTO calculate(UUID userId, PayrollCalculateRequestDTO request) throws SmartRoadException {
        WorkerEntity worker = workerService.requireWorkerAccess(userId, request.workerId());
        requireActiveWorker(worker);
        requireValidPeriod(request.startDate(), request.endDate());

        PayrollComputationDTO computation = calculationService.calculate(userId, worker, request.startDate(), request.endDate(),
                request.regularHoursPerDay(), request.overtimeRate(), request.allowances(), request.deductions());

        return payrollMapper.toPreviewResponseDTO(worker, siteName(worker), request.startDate(), request.endDate(),
                computation, request.allowances(), request.deductions());
    }

    /**
     * Creates and persists a new payroll in DRAFT status.
     *
     * @param userId the UUID of the user creating the payroll
     * @param request the payroll creation request
     * @return the persisted {@link PayrollResponseDTO}
     * @throws SmartRoadException if the worker is unknown, inactive, unauthorized, the period is
     *                            invalid or overlaps an existing payroll, or the wage configuration
     *                            is incomplete
     */
    @Transactional
    public PayrollResponseDTO create(UUID userId, PayrollRequestDTO request) throws SmartRoadException {
        WorkerEntity worker = workerService.requireWorkerAccess(userId, request.workerId());
        requireActiveWorker(worker);
        requireValidPeriod(request.startDate(), request.endDate());
        requireNoOverlap(worker.getId(), request.startDate(), request.endDate(), null);

        PayrollComputationDTO computation = calculationService.calculate(userId, worker, request.startDate(), request.endDate(),
                request.regularHoursPerDay(), request.overtimeRate(), request.allowances(), request.deductions());

        PayrollEntity entity = buildEntity(worker, request.startDate(), request.endDate(), computation);
        entity.setStatus(PayrollStatusEnum.DRAFT);
        entity.setCreatedBy(userId);
        entity.setModifiedBy(userId);
        payrollRepository.save(entity);

        replaceLineItems(entity.getId(), userId, request.allowances(), request.deductions());

        return toResponse(entity, worker);
    }

    /**
     * Updates a payroll while it is still in DRAFT status, recalculating every figure from the
     * current wage configuration and attendance.
     *
     * @param userId the UUID of the user updating the payroll
     * @param id the payroll UUID
     * @param request the payroll update request
     * @return the updated {@link PayrollResponseDTO}
     * @throws SmartRoadException if the payroll or worker is not found, the caller is not
     *                            authorized, the payroll is not in DRAFT status, the period is
     *                            invalid or overlaps another payroll, or the wage configuration
     *                            is incomplete
     */
    @Transactional
    public PayrollResponseDTO update(UUID userId, UUID id, PayrollRequestDTO request) throws SmartRoadException {
        PayrollEntity entity = requirePayroll(userId, id);
        requireStatus(entity, PayrollStatusEnum.DRAFT, "payroll.not.editable");

        WorkerEntity worker = workerService.requireWorkerAccess(userId, request.workerId());
        requireActiveWorker(worker);
        requireValidPeriod(request.startDate(), request.endDate());
        requireNoOverlap(worker.getId(), request.startDate(), request.endDate(), entity.getId());

        PayrollComputationDTO computation = calculationService.calculate(userId, worker, request.startDate(), request.endDate(),
                request.regularHoursPerDay(), request.overtimeRate(), request.allowances(), request.deductions());

        applyComputation(entity, worker, request.startDate(), request.endDate(), computation);
        entity.setModifiedBy(userId);
        payrollRepository.save(entity);

        replaceLineItems(entity.getId(), userId, request.allowances(), request.deductions());

        return toResponse(entity, worker);
    }

    /**
     * Processes a DRAFT payroll, locking its already-calculated figures from further routine
     * edits.
     *
     * @param userId the UUID of the user processing the payroll
     * @param id the payroll UUID
     * @return the updated {@link PayrollResponseDTO}
     * @throws SmartRoadException if the payroll is not found, the caller is not authorized, or
     *                            the payroll is not in DRAFT status
     */
    @Transactional
    public PayrollResponseDTO process(UUID userId, UUID id) throws SmartRoadException {
        PayrollEntity entity = requirePayroll(userId, id);
        requireStatus(entity, PayrollStatusEnum.DRAFT, "payroll.status.invalid.transition");
        entity.setStatus(PayrollStatusEnum.PROCESSED);
        entity.setProcessedAt(OffsetDateTime.now());
        entity.setModifiedBy(userId);
        payrollRepository.save(entity);
        return toResponse(entity, loadWorker(entity.getWorkerId()));
    }

    /**
     * Approves a PROCESSED payroll.
     *
     * @param userId the UUID of the user approving the payroll
     * @param id the payroll UUID
     * @return the updated {@link PayrollResponseDTO}
     * @throws SmartRoadException if the payroll is not found, the caller is not authorized, or
     *                            the payroll is not in PROCESSED status
     */
    @Transactional
    public PayrollResponseDTO approve(UUID userId, UUID id) throws SmartRoadException {
        PayrollEntity entity = requirePayroll(userId, id);
        requireStatus(entity, PayrollStatusEnum.PROCESSED, "payroll.status.invalid.transition");
        entity.setStatus(PayrollStatusEnum.APPROVED);
        entity.setApprovedAt(OffsetDateTime.now());
        entity.setModifiedBy(userId);
        payrollRepository.save(entity);
        return toResponse(entity, loadWorker(entity.getWorkerId()));
    }

    /**
     * Marks an APPROVED payroll as paid, finalizing it.
     *
     * @param userId the UUID of the user marking the payroll paid
     * @param id the payroll UUID
     * @return the updated {@link PayrollResponseDTO}
     * @throws SmartRoadException if the payroll is not found, the caller is not authorized, or
     *                            the payroll is not in APPROVED status
     */
    @Transactional
    public PayrollResponseDTO markPaid(UUID userId, UUID id) throws SmartRoadException {
        PayrollEntity entity = requirePayroll(userId, id);
        requireStatus(entity, PayrollStatusEnum.APPROVED, "payroll.status.invalid.transition");
        entity.setStatus(PayrollStatusEnum.PAID);
        entity.setPaidAt(OffsetDateTime.now());
        entity.setModifiedBy(userId);
        payrollRepository.save(entity);
        return toResponse(entity, loadWorker(entity.getWorkerId()));
    }

    /**
     * Cancels a payroll that has not yet been paid.
     *
     * @param userId the UUID of the user cancelling the payroll
     * @param id the payroll UUID
     * @return the updated {@link PayrollResponseDTO}
     * @throws SmartRoadException if the payroll is not found, the caller is not authorized, or
     *                            the payroll is already paid or already cancelled
     */
    @Transactional
    public PayrollResponseDTO cancel(UUID userId, UUID id) throws SmartRoadException {
        PayrollEntity entity = requirePayroll(userId, id);
        if (entity.getStatus() == PayrollStatusEnum.PAID) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "payroll.already.paid");
        }
        if (entity.getStatus() == PayrollStatusEnum.CANCELLED) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "payroll.already.cancelled");
        }
        entity.setStatus(PayrollStatusEnum.CANCELLED);
        entity.setModifiedBy(userId);
        payrollRepository.save(entity);
        return toResponse(entity, loadWorker(entity.getWorkerId()));
    }

    /**
     * Retrieves one payroll's full details.
     *
     * @param userId the UUID of the requesting user
     * @param id the payroll UUID
     * @return the {@link PayrollResponseDTO}
     * @throws SmartRoadException if the payroll is not found or the caller is not authorized
     */
    @Transactional(readOnly = true)
    public PayrollResponseDTO getById(UUID userId, UUID id) throws SmartRoadException {
        PayrollEntity entity = requirePayroll(userId, id);
        return toResponse(entity, loadWorker(entity.getWorkerId()));
    }

    /**
     * Retrieves a worker's full payroll history, most recent period first.
     *
     * @param userId the UUID of the requesting user
     * @param workerId the worker UUID
     * @return the worker's payrolls, newest period first
     * @throws SmartRoadException if the worker is not found or the caller is not authorized
     */
    @Transactional(readOnly = true)
    public List<PayrollResponseDTO> historyForWorker(UUID userId, UUID workerId) throws SmartRoadException {
        WorkerEntity worker = workerService.requireWorkerAccess(userId, workerId);
        return payrollRepository.findByWorkerIdOrderByPayrollStartDateDesc(workerId).stream()
                .map(entity -> toResponse(entity, worker))
                .toList();
    }

    /**
     * Retrieves a page of an organization's payrolls matching the optional filters.
     *
     * @param userId the UUID of the requesting user
     * @param organizationId the organization UUID
     * @param workerId the worker UUID filter, or null
     * @param employeeId the employee ID filter, or null
     * @param status the payroll status filter, or null
     * @param wageType the wage type filter, or null
     * @param startDate periods ending on or after this date, or null
     * @param endDate periods starting on or before this date, or null
     * @param search a free-text term matched against employee ID, or null
     * @param page the one-based page number
     * @param limit the page size
     * @return the matching {@link PayrollPageResponseDTO}
     * @throws SmartRoadException if the caller is not authorized for the organization
     */
    @Transactional(readOnly = true)
    public PayrollPageResponseDTO list(UUID userId, UUID organizationId, UUID workerId, String employeeId,
                                       PayrollStatusEnum status, WageTypeEnum wageType, LocalDate startDate,
                                       LocalDate endDate, String search, int page, int limit) throws SmartRoadException {
        organizationService.requireMember(userId, organizationId);

        int pageIndex = Math.max(page, 1) - 1;
        int pageSize = Math.max(limit, 1);
        Page<PayrollEntity> results = payrollRepository.search(organizationId, workerId, employeeId, status, wageType,
                startDate, endDate, search, PageRequest.of(pageIndex, pageSize));

        Map<UUID, WorkerEntity> workers = workerService.findAllById(
                results.getContent().stream().map(PayrollEntity::getWorkerId).collect(Collectors.toSet()));

        List<PayrollResponseDTO> data = results.getContent().stream()
                .map(entity -> toResponse(entity, workers.get(entity.getWorkerId())))
                .toList();

        PaginationMetaDTO pagination = new PaginationMetaDTO(pageIndex + 1, pageSize, results.getTotalElements(),
                results.getTotalPages(), results.hasNext(), results.hasPrevious());

        return new PayrollPageResponseDTO(data, pagination);
    }

    /**
     * Retrieves aggregated payroll figures for the dashboard summary cards.
     *
     * @param userId the UUID of the requesting user
     * @param organizationId the organization UUID
     * @param workerId the worker UUID filter, or null
     * @param status the payroll status filter, or null
     * @param wageType the wage type filter, or null
     * @param startDate periods ending on or after this date, or null
     * @param endDate periods starting on or before this date, or null
     * @return the {@link PayrollSummaryResponseDTO}
     * @throws SmartRoadException if the caller is not authorized for the organization
     */
    @Transactional(readOnly = true)
    public PayrollSummaryResponseDTO summary(UUID userId, UUID organizationId, UUID workerId, PayrollStatusEnum status,
                                             WageTypeEnum wageType, LocalDate startDate, LocalDate endDate) throws SmartRoadException {
        organizationService.requireMember(userId, organizationId);

        long totalWorkers = payrollRepository.countDistinctWorkers(organizationId, workerId, status, wageType, startDate, endDate);
        BigDecimal gross = nonNull(payrollRepository.sumGrossPay(organizationId, workerId, status, wageType, startDate, endDate));
        BigDecimal deductions = nonNull(payrollRepository.sumTotalDeductions(organizationId, workerId, status, wageType, startDate, endDate));
        BigDecimal net = nonNull(payrollRepository.sumNetPay(organizationId, workerId, status, wageType, startDate, endDate));
        long pending = countPending(organizationId, workerId, wageType, startDate, endDate);
        long paid = payrollRepository.countByStatus(organizationId, workerId, wageType, startDate, endDate, PayrollStatusEnum.PAID);

        return new PayrollSummaryResponseDTO(totalWorkers, gross, deductions, net, pending, paid);
    }

    /**
     * Counts payrolls still awaiting payment: in DRAFT, PROCESSED or APPROVED status.
     *
     * @param organizationId the organization UUID
     * @param workerId the worker UUID filter, or null
     * @param wageType the wage type filter, or null
     * @param startDate periods ending on or after this date, or null
     * @param endDate periods starting on or before this date, or null
     * @return the count of pending payrolls
     */
    private long countPending(UUID organizationId, UUID workerId, WageTypeEnum wageType, LocalDate startDate, LocalDate endDate) {
        long draft = payrollRepository.countByStatus(organizationId, workerId, wageType, startDate, endDate, PayrollStatusEnum.DRAFT);
        long processed = payrollRepository.countByStatus(organizationId, workerId, wageType, startDate, endDate, PayrollStatusEnum.PROCESSED);
        long approved = payrollRepository.countByStatus(organizationId, workerId, wageType, startDate, endDate, PayrollStatusEnum.APPROVED);
        return draft + processed + approved;
    }

    /**
     * Loads a payroll and verifies the caller belongs to the organization that owns it.
     *
     * @param userId the UUID of the requesting user
     * @param id the payroll UUID
     * @return the {@link PayrollEntity}
     * @throws SmartRoadException if the payroll is not found or the caller is not authorized
     */
    private PayrollEntity requirePayroll(UUID userId, UUID id) throws SmartRoadException {
        PayrollEntity entity = payrollRepository.findById(id)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.DAO_NOT_FOUND, "payroll.not.found"));
        organizationService.requireMember(userId, entity.getOrganizationId());
        return entity;
    }

    /**
     * Loads a worker by id without re-checking authorization, for use after a payroll has
     * already been authorized.
     *
     * @param workerId the worker UUID
     * @return the {@link WorkerEntity}, or null if the worker no longer exists
     */
    private WorkerEntity loadWorker(UUID workerId) {
        return workerService.findAllById(List.of(workerId)).get(workerId);
    }

    /**
     * Requires a worker to be active and not deleted.
     *
     * @param worker the worker to check
     * @throws SmartRoadException if the worker is inactive or deleted
     */
    private void requireActiveWorker(WorkerEntity worker) throws SmartRoadException {
        if (Boolean.TRUE.equals(worker.getIsDeleted()) || worker.getStatus() != WorkerStatusEnum.ACTIVE) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "worker.not.active");
        }
    }

    /**
     * Requires a payroll period's end date to be on or after its start date.
     *
     * @param startDate the period start date
     * @param endDate the period end date
     * @throws SmartRoadException if the end date is before the start date
     */
    private void requireValidPeriod(LocalDate startDate, LocalDate endDate) throws SmartRoadException {
        if (endDate.isBefore(startDate)) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "payroll.period.invalid");
        }
    }

    /**
     * Requires that no other non-cancelled payroll of this worker overlaps the candidate
     * period.
     *
     * @param workerId the worker UUID
     * @param startDate the candidate period's start date
     * @param endDate the candidate period's end date
     * @param excludeId a payroll id to leave out of the check (the payroll being updated), or null
     * @throws SmartRoadException if an overlapping payroll already exists
     */
    private void requireNoOverlap(UUID workerId, LocalDate startDate, LocalDate endDate, UUID excludeId) throws SmartRoadException {
        if (!payrollRepository.findOverlapping(workerId, startDate, endDate, excludeId).isEmpty()) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "payroll.period.overlap");
        }
    }

    /**
     * Requires a payroll to be in an expected status.
     *
     * @param entity the payroll entity
     * @param expected the status the payroll must be in
     * @param messageKey the message key to raise when the status does not match
     * @throws SmartRoadException if the payroll is not in the expected status
     */
    private void requireStatus(PayrollEntity entity, PayrollStatusEnum expected, String messageKey) throws SmartRoadException {
        if (entity.getStatus() != expected) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, messageKey);
        }
    }

    /**
     * Builds a new payroll entity from a worker's wage configuration and a computation result.
     *
     * @param worker the worker the payroll is for
     * @param startDate the first day of the payroll period, inclusive
     * @param endDate the last day of the payroll period, inclusive
     * @param computation the computed figures
     * @return the new {@link PayrollEntity}, not yet persisted
     */
    private PayrollEntity buildEntity(WorkerEntity worker, LocalDate startDate, LocalDate endDate, PayrollComputationDTO computation) {
        PayrollEntity entity = new PayrollEntity();
        entity.setOrganizationId(worker.getOrganizationId());
        entity.setEmployeeId(worker.getEmployeeId());
        applyComputation(entity, worker, startDate, endDate, computation);
        return entity;
    }

    /**
     * Applies a worker's wage configuration, a payroll period and a computation result onto a
     * payroll entity.
     *
     * @param entity the payroll entity to update
     * @param worker the worker the payroll is for
     * @param startDate the first day of the payroll period, inclusive
     * @param endDate the last day of the payroll period, inclusive
     * @param computation the computed figures
     */
    private void applyComputation(PayrollEntity entity, WorkerEntity worker, LocalDate startDate, LocalDate endDate,
                                  PayrollComputationDTO computation) {
        entity.setWorkerId(worker.getId());
        entity.setPayrollStartDate(startDate);
        entity.setPayrollEndDate(endDate);
        entity.setWageType(worker.getWageType());
        entity.setDailyWage(worker.getDailyWage());
        entity.setMonthlyWage(worker.getMonthlyWage());
        entity.setPaidDays(computation.paidDays());
        entity.setAbsentDays(computation.absentDays());
        entity.setTotalWorkedHours(computation.totalWorkedHours());
        entity.setRegularHours(computation.regularHours());
        entity.setOvertimeHours(computation.overtimeHours());
        entity.setOvertimeRate(computation.overtimeRate());
        entity.setBasicWage(computation.basicWage());
        entity.setOvertimeAmount(computation.overtimeAmount());
        entity.setTotalAllowances(computation.totalAllowances());
        entity.setGrossPay(computation.grossPay());
        entity.setTotalDeductions(computation.totalDeductions());
        entity.setNetPay(computation.netPay());
    }

    /**
     * Replaces a payroll's allowance and deduction lines with a fresh set, so an update never
     * needs to reconcile individual rows.
     *
     * @param payrollId the payroll UUID
     * @param userId the UUID of the user saving the lines
     * @param allowances the new allowance lines, or null for none
     * @param deductions the new deduction lines, or null for none
     */
    private void replaceLineItems(UUID payrollId, UUID userId, List<PayrollAllowanceDTO> allowances,
                                  List<PayrollDeductionDTO> deductions) {
        allowanceRepository.deleteByPayrollId(payrollId);
        deductionRepository.deleteByPayrollId(payrollId);

        if (allowances != null && !allowances.isEmpty()) {
            allowanceRepository.saveAll(allowances.stream().map(dto -> toAllowanceEntity(payrollId, userId, dto)).toList());
        }
        if (deductions != null && !deductions.isEmpty()) {
            deductionRepository.saveAll(deductions.stream().map(dto -> toDeductionEntity(payrollId, userId, dto)).toList());
        }
    }

    /**
     * Builds a new allowance entity for a payroll.
     *
     * @param payrollId the owning payroll UUID
     * @param userId the UUID of the user creating the line
     * @param dto the allowance data
     * @return the new {@link PayrollAllowanceEntity}, not yet persisted
     */
    private PayrollAllowanceEntity toAllowanceEntity(UUID payrollId, UUID userId, PayrollAllowanceDTO dto) {
        PayrollAllowanceEntity entity = new PayrollAllowanceEntity();
        entity.setPayrollId(payrollId);
        entity.setAllowanceType(dto.allowanceType());
        entity.setDescription(dto.description());
        entity.setAmount(dto.amount());
        entity.setCreatedBy(userId);
        entity.setModifiedBy(userId);
        return entity;
    }

    /**
     * Builds a new deduction entity for a payroll.
     *
     * @param payrollId the owning payroll UUID
     * @param userId the UUID of the user creating the line
     * @param dto the deduction data
     * @return the new {@link PayrollDeductionEntity}, not yet persisted
     */
    private PayrollDeductionEntity toDeductionEntity(UUID payrollId, UUID userId, PayrollDeductionDTO dto) {
        PayrollDeductionEntity entity = new PayrollDeductionEntity();
        entity.setPayrollId(payrollId);
        entity.setDeductionType(dto.deductionType());
        entity.setDescription(dto.description());
        entity.setAmount(dto.amount());
        entity.setCreatedBy(userId);
        entity.setModifiedBy(userId);
        return entity;
    }

    /**
     * Assembles a payroll's response DTO, loading its allowance and deduction lines and
     * resolving its worker's site name.
     *
     * @param entity the payroll entity
     * @param worker the payroll's worker, or null if no longer found
     * @return the assembled {@link PayrollResponseDTO}
     */
    private PayrollResponseDTO toResponse(PayrollEntity entity, WorkerEntity worker) {
        List<PayrollAllowanceEntity> allowances = allowanceRepository.findByPayrollId(entity.getId());
        List<PayrollDeductionEntity> deductions = deductionRepository.findByPayrollId(entity.getId());
        return payrollMapper.toPayrollResponseDTO(entity, worker, siteName(worker), allowances, deductions);
    }

    /**
     * Resolves a worker's assigned site name.
     *
     * @param worker the worker, or null
     * @return the site name, or null when the worker has no assigned site or it cannot be found
     */
    private String siteName(WorkerEntity worker) {
        if (worker == null || worker.getAssignedSiteId() == null) {
            return null;
        }
        return projectRepository.findById(worker.getAssignedSiteId()).map(ProjectEntity::getName).orElse(null);
    }

    /**
     * Substitutes zero for a null aggregate, when a filter matches no payroll.
     *
     * @param value the aggregate value, possibly null
     * @return the value, or zero when null
     */
    private BigDecimal nonNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
