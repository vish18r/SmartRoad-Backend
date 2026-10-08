package com.smartroad.services.core.dto.shift;

import com.smartroad.services.common.enums.shift.ShiftAssignmentStatusEnum;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for creating or updating a worker's shift assignment.
 *
 * @param workerId the worker to assign
 * @param shiftId the shift template to assign the worker to; must be ACTIVE
 * @param effectiveFrom the first date the assignment takes effect, inclusive
 * @param effectiveTo the last date the assignment is in effect, inclusive, or null for open-ended
 * @param status the assignment's lifecycle status; defaults to ACTIVE when omitted
 */
public record ShiftAssignmentRequestDTO(
    @NotNull(message = "{worker.id.required}")
    UUID workerId,

    @NotNull(message = "{shift.id.required}")
    UUID shiftId,

    @NotNull(message = "{shift.assignment.effective.from.required}")
    LocalDate effectiveFrom,

    LocalDate effectiveTo,

    ShiftAssignmentStatusEnum status
) {}
