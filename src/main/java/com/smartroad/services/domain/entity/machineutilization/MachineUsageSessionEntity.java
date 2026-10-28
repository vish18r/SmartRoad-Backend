package com.smartroad.services.domain.entity.machineutilization;

import com.smartroad.services.common.enums.machineutilization.UsageSourceEnum;
import com.smartroad.services.common.enums.machineutilization.UsageStatusEnum;
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
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity representing a single machine usage session stored in the sr_machine_usage_sessions
 * table. A session tracks one machine's recorded running/idle time on one project between a start
 * and (once completed) an end timestamp; it is the sole source of truth for utilization analytics
 * since {@link com.smartroad.services.domain.entity.machine.MachineEntity} holds only static,
 * non-historical fields.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_machine_usage_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class MachineUsageSessionEntity extends SmartRoadBaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "machine_id", nullable = false)
    private UUID machineId;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @Column(name = "running_hours")
    private BigDecimal runningHours;

    @Column(name = "idle_hours")
    private BigDecimal idleHours;

    @Column(name = "operator_worker_id")
    private UUID operatorWorkerId;

    @Column(name = "operator_name")
    private String operatorName;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private UsageStatusEnum status;

    @Column(name = "source", nullable = false)
    @Enumerated(EnumType.STRING)
    private UsageSourceEnum source;

    @Column(name = "fuel_consumed")
    private BigDecimal fuelConsumed;

    @Column(name = "fuel_cost")
    private BigDecimal fuelCost;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    /**
     * Returns whether this session has not yet been completed (no end time recorded).
     *
     * @return true when the session is still open
     */
    public boolean isOpen() {
        return endTime == null;
    }
}
