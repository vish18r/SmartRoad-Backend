package com.smartroad.services.domain.entity.worker;

import com.smartroad.services.common.enums.workers.AttendanceStatusEnum;
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
import java.util.UUID;

/**
 * JPA entity representing a worker's daily attendance record stored in the sr_worker_attendance table.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_worker_attendance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class WorkerAttendanceEntity extends SmartRoadBaseEntity {

    @Column(name = "worker_id", nullable = false)
    private UUID workerId;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AttendanceStatusEnum status;

    @Column(name = "hours_worked")
    private BigDecimal hoursWorked;

    @Column(name = "notes")
    private String notes;
}
