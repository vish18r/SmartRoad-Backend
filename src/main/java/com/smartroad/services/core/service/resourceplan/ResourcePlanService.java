package com.smartroad.services.core.service.resourceplan;

import com.smartroad.services.common.enums.machine.MachineStatusEnum;
import com.smartroad.services.common.enums.resourceplan.AllocationStatusEnum;
import com.smartroad.services.common.enums.resourceplan.ConflictSeverityEnum;
import com.smartroad.services.common.enums.resourceplan.ResourcePlanStatusEnum;
import com.smartroad.services.common.enums.resourceplan.ResourceTypeEnum;
import com.smartroad.services.common.enums.vehicle.VehicleStatusEnum;
import com.smartroad.services.common.enums.workers.WorkerStatusEnum;
import com.smartroad.services.common.exception.ApplicationLayer;
import com.smartroad.services.common.exception.ErrorCodeMapping;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.material.PaginationMetaDTO;
import com.smartroad.services.core.dto.resourceplan.ResourceAllocationRequestDTO;
import com.smartroad.services.core.dto.resourceplan.ResourceAllocationResponseDTO;
import com.smartroad.services.core.dto.resourceplan.ResourceAvailabilityEntryDTO;
import com.smartroad.services.core.dto.resourceplan.ResourceAvailabilityResponseDTO;
import com.smartroad.services.core.dto.resourceplan.ResourceConflictDTO;
import com.smartroad.services.core.dto.resourceplan.ResourcePlanDetailResponseDTO;
import com.smartroad.services.core.dto.resourceplan.ResourcePlanPageResponseDTO;
import com.smartroad.services.core.dto.resourceplan.ResourcePlanRequestDTO;
import com.smartroad.services.core.dto.resourceplan.ResourcePlanResponseDTO;
import com.smartroad.services.core.dto.resourceplan.ResourcePlanSummaryDTO;
import com.smartroad.services.core.dto.resourceplan.ResourcePlanValidateRequestDTO;
import com.smartroad.services.core.dto.resourceplan.ResourcePlanValidationResponseDTO;
import com.smartroad.services.core.service.organization.OrganizationService;
import com.smartroad.services.domain.entity.machine.MachineEntity;
import com.smartroad.services.domain.entity.material.MaterialEntity;
import com.smartroad.services.domain.entity.project.ProjectEntity;
import com.smartroad.services.domain.entity.resourceplan.ResourceAllocationEntity;
import com.smartroad.services.domain.entity.resourceplan.ResourcePlanEntity;
import com.smartroad.services.domain.entity.vehicle.VehicleEntity;
import com.smartroad.services.domain.entity.worker.WorkerEntity;
import com.smartroad.services.domain.repository.MachineRepository;
import com.smartroad.services.domain.repository.MaterialRepository;
import com.smartroad.services.domain.repository.ProjectRepository;
import com.smartroad.services.domain.repository.VehicleRepository;
import com.smartroad.services.domain.repository.WorkerRepository;
import com.smartroad.services.domain.repository.resourceplan.ResourceAllocationRepository;
import com.smartroad.services.domain.repository.resourceplan.ResourcePlanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Service layer for resource-plan business logic: plan CRUD, status-transition rules, allocation
 * management, conflict validation, availability lookups, and summary figures. All authorization is
 * organization-scoped via {@link OrganizationService#requireMember}, resolving the organization from
 * the project when only a plan id is known.
 *
 * @author Vishal
 * @version 1.0
 */
@Service
public class ResourcePlanService {

    private static final Map<ResourcePlanStatusEnum, Set<ResourcePlanStatusEnum>> ALLOWED_TRANSITIONS = Map.of(
            ResourcePlanStatusEnum.DRAFT, EnumSet.of(ResourcePlanStatusEnum.PLANNED, ResourcePlanStatusEnum.CANCELLED),
            ResourcePlanStatusEnum.PLANNED, EnumSet.of(ResourcePlanStatusEnum.CONFIRMED, ResourcePlanStatusEnum.CANCELLED,
                    ResourcePlanStatusEnum.DRAFT),
            ResourcePlanStatusEnum.CONFIRMED, EnumSet.of(ResourcePlanStatusEnum.IN_PROGRESS, ResourcePlanStatusEnum.CANCELLED),
            ResourcePlanStatusEnum.IN_PROGRESS, EnumSet.of(ResourcePlanStatusEnum.COMPLETED, ResourcePlanStatusEnum.CANCELLED),
            ResourcePlanStatusEnum.COMPLETED, EnumSet.noneOf(ResourcePlanStatusEnum.class),
            ResourcePlanStatusEnum.CANCELLED, EnumSet.noneOf(ResourcePlanStatusEnum.class)
    );

    private static final Set<ResourcePlanStatusEnum> EDITABLE_STATUSES =
            EnumSet.of(ResourcePlanStatusEnum.DRAFT, ResourcePlanStatusEnum.PLANNED);

    private final ResourcePlanRepository planRepository;
    private final ResourceAllocationRepository allocationRepository;
    private final ProjectRepository projectRepository;
    private final WorkerRepository workerRepository;
    private final MachineRepository machineRepository;
    private final VehicleRepository vehicleRepository;
    private final MaterialRepository materialRepository;
    private final OrganizationService organizationService;
    private final ResourceConflictService conflictService;

    /**
     * Constructs a ResourcePlanService with required dependencies.
     *
     * @param planRepository the resource plan repository
     * @param allocationRepository the resource allocation repository
     * @param projectRepository the project repository
     * @param workerRepository the worker repository
     * @param machineRepository the machine repository
     * @param vehicleRepository the vehicle repository
     * @param materialRepository the material repository
     * @param organizationService the organization service, used to authorize the caller
     * @param conflictService the conflict detection service
     */
    public ResourcePlanService(ResourcePlanRepository planRepository, ResourceAllocationRepository allocationRepository,
                               ProjectRepository projectRepository, WorkerRepository workerRepository,
                               MachineRepository machineRepository, VehicleRepository vehicleRepository,
                               MaterialRepository materialRepository, OrganizationService organizationService,
                               ResourceConflictService conflictService) {
        this.planRepository = planRepository;
        this.allocationRepository = allocationRepository;
        this.projectRepository = projectRepository;
        this.workerRepository = workerRepository;
        this.machineRepository = machineRepository;
        this.vehicleRepository = vehicleRepository;
        this.materialRepository = materialRepository;
        this.organizationService = organizationService;
        this.conflictService = conflictService;
    }

    /**
     * Creates a new resource plan in DRAFT status.
     *
     * @param userId the UUID of the user creating the plan
     * @param request the plan creation request
     * @return the created {@link ResourcePlanResponseDTO}
     * @throws SmartRoadException if the project is not found or the caller is not an organization member
     */
    @Transactional
    public ResourcePlanResponseDTO createPlan(UUID userId, ResourcePlanRequestDTO request) throws SmartRoadException {
        organizationService.requireMember(userId, request.organizationId());
        requireProject(request.projectId());

        ResourcePlanEntity entity = ResourcePlanEntity.builder()
                .organizationId(request.organizationId())
                .projectId(request.projectId())
                .planName(request.planName())
                .planDate(request.planDate())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .supervisorWorkerId(request.supervisorWorkerId())
                .description(request.description())
                .status(ResourcePlanStatusEnum.DRAFT)
                .notes(request.notes())
                .build();
        entity.setCreatedBy(userId);
        entity.setModifiedBy(userId);

        return toResponse(planRepository.save(entity), false);
    }

    /**
     * Retrieves a plan with its resolved allocations.
     *
     * @param userId the UUID of the caller
     * @param planId the plan UUID
     * @return the {@link ResourcePlanDetailResponseDTO}
     * @throws SmartRoadException if the plan is not found or the caller lacks access
     */
    @Transactional(readOnly = true)
    public ResourcePlanDetailResponseDTO getPlan(UUID userId, UUID planId) throws SmartRoadException {
        ResourcePlanEntity plan = requirePlan(planId);
        organizationService.requireMember(userId, plan.getOrganizationId());

        List<ResourceAllocationEntity> allocations = allocationRepository.findByResourcePlanId(planId);
        boolean hasConflicts = hasBlockingConflicts(plan, allocations);

        return new ResourcePlanDetailResponseDTO(toResponse(plan, hasConflicts),
                allocations.stream().map(this::toAllocationResponse).toList());
    }

    /**
     * Lists a page of an organization's plans matching the optional filters.
     *
     * @param userId the UUID of the caller
     * @param organizationId the organization UUID
     * @param projectId the optional project filter
     * @param dateFrom the optional inclusive start of the plan-date window
     * @param dateTo the optional inclusive end of the plan-date window
     * @param status the optional status filter
     * @param page the one-based page number
     * @param limit the page size
     * @return the {@link ResourcePlanPageResponseDTO} for the requested page
     * @throws SmartRoadException if the caller is not an organization member
     */
    @Transactional(readOnly = true)
    public ResourcePlanPageResponseDTO listPlans(UUID userId, UUID organizationId, UUID projectId, LocalDate dateFrom,
                                                 LocalDate dateTo, ResourcePlanStatusEnum status, int page, int limit)
            throws SmartRoadException {
        organizationService.requireMember(userId, organizationId);

        int pageIndex = Math.max(page, 1) - 1;
        int pageSize = Math.max(limit, 1);
        Page<ResourcePlanEntity> results = planRepository.search(organizationId, projectId, dateFrom, dateTo, status,
                PageRequest.of(pageIndex, pageSize));

        // Known tradeoff: hasConflicts is computed per row by re-running the overlap/availability
        // check against each plan's current allocations. For a typical page size (<=50) this is an
        // acceptable number of extra queries; it is not attempted for very large unpaged exports.
        List<ResourcePlanResponseDTO> items = results.getContent().stream()
                .map(plan -> toResponse(plan, hasBlockingConflicts(plan, allocationRepository.findByResourcePlanId(plan.getId()))))
                .toList();

        PaginationMetaDTO pagination = new PaginationMetaDTO(pageIndex + 1, pageSize, results.getTotalElements(),
                results.getTotalPages(), results.hasNext(), results.hasPrevious());

        return new ResourcePlanPageResponseDTO(items, pagination);
    }

    /**
     * Updates a plan's details. Only allowed while the plan is DRAFT or PLANNED.
     *
     * @param userId the UUID of the caller
     * @param planId the plan UUID
     * @param request the plan update request
     * @return the updated {@link ResourcePlanResponseDTO}
     * @throws SmartRoadException if the plan is not found, not editable, or the caller lacks access
     */
    @Transactional
    public ResourcePlanResponseDTO updatePlan(UUID userId, UUID planId, ResourcePlanRequestDTO request)
            throws SmartRoadException {
        ResourcePlanEntity plan = requirePlan(planId);
        organizationService.requireMember(userId, plan.getOrganizationId());
        requireEditable(plan);
        requireProject(request.projectId());

        plan.setProjectId(request.projectId());
        plan.setPlanName(request.planName());
        plan.setPlanDate(request.planDate());
        plan.setStartDate(request.startDate());
        plan.setEndDate(request.endDate());
        plan.setSupervisorWorkerId(request.supervisorWorkerId());
        plan.setDescription(request.description());
        plan.setNotes(request.notes());
        plan.setModifiedBy(userId);

        List<ResourceAllocationEntity> allocations = allocationRepository.findByResourcePlanId(planId);
        return toResponse(planRepository.save(plan), hasBlockingConflicts(plan, allocations));
    }

    /**
     * Cancels a plan from any non-terminal status. Cancellation is always allowed and never
     * hard-deletes the plan; it only transitions its status so history is preserved.
     *
     * @param userId the UUID of the caller
     * @param planId the plan UUID
     * @return the cancelled {@link ResourcePlanResponseDTO}
     * @throws SmartRoadException if the plan is not found or the caller lacks access
     */
    @Transactional
    public ResourcePlanResponseDTO cancelPlan(UUID userId, UUID planId) throws SmartRoadException {
        ResourcePlanEntity plan = requirePlan(planId);
        organizationService.requireMember(userId, plan.getOrganizationId());

        plan.setStatus(ResourcePlanStatusEnum.CANCELLED);
        plan.setModifiedBy(userId);
        return toResponse(planRepository.save(plan), false);
    }

    /**
     * Validates a plan's current allocations for blocking conflicts. Used by the controller to
     * decide whether to transition the plan to CONFIRMED or return a 409 with the conflict list.
     *
     * @param userId the UUID of the caller
     * @param planId the plan UUID
     * @return the {@link ResourcePlanValidationResponseDTO} describing the plan's current conflicts
     * @throws SmartRoadException if the plan is not found or the caller lacks access
     */
    @Transactional(readOnly = true)
    public ResourcePlanValidationResponseDTO validatePlan(UUID userId, UUID planId) throws SmartRoadException {
        ResourcePlanEntity plan = requirePlan(planId);
        organizationService.requireMember(userId, plan.getOrganizationId());

        List<ResourceConflictDTO> conflicts = conflictsForPlan(plan);
        boolean blocking = conflicts.stream().anyMatch(c -> c.getSeverity() == ConflictSeverityEnum.BLOCKING);
        return new ResourcePlanValidationResponseDTO(conflicts, blocking);
    }

    /**
     * Confirms a plan, transitioning it from DRAFT/PLANNED to CONFIRMED. The caller must have
     * already called {@link #validatePlan} and confirmed no blocking conflicts remain; this method
     * only performs the status transition.
     *
     * @param userId the UUID of the caller
     * @param planId the plan UUID
     * @return the confirmed {@link ResourcePlanResponseDTO}
     * @throws SmartRoadException if the plan is not found, the caller lacks access, or the
     *                            transition is not allowed from the plan's current status
     */
    @Transactional
    public ResourcePlanResponseDTO confirmPlan(UUID userId, UUID planId) throws SmartRoadException {
        ResourcePlanEntity plan = requirePlan(planId);
        organizationService.requireMember(userId, plan.getOrganizationId());
        requireTransition(plan.getStatus(), ResourcePlanStatusEnum.CONFIRMED);

        plan.setStatus(ResourcePlanStatusEnum.CONFIRMED);
        plan.setModifiedBy(userId);
        return toResponse(planRepository.save(plan), false);
    }

    /**
     * Computes resource-planning summary figures for a project (or an organization-wide view when
     * no project is given) on a date.
     *
     * @param userId the UUID of the caller
     * @param organizationId the organization UUID
     * @param projectId the optional project filter
     * @param date the date to summarize
     * @return the {@link ResourcePlanSummaryDTO}
     * @throws SmartRoadException if the caller is not an organization member
     */
    @Transactional(readOnly = true)
    public ResourcePlanSummaryDTO getSummary(UUID userId, UUID organizationId, UUID projectId, LocalDate date)
            throws SmartRoadException {
        organizationService.requireMember(userId, organizationId);

        long totalPlans = planRepository.countByOrganizationIdAndProjectIdAndPlanDate(organizationId, projectId, date);
        long workers = allocationRepository.countByOrganizationAndProjectAndDateAndType(organizationId, projectId, date,
                ResourceTypeEnum.WORKER);
        long machines = allocationRepository.countByOrganizationAndProjectAndDateAndType(organizationId, projectId, date,
                ResourceTypeEnum.MACHINE);
        long vehicles = allocationRepository.countByOrganizationAndProjectAndDateAndType(organizationId, projectId, date,
                ResourceTypeEnum.VEHICLE);
        long materials = allocationRepository.countByOrganizationAndProjectAndDateAndType(organizationId, projectId, date,
                ResourceTypeEnum.MATERIAL);

        Page<ResourcePlanEntity> plansOnDate = planRepository.search(organizationId, projectId, date, date, null,
                PageRequest.of(0, Math.max((int) totalPlans, 1)));
        long plansWithConflicts = plansOnDate.getContent().stream()
                .filter(plan -> hasBlockingConflicts(plan, allocationRepository.findByResourcePlanId(plan.getId())))
                .count();

        return new ResourcePlanSummaryDTO(totalPlans, workers, machines, vehicles, materials, plansWithConflicts);
    }

    /**
     * Adds a new allocation to a plan. Only allowed while the plan is DRAFT or PLANNED.
     *
     * @param userId the UUID of the caller
     * @param planId the plan UUID
     * @param request the allocation request
     * @return the created {@link ResourceAllocationResponseDTO}
     * @throws SmartRoadException if the plan is not found, not editable, or the caller lacks access
     */
    @Transactional
    public ResourceAllocationResponseDTO addAllocation(UUID userId, UUID planId, ResourceAllocationRequestDTO request)
            throws SmartRoadException {
        ResourcePlanEntity plan = requirePlan(planId);
        organizationService.requireMember(userId, plan.getOrganizationId());
        requireEditable(plan);

        ResourceAllocationEntity entity = ResourceAllocationEntity.builder()
                .resourcePlanId(planId)
                .resourceType(request.resourceType())
                .resourceId(request.resourceId())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .allocationStartDate(request.allocationStartDate())
                .allocationEndDate(request.allocationEndDate())
                .plannedQuantity(request.plannedQuantity())
                .unit(request.unit())
                .assignedWorkerId(request.assignedWorkerId())
                .taskDescription(request.taskDescription())
                .status(AllocationStatusEnum.PLANNED)
                .notes(request.notes())
                .build();
        entity.setCreatedBy(userId);
        entity.setModifiedBy(userId);

        return toAllocationResponse(allocationRepository.save(entity));
    }

    /**
     * Updates an existing allocation. Only allowed while the plan is DRAFT or PLANNED.
     *
     * @param userId the UUID of the caller
     * @param planId the plan UUID
     * @param allocationId the allocation UUID
     * @param request the allocation update request
     * @return the updated {@link ResourceAllocationResponseDTO}
     * @throws SmartRoadException if the plan or allocation is not found, the plan is not editable,
     *                            or the caller lacks access
     */
    @Transactional
    public ResourceAllocationResponseDTO updateAllocation(UUID userId, UUID planId, UUID allocationId,
                                                           ResourceAllocationRequestDTO request) throws SmartRoadException {
        ResourcePlanEntity plan = requirePlan(planId);
        organizationService.requireMember(userId, plan.getOrganizationId());
        requireEditable(plan);
        ResourceAllocationEntity entity = requireAllocation(planId, allocationId);

        entity.setResourceType(request.resourceType());
        entity.setResourceId(request.resourceId());
        entity.setStartTime(request.startTime());
        entity.setEndTime(request.endTime());
        entity.setAllocationStartDate(request.allocationStartDate());
        entity.setAllocationEndDate(request.allocationEndDate());
        entity.setPlannedQuantity(request.plannedQuantity());
        entity.setUnit(request.unit());
        entity.setAssignedWorkerId(request.assignedWorkerId());
        entity.setTaskDescription(request.taskDescription());
        entity.setNotes(request.notes());
        entity.setModifiedBy(userId);

        return toAllocationResponse(allocationRepository.save(entity));
    }

    /**
     * Removes an allocation from a plan by marking it CANCELLED; the row is kept for history.
     * Only allowed while the plan is DRAFT or PLANNED.
     *
     * @param userId the UUID of the caller
     * @param planId the plan UUID
     * @param allocationId the allocation UUID
     * @throws SmartRoadException if the plan or allocation is not found, the plan is not editable,
     *                            or the caller lacks access
     */
    @Transactional
    public void removeAllocation(UUID userId, UUID planId, UUID allocationId) throws SmartRoadException {
        ResourcePlanEntity plan = requirePlan(planId);
        organizationService.requireMember(userId, plan.getOrganizationId());
        requireEditable(plan);
        ResourceAllocationEntity entity = requireAllocation(planId, allocationId);

        entity.setStatus(AllocationStatusEnum.CANCELLED);
        entity.setModifiedBy(userId);
        allocationRepository.save(entity);
    }

    /**
     * Validates a candidate set of allocations without persisting anything.
     *
     * @param userId the UUID of the caller
     * @param request the validation request
     * @return the {@link ResourcePlanValidationResponseDTO} describing the conflicts found
     * @throws SmartRoadException if the project is not found or the caller lacks organization access
     */
    @Transactional(readOnly = true)
    public ResourcePlanValidationResponseDTO validatePlanRequest(UUID userId, ResourcePlanValidateRequestDTO request)
            throws SmartRoadException {
        ProjectEntity project = requireProject(request.projectId());
        organizationService.requireMember(userId, project.getOrganizationId());

        List<ResourceConflictDTO> conflicts = conflictService.checkConflicts(project.getOrganizationId(),
                request.projectId(), request.allocations(), request.planDate(), request.excludePlanId());
        boolean blocking = conflicts.stream().anyMatch(c -> c.getSeverity() == ConflictSeverityEnum.BLOCKING);
        return new ResourcePlanValidationResponseDTO(conflicts, blocking);
    }

    /**
     * Lists the resources of a project that are ACTIVE and not conflicting for a date/time window.
     *
     * @param userId the UUID of the caller
     * @param projectId the project UUID
     * @param date the date to check availability for
     * @param startTime the optional window start time
     * @param endTime the optional window end time
     * @param resourceType the optional resource type filter; when set, only that list is populated
     * @return the {@link ResourceAvailabilityResponseDTO}
     * @throws SmartRoadException if the project is not found or the caller lacks organization access
     */
    @Transactional(readOnly = true)
    public ResourceAvailabilityResponseDTO getAvailability(UUID userId, UUID projectId, LocalDate date,
                                                           LocalTime startTime, LocalTime endTime,
                                                           ResourceTypeEnum resourceType) throws SmartRoadException {
        ProjectEntity project = requireProject(projectId);
        organizationService.requireMember(userId, project.getOrganizationId());

        List<ResourceAvailabilityEntryDTO> workers = resourceType == null || resourceType == ResourceTypeEnum.WORKER
                ? workerAvailability(project.getOrganizationId(), date) : List.of();
        List<ResourceAvailabilityEntryDTO> machines = resourceType == null || resourceType == ResourceTypeEnum.MACHINE
                ? machineAvailability(project.getOrganizationId(), date) : List.of();
        List<ResourceAvailabilityEntryDTO> vehicles = resourceType == null || resourceType == ResourceTypeEnum.VEHICLE
                ? vehicleAvailability(project.getOrganizationId(), date) : List.of();
        List<ResourceAvailabilityEntryDTO> materials = resourceType == null || resourceType == ResourceTypeEnum.MATERIAL
                ? materialAvailability(project.getOrganizationId()) : List.of();

        return new ResourceAvailabilityResponseDTO(workers, machines, materials, vehicles);
    }

    /**
     * Builds the availability list for an organization's ACTIVE workers on a date.
     *
     * @param organizationId the organization UUID
     * @param date the date to check
     * @return the workers' availability entries
     */
    private List<ResourceAvailabilityEntryDTO> workerAvailability(UUID organizationId, LocalDate date) {
        List<ResourceAvailabilityEntryDTO> entries = new ArrayList<>();
        for (WorkerEntity worker : workerRepository.findByOrganizationIdAndNotDeleted(organizationId)) {
            if (worker.getStatus() != WorkerStatusEnum.ACTIVE) {
                continue;
            }
            boolean busy = !allocationRepository.findOverlapping(ResourceTypeEnum.WORKER, worker.getId(), date, date, null)
                    .isEmpty();
            String label = worker.getFirstName() + (worker.getLastName() == null ? "" : " " + worker.getLastName());
            entries.add(new ResourceAvailabilityEntryDTO(worker.getId(), label, !busy,
                    busy ? "Already allocated on this date" : null));
        }
        return entries;
    }

    /**
     * Builds the availability list for an organization's ACTIVE machines on a date.
     *
     * @param organizationId the organization UUID
     * @param date the date to check
     * @return the machines' availability entries
     */
    private List<ResourceAvailabilityEntryDTO> machineAvailability(UUID organizationId, LocalDate date) {
        List<ResourceAvailabilityEntryDTO> entries = new ArrayList<>();
        for (MachineEntity machine : machineRepository.findByOrganizationIdAndStatusAndIsDeletedFalse(organizationId,
                MachineStatusEnum.ACTIVE)) {
            boolean busy = !allocationRepository.findOverlapping(ResourceTypeEnum.MACHINE, machine.getId(), date, date, null)
                    .isEmpty();
            entries.add(new ResourceAvailabilityEntryDTO(machine.getId(), machine.getName(), !busy,
                    busy ? "Already allocated on this date" : null));
        }
        return entries;
    }

    /**
     * Builds the availability list for an organization's ACTIVE vehicles on a date.
     *
     * @param organizationId the organization UUID
     * @param date the date to check
     * @return the vehicles' availability entries
     */
    private List<ResourceAvailabilityEntryDTO> vehicleAvailability(UUID organizationId, LocalDate date) {
        List<ResourceAvailabilityEntryDTO> entries = new ArrayList<>();
        for (VehicleEntity vehicle : vehicleRepository.findByOrganizationIdAndStatusAndIsDeletedFalse(organizationId,
                VehicleStatusEnum.ACTIVE)) {
            boolean busy = !allocationRepository.findOverlapping(ResourceTypeEnum.VEHICLE, vehicle.getId(), date, date, null)
                    .isEmpty();
            entries.add(new ResourceAvailabilityEntryDTO(vehicle.getId(), vehicle.getRegistrationNumber(), !busy,
                    busy ? "Already allocated on this date" : null));
        }
        return entries;
    }

    /**
     * Builds the availability list for an organization's materials. Materials are always reported
     * available: material planning is a quantity/shortage check (see {@link ResourceConflictService}),
     * not a date-exclusive booking, so there is no single-resource busy/free state to report here.
     *
     * @param organizationId the organization UUID
     * @return the materials' availability entries
     */
    private List<ResourceAvailabilityEntryDTO> materialAvailability(UUID organizationId) {
        return materialRepository.findByOrganizationId(organizationId).stream()
                .map(m -> new ResourceAvailabilityEntryDTO(m.getId(), m.getMaterialName(), true, null))
                .toList();
    }

    /**
     * Computes whether a plan's current allocations carry any blocking conflict.
     *
     * @param plan the plan entity
     * @param allocations the plan's current non-cancelled allocations
     * @return true if at least one blocking conflict exists
     */
    private boolean hasBlockingConflicts(ResourcePlanEntity plan, List<ResourceAllocationEntity> allocations) {
        return conflictsForPlan(plan, allocations).stream()
                .anyMatch(c -> c.getSeverity() == ConflictSeverityEnum.BLOCKING);
    }

    /**
     * Builds the conflict list for a plan's current allocations, excluding the plan itself from
     * overlap reporting.
     *
     * @param plan the plan entity
     * @return the conflicts found
     */
    private List<ResourceConflictDTO> conflictsForPlan(ResourcePlanEntity plan) {
        return conflictsForPlan(plan, allocationRepository.findByResourcePlanId(plan.getId()));
    }

    /**
     * Builds the conflict list for a given set of a plan's allocations, excluding the plan itself
     * from overlap reporting.
     *
     * @param plan the plan entity
     * @param allocations the allocations to validate
     * @return the conflicts found
     */
    private List<ResourceConflictDTO> conflictsForPlan(ResourcePlanEntity plan, List<ResourceAllocationEntity> allocations) {
        List<ResourceAllocationRequestDTO> requests = allocations.stream().map(a -> new ResourceAllocationRequestDTO(
                a.getResourceType(), a.getResourceId(), a.getStartTime(), a.getEndTime(), a.getAllocationStartDate(),
                a.getAllocationEndDate(), a.getPlannedQuantity(), a.getUnit(), a.getAssignedWorkerId(),
                a.getTaskDescription(), a.getNotes())).toList();
        return conflictService.checkConflicts(plan.getOrganizationId(), plan.getProjectId(), requests, plan.getPlanDate(),
                plan.getId());
    }

    /**
     * Validates that a status transition is allowed from a plan's current status.
     *
     * @param from the plan's current status
     * @param to the requested target status
     * @throws SmartRoadException if the transition is not allowed
     */
    private void requireTransition(ResourcePlanStatusEnum from, ResourcePlanStatusEnum to) throws SmartRoadException {
        if (!ALLOWED_TRANSITIONS.getOrDefault(from, EnumSet.noneOf(ResourcePlanStatusEnum.class)).contains(to)) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED,
                    "resourceplan.invalid.status.transition");
        }
    }

    /**
     * Validates that a plan is in an editable status (DRAFT or PLANNED).
     *
     * @param plan the plan entity
     * @throws SmartRoadException if the plan is not editable
     */
    private void requireEditable(ResourcePlanEntity plan) throws SmartRoadException {
        if (!EDITABLE_STATUSES.contains(plan.getStatus())) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED,
                    "resourceplan.not.editable");
        }
    }

    /**
     * Loads a plan by ID.
     *
     * @param planId the plan UUID
     * @return the {@link ResourcePlanEntity}
     * @throws SmartRoadException if the plan is not found
     */
    private ResourcePlanEntity requirePlan(UUID planId) throws SmartRoadException {
        return planRepository.findById(planId)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER,
                        ErrorCodeMapping.DAO_NOT_FOUND, "resourceplan.not.found"));
    }

    /**
     * Loads a project by ID.
     *
     * @param projectId the project UUID
     * @return the {@link ProjectEntity}
     * @throws SmartRoadException if the project is not found
     */
    private ProjectEntity requireProject(UUID projectId) throws SmartRoadException {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER,
                        ErrorCodeMapping.DAO_NOT_FOUND, "project.not.found"));
    }

    /**
     * Loads an allocation belonging to a plan.
     *
     * @param planId the plan UUID the allocation must belong to
     * @param allocationId the allocation UUID
     * @return the {@link ResourceAllocationEntity}
     * @throws SmartRoadException if no such allocation exists on that plan
     */
    private ResourceAllocationEntity requireAllocation(UUID planId, UUID allocationId) throws SmartRoadException {
        ResourceAllocationEntity entity = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER,
                        ErrorCodeMapping.DAO_NOT_FOUND, "resourceplan.allocation.not.found"));
        if (!entity.getResourcePlanId().equals(planId)) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.DAO_NOT_FOUND,
                    "resourceplan.allocation.not.found");
        }
        return entity;
    }

    /**
     * Maps a plan entity to its response DTO, resolving the project name, supervisor name, and
     * resource-count figures.
     *
     * @param entity the plan entity
     * @param hasConflicts whether the plan's current allocations carry a blocking conflict
     * @return the plan response DTO
     */
    private ResourcePlanResponseDTO toResponse(ResourcePlanEntity entity, boolean hasConflicts) {
        String projectName = projectRepository.findById(entity.getProjectId()).map(ProjectEntity::getName).orElse(null);
        String supervisorName = entity.getSupervisorWorkerId() == null ? null
                : workerRepository.findById(entity.getSupervisorWorkerId())
                        .map(w -> w.getFirstName() + (w.getLastName() == null ? "" : " " + w.getLastName()))
                        .orElse(null);

        return ResourcePlanResponseDTO.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganizationId())
                .projectId(entity.getProjectId())
                .projectName(projectName)
                .planName(entity.getPlanName())
                .planDate(entity.getPlanDate())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .supervisorWorkerId(entity.getSupervisorWorkerId())
                .supervisorName(supervisorName)
                .description(entity.getDescription())
                .status(entity.getStatus())
                .notes(entity.getNotes())
                .hasConflicts(hasConflicts)
                .workersCount((int) allocationRepository.countByResourcePlanIdAndResourceType(entity.getId(), ResourceTypeEnum.WORKER))
                .machinesCount((int) allocationRepository.countByResourcePlanIdAndResourceType(entity.getId(), ResourceTypeEnum.MACHINE))
                .materialsCount((int) allocationRepository.countByResourcePlanIdAndResourceType(entity.getId(), ResourceTypeEnum.MATERIAL))
                .vehiclesCount((int) allocationRepository.countByResourcePlanIdAndResourceType(entity.getId(), ResourceTypeEnum.VEHICLE))
                .createdBy(entity.getCreatedBy())
                .modifiedBy(entity.getModifiedBy())
                .createdDate(entity.getDateCreated())
                .modifiedDate(entity.getDateModified())
                .build();
    }

    /**
     * Maps an allocation entity to its response DTO, resolving a human-readable resource label.
     *
     * @param entity the allocation entity
     * @return the allocation response DTO
     */
    private ResourceAllocationResponseDTO toAllocationResponse(ResourceAllocationEntity entity) {
        return ResourceAllocationResponseDTO.builder()
                .id(entity.getId())
                .resourcePlanId(entity.getResourcePlanId())
                .resourceType(entity.getResourceType())
                .resourceId(entity.getResourceId())
                .resourceLabel(resolveResourceLabel(entity.getResourceType(), entity.getResourceId()))
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .allocationStartDate(entity.getAllocationStartDate())
                .allocationEndDate(entity.getAllocationEndDate())
                .plannedQuantity(entity.getPlannedQuantity())
                .unit(entity.getUnit())
                .assignedWorkerId(entity.getAssignedWorkerId())
                .taskDescription(entity.getTaskDescription())
                .status(entity.getStatus())
                .notes(entity.getNotes())
                .build();
    }

    /**
     * Resolves a human-readable label for a polymorphic resource reference.
     *
     * @param resourceType the resource type
     * @param resourceId the resource UUID
     * @return the resolved label, or null when the resource can no longer be found
     */
    private String resolveResourceLabel(ResourceTypeEnum resourceType, UUID resourceId) {
        return switch (resourceType) {
            case WORKER -> workerRepository.findById(resourceId)
                    .map(w -> w.getFirstName() + (w.getLastName() == null ? "" : " " + w.getLastName())).orElse(null);
            case MACHINE -> machineRepository.findById(resourceId).map(MachineEntity::getName).orElse(null);
            case VEHICLE -> vehicleRepository.findById(resourceId).map(VehicleEntity::getRegistrationNumber).orElse(null);
            case MATERIAL -> materialRepository.findById(resourceId).map(MaterialEntity::getMaterialName).orElse(null);
        };
    }
}
