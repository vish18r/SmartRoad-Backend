package com.nextenti.services.domain.entity.tracking;

import com.nextenti.services.domain.entity.SmartRoadBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * JPA entity representing worker check-in/check-out records in the sr_worker_checkins table.
 * Tracks daily work attendance with GPS location and timestamps.
 */
@Entity
@Table(name = "sr_worker_checkins")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class WorkerCheckinEntity extends SmartRoadBaseEntity {

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "worker_id", nullable = false)
    private UUID workerId;

    @Column(name = "check_in_time", nullable = false)
    private OffsetDateTime checkInTime;

    @Column(name = "check_in_latitude")
    private BigDecimal checkInLatitude;

    @Column(name = "check_in_longitude")
    private BigDecimal checkInLongitude;

    @Column(name = "check_in_accuracy")
    private BigDecimal checkInAccuracy;

    @Column(name = "check_out_time")
    private OffsetDateTime checkOutTime;

    @Column(name = "check_out_latitude")
    private BigDecimal checkOutLatitude;

    @Column(name = "check_out_longitude")
    private BigDecimal checkOutLongitude;

    @Column(name = "check_out_accuracy")
    private BigDecimal checkOutAccuracy;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;
}
