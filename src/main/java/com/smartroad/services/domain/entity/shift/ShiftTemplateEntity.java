package com.smartroad.services.domain.entity.shift;

import com.smartroad.services.common.enums.shift.ShiftStatusEnum;
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

import java.time.LocalTime;
import java.util.UUID;

/**
 * JPA entity representing a reusable shift template stored in the sr_shift_templates table.
 * A shift whose end time is earlier than its start time crosses midnight; total_working_minutes
 * is always pre-computed and persisted so every consumer (attendance enrichment, payroll) reads
 * the same figure instead of re-deriving it.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_shift_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ShiftTemplateEntity extends SmartRoadBaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "shift_code", nullable = false, unique = true)
    private String shiftCode;

    @Column(name = "shift_name", nullable = false)
    private String shiftName;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "break_duration_minutes", nullable = false)
    private Integer breakDurationMinutes;

    @Column(name = "total_working_minutes", nullable = false)
    private Integer totalWorkingMinutes;

    @Column(name = "description")
    private String description;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ShiftStatusEnum status;
}
