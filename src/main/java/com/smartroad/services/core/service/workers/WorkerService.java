package com.smartroad.services.core.service.workers;

import com.smartroad.services.common.enums.workers.WageTypeEnum;
import com.smartroad.services.common.enums.workers.WorkerStatusEnum;
import com.smartroad.services.common.exception.ApplicationLayer;
import com.smartroad.services.common.enums.workers.WorkerFaceStatusEnum;
import com.smartroad.services.common.exception.ErrorCodeMapping;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.workers.AttendanceRequestDTO;
import com.smartroad.services.core.dto.workers.AttendanceResponseDTO;
import com.smartroad.services.core.dto.workers.WorkerFaceRequestDTO;
import com.smartroad.services.core.dto.workers.WorkerFaceStatusResponseDTO;
import com.smartroad.services.core.dto.workers.WorkerRequestDTO;
import com.smartroad.services.core.dto.workers.WorkerResponseDTO;
import com.smartroad.services.core.mapper.workers.WorkerMapper;
import com.smartroad.services.core.service.organization.OrganizationService;
import com.smartroad.services.domain.entity.worker.WorkerAttendanceEntity;
import com.smartroad.services.domain.entity.worker.WorkerEntity;
import com.smartroad.services.domain.repository.ProjectRepository;
import com.smartroad.services.domain.repository.WorkerAttendanceRepository;
import com.smartroad.services.domain.repository.WorkerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
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

    private static final Logger logger = LoggerFactory.getLogger(WorkerService.class);

    private static final int FACE_DESCRIPTOR_LENGTH = 128;

    private static final double FACE_MATCH_THRESHOLD = 0.5;

    private static final String WORKER_QR_PREFIX = "WORKER:";

    private final WorkerRepository workerRepository;
    private final WorkerAttendanceRepository attendanceRepository;
    private final ProjectRepository projectRepository;
    private final WorkerMapper workerMapper;
    private final OrganizationService organizationService;

    /**
     * Constructs the service with required dependencies.
     *
     * @param workerRepository the worker repository
     * @param attendanceRepository the worker attendance repository
     * @param projectRepository the project repository
     * @param workerMapper the worker mapper
     * @param organizationService the organization service
     */
    public WorkerService(WorkerRepository workerRepository,
                         WorkerAttendanceRepository attendanceRepository,
                         ProjectRepository projectRepository,
                         WorkerMapper workerMapper,
                         OrganizationService organizationService) {
        this.workerRepository = workerRepository;
        this.attendanceRepository = attendanceRepository;
        this.projectRepository = projectRepository;
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

        if (workerRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.SERVICE_VALIDATION_FAILED,
                "worker.mobile.already.exists"
            );
        }
        if (request.faceDescriptor() == null || request.faceDescriptor().isBlank()) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.SERVICE_INVALID_INPUT,
                "worker.face.required"
            );
        }
        requireValidFaceDescriptor(request.faceDescriptor());
        requireFaceNotRegisteredToAnother(request.organizationId(), request.faceDescriptor(), null);

        WorkerEntity entity = workerMapper.toWorkerEntity(request);
        entity.setIsDeleted(false);
        entity.setEmployeeId("SR" + workerRepository.nextEmployeeNumber());
        applyWageDefaults(entity);

        workerRepository.save(entity);

        return workerMapper.toWorkerResponseDTO(entity);
    }

    /**
     * Checks that a face descriptor is a JSON array of exactly 128 numbers.
     *
     * @param faceDescriptor the descriptor to check
     * @throws SmartRoadException if the descriptor is malformed
     */
    private void requireValidFaceDescriptor(String faceDescriptor) throws SmartRoadException {
        boolean valid;
        try {
            valid = new ObjectMapper().readValue(faceDescriptor, double[].class).length == FACE_DESCRIPTOR_LENGTH;
        } catch (Exception e) {
            valid = false;
        }
        if (!valid) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.SERVICE_INVALID_INPUT,
                "worker.face.invalid"
            );
        }
    }

    /**
     * Registers (or replaces) a worker's face template.
     *
     * @param userId the UUID of the registering user
     * @param workerId the worker UUID
     * @param request the face descriptor
     * @return the updated {@link WorkerResponseDTO}
     * @throws SmartRoadException if the worker is not found, the user is not authorized, or the descriptor is invalid
     */
    @Transactional
    public WorkerResponseDTO registerFace(UUID userId, UUID workerId, WorkerFaceRequestDTO request) throws SmartRoadException {
        WorkerEntity worker = requireWorker(userId, workerId);
        requireValidFaceDescriptor(request.faceDescriptor());
        requireFaceNotRegisteredToAnother(worker.getOrganizationId(), request.faceDescriptor(), worker.getId());

        worker.setFaceDescriptor(request.faceDescriptor());
        worker.setModifiedBy(userId);
        workerRepository.save(worker);

        return workerMapper.toWorkerResponseDTO(worker);
    }

    /**
     * Euclidean distance between two equal-length face descriptors.
     *
     * @param a the first descriptor
     * @param b the second descriptor
     * @return the distance, smaller meaning more similar
     */
    private static double faceDistance(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            double d = a[i] - b[i];
            sum += d * d;
        }
        return Math.sqrt(sum);
    }

    /**
     * Finds the enrolled worker of an organization whose registered face is closest to a descriptor,
     * provided it is within the match threshold.
     *
     * @param organizationId the organization UUID
     * @param faceDescriptor the descriptor to look up, as a JSON array
     * @param excludeWorkerId a worker to leave out of the comparison, or null
     * @return the closest worker within the threshold, or empty when nobody matches
     */
    private Optional<WorkerEntity> findClosestFace(UUID organizationId, String faceDescriptor, UUID excludeWorkerId) {
        ObjectMapper mapper = new ObjectMapper();
        double[] probe;
        try {
            probe = mapper.readValue(faceDescriptor, double[].class);
        } catch (Exception e) {
            return Optional.empty();
        }

        WorkerEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (WorkerEntity candidate : workerRepository.findFaceEnrolled(organizationId)) {
            if (candidate.getId().equals(excludeWorkerId)) {
                continue;
            }
            double distance = distanceTo(mapper, probe, candidate);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        // Only the numeric distance is logged, never the descriptor itself.
        logger.info("Face match: closest distance {} (threshold {}, excluded worker: {})",
                best == null ? "none" : String.format("%.3f", bestDistance), FACE_MATCH_THRESHOLD, excludeWorkerId != null);
        return bestDistance <= FACE_MATCH_THRESHOLD ? Optional.ofNullable(best) : Optional.empty();
    }

    /**
     * Distance from a probe descriptor to a worker's stored template; a malformed template never matches.
     *
     * @param mapper the JSON mapper
     * @param probe the descriptor being looked up
     * @param candidate the worker whose stored template is compared
     * @return the distance, or {@link Double#MAX_VALUE} when the stored template is unusable
     */
    private double distanceTo(ObjectMapper mapper, double[] probe, WorkerEntity candidate) {
        try {
            double[] stored = mapper.readValue(candidate.getFaceDescriptor(), double[].class);
            return stored.length == probe.length ? faceDistance(probe, stored) : Double.MAX_VALUE;
        } catch (Exception e) {
            return Double.MAX_VALUE;
        }
    }

    /**
     * Rejects a face that is already registered to a different worker, so two workers can never be confused.
     *
     * @param organizationId the organization UUID
     * @param faceDescriptor the descriptor being registered
     * @param workerId the worker being registered, excluded from the comparison; null for a new worker
     * @throws SmartRoadException if another worker already has this face
     */
    private void requireFaceNotRegisteredToAnother(UUID organizationId, String faceDescriptor, UUID workerId) throws SmartRoadException {
        if (findClosestFace(organizationId, faceDescriptor, workerId).isPresent()) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.SERVICE_VALIDATION_FAILED,
                "worker.face.already.registered"
            );
        }
    }

    /**
     * Identifies the worker whose registered face matches a captured face. Nothing is created or changed.
     *
     * @param userId the UUID of the requesting user
     * @param projectId the project the worker is checking in to; its organization scopes the search
     * @param faceDescriptor the captured descriptor, as a JSON array
     * @return the matching active {@link WorkerEntity}
     * @throws SmartRoadException if the project is unknown, the user is not authorized, the face is not recognized,
     *                            or the matching worker is not active
     */
    @Transactional(readOnly = true)
    public WorkerEntity matchFace(UUID userId, UUID projectId, String faceDescriptor) throws SmartRoadException {
        UUID organizationId = requireProjectAccess(userId, projectId);
        requireValidFaceDescriptor(faceDescriptor);

        WorkerEntity worker = findClosestFace(organizationId, faceDescriptor, null)
                .orElseThrow(() -> new SmartRoadException(
                        ApplicationLayer.SERVICE_LAYER,
                        ErrorCodeMapping.SERVICE_VALIDATION_FAILED,
                        "worker.face.not.recognized"));
        requireActive(worker);
        return worker;
    }

    /**
     * Reports whether a worker has a registered face.
     *
     * @param userId the UUID of the requesting user
     * @param workerId the worker UUID
     * @return the worker's {@link WorkerFaceStatusResponseDTO}
     * @throws SmartRoadException if the worker is not found or the user is not authorized
     */
    @Transactional(readOnly = true)
    public WorkerFaceStatusResponseDTO getFaceStatus(UUID userId, UUID workerId) throws SmartRoadException {
        WorkerEntity worker = requireWorker(userId, workerId);
        boolean registered = worker.getFaceDescriptor() != null && !worker.getFaceDescriptor().isBlank();

        return new WorkerFaceStatusResponseDTO(worker.getId(), worker.getEmployeeId(),
            registered ? WorkerFaceStatusEnum.FACE_REGISTERED : WorkerFaceStatusEnum.FACE_NOT_REGISTERED);
    }

    /**
     * Finds a worker by employee ID, for QR check-in.
     *
     * @param userId the UUID of the requesting user
     * @param employeeId the employee ID, e.g. SR1
     * @return the matching {@link WorkerResponseDTO}
     * @throws SmartRoadException if no worker holds the ID or the user is not a member of the worker's organization
     */
    @Transactional(readOnly = true)
    public WorkerResponseDTO findByEmployeeId(UUID userId, String employeeId) throws SmartRoadException {
        WorkerEntity worker = workerRepository.findByEmployeeId(employeeId.trim())
            .orElseThrow(() -> new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "worker.not.found"
            ));
        organizationService.requireMember(userId, worker.getOrganizationId());

        return workerMapper.toWorkerResponseDTO(worker);
    }

    /**
     * Loads a project's organization id, failing when the project does not exist.
     *
     * @param projectId the project UUID
     * @return the UUID of the organization that owns the project
     * @throws SmartRoadException if the project is not found
     */
    private UUID projectOrganization(UUID projectId) throws SmartRoadException {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new SmartRoadException(
                        ApplicationLayer.SERVICE_LAYER,
                        ErrorCodeMapping.DAO_NOT_FOUND,
                        "project.not.found"))
                .getOrganizationId();
    }

    /**
     * Confirms the user belongs to the organization that owns a project.
     *
     * @param userId the UUID of the requesting user
     * @param projectId the project UUID
     * @return the UUID of the project's organization
     * @throws SmartRoadException if the project is unknown or the user is not a member
     */
    @Transactional(readOnly = true)
    public UUID requireProjectAccess(UUID userId, UUID projectId) throws SmartRoadException {
        UUID organizationId = projectOrganization(projectId);
        organizationService.requireMember(userId, organizationId);
        return organizationId;
    }

    /**
     * Loads a worker the user is authorized to act on.
     *
     * @param userId the UUID of the requesting user
     * @param workerId the worker UUID
     * @return the {@link WorkerEntity}
     * @throws SmartRoadException if the worker is not found or the user is not authorized
     */
    @Transactional(readOnly = true)
    public WorkerEntity requireWorkerAccess(UUID userId, UUID workerId) throws SmartRoadException {
        return requireWorker(userId, workerId);
    }

    /**
     * Requires the worker to be active.
     *
     * @param worker the worker to check
     * @throws SmartRoadException if the worker is not active
     */
    private void requireActive(WorkerEntity worker) throws SmartRoadException {
        if (worker.getStatus() != WorkerStatusEnum.ACTIVE) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.SERVICE_VALIDATION_FAILED,
                "worker.not.active"
            );
        }
    }

    /**
     * Loads an active worker for an attendance action on a project, checking the user's access and
     * that the worker belongs to the project's organization.
     *
     * @param userId the UUID of the requesting user
     * @param workerId the worker UUID
     * @param projectId the project UUID
     * @return the active {@link WorkerEntity}
     * @throws SmartRoadException if the worker or project is unknown, the user is not authorized, or the worker is inactive
     */
    @Transactional(readOnly = true)
    public WorkerEntity requireActiveWorkerForProject(UUID userId, UUID workerId, UUID projectId) throws SmartRoadException {
        WorkerEntity worker = requireWorker(userId, workerId);
        if (!worker.getOrganizationId().equals(projectOrganization(projectId))) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "worker.not.found"
            );
        }
        requireActive(worker);
        return worker;
    }

    /**
     * Identifies the worker a scanned worker QR code belongs to. The QR text carries only the public
     * employee ID (WORKER:SR1); the worker is always looked up and authorized on the server.
     *
     * @param userId the UUID of the requesting user
     * @param projectId the project the worker is attending
     * @param qrCode the scanned QR text
     * @return the active {@link WorkerEntity}
     * @throws SmartRoadException if the QR text is not a worker QR, the worker is unknown or inactive,
     *                            or the user is not authorized
     */
    @Transactional(readOnly = true)
    public WorkerEntity identifyByQr(UUID userId, UUID projectId, String qrCode) throws SmartRoadException {
        String text = qrCode == null ? "" : qrCode.trim();
        if (!text.regionMatches(true, 0, WORKER_QR_PREFIX, 0, WORKER_QR_PREFIX.length())
                || !text.substring(WORKER_QR_PREFIX.length()).trim().matches("(?i)SR\\d+")) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.SERVICE_INVALID_INPUT,
                "worker.qr.invalid"
            );
        }

        WorkerEntity worker = workerRepository.findByEmployeeId(text.substring(WORKER_QR_PREFIX.length()).trim())
            .orElseThrow(() -> new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "worker.not.found"
            ));
        return requireActiveWorkerForProject(userId, worker.getId(), projectId);
    }

    /**
     * Confirms the user may act on a worker, for callers outside this service.
     *
     * @param userId the UUID of the requesting user
     * @param workerId the worker UUID
     * @throws SmartRoadException if the worker is not found or the user is not authorized
     */
    @Transactional(readOnly = true)
    public void verifyWorkerAccess(UUID userId, UUID workerId) throws SmartRoadException {
        requireWorker(userId, workerId);
    }

    /**
     * Loads workers by id, keyed by worker UUID, for enriching other records with names.
     *
     * @param workerIds the worker UUIDs
     * @return map of worker UUID to {@link WorkerEntity}
     */
    @Transactional(readOnly = true)
    public Map<UUID, WorkerEntity> findAllById(Collection<UUID> workerIds) {
        return workerRepository.findAllById(workerIds).stream()
            .collect(Collectors.toMap(WorkerEntity::getId, Function.identity()));
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
        applyWagePatch(entity, request);
        applyWageDefaults(entity);

        workerRepository.save(entity);

        return workerMapper.toWorkerResponseDTO(entity);
    }

    /**
     * Fills in a worker's wage configuration defaults. A new worker, and a worker whose wage
     * configuration has never been set, defaults to a DAILY wage type with wage tracking active,
     * so the NOT NULL wage_type column is always satisfied and payroll can always determine
     * how to compute a worker's basic wage.
     *
     * @param entity the worker entity to default
     */
    private void applyWageDefaults(WorkerEntity entity) {
        if (entity.getWageType() == null) {
            entity.setWageType(WageTypeEnum.DAILY);
        }
        if (entity.getWageActive() == null) {
            entity.setWageActive(Boolean.TRUE);
        }
    }

    /**
     * Applies only the wage configuration fields a caller actually supplied, leaving the
     * worker's existing configuration untouched otherwise. {@link WorkerMapper#updateWorkerEntityFromDto}
     * ignores these fields for exactly this reason: an update that edits unrelated worker
     * details, such as address or status, must never silently clear a wage configuration that
     * payroll already depends on just because the request omitted it.
     *
     * @param entity the worker entity being updated
     * @param request the update request
     */
    private void applyWagePatch(WorkerEntity entity, WorkerRequestDTO request) {
        if (request.wageType() != null) {
            entity.setWageType(request.wageType());
        }
        if (request.dailyWage() != null) {
            entity.setDailyWage(request.dailyWage());
        }
        if (request.monthlyWage() != null) {
            entity.setMonthlyWage(request.monthlyWage());
        }
        if (request.overtimeRate() != null) {
            entity.setOvertimeRate(request.overtimeRate());
        }
        if (request.wageEffectiveDate() != null) {
            entity.setWageEffectiveDate(request.wageEffectiveDate());
        }
        if (request.wageActive() != null) {
            entity.setWageActive(request.wageActive());
        }
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
     * Retrieves every attendance record of a project for one day.
     *
     * @param userId the UUID of the requesting user
     * @param projectId the project UUID
     * @param date the day to report on
     * @return list of {@link AttendanceResponseDTO} for that project and day
     * @throws SmartRoadException if the project is not found or the user does not belong to its organization
     */
    @Transactional(readOnly = true)
    public List<AttendanceResponseDTO> listProjectAttendance(UUID userId, UUID projectId, LocalDate date) throws SmartRoadException {
        UUID organizationId = projectRepository.findById(projectId)
                .orElseThrow(() -> new SmartRoadException(
                        ApplicationLayer.SERVICE_LAYER,
                        ErrorCodeMapping.DAO_NOT_FOUND,
                        "project.not.found"))
                .getOrganizationId();
        organizationService.requireMember(userId, organizationId);

        return attendanceRepository.findByProjectIdAndAttendanceDate(projectId, date).stream()
                .map(this::toAttendanceResponse).toList();
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
