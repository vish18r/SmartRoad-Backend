package com.smartroad.services.domain.entity.shift;

import com.smartroad.services.common.enums.shift.ShiftAssignmentStatusEnum;
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

import java.time.LocalDate;
import java.util.UUID;

/**
 * JPA entity representing a worker's assignment to a shift template over a date range, stored in
 * the sr_worker_shift_assignments table. Holds only the worker and shift relationship by id —
 * never duplicates worker or shift fields. A row is never deleted: ending an assignment sets
 * effective_to and moves status to ENDED, preserving the full history forever.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_worker_shift_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class WorkerShiftAssignmentEntity extends SmartRoadBaseEntity {

    @Column(name = "worker_id", nullable = false)
    private UUID workerId;

    @Column(name = "shift_id", nullable = false)
    private UUID shiftId;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ShiftAssignmentStatusEnum status;
}
