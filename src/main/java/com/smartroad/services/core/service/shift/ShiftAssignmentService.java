package com.smartroad.services.core.service.shift;

import com.smartroad.services.common.enums.shift.ShiftAssignmentStatusEnum;
import com.smartroad.services.common.enums.shift.ShiftStatusEnum;
import com.smartroad.services.common.enums.workers.WorkerStatusEnum;
import com.smartroad.services.common.exception.ApplicationLayer;
import com.smartroad.services.common.exception.ErrorCodeMapping;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.material.PaginationMetaDTO;
import com.smartroad.services.core.dto.shift.ShiftAssignmentPageResponseDTO;
import com.smartroad.services.core.dto.shift.ShiftAssignmentRequestDTO;
import com.smartroad.services.core.dto.shift.ShiftAssignmentResponseDTO;
import com.smartroad.services.core.dto.shift.ShiftScheduledMinutesDTO;
import com.smartroad.services.core.mapper.shift.ShiftAssignmentMapper;
import com.smartroad.services.core.service.organization.OrganizationService;
import com.smartroad.services.core.service.workers.WorkerService;
import com.smartroad.services.domain.entity.shift.ShiftTemplateEntity;
import com.smartroad.services.domain.entity.shift.WorkerShiftAssignmentEntity;
import com.smartroad.services.domain.entity.worker.WorkerEntity;
import com.smartroad.services.domain.repository.shift.ShiftTemplateRepository;
import com.smartroad.services.domain.repository.shift.WorkerShiftAssignmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for worker shift assignment business logic: assigning workers to shift
 * templates, resolving a worker's current or as-of-date shift, and exposing the read-only
 * schedule data that attendance enrichment and payroll consume. An assignment row is never
 * deleted; ending one sets {@code effectiveTo} and moves its status to ENDED so the full history
 * is preserved forever.
 *
 * @author Vishal
 * @version 1.0
 */
@Service
public class ShiftAssignmentService {

    private final WorkerShiftAssignmentRepository assignmentRepository;
    private final ShiftTemplateRepository shiftTemplateRepository;
    private final WorkerService workerService;
    private final OrganizationService organizationService;
    private final ShiftAssignmentMapper assignmentMapper;

    /**
     * Constructs the service with required dependencies.
     *
     * @param assignmentRepository the worker shift assignment repository
     * @param shiftTemplateRepository the shift template repository
     * @param workerService the worker service, reused for worker lookup and authorization
     * @param organizationService the organization service, used to scope the assignment list to one organization
     * @param assignmentMapper the assignment response assembler
     */
    public ShiftAssignmentService(WorkerShiftAssignmentRepository assignmentRepository, ShiftTemplateRepository shiftTemplateRepository,
                                  WorkerService workerService, OrganizationService organizationService,
                                  ShiftAssignmentMapper assignmentMapper) {
        this.assignmentRepository = assignmentRepository;
        this.shiftTemplateRepository = shiftTemplateRepository;
        this.workerService = workerService;
        this.organizationService = organizationService;
        this.assignmentMapper = assignmentMapper;
    }

    /**
     * Creates a new worker shift assignment.
     *
     * @param userId the UUID of the user creating the assignment
     * @param request the assignment creation request
     * @return the persisted {@link ShiftAssignmentResponseDTO}
     * @throws SmartRoadException if the worker or shift is not found, not active, belongs to a
     *                            different organization, the caller is not authorized, the
     *                            effective date range is invalid, or it overlaps another ACTIVE
     *                            assignment of the same worker
     */
    @Transactional
    public ShiftAssignmentResponseDTO create(UUID userId, ShiftAssignmentRequestDTO request) throws SmartRoadException {
        WorkerEntity worker = workerService.requireWorkerAccess(userId, request.workerId());
        requireActiveWorker(worker);
        ShiftTemplateEntity shift = requireActiveShift(userId, request.shiftId(), worker.getOrganizationId());

        requireValidRange(request.effectiveFrom(), request.effectiveTo());
        ShiftAssignmentStatusEnum status = request.status() != null ? request.status() : ShiftAssignmentStatusEnum.ACTIVE;
        if (status == ShiftAssignmentStatusEnum.ACTIVE) {
            requireNoOverlap(worker.getId(), request.effectiveFrom(), request.effectiveTo(), null);
        }

        WorkerShiftAssignmentEntity entity = new WorkerShiftAssignmentEntity();
        entity.setWorkerId(worker.getId());
        entity.setShiftId(shift.getId());
        entity.setEffectiveFrom(request.effectiveFrom());
        entity.setEffectiveTo(request.effectiveTo());
        entity.setStatus(status);
        entity.setCreatedBy(userId);
        entity.setModifiedBy(userId);
        assignmentRepository.save(entity);

        return assignmentMapper.toShiftAssignmentResponseDTO(entity, worker, shift);
    }

    /**
     * Updates a worker shift assignment's shift, effective date range or status.
     *
     * @param userId the UUID of the user updating the assignment
     * @param id the assignment UUID
     * @param request the assignment update request
     * @return the updated {@link ShiftAssignmentResponseDTO}
     * @throws SmartRoadException if the assignment, worker or shift is not found, not active,
     *                            belongs to a different organization, the caller is not
     *                            authorized, the effective date range is invalid, or it overlaps
     *                            another ACTIVE assignment of the same worker
     */
    @Transactional
    public ShiftAssignmentResponseDTO update(UUID userId, UUID id, ShiftAssignmentRequestDTO request) throws SmartRoadException {
        WorkerShiftAssignmentEntity entity = requireAssignment(userId, id);
        WorkerEntity worker = workerService.requireWorkerAccess(userId, request.workerId());
        requireActiveWorker(worker);
        ShiftTemplateEntity shift = requireActiveShift(userId, request.shiftId(), worker.getOrganizationId());

        requireValidRange(request.effectiveFrom(), request.effectiveTo());
        ShiftAssignmentStatusEnum status = request.status() != null ? request.status() : entity.getStatus();
        if (status == ShiftAssignmentStatusEnum.ACTIVE) {
            requireNoOverlap(worker.getId(), request.effectiveFrom(), request.effectiveTo(), entity.getId());
        }

        entity.setWorkerId(worker.getId());
        entity.setShiftId(shift.getId());
        entity.setEffectiveFrom(request.effectiveFrom());
        entity.setEffectiveTo(request.effectiveTo());
        entity.setStatus(status);
        entity.setModifiedBy(userId);
        assignmentRepository.save(entity);

        return assignmentMapper.toShiftAssignmentResponseDTO(entity, worker, shift);
    }

    /**
     * Ends a worker shift assignment. The row is never deleted: its status moves to ENDED and
     * {@code effectiveTo} is set to today when it is currently null or in the future, preserving
     * the assignment as closed history instead of an open-ended or future-dated one.
     *
     * @param userId the UUID of the user ending the assignment
     * @param id the assignment UUID
     * @return the updated {@link ShiftAssignmentResponseDTO}
     * @throws SmartRoadException if the assignment is not found or the caller is not authorized
     */
    @Transactional
    public ShiftAssignmentResponseDTO end(UUID userId, UUID id) throws SmartRoadException {
        WorkerShiftAssignmentEntity entity = requireAssignment(userId, id);
        LocalDate today = LocalDate.now();
        if (entity.getEffectiveTo() == null || entity.getEffectiveTo().isAfter(today)) {
            entity.setEffectiveTo(today);
        }
        entity.setStatus(ShiftAssignmentStatusEnum.ENDED);
        entity.setModifiedBy(userId);
        assignmentRepository.save(entity);

        WorkerEntity worker = workerService.findAllById(List.of(entity.getWorkerId())).get(entity.getWorkerId());
        ShiftTemplateEntity shift = shiftTemplateRepository.findById(entity.getShiftId()).orElse(null);
        return assignmentMapper.toShiftAssignmentResponseDTO(entity, worker, shift);
    }

    /**
     * Retrieves one assignment's full details.
     *
     * @param userId the UUID of the requesting user
     * @param id the assignment UUID
     * @return the {@link ShiftAssignmentResponseDTO}
     * @throws SmartRoadException if the assignment is not found or the caller is not authorized
     */
    @Transactional(readOnly = true)
    public ShiftAssignmentResponseDTO getById(UUID userId, UUID id) throws SmartRoadException {
        WorkerShiftAssignmentEntity entity = requireAssignment(userId, id);
        WorkerEntity worker = workerService.findAllById(List.of(entity.getWorkerId())).get(entity.getWorkerId());
        ShiftTemplateEntity shift = shiftTemplateRepository.findById(entity.getShiftId()).orElse(null);
        return assignmentMapper.toShiftAssignmentResponseDTO(entity, worker, shift);
    }

    /**
     * Retrieves a page of an organization's assignments matching the optional filters. The
     * organization is resolved through the assigned shift template, so the list never crosses
     * tenant boundaries.
     *
     * @param userId the UUID of the requesting user
     * @param organizationId the organization UUID
     * @param workerId the worker UUID filter, or null
     * @param shiftId the shift UUID filter, or null
     * @param status the assignment status filter, or null
     * @param effectiveDate a date the assignment's range must cover, or null
     * @param page the one-based page number
     * @param limit the page size
     * @return the matching {@link ShiftAssignmentPageResponseDTO}
     * @throws SmartRoadException if the caller is not authorized for the organization
     */
    @Transactional(readOnly = true)
    public ShiftAssignmentPageResponseDTO list(UUID userId, UUID organizationId, UUID workerId, UUID shiftId,
                                               ShiftAssignmentStatusEnum status, LocalDate effectiveDate,
                                               int page, int limit) throws SmartRoadException {
        organizationService.requireMember(userId, organizationId);

        int pageIndex = Math.max(page, 1) - 1;
        int pageSize = Math.max(limit, 1);
        Page<WorkerShiftAssignmentEntity> results = assignmentRepository.searchByOrganization(organizationId, workerId,
                shiftId, status, effectiveDate, PageRequest.of(pageIndex, pageSize));

        var data = results.getContent().stream().map(this::toResponse).toList();
        PaginationMetaDTO pagination = new PaginationMetaDTO(pageIndex + 1, pageSize, results.getTotalElements(),
                results.getTotalPages(), results.hasNext(), results.hasPrevious());

        return new ShiftAssignmentPageResponseDTO(data, pagination);
    }

    /**
     * Resolves the shift a worker is assigned to on a given date (defaulting to today), for the
     * worker's shift detail panel and for {@code GET /api/v1/workers/{workerId}/shift}.
     *
     * @param userId the UUID of the requesting user
     * @param workerId the worker UUID
     * @param date the date to resolve the shift for, or null for today
     * @return the {@link ShiftAssignmentResponseDTO} in effect on that date, or null when the
     *         worker holds no assignment covering it
     * @throws SmartRoadException if the worker is not found or the caller is not authorized
     */
    @Transactional(readOnly = true)
    public ShiftAssignmentResponseDTO resolveForWorker(UUID userId, UUID workerId, LocalDate date) throws SmartRoadException {
        WorkerEntity worker = workerService.requireWorkerAccess(userId, workerId);
        LocalDate resolveDate = date != null ? date : LocalDate.now();

        return resolveActiveAssignment(workerId, resolveDate)
                .map(entity -> assignmentMapper.toShiftAssignmentResponseDTO(entity, worker,
                        shiftTemplateRepository.findById(entity.getShiftId()).orElse(null)))
                .orElse(null);
    }

    /**
     * Retrieves a worker's full shift assignment history, most recently started first.
     *
     * @param userId the UUID of the requesting user
     * @param workerId the worker UUID
     * @return the worker's assignments, newest effective-from date first
     * @throws SmartRoadException if the worker is not found or the caller is not authorized
     */
    @Transactional(readOnly = true)
    public List<ShiftAssignmentResponseDTO> historyForWorker(UUID userId, UUID workerId) throws SmartRoadException {
        WorkerEntity worker = workerService.requireWorkerAccess(userId, workerId);
        return assignmentRepository.findByWorkerIdOrderByEffectiveFromDesc(workerId).stream()
                .map(entity -> assignmentMapper.toShiftAssignmentResponseDTO(entity, worker,
                        shiftTemplateRepository.findById(entity.getShiftId()).orElse(null)))
                .toList();
    }

    /**
     * Resolves the assignment in effect for a worker on a date, without any authorization check.
     * This is the additive, read-only hook {@code AttendanceService} calls to enrich a day's
     * attendance with the worker's scheduled shift; it never changes check-in/check-out recording.
     *
     * @param workerId the worker UUID
     * @param date the date to resolve the shift for
     * @return the assignment in effect, if any
     */
    @Transactional(readOnly = true)
    public Optional<WorkerShiftAssignmentEntity> resolveActiveAssignment(UUID workerId, LocalDate date) {
        return assignmentRepository.findInEffectOn(workerId, date).stream().findFirst();
    }

    /**
     * Loads a shift template by id, without any authorization check, for the shift's own fields
     * to be attached to an assignment or attendance enrichment.
     *
     * @param shiftId the shift template UUID
     * @return the shift template, or null if it no longer exists
     */
    @Transactional(readOnly = true)
    public ShiftTemplateEntity loadShift(UUID shiftId) {
        return shiftTemplateRepository.findById(shiftId).orElse(null);
    }

    /**
     * Read-only schedule input for payroll (or any other consumer) to pull a worker's scheduled
     * working minutes per day over a period, without this module duplicating payroll's own
     * calculation logic and without payroll's existing calculation code being modified here.
     *
     * @param workerId the worker UUID
     * @param startDate the first date of the period, inclusive
     * @param endDate the last date of the period, inclusive
     * @return one {@link ShiftScheduledMinutesDTO} per date in the period
     */
    @Transactional(readOnly = true)
    public List<ShiftScheduledMinutesDTO> getScheduledMinutesForPeriod(UUID workerId, LocalDate startDate, LocalDate endDate) {
        return startDate.datesUntil(endDate.plusDays(1))
                .map(date -> resolveActiveAssignment(workerId, date)
                        .map(assignment -> {
                            ShiftTemplateEntity shift = loadShift(assignment.getShiftId());
                            return new ShiftScheduledMinutesDTO(date, assignment.getShiftId(),
                                    shift == null ? null : shift.getTotalWorkingMinutes());
                        })
                        .orElse(new ShiftScheduledMinutesDTO(date, null, null)))
                .toList();
    }

    /**
     * Loads an assignment a user is authorized to act on (through the worker it belongs to).
     *
     * @param userId the UUID of the requesting user
     * @param id the assignment UUID
     * @return the {@link WorkerShiftAssignmentEntity}
     * @throws SmartRoadException if the assignment is not found or the caller is not authorized
     */
    private WorkerShiftAssignmentEntity requireAssignment(UUID userId, UUID id) throws SmartRoadException {
        WorkerShiftAssignmentEntity entity = assignmentRepository.findById(id)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.DAO_NOT_FOUND,
                        "shift.assignment.not.found"));
        workerService.verifyWorkerAccess(userId, entity.getWorkerId());
        return entity;
    }

    /**
     * Loads an ACTIVE shift template belonging to a given organization.
     *
     * @param userId the UUID of the requesting user
     * @param shiftId the shift template UUID
     * @param organizationId the organization the shift must belong to
     * @return the active {@link ShiftTemplateEntity}
     * @throws SmartRoadException if the shift is not found, belongs to a different organization,
     *                            or is not ACTIVE
     */
    private ShiftTemplateEntity requireActiveShift(UUID userId, UUID shiftId, UUID organizationId) throws SmartRoadException {
        ShiftTemplateEntity shift = shiftTemplateRepository.findById(shiftId)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.DAO_NOT_FOUND, "shift.not.found"));
        if (!shift.getOrganizationId().equals(organizationId)) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.DAO_NOT_FOUND, "shift.not.found");
        }
        if (shift.getStatus() != ShiftStatusEnum.ACTIVE) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "shift.not.active");
        }
        return shift;
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
     * Requires an assignment's effective date range to be well-formed.
     *
     * @param effectiveFrom the range's start date
     * @param effectiveTo the range's end date, or null for open-ended
     * @throws SmartRoadException if the end date is before the start date
     */
    private void requireValidRange(LocalDate effectiveFrom, LocalDate effectiveTo) throws SmartRoadException {
        if (effectiveTo != null && effectiveTo.isBefore(effectiveFrom)) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED,
                    "shift.assignment.effective.range.invalid");
        }
    }

    /**
     * Requires that no other ACTIVE assignment of this worker overlaps the candidate date range.
     *
     * @param workerId the worker UUID
     * @param effectiveFrom the candidate range's start date
     * @param effectiveTo the candidate range's end date, or null for open-ended
     * @param excludeId an assignment id to leave out of the check (the assignment being updated), or null
     * @throws SmartRoadException if an overlapping ACTIVE assignment already exists
     */
    private void requireNoOverlap(UUID workerId, LocalDate effectiveFrom, LocalDate effectiveTo, UUID excludeId) throws SmartRoadException {
        if (!assignmentRepository.findOverlapping(workerId, effectiveFrom, effectiveTo, excludeId).isEmpty()) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED,
                    "shift.assignment.overlap");
        }
    }

    /**
     * Assembles an assignment's response DTO, resolving its worker and shift.
     *
     * @param entity the assignment entity
     * @return the assembled {@link ShiftAssignmentResponseDTO}
     */
    private ShiftAssignmentResponseDTO toResponse(WorkerShiftAssignmentEntity entity) {
        WorkerEntity worker = workerService.findAllById(List.of(entity.getWorkerId())).get(entity.getWorkerId());
        ShiftTemplateEntity shift = shiftTemplateRepository.findById(entity.getShiftId()).orElse(null);
        return assignmentMapper.toShiftAssignmentResponseDTO(entity, worker, shift);
    }
}
