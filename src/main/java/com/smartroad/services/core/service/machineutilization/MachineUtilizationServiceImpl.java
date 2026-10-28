package com.smartroad.services.core.service.machineutilization;

import com.smartroad.services.common.enums.machineutilization.UsageSourceEnum;
import com.smartroad.services.common.enums.machineutilization.UsageStatusEnum;
import com.smartroad.services.common.exception.ApplicationLayer;
import com.smartroad.services.common.exception.ErrorCodeMapping;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.common.util.NextentiConstants;
import com.smartroad.services.core.dto.machineutilization.MachineUsageSessionCompleteRequestDTO;
import com.smartroad.services.core.dto.machineutilization.MachineUsageSessionPageResponseDTO;
import com.smartroad.services.core.dto.machineutilization.MachineUsageSessionRequestDTO;
import com.smartroad.services.core.dto.machineutilization.MachineUsageSessionResponseDTO;
import com.smartroad.services.core.dto.machineutilization.MachineUtilizationPageResponseDTO;
import com.smartroad.services.core.dto.machineutilization.MachineUtilizationRequestDTO;
import com.smartroad.services.core.dto.machineutilization.MachineUtilizationResponseDTO;
import com.smartroad.services.core.dto.machineutilization.MachineUtilizationSummaryDTO;
import com.smartroad.services.core.dto.machineutilization.ProjectMachineUtilizationDTO;
import com.smartroad.services.core.dto.material.PaginationMetaDTO;
import com.smartroad.services.core.mapper.machineutilization.MachineUsageSessionMapper;
import com.smartroad.services.core.mapper.machineutilization.MachineUtilizationMapper;
import com.smartroad.services.domain.entity.machine.MachineEntity;
import com.smartroad.services.domain.entity.machineutilization.MachineUsageSessionEntity;
import com.smartroad.services.domain.entity.project.ProjectEntity;
import com.smartroad.services.domain.repository.MachineRepository;
import com.smartroad.services.domain.repository.MachineUsageSessionRepository;
import com.smartroad.services.domain.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service layer for machine utilization analytics. Computes all KPIs from real
 * {@link MachineUsageSessionEntity} rows (and the existing {@link MachineEntity}/project data) via
 * database-level aggregate queries, marking any metric whose source data does not exist as
 * unavailable rather than inventing a value, and owns the machine usage session
 * create/complete workflow including overlap prevention.
 *
 * @author Vishal
 * @version 1.0
 */
@Service
public class MachineUtilizationServiceImpl implements MachineUtilizationService {

    private static final String NO_MAINTENANCE_HISTORY_REASON =
            "No maintenance history table exists in this codebase; MachineEntity stores only a current "
            + "maintenanceStatus label and no cost or date-stamped maintenance records, so maintenance "
            + "cost trend/breakdown cannot be computed.";
    private static final String NO_HOURS_RECORDED_REASON =
            "No usage sessions with recorded running/idle hours exist for this scope and period.";

    private final MachineUsageSessionRepository sessionRepository;
    private final MachineRepository machineRepository;
    private final ProjectRepository projectRepository;
    private final MachineUsageSessionMapper sessionMapper;
    private final MachineUtilizationMapper utilizationMapper;

    /**
     * Constructs a MachineUtilizationServiceImpl with required dependencies.
     *
     * @param sessionRepository the machine usage session repository
     * @param machineRepository the machine repository
     * @param projectRepository the project repository, used to resolve project names
     * @param sessionMapper the usage session mapper
     * @param utilizationMapper the per-machine utilization mapper
     */
    public MachineUtilizationServiceImpl(MachineUsageSessionRepository sessionRepository,
            MachineRepository machineRepository, ProjectRepository projectRepository,
            MachineUsageSessionMapper sessionMapper, MachineUtilizationMapper utilizationMapper) {
        this.sessionRepository = sessionRepository;
        this.machineRepository = machineRepository;
        this.projectRepository = projectRepository;
        this.sessionMapper = sessionMapper;
        this.utilizationMapper = utilizationMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public MachineUtilizationSummaryDTO getSummary(UUID organizationId, LocalDate startDate, LocalDate endDate,
                                                    UUID projectId) throws SmartRoadException {
        LocalDate[] range = resolveRange(startDate, endDate);

        long totalMachines = machineRepository.countByOrganizationIdAndIsDeletedFalse(organizationId);
        long machinesUsed = sessionRepository.countDistinctMachinesUsed(organizationId, projectId, range[0], range[1]);

        BigDecimal runningHours = nullToZero(sessionRepository.sumRunningHours(organizationId, projectId, range[0], range[1]));
        BigDecimal idleHours = nullToZero(sessionRepository.sumIdleHours(organizationId, projectId, range[0], range[1]));

        MachineUtilizationSummaryDTO.MachineUtilizationSummaryDTOBuilder builder = MachineUtilizationSummaryDTO.builder()
                .periodStart(range[0])
                .periodEnd(range[1])
                .projectId(projectId)
                .totalMachines(totalMachines)
                .machinesUsedInPeriod(machinesUsed)
                .totalRunningHours(runningHours)
                .totalIdleHours(idleHours);

        applyUtilization(builder, runningHours, idleHours);

        long fuelSessionCount = sessionRepository.countSessionsWithFuelData(organizationId, projectId, range[0], range[1]);
        BigDecimal fuelConsumed = nullToZero(sessionRepository.sumFuelConsumed(organizationId, projectId, range[0], range[1]));
        BigDecimal fuelCost = nullToZero(sessionRepository.sumFuelCost(organizationId, projectId, range[0], range[1]));
        boolean fuelAvailable = fuelSessionCount > 0;
        applyFuel(builder, fuelAvailable, fuelConsumed, fuelCost);

        builder.maintenanceCostAvailable(false)
                .maintenanceCostUnavailableReason(NO_MAINTENANCE_HISTORY_REASON)
                .totalMaintenanceCost(null);

        applyOperatingCostPerHour(builder, fuelAvailable, fuelCost, runningHours);

        return builder.build();
    }

    @Override
    @Transactional(readOnly = true)
    public MachineUtilizationPageResponseDTO list(MachineUtilizationRequestDTO filter) throws SmartRoadException {
        LocalDate[] range = resolveRange(filter.startDate(), filter.endDate());
        int pageIndex = Math.max(filter.page(), 1) - 1;
        int pageSize = Math.max(filter.limit(), 1);

        List<MachineEntity> machines = machineRepository.findByOrganizationIdAndIsDeletedFalse(filter.organizationId());
        List<MachineEntity> filtered = machines.stream()
                .filter(m -> filter.machineId() == null || filter.machineId().equals(m.getId()))
                .filter(m -> filter.machineType() == null || filter.machineType().equalsIgnoreCase(m.getType()))
                .filter(m -> filter.status() == null || filter.status().equalsIgnoreCase(
                        m.getStatus() == null ? "" : m.getStatus().name()))
                .sorted(Comparator.comparing(MachineEntity::getName, Comparator.nullsLast(String::compareTo)))
                .toList();

        int fromIndex = Math.min(pageIndex * pageSize, filtered.size());
        int toIndex = Math.min(fromIndex + pageSize, filtered.size());
        List<MachineEntity> pageContent = filtered.subList(fromIndex, toIndex);

        List<MachineUtilizationResponseDTO> items = new ArrayList<>();
        for (MachineEntity machine : pageContent) {
            items.add(buildMachineUtilization(machine, range[0], range[1], filter.projectId(), false));
        }

        PaginationMetaDTO pagination = new PaginationMetaDTO(
                pageIndex + 1, pageSize, filtered.size(),
                (int) Math.ceil(filtered.size() / (double) pageSize),
                toIndex < filtered.size(), pageIndex > 0);

        return new MachineUtilizationPageResponseDTO(items, pagination);
    }

    @Override
    @Transactional(readOnly = true)
    public MachineUtilizationResponseDTO getMachineDetail(UUID organizationId, UUID machineId, LocalDate startDate,
                                                           LocalDate endDate) throws SmartRoadException {
        LocalDate[] range = resolveRange(startDate, endDate);
        MachineEntity machine = requireMachine(organizationId, machineId);
        return buildMachineUtilization(machine, range[0], range[1], null, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectMachineUtilizationDTO> getProjectUsage(UUID organizationId, LocalDate startDate,
                                                               LocalDate endDate) throws SmartRoadException {
        LocalDate[] range = resolveRange(startDate, endDate);
        List<UUID> projectIds = sessionRepository.findDistinctProjectIds(organizationId, range[0], range[1]);

        List<ProjectMachineUtilizationDTO> rows = new ArrayList<>();
        for (UUID projectId : projectIds) {
            rows.add(buildProjectRow(organizationId, projectId, range[0], range[1]));
        }
        return rows;
    }

    @Override
    @Transactional(readOnly = true)
    public MachineUsageSessionPageResponseDTO getMachineUsageHistory(UUID organizationId, UUID machineId,
            LocalDate startDate, LocalDate endDate, int page, int limit) throws SmartRoadException {
        requireMachine(organizationId, machineId);
        LocalDate[] range = resolveRange(startDate, endDate);
        int pageIndex = Math.max(page, 1) - 1;
        int pageSize = Math.max(limit, 1);

        Page<MachineUsageSessionEntity> results = sessionRepository.search(organizationId, null, machineId,
                range[0], range[1], PageRequest.of(pageIndex, pageSize));

        List<MachineUsageSessionResponseDTO> items = results.getContent().stream()
                .map(this::toSessionResponse)
                .toList();

        PaginationMetaDTO pagination = new PaginationMetaDTO(pageIndex + 1, pageSize, results.getTotalElements(),
                results.getTotalPages(), results.hasNext(), results.hasPrevious());

        return new MachineUsageSessionPageResponseDTO(items, pagination);
    }

    @Override
    @Transactional
    public MachineUsageSessionResponseDTO startSession(UUID userId, UUID organizationId,
            MachineUsageSessionRequestDTO request) throws SmartRoadException {
        requireMachine(organizationId, request.machineId());
        if (request.projectId() != null) {
            requireProject(organizationId, request.projectId());
        }

        assertNoOverlap(request.machineId(), request.startTime(), request.startTime(), null);

        MachineUsageSessionEntity entity = MachineUsageSessionEntity.builder()
                .organizationId(organizationId)
                .machineId(request.machineId())
                .projectId(request.projectId())
                .usageDate(request.startTime().toLocalDate())
                .startTime(request.startTime())
                .operatorWorkerId(request.operatorWorkerId())
                .operatorName(request.operatorName())
                .status(UsageStatusEnum.RUNNING)
                .source(request.source() == null ? UsageSourceEnum.MANUAL : request.source())
                .notes(request.notes())
                .isDeleted(false)
                .build();
        entity.setCreatedBy(userId);
        entity.setModifiedBy(userId);

        return toSessionResponse(sessionRepository.save(entity));
    }

    @Override
    @Transactional
    public MachineUsageSessionResponseDTO completeSession(UUID userId, UUID sessionId,
            MachineUsageSessionCompleteRequestDTO request) throws SmartRoadException {
        MachineUsageSessionEntity entity = sessionRepository.findByIdAndIsDeletedFalse(sessionId)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER,
                        ErrorCodeMapping.DAO_NOT_FOUND, "machineutilization.session.not.found"));

        if (!entity.isOpen()) {
            throw new SmartRoadException(ApplicationLayer.BUSINESS_LAYER,
                    ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "machineutilization.session.already.completed");
        }
        if (!request.endTime().isAfter(entity.getStartTime())) {
            throw new SmartRoadException(ApplicationLayer.BUSINESS_LAYER,
                    ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "machineutilization.end.before.start");
        }

        assertNoOverlap(entity.getMachineId(), entity.getStartTime(), request.endTime(), entity.getId());

        BigDecimal totalHours = toHours(Duration.between(entity.getStartTime(), request.endTime()));
        BigDecimal idleHours = request.idleHours() == null ? BigDecimal.ZERO : request.idleHours();
        BigDecimal runningHours = totalHours.subtract(idleHours).max(BigDecimal.ZERO);

        entity.setEndTime(request.endTime());
        entity.setRunningHours(runningHours);
        entity.setIdleHours(idleHours);
        entity.setStatus(resolveCompletionStatus(request, runningHours));
        if (request.fuelConsumed() != null) {
            entity.setFuelConsumed(request.fuelConsumed());
        }
        if (request.fuelCost() != null) {
            entity.setFuelCost(request.fuelCost());
        }
        if (request.notes() != null) {
            entity.setNotes(request.notes());
        }
        entity.setModifiedBy(userId);

        return toSessionResponse(sessionRepository.save(entity));
    }

    /**
     * Resolves the completion status: the caller-supplied status when given, otherwise RUNNING
     * when running hours were recorded, or IDLE when none were.
     *
     * @param request the completion request
     * @param runningHours the computed running hours
     * @return the resolved {@link UsageStatusEnum}
     */
    private UsageStatusEnum resolveCompletionStatus(MachineUsageSessionCompleteRequestDTO request,
                                                    BigDecimal runningHours) {
        if (request.status() != null) {
            return request.status();
        }
        return runningHours.compareTo(BigDecimal.ZERO) > 0 ? UsageStatusEnum.RUNNING : UsageStatusEnum.IDLE;
    }

    /**
     * Rejects a candidate session window that overlaps any other non-deleted session of the same
     * machine. This is the authoritative overlap guard (it runs identically against PostgreSQL and
     * the H2 test database); the migration additionally adds a best-effort partial unique index so
     * the common "two open sessions at once" case is also rejected at the database level.
     *
     * @param machineId the machine UUID
     * @param candidateStart the candidate window's start time
     * @param candidateEnd the candidate window's end time (equal to start for a not-yet-completed session)
     * @param excludeSessionId the session ID to exclude from the check, or null
     * @throws SmartRoadException if an overlapping session exists
     */
    private void assertNoOverlap(UUID machineId, OffsetDateTime candidateStart, OffsetDateTime candidateEnd,
                                 UUID excludeSessionId) throws SmartRoadException {
        List<MachineUsageSessionEntity> overlapping = sessionRepository.findOverlapping(machineId, candidateStart,
                candidateEnd, excludeSessionId);
        if (!overlapping.isEmpty()) {
            throw new SmartRoadException(ApplicationLayer.BUSINESS_LAYER,
                    ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "machineutilization.overlap");
        }
    }

    /**
     * Builds the full per-machine utilization response, computing hours, utilization, fuel,
     * maintenance, and operating-cost figures from real session data, plus the project-wise split
     * and (when requested) the usage history.
     *
     * @param machine the machine entity
     * @param startDate the inclusive period start
     * @param endDate the inclusive period end
     * @param restrictToProjectId an optional project to restrict the per-machine totals to (used by
     *                            the list endpoint's project filter); null means all projects
     * @param includeHistory whether to populate the usage history and project breakdown (true for
     *                       the single-machine detail endpoint, false for the list endpoint, to
     *                       keep list pages lightweight)
     * @return the assembled {@link MachineUtilizationResponseDTO}
     */
    private MachineUtilizationResponseDTO buildMachineUtilization(MachineEntity machine, LocalDate startDate,
            LocalDate endDate, UUID restrictToProjectId, boolean includeHistory) {
        List<MachineUsageSessionEntity> sessions = sessionRepository.findForMachineInRange(machine.getId(),
                startDate, endDate);
        if (restrictToProjectId != null) {
            sessions = sessions.stream().filter(s -> restrictToProjectId.equals(s.getProjectId())).toList();
        }

        BigDecimal runningHours = sumField(sessions, MachineUsageSessionEntity::getRunningHours);
        BigDecimal idleHours = sumField(sessions, MachineUsageSessionEntity::getIdleHours);
        BigDecimal availableHours = runningHours.add(idleHours);

        boolean utilizationAvailable = availableHours.compareTo(BigDecimal.ZERO) > 0;
        BigDecimal utilizationPercent = utilizationAvailable
                ? runningHours.multiply(BigDecimal.valueOf(100)).divide(availableHours, 2, RoundingMode.HALF_UP)
                : null;

        long fuelSessionCount = sessions.stream()
                .filter(s -> s.getFuelConsumed() != null || s.getFuelCost() != null)
                .count();
        boolean fuelAvailable = fuelSessionCount > 0;
        BigDecimal fuelConsumed = fuelAvailable ? sumField(sessions, MachineUsageSessionEntity::getFuelConsumed) : null;
        BigDecimal fuelCost = fuelAvailable ? sumField(sessions, MachineUsageSessionEntity::getFuelCost) : null;

        boolean operatingCostAvailable = fuelAvailable && runningHours.compareTo(BigDecimal.ZERO) > 0;
        BigDecimal operatingCostPerHour = operatingCostAvailable
                ? fuelCost.divide(runningHours, 2, RoundingMode.HALF_UP)
                : null;

        List<ProjectMachineUtilizationDTO> projectUsage = includeHistory
                ? buildProjectUsageForMachine(sessions)
                : List.of();
        List<MachineUsageSessionResponseDTO> usageHistory = includeHistory
                ? sessions.stream().map(this::toSessionResponse).toList()
                : List.of();

        return utilizationMapper.toResponseDTO(machine, startDate, endDate, runningHours, idleHours, availableHours,
                utilizationAvailable, utilizationAvailable ? null : NO_HOURS_RECORDED_REASON, utilizationPercent,
                fuelAvailable, fuelAvailable ? null : fuelUnavailableReason(), fuelConsumed, fuelCost,
                false, NO_MAINTENANCE_HISTORY_REASON, null,
                operatingCostAvailable, operatingCostAvailable ? null : operatingCostUnavailableReason(fuelAvailable, runningHours),
                operatingCostPerHour, NO_MAINTENANCE_HISTORY_REASON, projectUsage, usageHistory);
    }

    /**
     * Groups one machine's sessions by project and summarizes hours/fuel cost per project.
     *
     * @param sessions the machine's sessions in the reporting period
     * @return list of per-project usage rows
     */
    private List<ProjectMachineUtilizationDTO> buildProjectUsageForMachine(List<MachineUsageSessionEntity> sessions) {
        Map<UUID, List<MachineUsageSessionEntity>> byProject = new HashMap<>();
        for (MachineUsageSessionEntity session : sessions) {
            if (session.getProjectId() != null) {
                byProject.computeIfAbsent(session.getProjectId(), k -> new ArrayList<>()).add(session);
            }
        }

        List<ProjectMachineUtilizationDTO> rows = new ArrayList<>();
        for (Map.Entry<UUID, List<MachineUsageSessionEntity>> entry : byProject.entrySet()) {
            List<MachineUsageSessionEntity> projectSessions = entry.getValue();
            BigDecimal running = sumField(projectSessions, MachineUsageSessionEntity::getRunningHours);
            BigDecimal idle = sumField(projectSessions, MachineUsageSessionEntity::getIdleHours);
            BigDecimal available = running.add(idle);
            boolean fuelAvail = projectSessions.stream().anyMatch(s -> s.getFuelCost() != null);

            rows.add(ProjectMachineUtilizationDTO.builder()
                    .projectId(entry.getKey())
                    .projectName(resolveProjectName(entry.getKey()))
                    .machinesUsed(1)
                    .runningHours(running)
                    .idleHours(idle)
                    .utilizationAvailable(available.compareTo(BigDecimal.ZERO) > 0)
                    .utilizationPercent(available.compareTo(BigDecimal.ZERO) > 0
                            ? running.multiply(BigDecimal.valueOf(100)).divide(available, 2, RoundingMode.HALF_UP)
                            : null)
                    .fuelCostAvailable(fuelAvail)
                    .fuelCost(fuelAvail ? sumField(projectSessions, MachineUsageSessionEntity::getFuelCost) : null)
                    .build());
        }
        return rows;
    }

    /**
     * Builds one organization-wide project usage comparison row via database-level aggregates.
     *
     * @param organizationId the organization UUID
     * @param projectId the project UUID
     * @param startDate the inclusive period start
     * @param endDate the inclusive period end
     * @return the project's usage row
     */
    private ProjectMachineUtilizationDTO buildProjectRow(UUID organizationId, UUID projectId, LocalDate startDate,
                                                         LocalDate endDate) {
        long machinesUsed = sessionRepository.countDistinctMachinesUsed(organizationId, projectId, startDate, endDate);
        BigDecimal running = nullToZero(sessionRepository.sumRunningHours(organizationId, projectId, startDate, endDate));
        BigDecimal idle = nullToZero(sessionRepository.sumIdleHours(organizationId, projectId, startDate, endDate));
        BigDecimal available = running.add(idle);
        long fuelSessionCount = sessionRepository.countSessionsWithFuelData(organizationId, projectId, startDate, endDate);
        boolean fuelAvailable = fuelSessionCount > 0;

        return ProjectMachineUtilizationDTO.builder()
                .projectId(projectId)
                .projectName(resolveProjectName(projectId))
                .machinesUsed(machinesUsed)
                .runningHours(running)
                .idleHours(idle)
                .utilizationAvailable(available.compareTo(BigDecimal.ZERO) > 0)
                .utilizationPercent(available.compareTo(BigDecimal.ZERO) > 0
                        ? running.multiply(BigDecimal.valueOf(100)).divide(available, 2, RoundingMode.HALF_UP)
                        : null)
                .fuelCostAvailable(fuelAvailable)
                .fuelCost(fuelAvailable ? nullToZero(sessionRepository.sumFuelCost(organizationId, projectId, startDate, endDate)) : null)
                .build();
    }

    /**
     * Sets the utilization fields on the summary builder from the already-summed hours.
     *
     * @param builder the summary DTO builder
     * @param runningHours the summed running hours
     * @param idleHours the summed idle hours
     */
    private void applyUtilization(MachineUtilizationSummaryDTO.MachineUtilizationSummaryDTOBuilder builder,
                                  BigDecimal runningHours, BigDecimal idleHours) {
        BigDecimal available = runningHours.add(idleHours);
        boolean available0 = available.compareTo(BigDecimal.ZERO) > 0;
        builder.utilizationAvailable(available0);
        builder.utilizationUnavailableReason(available0 ? null : NO_HOURS_RECORDED_REASON);
        builder.averageUtilizationPercent(available0
                ? runningHours.multiply(BigDecimal.valueOf(100)).divide(available, 2, RoundingMode.HALF_UP)
                : null);
    }

    /**
     * Sets the fuel fields on the summary builder.
     *
     * @param builder the summary DTO builder
     * @param fuelAvailable whether any session in scope recorded fuel data
     * @param fuelConsumed the summed fuel consumed
     * @param fuelCost the summed fuel cost
     */
    private void applyFuel(MachineUtilizationSummaryDTO.MachineUtilizationSummaryDTOBuilder builder,
                           boolean fuelAvailable, BigDecimal fuelConsumed, BigDecimal fuelCost) {
        builder.fuelAvailable(fuelAvailable);
        builder.fuelUnavailableReason(fuelAvailable ? null : fuelUnavailableReason());
        builder.totalFuelConsumed(fuelAvailable ? fuelConsumed : null);
        builder.totalFuelCost(fuelAvailable ? fuelCost : null);
    }

    /**
     * Sets the operating-cost-per-hour fields on the summary builder.
     *
     * @param builder the summary DTO builder
     * @param fuelAvailable whether fuel cost is available
     * @param fuelCost the summed fuel cost (ignored unless fuelAvailable)
     * @param runningHours the summed running hours
     */
    private void applyOperatingCostPerHour(MachineUtilizationSummaryDTO.MachineUtilizationSummaryDTOBuilder builder,
                                           boolean fuelAvailable, BigDecimal fuelCost, BigDecimal runningHours) {
        boolean available = fuelAvailable && runningHours.compareTo(BigDecimal.ZERO) > 0;
        builder.operatingCostPerHourAvailable(available);
        builder.operatingCostPerHourUnavailableReason(available ? null : operatingCostUnavailableReason(fuelAvailable, runningHours));
        builder.averageOperatingCostPerHour(available
                ? fuelCost.divide(runningHours, 2, RoundingMode.HALF_UP)
                : null);
    }

    /**
     * Builds the reason string for unavailable operating cost per hour.
     *
     * @param fuelAvailable whether fuel cost is available
     * @param runningHours the running hours in scope
     * @return the explanatory reason
     */
    private String operatingCostUnavailableReason(boolean fuelAvailable, BigDecimal runningHours) {
        if (runningHours.compareTo(BigDecimal.ZERO) <= 0) {
            return "No running hours recorded in this period, so cost per hour cannot be divided.";
        }
        if (!fuelAvailable) {
            return "Fuel cost is unavailable (" + fuelUnavailableReason()
                    + ") and maintenance cost is unavailable (" + NO_MAINTENANCE_HISTORY_REASON + ").";
        }
        return NO_HOURS_RECORDED_REASON;
    }

    /**
     * Returns the standard explanation for unavailable fuel analytics.
     *
     * @return the reason string
     */
    private String fuelUnavailableReason() {
        return "No usage sessions in this scope and period have recorded fuel consumption/cost; there is no "
                + "separate fuel issuance/transaction table in this codebase, so fuel must be entered per usage "
                + "session (via the session start/complete APIs) to populate this metric.";
    }

    /**
     * Resolves a session's response DTO, looking up the machine and project display names.
     *
     * @param entity the session entity
     * @return the mapped response DTO
     */
    private MachineUsageSessionResponseDTO toSessionResponse(MachineUsageSessionEntity entity) {
        String machineName = machineRepository.findByIdAndIsDeletedFalse(entity.getMachineId())
                .map(MachineEntity::getName).orElse(null);
        String projectName = entity.getProjectId() == null ? null : resolveProjectName(entity.getProjectId());
        return sessionMapper.toResponseDTO(entity, machineName, projectName);
    }

    /**
     * Resolves a project's display name by ID.
     *
     * @param projectId the project UUID
     * @return the project name, or null if not found
     */
    private String resolveProjectName(UUID projectId) {
        return projectRepository.findById(projectId).map(ProjectEntity::getName).orElse(null);
    }

    /**
     * Loads a non-deleted machine belonging to the organization.
     *
     * @param organizationId the organization UUID
     * @param machineId the machine UUID
     * @return the {@link MachineEntity}
     * @throws SmartRoadException if the machine is not found
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
     * Verifies a project exists (and, when the machine's organization is known, is loadable);
     * project-to-organization scoping mirrors {@link com.smartroad.services.core.service.machine.MachineService}.
     *
     * @param organizationId the organization UUID (unused for scoping beyond existence today, since
     *                       {@link ProjectEntity} does not require organization-matching here)
     * @param projectId the project UUID
     * @throws SmartRoadException if the project is not found
     */
    private void requireProject(UUID organizationId, UUID projectId) throws SmartRoadException {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER,
                        ErrorCodeMapping.DAO_NOT_FOUND, "project.not.found"));
    }

    /**
     * Resolves the effective reporting period, defaulting to the current day when neither bound is
     * supplied, and validating that start is not after end.
     *
     * @param startDate the requested start date, or null
     * @param endDate the requested end date, or null
     * @return a two-element array of {resolvedStart, resolvedEnd}
     * @throws SmartRoadException if start is after end
     */
    private LocalDate[] resolveRange(LocalDate startDate, LocalDate endDate) throws SmartRoadException {
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        LocalDate start = startDate != null ? startDate : end;
        if (start.isAfter(end)) {
            throw new SmartRoadException(ApplicationLayer.BUSINESS_LAYER,
                    ErrorCodeMapping.SERVICE_INVALID_INPUT, "machineutilization.end.before.start");
        }
        return new LocalDate[] {start, end};
    }

    /**
     * Converts a duration to decimal hours, rounded to 2 places.
     *
     * @param duration the duration
     * @return the duration in hours
     */
    private BigDecimal toHours(Duration duration) {
        return BigDecimal.valueOf(duration.toSeconds())
                .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
    }

    /**
     * Returns zero for a null aggregate sum (SQL SUM over no/all-null rows returns null).
     *
     * @param value the possibly-null sum
     * @return the value, or zero when null
     */
    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * Sums a BigDecimal field across a list of sessions, treating null field values as zero.
     *
     * @param sessions the sessions to sum over
     * @param extractor the field accessor
     * @return the summed value, never null
     */
    private BigDecimal sumField(List<MachineUsageSessionEntity> sessions,
                                java.util.function.Function<MachineUsageSessionEntity, BigDecimal> extractor) {
        return sessions.stream()
                .map(extractor)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
