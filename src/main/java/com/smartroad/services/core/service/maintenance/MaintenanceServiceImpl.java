package com.smartroad.services.core.service.maintenance;

import com.smartroad.services.common.enums.maintenance.MaintenanceCategoryEnum;
import com.smartroad.services.common.enums.maintenance.MaintenancePriorityEnum;
import com.smartroad.services.common.enums.maintenance.MaintenanceRecurrenceEnum;
import com.smartroad.services.common.enums.maintenance.MaintenanceReminderTypeEnum;
import com.smartroad.services.common.enums.maintenance.MaintenanceStatusEnum;
import com.smartroad.services.common.enums.maintenance.MaintenanceTypeEnum;
import com.smartroad.services.common.exception.ApplicationLayer;
import com.smartroad.services.common.exception.ErrorCodeMapping;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.maintenance.MaintenanceCancelRequestDTO;
import com.smartroad.services.core.dto.maintenance.MaintenanceCompleteRequestDTO;
import com.smartroad.services.core.dto.maintenance.MaintenanceFilterDTO;
import com.smartroad.services.core.dto.maintenance.MaintenancePageResponseDTO;
import com.smartroad.services.core.dto.maintenance.MaintenanceReminderDTO;
import com.smartroad.services.core.dto.maintenance.MaintenanceRemindersDTO;
import com.smartroad.services.core.dto.maintenance.MaintenanceRequestDTO;
import com.smartroad.services.core.dto.maintenance.MaintenanceResponseDTO;
import com.smartroad.services.core.dto.maintenance.MaintenanceStartRequestDTO;
import com.smartroad.services.core.dto.maintenance.MaintenanceSummaryDTO;
import com.smartroad.services.core.dto.material.PaginationMetaDTO;
import com.smartroad.services.core.mapper.maintenance.MaintenanceMapper;
import com.smartroad.services.core.service.organization.OrganizationService;
import com.smartroad.services.domain.entity.machine.MachineEntity;
import com.smartroad.services.domain.entity.maintenance.MaintenanceRecordEntity;
import com.smartroad.services.domain.entity.project.ProjectEntity;
import com.smartroad.services.domain.entity.worker.WorkerEntity;
import com.smartroad.services.domain.repository.MachineRepository;
import com.smartroad.services.domain.repository.MaintenanceRecordRepository;
import com.smartroad.services.domain.repository.ProjectRepository;
import com.smartroad.services.domain.repository.WorkerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service layer for maintenance management. Owns record CRUD, the server-enforced
 * SCHEDULED/DUE/OVERDUE -> IN_PROGRESS -> COMPLETED workflow (plus cancellation), derivation of the
 * DUE/OVERDUE statuses on read (no scheduler exists in this codebase, so nothing persists them),
 * the bounded one-occurrence-at-a-time recurrence model, downtime computation, the dashboard
 * summary, the calendar feed, and on-demand reminder computation.
 *
 * @author Vishal
 * @version 1.0
 */
@Service
public class MaintenanceServiceImpl implements MaintenanceService {

    private static final Logger logger = LoggerFactory.getLogger(MaintenanceServiceImpl.class);

    /** A SCHEDULED record becomes DUE once its scheduled start is less than this far away. */
    static final Duration DUE_WINDOW = Duration.ofHours(24);
    private static final int DEFAULT_UPCOMING_DAYS = 7;
    private static final int MAX_UPCOMING_DAYS = 90;
    private static final int MAX_CALENDAR_DAYS = 92;
    private static final int MAX_REMINDERS = 100;
    private static final int MAX_RECURRENCE_STEPS = 1000;
    private static final Duration START_CLOCK_SKEW = Duration.ofMinutes(5);
    private static final String UNDER_MAINTENANCE_LABEL = "UNDER_MAINTENANCE";
    private static final String REFERENCE_PREFIX = "MNT-";
    private static final OffsetDateTime MIN_BOUND = OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime MAX_BOUND = OffsetDateTime.of(9999, 12, 31, 0, 0, 0, 0, ZoneOffset.UTC);
    private static final String NOT_FOUND_KEY = "maintenance.not.found";
    private static final String INVALID_TRANSITION_KEY = "maintenance.invalid.status.transition";

    private final MaintenanceRecordRepository maintenanceRepository;
    private final MachineRepository machineRepository;
    private final ProjectRepository projectRepository;
    private final WorkerRepository workerRepository;
    private final OrganizationService organizationService;
    private final MaintenanceMapper maintenanceMapper;

    /**
     * Constructs a MaintenanceServiceImpl with required dependencies.
     *
     * @param maintenanceRepository the maintenance record repository
     * @param machineRepository the machine repository, used to validate and display the referenced machine
     * @param projectRepository the project repository, used to validate and display the project/site
     * @param workerRepository the worker repository, used to validate the assigned technician
     * @param organizationService the organization service, used to authorize record creation
     * @param maintenanceMapper the maintenance mapper
     */
    public MaintenanceServiceImpl(MaintenanceRecordRepository maintenanceRepository,
                                  MachineRepository machineRepository, ProjectRepository projectRepository,
                                  WorkerRepository workerRepository, OrganizationService organizationService,
                                  MaintenanceMapper maintenanceMapper) {
        this.maintenanceRepository = maintenanceRepository;
        this.machineRepository = machineRepository;
        this.projectRepository = projectRepository;
        this.workerRepository = workerRepository;
        this.organizationService = organizationService;
        this.maintenanceMapper = maintenanceMapper;
    }

    @Override
    @Transactional
    public MaintenanceResponseDTO create(UUID userId, UUID organizationId, MaintenanceRequestDTO request)
            throws SmartRoadException {
        organizationService.requireMember(userId, organizationId);
        requireMachine(organizationId, request.machineId());
        validateRequest(request);

        MaintenanceTypeEnum type = request.maintenanceType();
        OffsetDateTime now = OffsetDateTime.now();
        MaintenanceRecordEntity entity = MaintenanceRecordEntity.builder()
                .organizationId(organizationId)
                .maintenanceReference(nextReference())
                .machineId(request.machineId())
                .status(MaintenanceStatusEnum.SCHEDULED)
                .recurrenceType(resolveRecurrence(request.recurrenceType()))
                .reportedAt(resolveReportedAt(type, request.reportedAt(), now))
                .priority(resolvePriority(type, request.priority()))
                .build();
        applyEditableFields(entity, request);
        entity.setActualCost(request.actualCost());
        entity.setCreatedBy(userId);
        entity.setModifiedBy(userId);

        MaintenanceRecordEntity saved = maintenanceRepository.save(entity);
        logNotificationEvent(MaintenanceReminderTypeEnum.CREATED, saved);
        generateNextOccurrence(userId, saved, now);

        return toResponse(saved, now);
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenancePageResponseDTO list(MaintenanceFilterDTO filter) throws SmartRoadException {
        OffsetDateTime now = OffsetDateTime.now();
        SearchCriteria criteria = new SearchCriteria(filter.organizationId());
        criteria.machineId = filter.machineId();
        criteria.projectId = filter.projectId();
        criteria.maintenanceType = parseEnumFilter(filter.type(), MaintenanceTypeEnum::fromValue);
        criteria.priority = parseEnumFilter(filter.priority(), MaintenancePriorityEnum::fromValue);
        applyDateRange(criteria, filter.dateFrom(), filter.dateTo());
        applyStatus(criteria, parseEnumFilter(filter.status(), MaintenanceStatusEnum::fromValue), now);
        if (filter.overdue()) {
            applyStatus(criteria, MaintenanceStatusEnum.OVERDUE, now);
        }
        if (filter.upcoming()) {
            applyUpcoming(criteria, now, DEFAULT_UPCOMING_DAYS);
        }
        Sort sort = filter.upcoming()
                ? Sort.by(Sort.Direction.ASC, "scheduledStart")
                : Sort.by(Sort.Direction.DESC, "scheduledStart");
        return searchPage(criteria, filter.page(), filter.limit(), sort, now);
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceResponseDTO getById(UUID id) throws SmartRoadException {
        return toResponse(requireRecord(id), OffsetDateTime.now());
    }

    @Override
    @Transactional
    public MaintenanceResponseDTO update(UUID userId, UUID id, MaintenanceRequestDTO request)
            throws SmartRoadException {
        MaintenanceRecordEntity entity = requireRecord(id);
        if (entity.isTerminal()) {
            applyTerminalUpdate(entity, request);
        } else {
            applyActiveUpdate(entity, request);
        }
        entity.setModifiedBy(userId);
        return toResponse(maintenanceRepository.save(entity), OffsetDateTime.now());
    }

    @Override
    @Transactional
    public MaintenanceResponseDTO delete(UUID userId, UUID id) throws SmartRoadException {
        MaintenanceRecordEntity entity = requireRecord(id);
        if (entity.getStatus() == MaintenanceStatusEnum.COMPLETED) {
            throw new SmartRoadException(ApplicationLayer.BUSINESS_LAYER,
                    ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "maintenance.delete.completed");
        }
        return cancel(userId, id, new MaintenanceCancelRequestDTO(null));
    }

    @Override
    @Transactional
    public MaintenanceResponseDTO start(UUID userId, UUID id, MaintenanceStartRequestDTO request)
            throws SmartRoadException {
        MaintenanceRecordEntity entity = requireRecord(id);
        if (entity.getStatus() != MaintenanceStatusEnum.SCHEDULED) {
            throw invalidTransition();
        }
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime actualStart = request == null || request.actualStart() == null ? now : request.actualStart();
        if (actualStart.isAfter(now.plus(START_CLOCK_SKEW))) {
            throw new SmartRoadException(ApplicationLayer.BUSINESS_LAYER,
                    ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "maintenance.actual.start.future");
        }
        if (request != null) {
            applyTechnicianIfPresent(entity, request.technicianWorkerId(), request.technicianName());
            entity.setNotes(appendNote(entity.getNotes(), request.notes()));
        }
        entity.setActualStart(actualStart);
        entity.setStatus(MaintenanceStatusEnum.IN_PROGRESS);
        entity.setModifiedBy(userId);
        markMachineUnderMaintenance(userId, entity);

        return toResponse(maintenanceRepository.save(entity), now);
    }

    @Override
    @Transactional
    public MaintenanceResponseDTO complete(UUID userId, UUID id, MaintenanceCompleteRequestDTO request)
            throws SmartRoadException {
        MaintenanceRecordEntity entity = requireRecord(id);
        if (entity.getStatus() != MaintenanceStatusEnum.IN_PROGRESS) {
            throw invalidTransition();
        }
        if (entity.getActualStart() == null) {
            throw new SmartRoadException(ApplicationLayer.BUSINESS_LAYER,
                    ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "maintenance.actual.start.missing");
        }
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime actualEnd = request == null || request.actualEnd() == null ? now : request.actualEnd();
        if (actualEnd.isBefore(entity.getActualStart())) {
            throw new SmartRoadException(ApplicationLayer.BUSINESS_LAYER,
                    ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "maintenance.actual.end.before.start");
        }
        if (request != null) {
            applyCompletionDetails(entity, request);
        }
        entity.setActualEnd(actualEnd);
        entity.setDowntimeMinutes(Duration.between(entity.getActualStart(), actualEnd).toMinutes());
        entity.setStatus(MaintenanceStatusEnum.COMPLETED);
        entity.setModifiedBy(userId);
        restoreMachineLabel(userId, entity);

        MaintenanceRecordEntity saved = maintenanceRepository.save(entity);
        logNotificationEvent(MaintenanceReminderTypeEnum.COMPLETED, saved);
        generateNextOccurrence(userId, saved, now);
        return toResponse(saved, now);
    }

    @Override
    @Transactional
    public MaintenanceResponseDTO cancel(UUID userId, UUID id, MaintenanceCancelRequestDTO request)
            throws SmartRoadException {
        MaintenanceRecordEntity entity = requireRecord(id);
        if (entity.isTerminal()) {
            throw invalidTransition();
        }
        if (entity.getStatus() == MaintenanceStatusEnum.IN_PROGRESS) {
            restoreMachineLabel(userId, entity);
        }
        entity.setStatus(MaintenanceStatusEnum.CANCELLED);
        entity.setCancellationReason(request == null ? null : trimToNull(request.reason()));
        entity.setModifiedBy(userId);
        return toResponse(maintenanceRepository.save(entity), OffsetDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenancePageResponseDTO getMachineHistory(UUID organizationId, UUID machineId, int page, int limit)
            throws SmartRoadException {
        requireMachine(organizationId, machineId);
        SearchCriteria criteria = new SearchCriteria(organizationId);
        criteria.machineId = machineId;
        return searchPage(criteria, page, limit, Sort.by(Sort.Direction.DESC, "scheduledStart"), OffsetDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenancePageResponseDTO getUpcoming(UUID organizationId, Integer days, int page, int limit)
            throws SmartRoadException {
        int resolvedDays = days == null ? DEFAULT_UPCOMING_DAYS : days;
        if (resolvedDays < 1 || resolvedDays > MAX_UPCOMING_DAYS) {
            throw new SmartRoadException(ApplicationLayer.BUSINESS_LAYER,
                    ErrorCodeMapping.SERVICE_INVALID_INPUT, "maintenance.upcoming.days.invalid");
        }
        OffsetDateTime now = OffsetDateTime.now();
        SearchCriteria criteria = new SearchCriteria(organizationId);
        applyUpcoming(criteria, now, resolvedDays);
        return searchPage(criteria, page, limit, Sort.by(Sort.Direction.ASC, "scheduledStart"), now);
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenancePageResponseDTO getOverdue(UUID organizationId, int page, int limit) {
        OffsetDateTime now = OffsetDateTime.now();
        SearchCriteria criteria = new SearchCriteria(organizationId);
        applyStatus(criteria, MaintenanceStatusEnum.OVERDUE, now);
        return searchPage(criteria, page, limit, Sort.by(Sort.Direction.ASC, "dueBy"), now);
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceSummaryDTO getSummary(UUID organizationId, LocalDate startDate, LocalDate endDate)
            throws SmartRoadException {
        LocalDate[] range = resolveRange(startDate, endDate);
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime periodFrom = startOfDay(range[0]);
        OffsetDateTime periodBefore = startOfDay(range[1].plusDays(1));

        BigDecimal actualCost = nullToZero(maintenanceRepository.sumActualCost(organizationId, periodFrom,
                periodBefore, MaintenanceStatusEnum.CANCELLED));
        BigDecimal estimatedFallback = nullToZero(maintenanceRepository.sumEstimatedCostWithoutActual(
                organizationId, periodFrom, periodBefore, MaintenanceStatusEnum.CANCELLED));
        Long downtime = maintenanceRepository.sumDowntimeMinutes(organizationId, periodFrom, periodBefore,
                MaintenanceStatusEnum.COMPLETED);

        return MaintenanceSummaryDTO.builder()
                .periodStart(range[0])
                .periodEnd(range[1])
                .totalMachines(machineRepository.countByOrganizationIdAndIsDeletedFalse(organizationId))
                .scheduled(countDerived(organizationId, MaintenanceStatusEnum.SCHEDULED, now))
                .dueSoon(countDerived(organizationId, MaintenanceStatusEnum.DUE, now))
                .overdue(countDerived(organizationId, MaintenanceStatusEnum.OVERDUE, now))
                .inProgress(countDerived(organizationId, MaintenanceStatusEnum.IN_PROGRESS, now))
                .machinesUnderMaintenance(maintenanceRepository.countDistinctMachinesInStatus(organizationId,
                        MaintenanceStatusEnum.IN_PROGRESS))
                .totalRecords(countInPeriod(organizationId, null, periodFrom, periodBefore))
                .completed(countInPeriod(organizationId, MaintenanceStatusEnum.COMPLETED, periodFrom, periodBefore))
                .cancelled(countInPeriod(organizationId, MaintenanceStatusEnum.CANCELLED, periodFrom, periodBefore))
                .actualCostInPeriod(actualCost)
                .estimatedCostFallbackInPeriod(estimatedFallback)
                .totalCostInPeriod(actualCost.add(estimatedFallback))
                .totalDowntimeMinutesInPeriod(downtime == null ? 0L : downtime)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceResponseDTO> getCalendar(UUID organizationId, LocalDate startDate, LocalDate endDate)
            throws SmartRoadException {
        LocalDate[] range = resolveRange(startDate, endDate);
        if (ChronoUnit.DAYS.between(range[0], range[1]) + 1 > MAX_CALENDAR_DAYS) {
            throw new SmartRoadException(ApplicationLayer.BUSINESS_LAYER,
                    ErrorCodeMapping.SERVICE_INVALID_INPUT, "maintenance.calendar.range.too.large");
        }
        List<MaintenanceRecordEntity> records = maintenanceRepository.findForCalendar(organizationId,
                startOfDay(range[0]), startOfDay(range[1].plusDays(1)));
        return toResponses(records, OffsetDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceRemindersDTO getReminders(UUID organizationId) {
        OffsetDateTime now = OffsetDateTime.now();

        SearchCriteria dueCriteria = new SearchCriteria(organizationId);
        applyStatus(dueCriteria, MaintenanceStatusEnum.DUE, now);
        List<MaintenanceRecordEntity> due = search(dueCriteria, PageRequest.of(0, MAX_REMINDERS,
                Sort.by(Sort.Direction.ASC, "scheduledStart"))).getContent();

        SearchCriteria overdueCriteria = new SearchCriteria(organizationId);
        applyStatus(overdueCriteria, MaintenanceStatusEnum.OVERDUE, now);
        List<MaintenanceRecordEntity> overdue = search(overdueCriteria, PageRequest.of(0, MAX_REMINDERS,
                Sort.by(Sort.Direction.ASC, "dueBy"))).getContent();

        Map<UUID, MachineEntity> machines = loadMachines(due, overdue);
        return MaintenanceRemindersDTO.builder()
                .generatedAt(now)
                .dueReminders(toReminders(due, MaintenanceReminderTypeEnum.DUE, machines))
                .overdueReminders(toReminders(overdue, MaintenanceReminderTypeEnum.OVERDUE, machines))
                .build();
    }

    /**
     * Derives the effective status of a record: non-SCHEDULED persisted statuses are returned as-is;
     * a SCHEDULED record is OVERDUE once its due-by deadline has passed, DUE once its scheduled start
     * is within {@link #DUE_WINDOW} (or already past), and SCHEDULED otherwise. The repository-level
     * filters built by {@link #applyStatus} use exactly the same boundaries.
     *
     * @param entity the record
     * @param now the reference time
     * @return the effective {@link MaintenanceStatusEnum}
     */
    static MaintenanceStatusEnum deriveStatus(MaintenanceRecordEntity entity, OffsetDateTime now) {
        if (entity.getStatus() != MaintenanceStatusEnum.SCHEDULED) {
            return entity.getStatus();
        }
        if (entity.getDueBy().isBefore(now)) {
            return MaintenanceStatusEnum.OVERDUE;
        }
        if (entity.getScheduledStart().isBefore(now.plus(DUE_WINDOW))) {
            return MaintenanceStatusEnum.DUE;
        }
        return MaintenanceStatusEnum.SCHEDULED;
    }

    /**
     * Computes the due-by deadline: the scheduled end when supplied, otherwise the end of the
     * scheduled start's calendar day in the offset the caller supplied.
     *
     * @param scheduledStart the scheduled start
     * @param scheduledEnd the scheduled end, or null
     * @return the due-by deadline
     */
    static OffsetDateTime computeDueBy(OffsetDateTime scheduledStart, OffsetDateTime scheduledEnd) {
        if (scheduledEnd != null) {
            return scheduledEnd;
        }
        return scheduledStart.toLocalDate().plusDays(1).atStartOfDay().atOffset(scheduledStart.getOffset());
    }

    /**
     * Validates the cross-field rules of a create/update request: scheduled end after start,
     * category allowed for the type, recurrence only for preventive work, and that the referenced
     * project and technician exist.
     *
     * @param request the request
     * @throws SmartRoadException if any rule is violated
     */
    private void validateRequest(MaintenanceRequestDTO request) throws SmartRoadException {
        if (request.scheduledEnd() != null && !request.scheduledEnd().isAfter(request.scheduledStart())) {
            throw new SmartRoadException(ApplicationLayer.BUSINESS_LAYER,
                    ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "maintenance.scheduled.end.before.start");
        }
        if (!resolveCategory(request.category()).isAllowedFor(request.maintenanceType())) {
            throw new SmartRoadException(ApplicationLayer.BUSINESS_LAYER,
                    ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "maintenance.category.type.mismatch");
        }
        if (request.maintenanceType() == MaintenanceTypeEnum.CORRECTIVE
                && resolveRecurrence(request.recurrenceType()) != MaintenanceRecurrenceEnum.ONE_TIME) {
            throw new SmartRoadException(ApplicationLayer.BUSINESS_LAYER,
                    ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "maintenance.recurrence.preventive.only");
        }
        if (request.projectId() != null) {
            requireProject(request.projectId());
        }
        if (request.technicianWorkerId() != null) {
            requireWorker(request.technicianWorkerId());
        }
    }

    /**
     * Copies the freely editable (non-workflow) request fields onto an entity and recomputes the
     * due-by deadline. Workflow fields (status, actual start/end, downtime) are never touched here.
     *
     * @param entity the target entity
     * @param request the validated request
     * @throws SmartRoadException if the technician worker cannot be resolved
     */
    private void applyEditableFields(MaintenanceRecordEntity entity, MaintenanceRequestDTO request)
            throws SmartRoadException {
        entity.setMachineId(request.machineId());
        entity.setProjectId(request.projectId());
        entity.setTitle(request.title().trim());
        entity.setMaintenanceType(request.maintenanceType());
        entity.setCategory(resolveCategory(request.category()));
        entity.setDescription(trimToNull(request.description()));
        entity.setScheduledStart(request.scheduledStart());
        entity.setScheduledEnd(request.scheduledEnd());
        entity.setDueBy(computeDueBy(request.scheduledStart(), request.scheduledEnd()));
        entity.setEstimatedCost(request.estimatedCost());
        entity.setPartsUsed(trimToNull(request.partsUsed()));
        entity.setNotes(trimToNull(request.notes()));
        entity.setTechnicianWorkerId(request.technicianWorkerId());
        entity.setTechnicianName(resolveTechnicianName(request.technicianWorkerId(), request.technicianName()));
    }

    /**
     * Applies a PUT to a SCHEDULED or IN_PROGRESS record: all editable fields may change, except
     * that the machine of an in-progress record cannot be swapped (it is physically under repair).
     *
     * @param entity the record
     * @param request the update request
     * @throws SmartRoadException if validation fails
     */
    private void applyActiveUpdate(MaintenanceRecordEntity entity, MaintenanceRequestDTO request)
            throws SmartRoadException {
        if (entity.getStatus() == MaintenanceStatusEnum.IN_PROGRESS
                && !entity.getMachineId().equals(request.machineId())) {
            throw new SmartRoadException(ApplicationLayer.BUSINESS_LAYER,
                    ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "maintenance.machine.change.in.progress");
        }
        requireMachine(entity.getOrganizationId(), request.machineId());
        validateRequest(request);
        applyEditableFields(entity, request);
        entity.setActualCost(request.actualCost());
        if (request.priority() != null) {
            entity.setPriority(request.priority());
        }
        if (request.recurrenceType() != null) {
            entity.setRecurrenceType(request.recurrenceType());
        }
        if (request.reportedAt() != null) {
            entity.setReportedAt(request.reportedAt());
        }
    }

    /**
     * Applies a PUT to a COMPLETED or CANCELLED record. Core workflow fields must be unchanged (a
     * terminal record can never be reopened or rescheduled); only notes may change, plus actual cost
     * and parts used on a completed record (post-completion cost reconciliation).
     *
     * @param entity the terminal record
     * @param request the update request
     * @throws SmartRoadException if any core field differs from the stored value
     */
    private void applyTerminalUpdate(MaintenanceRecordEntity entity, MaintenanceRequestDTO request)
            throws SmartRoadException {
        boolean completed = entity.getStatus() == MaintenanceStatusEnum.COMPLETED;
        boolean costChanged = !sameAmount(entity.getActualCost(), request.actualCost())
                || !Objects.equals(trimToNull(entity.getPartsUsed()), trimToNull(request.partsUsed()));
        if (coreFieldsChanged(entity, request) || (!completed && costChanged)) {
            throw new SmartRoadException(ApplicationLayer.BUSINESS_LAYER,
                    ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "maintenance.not.editable");
        }
        entity.setNotes(trimToNull(request.notes()));
        if (completed) {
            entity.setActualCost(request.actualCost());
            entity.setPartsUsed(trimToNull(request.partsUsed()));
        }
    }

    /**
     * Returns whether a request changes any core (schedule/identity) field of a stored record.
     *
     * @param entity the stored record
     * @param request the incoming request
     * @return true if at least one core field differs
     */
    private boolean coreFieldsChanged(MaintenanceRecordEntity entity, MaintenanceRequestDTO request) {
        List<Boolean> unchanged = List.of(
                Objects.equals(entity.getMachineId(), request.machineId()),
                Objects.equals(entity.getProjectId(), request.projectId()),
                Objects.equals(trimToNull(entity.getTitle()), trimToNull(request.title())),
                entity.getMaintenanceType() == request.maintenanceType(),
                request.category() == null || entity.getCategory() == request.category(),
                request.priority() == null || entity.getPriority() == request.priority(),
                request.recurrenceType() == null || entity.getRecurrenceType() == request.recurrenceType(),
                sameInstant(entity.getScheduledStart(), request.scheduledStart()),
                sameInstant(entity.getScheduledEnd(), request.scheduledEnd()),
                sameAmount(entity.getEstimatedCost(), request.estimatedCost()),
                Objects.equals(trimToNull(entity.getDescription()), trimToNull(request.description())),
                Objects.equals(entity.getTechnicianWorkerId(), request.technicianWorkerId()));
        return unchanged.contains(Boolean.FALSE);
    }

    /**
     * Applies the optional completion details (actual cost, parts used, notes) of a complete request.
     *
     * @param entity the record being completed
     * @param request the completion request
     */
    private void applyCompletionDetails(MaintenanceRecordEntity entity, MaintenanceCompleteRequestDTO request) {
        if (request.actualCost() != null) {
            entity.setActualCost(request.actualCost());
        }
        if (request.partsUsed() != null && !request.partsUsed().isBlank()) {
            entity.setPartsUsed(request.partsUsed().trim());
        }
        entity.setNotes(appendNote(entity.getNotes(), request.notes()));
    }

    /**
     * Updates the technician on start when one is supplied in the start request.
     *
     * @param entity the record
     * @param workerId the technician worker UUID, or null
     * @param name the free-text technician name, or null
     * @throws SmartRoadException if the worker is not found
     */
    private void applyTechnicianIfPresent(MaintenanceRecordEntity entity, UUID workerId, String name)
            throws SmartRoadException {
        if (workerId == null && (name == null || name.isBlank())) {
            return;
        }
        if (workerId != null) {
            requireWorker(workerId);
        }
        entity.setTechnicianWorkerId(workerId);
        entity.setTechnicianName(resolveTechnicianName(workerId, name));
    }

    /**
     * Sets the machine's free-text maintenance label to UNDER_MAINTENANCE when work starts,
     * remembering the previous label so it can be restored. Only the existing free-text
     * {@code maintenanceStatus} label is touched - never the machine's lifecycle status enum.
     *
     * @param userId the acting user
     * @param entity the record being started
     */
    private void markMachineUnderMaintenance(UUID userId, MaintenanceRecordEntity entity) {
        machineRepository.findByIdAndIsDeletedFalse(entity.getMachineId()).ifPresent(machine -> {
            String current = machine.getMaintenanceStatus();
            entity.setMachineLabelBeforeStart(UNDER_MAINTENANCE_LABEL.equals(current) ? null : current);
            machine.setMaintenanceStatus(UNDER_MAINTENANCE_LABEL);
            machine.setModifiedBy(userId);
            machineRepository.save(machine);
        });
    }

    /**
     * Restores the machine's free-text maintenance label captured at start, but only when no other
     * maintenance record for the machine is still in progress and the label was not changed by
     * someone else in the meantime.
     *
     * @param userId the acting user
     * @param entity the record leaving IN_PROGRESS
     */
    private void restoreMachineLabel(UUID userId, MaintenanceRecordEntity entity) {
        long othersInProgress = maintenanceRepository.countOtherForMachineInStatus(entity.getMachineId(),
                MaintenanceStatusEnum.IN_PROGRESS, entity.getId());
        if (othersInProgress > 0) {
            return;
        }
        machineRepository.findByIdAndIsDeletedFalse(entity.getMachineId())
                .filter(machine -> UNDER_MAINTENANCE_LABEL.equals(machine.getMaintenanceStatus()))
                .ifPresent(machine -> {
                    machine.setMaintenanceStatus(entity.getMachineLabelBeforeStart());
                    machine.setModifiedBy(userId);
                    machineRepository.save(machine);
                });
    }

    /**
     * Generates the single next occurrence of a recurring preventive record, unless it is one-time,
     * corrective, or already has a generated successor. The occurrence is advanced by the
     * recurrence interval until it starts in the future, so at most one pending future occurrence
     * exists per chain (no unbounded series is ever materialized).
     *
     * @param userId the acting user
     * @param source the record to recur from
     * @param now the reference time
     */
    private void generateNextOccurrence(UUID userId, MaintenanceRecordEntity source, OffsetDateTime now) {
        MaintenanceRecurrenceEnum recurrence = source.getRecurrenceType();
        if (recurrence == null || recurrence == MaintenanceRecurrenceEnum.ONE_TIME
                || source.getMaintenanceType() != MaintenanceTypeEnum.PREVENTIVE
                || maintenanceRepository.existsByParentMaintenanceId(source.getId())) {
            return;
        }
        OffsetDateTime nextStart = source.getScheduledStart();
        OffsetDateTime nextEnd = source.getScheduledEnd();
        int steps = 0;
        do {
            nextStart = advance(nextStart, recurrence);
            nextEnd = nextEnd == null ? null : advance(nextEnd, recurrence);
            steps++;
        } while (!nextStart.isAfter(now) && steps < MAX_RECURRENCE_STEPS);

        MaintenanceRecordEntity next = MaintenanceRecordEntity.builder()
                .organizationId(source.getOrganizationId())
                .maintenanceReference(nextReference())
                .machineId(source.getMachineId())
                .projectId(source.getProjectId())
                .title(source.getTitle())
                .maintenanceType(source.getMaintenanceType())
                .category(source.getCategory())
                .description(source.getDescription())
                .priority(source.getPriority())
                .status(MaintenanceStatusEnum.SCHEDULED)
                .scheduledStart(nextStart)
                .scheduledEnd(nextEnd)
                .dueBy(computeDueBy(nextStart, nextEnd))
                .technicianWorkerId(source.getTechnicianWorkerId())
                .technicianName(source.getTechnicianName())
                .estimatedCost(source.getEstimatedCost())
                .recurrenceType(recurrence)
                .parentMaintenanceId(source.getId())
                .build();
        next.setCreatedBy(userId);
        next.setModifiedBy(userId);
        MaintenanceRecordEntity saved = maintenanceRepository.save(next);
        logNotificationEvent(MaintenanceReminderTypeEnum.CREATED, saved);
    }

    /**
     * Advances a timestamp by one recurrence interval.
     *
     * @param value the timestamp
     * @param recurrence the recurrence interval (never ONE_TIME here)
     * @return the advanced timestamp
     */
    private OffsetDateTime advance(OffsetDateTime value, MaintenanceRecurrenceEnum recurrence) {
        return switch (recurrence) {
            case DAILY -> value.plusDays(1);
            case WEEKLY -> value.plusWeeks(1);
            case MONTHLY -> value.plusMonths(1);
            case ONE_TIME -> value;
        };
    }

    /**
     * Emits a structured log line for a notification-eligible maintenance event. This is the hook
     * point for future dispatch: the existing NotificationService only supports OTP SMS/e-mail to an
     * explicit recipient and there is no in-app notification store or recipient model for
     * maintenance, so nothing is sent today (see docs/MAINTENANCE_MANAGEMENT.md).
     *
     * @param eventType the event type
     * @param entity the record the event concerns
     */
    private void logNotificationEvent(MaintenanceReminderTypeEnum eventType, MaintenanceRecordEntity entity) {
        logger.info("Maintenance notification event [{}] reference={} machineId={} scheduledStart={}",
                eventType.name(), entity.getMaintenanceReference(), entity.getMachineId(), entity.getScheduledStart());
    }

    /**
     * Narrows search criteria to an effective status, translating derived statuses (SCHEDULED, DUE,
     * OVERDUE) into persisted-status plus scheduled-start/due-by window predicates that match
     * {@link #deriveStatus} exactly.
     *
     * @param criteria the criteria to narrow
     * @param status the effective status, or null for no status filter
     * @param now the reference time
     */
    private void applyStatus(SearchCriteria criteria, MaintenanceStatusEnum status, OffsetDateTime now) {
        if (status == null) {
            return;
        }
        switch (status) {
            case SCHEDULED -> {
                criteria.requireStatus(MaintenanceStatusEnum.SCHEDULED);
                criteria.narrowStartFrom(now.plus(DUE_WINDOW));
                criteria.narrowDueByFrom(now);
            }
            case DUE -> {
                criteria.requireStatus(MaintenanceStatusEnum.SCHEDULED);
                criteria.narrowStartBefore(now.plus(DUE_WINDOW));
                criteria.narrowDueByFrom(now);
            }
            case OVERDUE -> {
                criteria.requireStatus(MaintenanceStatusEnum.SCHEDULED);
                criteria.narrowDueByBefore(now);
            }
            default -> criteria.requireStatus(status);
        }
    }

    /**
     * Narrows search criteria to upcoming work: scheduled (not overdue) records starting within the
     * next {@code days} days.
     *
     * @param criteria the criteria to narrow
     * @param now the reference time
     * @param days the look-ahead window in days
     */
    private void applyUpcoming(SearchCriteria criteria, OffsetDateTime now, int days) {
        criteria.requireStatus(MaintenanceStatusEnum.SCHEDULED);
        criteria.narrowStartFrom(now);
        criteria.narrowStartBefore(now.plusDays(days));
        criteria.narrowDueByFrom(now);
    }

    /**
     * Narrows search criteria to records whose scheduled start falls in an inclusive date range.
     *
     * @param criteria the criteria to narrow
     * @param dateFrom the inclusive start date, or null
     * @param dateTo the inclusive end date, or null
     * @throws SmartRoadException if dateFrom is after dateTo
     */
    private void applyDateRange(SearchCriteria criteria, LocalDate dateFrom, LocalDate dateTo)
            throws SmartRoadException {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new SmartRoadException(ApplicationLayer.BUSINESS_LAYER,
                    ErrorCodeMapping.SERVICE_INVALID_INPUT, "maintenance.date.range.invalid");
        }
        if (dateFrom != null) {
            criteria.narrowStartFrom(startOfDay(dateFrom));
        }
        if (dateTo != null) {
            criteria.narrowStartBefore(startOfDay(dateTo.plusDays(1)));
        }
    }

    /**
     * Counts an organization's records in an effective (possibly derived) status.
     *
     * @param organizationId the organization UUID
     * @param status the effective status
     * @param now the reference time
     * @return the count
     */
    private long countDerived(UUID organizationId, MaintenanceStatusEnum status, OffsetDateTime now) {
        SearchCriteria criteria = new SearchCriteria(organizationId);
        applyStatus(criteria, status, now);
        return count(criteria);
    }

    /**
     * Counts an organization's records with an optional persisted status scheduled in a window.
     *
     * @param organizationId the organization UUID
     * @param status the persisted status, or null for any
     * @param from the inclusive window start
     * @param before the exclusive window end
     * @return the count
     */
    private long countInPeriod(UUID organizationId, MaintenanceStatusEnum status, OffsetDateTime from,
                               OffsetDateTime before) {
        SearchCriteria criteria = new SearchCriteria(organizationId);
        criteria.requireStatus(status);
        criteria.narrowStartFrom(from);
        criteria.narrowStartBefore(before);
        return count(criteria);
    }

    /**
     * Runs the filtered count query for a set of criteria.
     *
     * @param c the criteria
     * @return the count, or zero when the criteria are contradictory
     */
    private long count(SearchCriteria c) {
        if (c.empty) {
            return 0L;
        }
        return maintenanceRepository.countFiltered(c.organizationId, c.machineId, c.projectId, c.maintenanceType,
                c.priority, c.status, c.startFrom, c.startBefore, c.dueByFrom, c.dueByBefore);
    }

    /**
     * Runs the filtered search query for a set of criteria.
     *
     * @param c the criteria
     * @param pageRequest the page request including sort
     * @return the page of matching records (empty when the criteria are contradictory)
     */
    private Page<MaintenanceRecordEntity> search(SearchCriteria c, PageRequest pageRequest) {
        if (c.empty) {
            return Page.empty(pageRequest);
        }
        return maintenanceRepository.search(c.organizationId, c.machineId, c.projectId, c.maintenanceType,
                c.priority, c.status, c.startFrom, c.startBefore, c.dueByFrom, c.dueByBefore, pageRequest);
    }

    /**
     * Runs a paged search and converts it into the page response DTO.
     *
     * @param criteria the search criteria
     * @param page the one-based page number
     * @param limit the page size
     * @param sort the sort order
     * @param now the reference time for status derivation
     * @return the {@link MaintenancePageResponseDTO}
     */
    private MaintenancePageResponseDTO searchPage(SearchCriteria criteria, int page, int limit, Sort sort,
                                                  OffsetDateTime now) {
        int pageIndex = Math.max(page, 1) - 1;
        int pageSize = Math.max(limit, 1);
        Page<MaintenanceRecordEntity> results = search(criteria, PageRequest.of(pageIndex, pageSize, sort));
        PaginationMetaDTO pagination = new PaginationMetaDTO(pageIndex + 1, pageSize, results.getTotalElements(),
                results.getTotalPages(), results.hasNext(), results.hasPrevious());
        return new MaintenancePageResponseDTO(toResponses(results.getContent(), now), pagination);
    }

    /**
     * Converts a single record to its response DTO, resolving machine and project display data.
     *
     * @param entity the record
     * @param now the reference time for status derivation
     * @return the response DTO
     */
    private MaintenanceResponseDTO toResponse(MaintenanceRecordEntity entity, OffsetDateTime now) {
        return toResponses(List.of(entity), now).get(0);
    }

    /**
     * Converts records to response DTOs, batch-loading referenced machines and projects to avoid
     * one query per row.
     *
     * @param records the records
     * @param now the reference time for status derivation
     * @return the response DTOs, in the same order
     */
    private List<MaintenanceResponseDTO> toResponses(List<MaintenanceRecordEntity> records, OffsetDateTime now) {
        Map<UUID, MachineEntity> machines = loadMachines(records);
        Set<UUID> projectIds = records.stream().map(MaintenanceRecordEntity::getProjectId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<UUID, String> projectNames = projectRepository.findAllById(projectIds).stream()
                .collect(Collectors.toMap(ProjectEntity::getId, p -> p.getName() == null ? "" : p.getName()));
        return records.stream()
                .map(r -> maintenanceMapper.toResponseDTO(r, deriveStatus(r, now), machines.get(r.getMachineId()),
                        r.getProjectId() == null ? null : projectNames.get(r.getProjectId())))
                .toList();
    }

    /**
     * Batch-loads the machines referenced by one or more record lists (including soft-deleted
     * machines, so historical records still show the machine name).
     *
     * @param recordLists the record lists
     * @return map of machine ID to machine
     */
    @SafeVarargs
    private Map<UUID, MachineEntity> loadMachines(List<MaintenanceRecordEntity>... recordLists) {
        Set<UUID> machineIds = new HashSet<>();
        for (List<MaintenanceRecordEntity> records : recordLists) {
            records.forEach(r -> machineIds.add(r.getMachineId()));
        }
        return machineRepository.findAllById(machineIds).stream()
                .collect(Collectors.toMap(MachineEntity::getId, Function.identity()));
    }

    /**
     * Converts records to reminder DTOs with a human-readable message.
     *
     * @param records the records
     * @param type the reminder type
     * @param machines the pre-loaded machines by ID
     * @return the reminder DTOs
     */
    private List<MaintenanceReminderDTO> toReminders(List<MaintenanceRecordEntity> records,
                                                     MaintenanceReminderTypeEnum type,
                                                     Map<UUID, MachineEntity> machines) {
        return records.stream().map(r -> {
            MachineEntity machine = machines.get(r.getMachineId());
            String machineName = machine == null ? "machine" : machine.getName();
            String message = type == MaintenanceReminderTypeEnum.OVERDUE
                    ? r.getMaintenanceReference() + " (" + r.getTitle() + ") on " + machineName
                        + " is overdue; it was due by " + r.getDueBy()
                    : r.getMaintenanceReference() + " (" + r.getTitle() + ") on " + machineName
                        + " is due, scheduled to start " + r.getScheduledStart();
            return maintenanceMapper.toReminderDTO(r, type, machine == null ? null : machine.getName(), message);
        }).toList();
    }

    /**
     * Parses an optional enum filter value, mapping unknown values to a 400 error.
     *
     * @param raw the raw query value, or null/blank for no filter
     * @param parser the enum's fromValue parser
     * @param <E> the enum type
     * @return the parsed enum, or null when no filter was supplied
     * @throws SmartRoadException if the value is not a valid enum name or wire value
     */
    private <E> E parseEnumFilter(String raw, Function<String, E> parser) throws SmartRoadException {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return parser.apply(raw.trim());
        } catch (IllegalArgumentException ex) {
            throw new SmartRoadException(ApplicationLayer.BUSINESS_LAYER,
                    ErrorCodeMapping.SERVICE_INVALID_INPUT, "maintenance.invalid.filter", raw);
        }
    }

    /**
     * Loads a maintenance record by ID.
     *
     * @param id the record UUID
     * @return the record
     * @throws SmartRoadException if not found
     */
    private MaintenanceRecordEntity requireRecord(UUID id) throws SmartRoadException {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER,
                        ErrorCodeMapping.DAO_NOT_FOUND, NOT_FOUND_KEY));
    }

    /**
     * Loads a non-deleted machine belonging to the organization.
     *
     * @param organizationId the organization UUID, or null to skip the organization check
     * @param machineId the machine UUID
     * @return the machine
     * @throws SmartRoadException if the machine does not exist, is deleted, or belongs to another organization
     */
    private MachineEntity requireMachine(UUID organizationId, UUID machineId) throws SmartRoadException {
        MachineEntity machine = machineRepository.findByIdAndIsDeletedFalse(machineId)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER,
                        ErrorCodeMapping.DAO_NOT_FOUND, "machine.not.found"));
        if (organizationId != null && !organizationId.equals(machine.getOrganizationId())) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER,
                    ErrorCodeMapping.DAO_NOT_FOUND, "machine.not.found");
        }
        return machine;
    }

    /**
     * Verifies a project exists.
     *
     * @param projectId the project UUID
     * @throws SmartRoadException if not found
     */
    private void requireProject(UUID projectId) throws SmartRoadException {
        if (!projectRepository.existsById(projectId)) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER,
                    ErrorCodeMapping.DAO_NOT_FOUND, "project.not.found");
        }
    }

    /**
     * Loads a non-deleted worker.
     *
     * @param workerId the worker UUID
     * @return the worker
     * @throws SmartRoadException if not found or deleted
     */
    private WorkerEntity requireWorker(UUID workerId) throws SmartRoadException {
        return workerRepository.findById(workerId)
                .filter(w -> !Boolean.TRUE.equals(w.getIsDeleted()))
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER,
                        ErrorCodeMapping.DAO_NOT_FOUND, "worker.not.found"));
    }

    /**
     * Resolves the technician display name: the supplied free-text name when present, otherwise the
     * referenced worker's full name.
     *
     * @param workerId the technician worker UUID, or null
     * @param name the free-text name, or null
     * @return the resolved name, or null
     * @throws SmartRoadException if the worker is not found
     */
    private String resolveTechnicianName(UUID workerId, String name) throws SmartRoadException {
        String trimmed = trimToNull(name);
        if (trimmed != null || workerId == null) {
            return trimmed;
        }
        WorkerEntity worker = requireWorker(workerId);
        String fullName = ((worker.getFirstName() == null ? "" : worker.getFirstName()) + " "
                + (worker.getLastName() == null ? "" : worker.getLastName())).trim();
        return fullName.isEmpty() ? null : fullName;
    }

    /**
     * Draws the next human-readable maintenance reference, e.g. {@code MNT-000123}.
     *
     * @return the reference
     */
    private String nextReference() {
        return REFERENCE_PREFIX + String.format("%06d", maintenanceRepository.nextReferenceNumber());
    }

    /**
     * Resolves the category, defaulting to OTHER.
     *
     * @param category the requested category, or null
     * @return the resolved category
     */
    private MaintenanceCategoryEnum resolveCategory(MaintenanceCategoryEnum category) {
        return category == null ? MaintenanceCategoryEnum.OTHER : category;
    }

    /**
     * Resolves the recurrence, defaulting to ONE_TIME.
     *
     * @param recurrence the requested recurrence, or null
     * @return the resolved recurrence
     */
    private MaintenanceRecurrenceEnum resolveRecurrence(MaintenanceRecurrenceEnum recurrence) {
        return recurrence == null ? MaintenanceRecurrenceEnum.ONE_TIME : recurrence;
    }

    /**
     * Resolves the priority, defaulting to HIGH for corrective (breakdown) work and MEDIUM otherwise.
     *
     * @param type the maintenance type
     * @param priority the requested priority, or null
     * @return the resolved priority
     */
    private MaintenancePriorityEnum resolvePriority(MaintenanceTypeEnum type, MaintenancePriorityEnum priority) {
        if (priority != null) {
            return priority;
        }
        return type == MaintenanceTypeEnum.CORRECTIVE ? MaintenancePriorityEnum.HIGH : MaintenancePriorityEnum.MEDIUM;
    }

    /**
     * Resolves the reported-at timestamp: as supplied, or "now" for a corrective record reported without one.
     *
     * @param type the maintenance type
     * @param reportedAt the supplied value, or null
     * @param now the current time
     * @return the resolved value (null for a preventive record without one)
     */
    private OffsetDateTime resolveReportedAt(MaintenanceTypeEnum type, OffsetDateTime reportedAt, OffsetDateTime now) {
        if (reportedAt != null) {
            return reportedAt;
        }
        return type == MaintenanceTypeEnum.CORRECTIVE ? now : null;
    }

    /**
     * Resolves the reporting period, defaulting to the current calendar month and validating order.
     *
     * @param startDate the requested start, or null
     * @param endDate the requested end, or null
     * @return a two-element array of {resolvedStart, resolvedEnd}
     * @throws SmartRoadException if start is after end
     */
    private LocalDate[] resolveRange(LocalDate startDate, LocalDate endDate) throws SmartRoadException {
        LocalDate today = LocalDate.now();
        LocalDate start = startDate != null ? startDate : today.withDayOfMonth(1);
        LocalDate end = endDate != null ? endDate : today.withDayOfMonth(today.lengthOfMonth());
        if (start.isAfter(end)) {
            throw new SmartRoadException(ApplicationLayer.BUSINESS_LAYER,
                    ErrorCodeMapping.SERVICE_INVALID_INPUT, "maintenance.date.range.invalid");
        }
        return new LocalDate[] {start, end};
    }

    /**
     * Converts a date to the start of that day in the server's default time zone.
     *
     * @param date the date
     * @return the start-of-day timestamp
     */
    private OffsetDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
    }

    /**
     * Builds the standard invalid-status-transition error.
     *
     * @return the exception
     */
    private SmartRoadException invalidTransition() {
        return new SmartRoadException(ApplicationLayer.BUSINESS_LAYER,
                ErrorCodeMapping.SERVICE_VALIDATION_FAILED, INVALID_TRANSITION_KEY);
    }

    /**
     * Appends a new note to existing notes on a new line, ignoring blank additions.
     *
     * @param existing the existing notes, or null
     * @param addition the note to append, or null
     * @return the combined notes
     */
    private String appendNote(String existing, String addition) {
        String trimmed = trimToNull(addition);
        if (trimmed == null) {
            return existing;
        }
        return existing == null || existing.isBlank() ? trimmed : existing + "\n" + trimmed;
    }

    /**
     * Trims a string, returning null for null or blank input.
     *
     * @param value the value
     * @return the trimmed value, or null
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Compares two timestamps by instant, treating two nulls as equal.
     *
     * @param a the first timestamp
     * @param b the second timestamp
     * @return true when both are null or represent the same instant
     */
    private boolean sameInstant(OffsetDateTime a, OffsetDateTime b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.isEqual(b);
    }

    /**
     * Compares two amounts numerically (ignoring scale), treating two nulls as equal.
     *
     * @param a the first amount
     * @param b the second amount
     * @return true when both are null or numerically equal
     */
    private boolean sameAmount(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.compareTo(b) == 0;
    }

    /**
     * Returns zero for a null aggregate sum.
     *
     * @param value the possibly-null sum
     * @return the value, or zero when null
     */
    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * Mutable search criteria accumulated from filters. Window bounds start at wide sentinels and
     * are only ever narrowed; requesting two different persisted statuses marks the criteria as
     * contradictory ({@code empty}) so no query is run.
     */
    private static final class SearchCriteria {

        private final UUID organizationId;
        private UUID machineId;
        private UUID projectId;
        private MaintenanceTypeEnum maintenanceType;
        private MaintenancePriorityEnum priority;
        private MaintenanceStatusEnum status;
        private OffsetDateTime startFrom = MIN_BOUND;
        private OffsetDateTime startBefore = MAX_BOUND;
        private OffsetDateTime dueByFrom = MIN_BOUND;
        private OffsetDateTime dueByBefore = MAX_BOUND;
        private boolean empty;

        /**
         * Creates criteria scoped to an organization.
         *
         * @param organizationId the organization UUID
         */
        private SearchCriteria(UUID organizationId) {
            this.organizationId = organizationId;
        }

        /**
         * Requires a persisted status; a conflicting earlier requirement empties the criteria.
         *
         * @param required the persisted status, or null for no requirement
         */
        private void requireStatus(MaintenanceStatusEnum required) {
            if (required == null) {
                return;
            }
            if (status != null && status != required) {
                empty = true;
            }
            status = required;
        }

        /**
         * Raises the inclusive scheduled-start lower bound.
         *
         * @param value the candidate bound
         */
        private void narrowStartFrom(OffsetDateTime value) {
            startFrom = value.isAfter(startFrom) ? value : startFrom;
        }

        /**
         * Lowers the exclusive scheduled-start upper bound.
         *
         * @param value the candidate bound
         */
        private void narrowStartBefore(OffsetDateTime value) {
            startBefore = value.isBefore(startBefore) ? value : startBefore;
        }

        /**
         * Raises the inclusive due-by lower bound.
         *
         * @param value the candidate bound
         */
        private void narrowDueByFrom(OffsetDateTime value) {
            dueByFrom = value.isAfter(dueByFrom) ? value : dueByFrom;
        }

        /**
         * Lowers the exclusive due-by upper bound.
         *
         * @param value the candidate bound
         */
        private void narrowDueByBefore(OffsetDateTime value) {
            dueByBefore = value.isBefore(dueByBefore) ? value : dueByBefore;
        }
    }
}
