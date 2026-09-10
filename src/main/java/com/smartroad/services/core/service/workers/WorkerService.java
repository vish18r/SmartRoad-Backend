package com.smartroad.services.core.service.workers;

import com.smartroad.services.common.enums.workers.WorkerStatusEnum;
import com.smartroad.services.common.exception.ApplicationLayer;
import com.smartroad.services.common.exception.ErrorCodeMapping;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.workers.AttendanceRequestDTO;
import com.smartroad.services.core.dto.workers.AttendanceResponseDTO;
import com.smartroad.services.core.dto.workers.WorkerRequestDTO;
import com.smartroad.services.core.dto.workers.WorkerResponseDTO;
import com.smartroad.services.core.mapper.workers.WorkerMapper;
import com.smartroad.services.core.service.organization.OrganizationService;
import com.smartroad.services.domain.entity.worker.WorkerAttendanceEntity;
import com.smartroad.services.domain.entity.worker.WorkerEntity;
import com.smartroad.services.domain.repository.WorkerAttendanceRepository;
import com.smartroad.services.domain.repository.WorkerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

/**
 * Service layer for worker business logic.
 * Coordinates between {@link WorkerRepository} and {@link WorkerMapper}.
 *
 * @author Vishal
 * @version 1.0
 */
@Service
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final WorkerAttendanceRepository attendanceRepository;
    private final WorkerMapper workerMapper;
    private final OrganizationService organizationService;

    /**
     * Constructs the service with required dependencies.
     *
     * @param workerRepository the worker repository
     * @param attendanceRepository the worker attendance repository
     * @param workerMapper the worker mapper
     * @param organizationService the organization service
     */
    public WorkerService(WorkerRepository workerRepository,
                         WorkerAttendanceRepository attendanceRepository,
                         WorkerMapper workerMapper,
                         OrganizationService organizationService) {
        this.workerRepository = workerRepository;
        this.attendanceRepository = attendanceRepository;
        this.workerMapper = workerMapper;
        this.organizationService = organizationService;
    }

    /**
     * Creates a new worker.
     *
     * @param userId the UUID of the user creating the worker
     * @param request the worker creation request
     * @return the created {@link WorkerResponseDTO}
     * @throws SmartRoadException if validation fails
     */
    @Transactional
    public WorkerResponseDTO create(UUID userId, WorkerRequestDTO request) throws SmartRoadException {
        organizationService.requireMember(userId, request.organizationId());

        if (request.emailId() != null && workerRepository.existsByEmailIdAndOrganizationId(request.emailId(), request.organizationId())) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.SERVICE_VALIDATION_FAILED,
                "worker.email.already.exists"
            );
        }

        WorkerEntity entity = workerMapper.toWorkerEntity(request);
        entity.setIsDeleted(false);

        workerRepository.save(entity);

        return workerMapper.toWorkerResponseDTO(entity);
    }

    /**
     * Retrieves all workers in an organization.
     *
     * @param userId the UUID of the user
     * @param organizationId the UUID of the organization
     * @return list of {@link WorkerResponseDTO}
     * @throws SmartRoadException if user is not an organization member
     */
    @Transactional(readOnly = true)
    public List<WorkerResponseDTO> listByOrganization(UUID userId, UUID organizationId) throws SmartRoadException {
        organizationService.requireMember(userId, organizationId);

        return workerRepository.findByOrganizationIdAndNotDeleted(organizationId)
            .stream()
            .map(workerMapper::toWorkerResponseDTO)
            .toList();
    }

    /**
     * Retrieves workers assigned to a site.
     *
     * @param siteId the UUID of the site
     * @return list of {@link WorkerResponseDTO}
     */
    @Transactional(readOnly = true)
    public List<WorkerResponseDTO> listBySite(UUID siteId) {
        return workerRepository.findByAssignedSiteId(siteId)
            .stream()
            .map(workerMapper::toWorkerResponseDTO)
            .toList();
    }

    /**
     * Searches an organization's workers by name, phone number, or email.
     * A blank or missing term returns an empty list rather than the full roster.
     *
     * @param userId the UUID of the requesting user
     * @param organizationId the organization UUID
     * @param query the free-text search term
     * @return list of matching {@link WorkerResponseDTO}
     * @throws SmartRoadException if the user is not a member of the organization
     */
    @Transactional(readOnly = true)
    public List<WorkerResponseDTO> search(UUID userId, UUID organizationId, String query) throws SmartRoadException {
        organizationService.requireMember(userId, organizationId);
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String term = "%" + query.trim().toLowerCase() + "%";
        return workerRepository.search(organizationId, term).stream()
                .map(workerMapper::toWorkerResponseDTO)
                .toList();
    }

    /**
     * Retrieves a specific worker by ID.
     *
     * @param id the UUID of the worker
     * @return the {@link WorkerResponseDTO}
     * @throws SmartRoadException if worker not found
     */
    @Transactional(readOnly = true)
    public WorkerResponseDTO getById(UUID id) throws SmartRoadException {
        WorkerEntity entity = workerRepository.findById(id)
            .orElseThrow(() -> new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "worker.not.found"
            ));

        return workerMapper.toWorkerResponseDTO(entity);
    }

    /**
     * Updates an existing worker.
     *
     * @param userId the UUID of the user updating the worker
     * @param id the UUID of the worker to update
     * @param request the worker update request
     * @return the updated {@link WorkerResponseDTO}
     * @throws SmartRoadException if worker not found or update fails
     */
    @Transactional
    public WorkerResponseDTO update(UUID userId, UUID id, WorkerRequestDTO request) throws SmartRoadException {
        WorkerEntity entity = workerRepository.findById(id)
            .orElseThrow(() -> new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "worker.not.found"
            ));

        organizationService.requireMember(userId, entity.getOrganizationId());

        if (request.emailId() != null && !request.emailId().equals(entity.getEmailId())) {
            if (workerRepository.existsByEmailIdAndOrganizationId(request.emailId(), entity.getOrganizationId())) {
                throw new SmartRoadException(
                    ApplicationLayer.SERVICE_LAYER,
                    ErrorCodeMapping.SERVICE_VALIDATION_FAILED,
                    "worker.email.already.exists"
                );
            }
        }

        workerMapper.updateWorkerEntityFromDto(request, entity);
        entity.setModifiedBy(userId);

        workerRepository.save(entity);

        return workerMapper.toWorkerResponseDTO(entity);
    }

    /**
     * Soft-deletes a worker.
     *
     * @param userId the UUID of the user deleting the worker
     * @param id the UUID of the worker to delete
     * @throws SmartRoadException if worker not found
     */
    @Transactional
    public void delete(UUID userId, UUID id) throws SmartRoadException {
        WorkerEntity entity = workerRepository.findById(id)
            .orElseThrow(() -> new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "worker.not.found"
            ));

        organizationService.requireMember(userId, entity.getOrganizationId());

        entity.setIsDeleted(true);
        entity.setModifiedBy(userId);

        workerRepository.save(entity);
    }

    /**
     * Gets the count of active workers in an organization.
     *
     * @param organizationId the UUID of the organization
     * @return the count of active workers
     */
    @Transactional(readOnly = true)
    public long getWorkerCountByOrganization(UUID organizationId) {
        return workerRepository.countByOrganizationId(organizationId);
    }

    /**
     * Marks a worker attendance entry for a project on a given day.
     * Marking the same worker, project, and date twice updates the existing record rather than
     * creating a duplicate, so the register holds at most one row per worker per project per day.
     *
     * @param userId the UUID of the user recording the attendance
     * @param request the attendance request
     * @return the persisted {@link AttendanceResponseDTO}
     * @throws SmartRoadException if the worker is not found or the user does not belong to its organization
     */
    @Transactional
    public AttendanceResponseDTO markAttendance(UUID userId, AttendanceRequestDTO request) throws SmartRoadException {
        WorkerEntity worker = requireWorker(userId, request.workerId());

        WorkerAttendanceEntity entity = attendanceRepository
                .findByWorkerIdAndProjectIdAndAttendanceDate(worker.getId(), request.projectId(), request.attendanceDate())
                .orElseGet(() -> WorkerAttendanceEntity.builder()
                        .workerId(worker.getId())
                        .projectId(request.projectId())
                        .attendanceDate(request.attendanceDate())
                        .build());

        entity.setStatus(request.status());
        entity.setHoursWorked(request.hoursWorked());
        entity.setNotes(request.notes());
        entity.setModifiedBy(userId);

        return toAttendanceResponse(attendanceRepository.save(entity));
    }

    /**
     * Retrieves the attendance records of a worker, optionally narrowed to a single calendar month.
     *
     * @param userId the UUID of the requesting user
     * @param workerId the worker UUID
     * @param month the month to report on in yyyy-MM form, or null for the full history
     * @return list of {@link AttendanceResponseDTO} ordered most recent first
     * @throws SmartRoadException if the worker is not found, the user does not belong to its
     *                            organization, or the month cannot be parsed
     */
    @Transactional(readOnly = true)
    public List<AttendanceResponseDTO> listAttendance(UUID userId, UUID workerId, String month) throws SmartRoadException {
        requireWorker(userId, workerId);

        List<WorkerAttendanceEntity> records = month == null || month.isBlank()
                ? attendanceRepository.findByWorkerId(workerId)
                : findAttendanceForMonth(workerId, month);

        return records.stream().map(this::toAttendanceResponse).toList();
    }

    /**
     * Loads the attendance of a worker for the given calendar month.
     *
     * @param workerId the worker UUID
     * @param month the month in yyyy-MM form
     * @return list of attendance records inside that month
     * @throws SmartRoadException if the month cannot be parsed
     */
    private List<WorkerAttendanceEntity> findAttendanceForMonth(UUID workerId, String month) throws SmartRoadException {
        YearMonth yearMonth = parseMonth(month);
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();
        return attendanceRepository.findByWorkerIdAndDateRange(workerId, from, to);
    }

    /**
     * Parses a yyyy-MM month string.
     *
     * @param month the month string
     * @return the parsed {@link YearMonth}
     * @throws SmartRoadException if the string is not a valid yyyy-MM month
     */
    private YearMonth parseMonth(String month) throws SmartRoadException {
        try {
            return YearMonth.parse(month.trim());
        } catch (DateTimeParseException e) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.SERVICE_INVALID_INPUT,
                "attendance.month.invalid"
            );
        }
    }

    /**
     * Loads a worker and verifies the caller belongs to the organization of that worker.
     *
     * @param userId the UUID of the requesting user
     * @param workerId the worker UUID
     * @return the {@link WorkerEntity}
     * @throws SmartRoadException if the worker is not found or the user does not belong to its organization
     */
    private WorkerEntity requireWorker(UUID userId, UUID workerId) throws SmartRoadException {
        WorkerEntity worker = workerRepository.findById(workerId)
            .orElseThrow(() -> new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "worker.not.found"
            ));
        organizationService.requireMember(userId, worker.getOrganizationId());
        return worker;
    }

    /**
     * Maps an attendance entity to its response DTO.
     *
     * @param entity the attendance entity
     * @return the attendance response DTO
     */
    private AttendanceResponseDTO toAttendanceResponse(WorkerAttendanceEntity entity) {
        return new AttendanceResponseDTO(entity.getId(), entity.getWorkerId(), entity.getProjectId(),
                entity.getAttendanceDate(), entity.getStatus(), entity.getHoursWorked(), entity.getNotes());
    }
}
