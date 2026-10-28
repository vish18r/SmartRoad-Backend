package com.smartroad.services.domain.entity.maintenance;

import com.smartroad.services.common.enums.maintenance.MaintenanceCategoryEnum;
import com.smartroad.services.common.enums.maintenance.MaintenancePriorityEnum;
import com.smartroad.services.common.enums.maintenance.MaintenanceRecurrenceEnum;
import com.smartroad.services.common.enums.maintenance.MaintenanceStatusEnum;
import com.smartroad.services.common.enums.maintenance.MaintenanceTypeEnum;
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
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity representing one preventive or corrective maintenance record for a machine, stored in
 * the sr_maintenance_records table. Machine details are never duplicated here; the record only
 * references {@link com.smartroad.services.domain.entity.machine.MachineEntity} by ID. The persisted
 * {@code status} is one of SCHEDULED, IN_PROGRESS, COMPLETED or CANCELLED; DUE and OVERDUE are
 * derived on read from {@code scheduledStart}/{@code dueBy} and the current time.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_maintenance_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class MaintenanceRecordEntity extends SmartRoadBaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "maintenance_reference", nullable = false, unique = true)
    private String maintenanceReference;

    @Column(name = "machine_id", nullable = false)
    private UUID machineId;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "maintenance_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MaintenanceTypeEnum maintenanceType;

    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private MaintenanceCategoryEnum category;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "priority", nullable = false)
    @Enumerated(EnumType.STRING)
    private MaintenancePriorityEnum priority;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private MaintenanceStatusEnum status;

    @Column(name = "scheduled_start", nullable = false)
    private OffsetDateTime scheduledStart;

    @Column(name = "scheduled_end")
    private OffsetDateTime scheduledEnd;

    @Column(name = "due_by", nullable = false)
    private OffsetDateTime dueBy;

    @Column(name = "actual_start")
    private OffsetDateTime actualStart;

    @Column(name = "actual_end")
    private OffsetDateTime actualEnd;

    @Column(name = "reported_at")
    private OffsetDateTime reportedAt;

    @Column(name = "technician_worker_id")
    private UUID technicianWorkerId;

    @Column(name = "technician_name")
    private String technicianName;

    @Column(name = "estimated_cost")
    private BigDecimal estimatedCost;

    @Column(name = "actual_cost")
    private BigDecimal actualCost;

    @Column(name = "downtime_minutes")
    private Long downtimeMinutes;

    @Column(name = "parts_used", columnDefinition = "TEXT")
    private String partsUsed;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Builder.Default
    @Column(name = "recurrence_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MaintenanceRecurrenceEnum recurrenceType = MaintenanceRecurrenceEnum.ONE_TIME;

    @Column(name = "parent_maintenance_id")
    private UUID parentMaintenanceId;

    @Column(name = "machine_label_before_start")
    private String machineLabelBeforeStart;

    /**
     * Returns whether the record has reached a terminal state (COMPLETED or CANCELLED) and can no
     * longer move through the workflow.
     *
     * @return true when the persisted status is COMPLETED or CANCELLED
     */
    public boolean isTerminal() {
        return status == MaintenanceStatusEnum.COMPLETED || status == MaintenanceStatusEnum.CANCELLED;
    }
}
