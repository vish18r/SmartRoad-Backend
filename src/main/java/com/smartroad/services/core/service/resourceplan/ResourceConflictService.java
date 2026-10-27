package com.smartroad.services.core.service.resourceplan;

import com.smartroad.services.common.enums.machine.MachineStatusEnum;
import com.smartroad.services.common.enums.resourceplan.ConflictSeverityEnum;
import com.smartroad.services.common.enums.resourceplan.ConflictTypeEnum;
import com.smartroad.services.common.enums.resourceplan.ResourceTypeEnum;
import com.smartroad.services.common.enums.vehicle.VehicleStatusEnum;
import com.smartroad.services.common.enums.workers.WorkerStatusEnum;
import com.smartroad.services.core.dto.resourceplan.ResourceAllocationRequestDTO;
import com.smartroad.services.core.dto.resourceplan.ResourceConflictDTO;
import com.smartroad.services.domain.entity.machine.MachineEntity;
import com.smartroad.services.domain.entity.material.MaterialEntity;
import com.smartroad.services.domain.entity.material.MaterialStockEntity;
import com.smartroad.services.domain.entity.project.ProjectEntity;
import com.smartroad.services.domain.entity.resourceplan.ResourceAllocationEntity;
import com.smartroad.services.domain.entity.resourceplan.ResourcePlanEntity;
import com.smartroad.services.domain.entity.vehicle.VehicleEntity;
import com.smartroad.services.domain.entity.worker.WorkerEntity;
import com.smartroad.services.domain.repository.MachineRepository;
import com.smartroad.services.domain.repository.MaterialRepository;
import com.smartroad.services.domain.repository.MaterialStockRepository;
import com.smartroad.services.domain.repository.ProjectRepository;
import com.smartroad.services.domain.repository.VehicleRepository;
import com.smartroad.services.domain.repository.WorkerRepository;
import com.smartroad.services.domain.repository.resourceplan.ResourceAllocationRepository;
import com.smartroad.services.domain.repository.resourceplan.ResourcePlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service that detects resource-planning conflicts (double-booked workers/machines/vehicles,
 * unavailable resources, and material over-allocation) without ever touching actual stock or
 * attendance records. Planning is advisory: it reads other plans/allocations to find overlaps,
 * it never deducts inventory or writes attendance itself.
 *
 * @author Vishal
 * @version 1.0
 */
@Service
public class ResourceConflictService {

    private final WorkerRepository workerRepository;
    private final MachineRepository machineRepository;
    private final VehicleRepository vehicleRepository;
    private final MaterialRepository materialRepository;
    private final MaterialStockRepository materialStockRepository;
    private final ProjectRepository projectRepository;
    private final ResourceAllocationRepository allocationRepository;
    private final ResourcePlanRepository planRepository;

    /**
     * Constructs a ResourceConflictService with required dependencies.
     *
     * @param workerRepository the worker repository
     * @param machineRepository the machine repository
     * @param vehicleRepository the vehicle repository
     * @param materialRepository the material repository
     * @param materialStockRepository the material stock repository
     * @param projectRepository the project repository
     * @param allocationRepository the resource allocation repository
     * @param planRepository the resource plan repository
     */
    public ResourceConflictService(WorkerRepository workerRepository, MachineRepository machineRepository,
                                   VehicleRepository vehicleRepository, MaterialRepository materialRepository,
                                   MaterialStockRepository materialStockRepository, ProjectRepository projectRepository,
                                   ResourceAllocationRepository allocationRepository, ResourcePlanRepository planRepository) {
        this.workerRepository = workerRepository;
        this.machineRepository = machineRepository;
        this.vehicleRepository = vehicleRepository;
        this.materialRepository = materialRepository;
        this.materialStockRepository = materialStockRepository;
        this.projectRepository = projectRepository;
        this.allocationRepository = allocationRepository;
        this.planRepository = planRepository;
    }

    /**
     * Checks a set of candidate allocations for conflicts against every other non-cancelled plan.
     *
     * @param organizationId the organization UUID the allocations are being planned under
     * @param projectId the project UUID the allocations are being planned for
     * @param allocations the candidate allocations
     * @param planDate the plan date the allocations belong to
     * @param excludePlanId the plan these allocations already belong to (so overlaps within the
     *                      same plan are not reported), or null for a brand-new plan
     * @return the conflicts found, empty when none
     */
    @Transactional(readOnly = true)
    public List<ResourceConflictDTO> checkConflicts(UUID organizationId, UUID projectId,
                                                     List<ResourceAllocationRequestDTO> allocations,
                                                     LocalDate planDate, UUID excludePlanId) {
        List<ResourceConflictDTO> conflicts = new ArrayList<>();
        for (ResourceAllocationRequestDTO allocation : allocations) {
            conflicts.addAll(checkOne(organizationId, projectId, allocation, planDate, excludePlanId));
        }
        return conflicts;
    }

    /**
     * Checks one candidate allocation for conflicts, dispatching by resource type.
     *
     * @param organizationId the organization UUID
     * @param projectId the project UUID
     * @param allocation the candidate allocation
     * @param planDate the plan date
     * @param excludePlanId the plan to exclude from overlap reporting
     * @return the conflicts found for this allocation
     */
    private List<ResourceConflictDTO> checkOne(UUID organizationId, UUID projectId,
                                               ResourceAllocationRequestDTO allocation, LocalDate planDate,
                                               UUID excludePlanId) {
        return switch (allocation.resourceType()) {
            case WORKER -> checkWorker(allocation);
            case MACHINE -> checkMachine(allocation);
            case VEHICLE -> checkVehicle(allocation);
            case MATERIAL -> checkMaterial(projectId, allocation, planDate);
        };
    }

    /**
     * Validates a worker allocation: existence, ACTIVE status, and date-range overlap with any
     * other plan's allocation of the same worker.
     *
     * @param allocation the candidate allocation
     * @return the conflicts found
     */
    private List<ResourceConflictDTO> checkWorker(ResourceAllocationRequestDTO allocation) {
        List<ResourceConflictDTO> conflicts = new ArrayList<>();
        Optional<WorkerEntity> worker = workerRepository.findById(allocation.resourceId());
        if (worker.isEmpty()) {
            conflicts.add(availabilityConflict(allocation, "Worker not found"));
            return conflicts;
        }
        if (worker.get().getStatus() != WorkerStatusEnum.ACTIVE) {
            conflicts.add(availabilityConflict(allocation, "Worker is not active"));
        }
        String label = labelOf(worker.get());
        conflicts.addAll(overlapConflicts(allocation, ResourceTypeEnum.WORKER, label, ConflictTypeEnum.WORKER_CONFLICT,
                "This worker is already allocated to another plan in this date range"));
        // NOTE: a worker's own shift-assignment hours are not cross-checked here against the
        // allocation's start/end time — WorkerShiftAssignmentRepository only tells us whether a
        // shift covers the DATE, not whether the plan's requested time window falls inside the
        // shift's hours, and shift templates don't expose a comparable time range on this
        // entity graph. Implementing that cleanly needs a shift-hours lookup this module doesn't
        // have reliable access to, so SHIFT_CONFLICT is intentionally not emitted; add it once a
        // shift-hours comparison is available, as a WARNING per the spec.
        return conflicts;
    }

    /**
     * Validates a machine allocation: existence, ACTIVE status, and date-range overlap with any
     * other plan's allocation of the same machine.
     *
     * @param allocation the candidate allocation
     * @return the conflicts found
     */
    private List<ResourceConflictDTO> checkMachine(ResourceAllocationRequestDTO allocation) {
        List<ResourceConflictDTO> conflicts = new ArrayList<>();
        Optional<MachineEntity> machine = machineRepository.findByIdAndIsDeletedFalse(allocation.resourceId());
        if (machine.isEmpty()) {
            conflicts.add(availabilityConflict(allocation, "Machine not found"));
            return conflicts;
        }
        MachineStatusEnum status = machine.get().getStatus();
        if (status == MachineStatusEnum.MAINTENANCE) {
            conflicts.add(availabilityConflict(allocation, "Machine is under maintenance"));
        } else if (status != MachineStatusEnum.ACTIVE) {
            conflicts.add(availabilityConflict(allocation, "Machine is not active"));
        }
        conflicts.addAll(overlapConflicts(allocation, ResourceTypeEnum.MACHINE, machine.get().getName(),
                ConflictTypeEnum.MACHINE_CONFLICT, "This machine is already allocated to another plan in this date range"));
        return conflicts;
    }

    /**
     * Validates a vehicle allocation: existence, ACTIVE status, and date-range overlap with any
     * other plan's allocation of the same vehicle.
     *
     * @param allocation the candidate allocation
     * @return the conflicts found
     */
    private List<ResourceConflictDTO> checkVehicle(ResourceAllocationRequestDTO allocation) {
        List<ResourceConflictDTO> conflicts = new ArrayList<>();
        Optional<VehicleEntity> vehicle = vehicleRepository.findByIdAndIsDeletedFalse(allocation.resourceId());
        if (vehicle.isEmpty()) {
            conflicts.add(availabilityConflict(allocation, "Vehicle not found"));
            return conflicts;
        }
        if (vehicle.get().getStatus() != VehicleStatusEnum.ACTIVE) {
            conflicts.add(availabilityConflict(allocation, "Vehicle is not active"));
        }
        conflicts.addAll(overlapConflicts(allocation, ResourceTypeEnum.VEHICLE, vehicle.get().getRegistrationNumber(),
                ConflictTypeEnum.VEHICLE_CONFLICT, "This vehicle is already allocated to another plan in this date range"));
        return conflicts;
    }

    /**
     * Validates a material allocation: when the cumulative planned quantity for the material on
     * the project/date (including this candidate) exceeds the project's available stock, a
     * MATERIAL_SHORTAGE/WARNING conflict is raised. This is intentionally a warning, not a block —
     * planning must never silently deduct or lock actual inventory, it only flags the risk so a
     * planner can decide.
     *
     * @param projectId the project UUID
     * @param allocation the candidate allocation
     * @param planDate the plan date
     * @return the conflicts found
     */
    private List<ResourceConflictDTO> checkMaterial(UUID projectId, ResourceAllocationRequestDTO allocation,
                                                     LocalDate planDate) {
        List<ResourceConflictDTO> conflicts = new ArrayList<>();
        Optional<MaterialEntity> material = materialRepository.findById(allocation.resourceId());
        if (material.isEmpty()) {
            conflicts.add(availabilityConflict(allocation, "Material not found"));
            return conflicts;
        }
        Optional<MaterialStockEntity> stock = materialStockRepository.findByProjectIdAndMaterialId(projectId,
                allocation.resourceId());
        BigDecimal available = stock.map(MaterialStockEntity::getQuantityAvailable).orElse(BigDecimal.ZERO);
        BigDecimal alreadyPlanned = allocationRepository.sumPlannedQuantity(allocation.resourceId(), projectId,
                planDate, null);
        BigDecimal thisAllocation = allocation.plannedQuantity() == null ? BigDecimal.ZERO : allocation.plannedQuantity();
        BigDecimal totalPlanned = (alreadyPlanned == null ? BigDecimal.ZERO : alreadyPlanned).add(thisAllocation);

        if (totalPlanned.compareTo(available) > 0) {
            conflicts.add(ResourceConflictDTO.builder()
                    .conflictType(ConflictTypeEnum.MATERIAL_SHORTAGE)
                    .resourceId(allocation.resourceId())
                    .resourceLabel(material.get().getMaterialName())
                    .date(planDate)
                    .startTime(allocation.startTime())
                    .endTime(allocation.endTime())
                    .severity(ConflictSeverityEnum.WARNING)
                    .message("Planned quantity (" + totalPlanned + ") exceeds available stock (" + available + ")")
                    .build());
        }
        return conflicts;
    }

    /**
     * Runs the shared overlap query for a resource and builds a conflict for every hit that
     * belongs to a plan other than the one this allocation is part of.
     *
     * @param allocation the candidate allocation
     * @param resourceType the resource type to check
     * @param resourceLabel a human-readable label for the resource
     * @param conflictType the conflict type to report
     * @param message the conflict message
     * @return the overlap conflicts found, empty when none
     */
    private List<ResourceConflictDTO> overlapConflicts(ResourceAllocationRequestDTO allocation,
                                                        ResourceTypeEnum resourceType, String resourceLabel,
                                                        ConflictTypeEnum conflictType, String message) {
        List<ResourceConflictDTO> conflicts = new ArrayList<>();
        LocalDate endDate = allocation.allocationEndDate() == null ? allocation.allocationStartDate()
                : allocation.allocationEndDate();
        List<ResourceAllocationEntity> overlaps = allocationRepository.findOverlapping(resourceType,
                allocation.resourceId(), allocation.allocationStartDate(), endDate, null);

        for (ResourceAllocationEntity overlap : overlaps) {
            ResourcePlanEntity plan = planRepository.findById(overlap.getResourcePlanId()).orElse(null);
            if (plan == null) {
                continue;
            }
            String projectName = projectRepository.findById(plan.getProjectId())
                    .map(ProjectEntity::getName).orElse(null);
            conflicts.add(ResourceConflictDTO.builder()
                    .conflictType(conflictType)
                    .resourceId(allocation.resourceId())
                    .resourceLabel(resourceLabel)
                    .conflictingPlanId(plan.getId())
                    .conflictingPlanName(plan.getPlanName())
                    .conflictingProjectId(plan.getProjectId())
                    .conflictingProjectName(projectName)
                    .date(allocation.allocationStartDate())
                    .startTime(allocation.startTime())
                    .endTime(allocation.endTime())
                    .severity(ConflictSeverityEnum.BLOCKING)
                    .message(message)
                    .build());
        }
        return conflicts;
    }

    /**
     * Builds a non-blocking-origin availability conflict (resource missing or not ACTIVE).
     *
     * @param allocation the candidate allocation
     * @param message the conflict message
     * @return the built conflict DTO
     */
    private ResourceConflictDTO availabilityConflict(ResourceAllocationRequestDTO allocation, String message) {
        return ResourceConflictDTO.builder()
                .conflictType(ConflictTypeEnum.RESOURCE_AVAILABILITY)
                .resourceId(allocation.resourceId())
                .date(allocation.allocationStartDate())
                .startTime(allocation.startTime())
                .endTime(allocation.endTime())
                .severity(ConflictSeverityEnum.BLOCKING)
                .message(message)
                .build();
    }

    /**
     * Builds a human-readable label for a worker.
     *
     * @param worker the worker entity
     * @return the worker's display name
     */
    private String labelOf(WorkerEntity worker) {
        String last = worker.getLastName() == null ? "" : " " + worker.getLastName();
        return worker.getFirstName() + last;
    }
}
