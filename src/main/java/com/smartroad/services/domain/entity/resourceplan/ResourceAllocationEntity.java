package com.smartroad.services.domain.entity.resourceplan;

import com.smartroad.services.common.enums.resourceplan.AllocationStatusEnum;
import com.smartroad.services.common.enums.resourceplan.ResourceTypeEnum;
import com.smartroad.services.domain.entity.SmartRoadBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * JPA entity representing a single resource allocation stored in the sr_resource_allocations table.
 * The {@code resourceId} column is polymorphic — it holds a worker, machine, material, or vehicle
 * id depending on {@code resourceType}; no database foreign key constrains it, existence is
 * validated in the service layer.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_resource_allocations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ResourceAllocationEntity extends SmartRoadBaseEntity {

    @Column(name = "resource_plan_id", nullable = false)
    private UUID resourcePlanId;

    @Column(name = "resource_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ResourceTypeEnum resourceType;

    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "allocation_start_date", nullable = false)
    private LocalDate allocationStartDate;

    @Column(name = "allocation_end_date")
    private LocalDate allocationEndDate;

    @Column(name = "planned_quantity")
    private BigDecimal plannedQuantity;

    @Column(name = "unit")
    private String unit;

    @Column(name = "assigned_worker_id")
    private UUID assignedWorkerId;

    @Column(name = "task_description", columnDefinition = "TEXT")
    private String taskDescription;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AllocationStatusEnum status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
