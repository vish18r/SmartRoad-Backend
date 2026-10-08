package com.smartroad.services.core.service.shift;

import com.smartroad.services.common.enums.shift.ShiftAssignmentStatusEnum;
import com.smartroad.services.common.enums.shift.ShiftStatusEnum;
import com.smartroad.services.common.exception.ApplicationLayer;
import com.smartroad.services.common.exception.ErrorCodeMapping;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.material.PaginationMetaDTO;
import com.smartroad.services.core.dto.shift.ShiftPageResponseDTO;
import com.smartroad.services.core.dto.shift.ShiftRequestDTO;
import com.smartroad.services.core.dto.shift.ShiftResponseDTO;
import com.smartroad.services.core.dto.shift.ShiftStatusUpdateRequestDTO;
import com.smartroad.services.core.dto.shift.ShiftSummaryResponseDTO;
import com.smartroad.services.core.mapper.shift.ShiftMapper;
import com.smartroad.services.core.service.organization.OrganizationService;
import com.smartroad.services.domain.entity.shift.ShiftTemplateEntity;
import com.smartroad.services.domain.repository.WorkerRepository;
import com.smartroad.services.domain.repository.shift.ShiftTemplateRepository;
import com.smartroad.services.domain.repository.shift.WorkerShiftAssignmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Service layer for shift template business logic: creating, editing and retiring the reusable
 * shift templates that {@link ShiftAssignmentService} assigns workers to. Every overnight-aware
 * working-minutes figure is computed here and persisted, so every other consumer (attendance
 * enrichment, payroll) reads the same backend-authoritative value instead of re-deriving it.
 *
 * @author Vishal
 * @version 1.0
 */
@Service
public class ShiftService {

    private static final int MINUTES_PER_DAY = 24 * 60;

    private final ShiftTemplateRepository shiftTemplateRepository;
    private final WorkerShiftAssignmentRepository assignmentRepository;
    private final WorkerRepository workerRepository;
    private final OrganizationService organizationService;
    private final ShiftMapper shiftMapper;

    /**
     * Constructs the service with required dependencies.
     *
     * @param shiftTemplateRepository the shift template repository
     * @param assignmentRepository the worker shift assignment repository, used to block deleting
     *                             a shift that still has assignment history and to count assigned workers
     * @param workerRepository the worker repository, used for the organization's unassigned-worker count
     * @param organizationService the organization service
     * @param shiftMapper the shift template MapStruct mapper
     */
    public ShiftService(ShiftTemplateRepository shiftTemplateRepository, WorkerShiftAssignmentRepository assignmentRepository,
                        WorkerRepository workerRepository, OrganizationService organizationService, ShiftMapper shiftMapper) {
        this.shiftTemplateRepository = shiftTemplateRepository;
        this.assignmentRepository = assignmentRepository;
        this.workerRepository = workerRepository;
        this.organizationService = organizationService;
        this.shiftMapper = shiftMapper;
    }

    /**
     * Creates a new shift template.
     *
     * @param userId the UUID of the user creating the shift template
     * @param request the shift creation request
     * @return the persisted {@link ShiftResponseDTO}
     * @throws SmartRoadException if the caller is not authorized for the organization, the shift
     *                            code is already in use, or the times/break duration are invalid
     */
    @Transactional
    public ShiftResponseDTO create(UUID userId, ShiftRequestDTO request) throws SmartRoadException {
        organizationService.requireMember(userId, request.organizationId());
        requireUniqueCode(request.shiftCode(), request.organizationId(), null);

        ShiftTemplateEntity entity = shiftMapper.toShiftEntity(request);
        entity.setOrganizationId(request.organizationId());
        entity.setStatus(request.status() != null ? request.status() : ShiftStatusEnum.ACTIVE);
        entity.setTotalWorkingMinutes(computeTotalWorkingMinutes(request.startTime(), request.endTime(), request.breakDurationMinutes()));
        entity.setCreatedBy(userId);
        entity.setModifiedBy(userId);
        shiftTemplateRepository.save(entity);

        return toResponse(entity);
    }

    /**
     * Updates a shift template.
     *
     * @param userId the UUID of the user updating the shift template
     * @param id the shift template UUID
     * @param request the shift update request
     * @return the updated {@link ShiftResponseDTO}
     * @throws SmartRoadException if the shift is not found, the caller is not authorized, the
     *                            shift code is already in use by another template, or the
     *                            times/break duration are invalid
     */
    @Transactional
    public ShiftResponseDTO update(UUID userId, UUID id, ShiftRequestDTO request) throws SmartRoadException {
        ShiftTemplateEntity entity = requireShift(userId, id);
        requireUniqueCode(request.shiftCode(), entity.getOrganizationId(), entity.getId());

        shiftMapper.updateShiftEntityFromDto(request, entity);
        entity.setStatus(request.status() != null ? request.status() : entity.getStatus());
        entity.setTotalWorkingMinutes(computeTotalWorkingMinutes(request.startTime(), request.endTime(), request.breakDurationMinutes()));
        entity.setModifiedBy(userId);
        shiftTemplateRepository.save(entity);

        return toResponse(entity);
    }

    /**
     * Moves a shift template to a new lifecycle status without touching its other fields.
     *
     * @param userId the UUID of the user changing the status
     * @param id the shift template UUID
     * @param request the status change request
     * @return the updated {@link ShiftResponseDTO}
     * @throws SmartRoadException if the shift is not found or the caller is not authorized
     */
    @Transactional
    public ShiftResponseDTO updateStatus(UUID userId, UUID id, ShiftStatusUpdateRequestDTO request) throws SmartRoadException {
        ShiftTemplateEntity entity = requireShift(userId, id);
        entity.setStatus(request.status());
        entity.setModifiedBy(userId);
        shiftTemplateRepository.save(entity);
        return toResponse(entity);
    }

    /**
     * Retrieves one shift template's full details, including its currently assigned worker count.
     *
     * @param userId the UUID of the requesting user
     * @param id the shift template UUID
     * @return the {@link ShiftResponseDTO}
     * @throws SmartRoadException if the shift is not found or the caller is not authorized
     */
    @Transactional(readOnly = true)
    public ShiftResponseDTO getById(UUID userId, UUID id) throws SmartRoadException {
        return toResponse(requireShift(userId, id));
    }

    /**
     * Retrieves a page of an organization's shift templates matching the optional filters.
     *
     * @param userId the UUID of the requesting user
     * @param organizationId the organization UUID
     * @param status the shift status filter, or null
     * @param search a free-text term matched against shift name or shift code, or null
     * @param page the one-based page number
     * @param limit the page size
     * @return the matching {@link ShiftPageResponseDTO}
     * @throws SmartRoadException if the caller is not authorized for the organization
     */
    @Transactional(readOnly = true)
    public ShiftPageResponseDTO list(UUID userId, UUID organizationId, ShiftStatusEnum status, String search,
                                     int page, int limit) throws SmartRoadException {
        organizationService.requireMember(userId, organizationId);

        int pageIndex = Math.max(page, 1) - 1;
        int pageSize = Math.max(limit, 1);
        Page<ShiftTemplateEntity> results = shiftTemplateRepository.search(organizationId, status, search,
                PageRequest.of(pageIndex, pageSize));

        var data = results.getContent().stream().map(this::toResponse).toList();
        PaginationMetaDTO pagination = new PaginationMetaDTO(pageIndex + 1, pageSize, results.getTotalElements(),
                results.getTotalPages(), results.hasNext(), results.hasPrevious());

        return new ShiftPageResponseDTO(data, pagination);
    }

    /**
     * Deletes a shift template. A shift template that still has assignment history (current or
     * past) cannot be deleted, since assignment rows reference it and are never removed; it must
     * be deactivated instead.
     *
     * @param userId the UUID of the user deleting the shift template
     * @param id the shift template UUID
     * @throws SmartRoadException if the shift is not found, the caller is not authorized, or the
     *                            shift still has assignment history
     */
    @Transactional
    public void delete(UUID userId, UUID id) throws SmartRoadException {
        ShiftTemplateEntity entity = requireShift(userId, id);
        if (assignmentRepository.countByShiftId(id) > 0) {
            throw new SmartRoadException(
                    ApplicationLayer.SERVICE_LAYER,
                    ErrorCodeMapping.SERVICE_VALIDATION_FAILED,
                    "shift.delete.has.assignments"
            );
        }
        shiftTemplateRepository.delete(entity);
    }

    /**
     * Retrieves aggregated shift management figures for the dashboard summary cards.
     *
     * @param userId the UUID of the requesting user
     * @param organizationId the organization UUID
     * @return the {@link ShiftSummaryResponseDTO}
     * @throws SmartRoadException if the caller is not authorized for the organization
     */
    @Transactional(readOnly = true)
    public ShiftSummaryResponseDTO summary(UUID userId, UUID organizationId) throws SmartRoadException {
        organizationService.requireMember(userId, organizationId);

        long total = shiftTemplateRepository.countByOrganizationIdAndOptionalStatus(organizationId, null);
        long active = shiftTemplateRepository.countByOrganizationIdAndOptionalStatus(organizationId, ShiftStatusEnum.ACTIVE);
        long inactive = shiftTemplateRepository.countByOrganizationIdAndOptionalStatus(organizationId, ShiftStatusEnum.INACTIVE);

        LocalDate today = LocalDate.now();
        long totalWorkers = workerRepository.countByOrganizationId(organizationId);
        long assigned = assignmentRepository.countDistinctWorkersAssignedOn(today);
        long unassigned = Math.max(0, totalWorkers - assigned);

        return new ShiftSummaryResponseDTO(total, active, inactive, assigned, unassigned);
    }

    /**
     * Loads a shift template a user is authorized to act on.
     *
     * @param userId the UUID of the requesting user
     * @param id the shift template UUID
     * @return the {@link ShiftTemplateEntity}
     * @throws SmartRoadException if the shift is not found or the caller is not authorized
     */
    @Transactional(readOnly = true)
    public ShiftTemplateEntity requireShift(UUID userId, UUID id) throws SmartRoadException {
        ShiftTemplateEntity entity = shiftTemplateRepository.findById(id)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.DAO_NOT_FOUND, "shift.not.found"));
        organizationService.requireMember(userId, entity.getOrganizationId());
        return entity;
    }

    /**
     * Confirms a shift code is not already used by another shift template in the organization.
     *
     * @param shiftCode the shift code to check
     * @param organizationId the organization UUID
     * @param excludeId a shift id to leave out of the check (the shift being updated), or null
     * @throws SmartRoadException if another shift template already holds the code
     */
    private void requireUniqueCode(String shiftCode, UUID organizationId, UUID excludeId) throws SmartRoadException {
        if (shiftTemplateRepository.existsByShiftCodeIgnoreCaseAndOrganizationId(shiftCode, organizationId, excludeId)) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "shift.code.already.exists");
        }
    }

    /**
     * Computes a shift's net working minutes from its start time, end time and break duration.
     * When the end time is earlier than the start time the shift is treated as crossing midnight,
     * so the raw duration wraps through 24:00. The break duration is validated to never exceed
     * the raw (pre-break) shift duration, and the result is never negative.
     *
     * @param startTime the shift's start time
     * @param endTime the shift's end time
     * @param breakDurationMinutes the unpaid break duration in minutes
     * @return the net working minutes
     * @throws SmartRoadException if the end time equals the start time, or the break duration
     *                            exceeds the raw shift duration
     */
    private int computeTotalWorkingMinutes(LocalTime startTime, LocalTime endTime, int breakDurationMinutes) throws SmartRoadException {
        if (startTime.equals(endTime)) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "shift.time.range.invalid");
        }

        long rawMinutes = Duration.between(startTime, endTime).toMinutes();
        if (rawMinutes < 0) {
            rawMinutes += MINUTES_PER_DAY;
        }

        if (breakDurationMinutes > rawMinutes) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "shift.break.duration.exceeds.shift");
        }

        return (int) Math.max(0, rawMinutes - breakDurationMinutes);
    }

    /**
     * Assembles a shift template's response DTO, including its currently assigned worker count.
     *
     * @param entity the shift template entity
     * @return the assembled {@link ShiftResponseDTO}
     */
    private ShiftResponseDTO toResponse(ShiftTemplateEntity entity) {
        ShiftResponseDTO mapped = shiftMapper.toShiftResponseDTO(entity);
        long assignedWorkers = assignmentRepository.search(null, entity.getId(),
                ShiftAssignmentStatusEnum.ACTIVE, LocalDate.now(),
                PageRequest.of(0, 1)).getTotalElements();
        return new ShiftResponseDTO(mapped.id(), mapped.organizationId(), mapped.shiftCode(), mapped.shiftName(),
                mapped.startTime(), mapped.endTime(), mapped.breakDurationMinutes(), mapped.totalWorkingMinutes(),
                mapped.description(), mapped.status(), assignedWorkers);
    }
}
