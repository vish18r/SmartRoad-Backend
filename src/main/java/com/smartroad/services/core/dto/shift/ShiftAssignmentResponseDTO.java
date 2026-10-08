package com.smartroad.services.core.dto.shift;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.smartroad.services.common.enums.shift.ShiftAssignmentStatusEnum;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Response DTO for a worker's shift assignment. Worker and shift identity fields are
 * denormalized at read time for display; the persisted row holds only the two ids.
 *
 * @param id the assignment UUID
 * @param workerId the assigned worker's UUID
 * @param employeeId the worker's employee ID, e.g. SR1
 * @param workerName the worker's display name
 * @param shiftId the assigned shift template's UUID
 * @param shiftCode the shift template's unique code
 * @param shiftName the shift template's display name
 * @param startTime the shift's start time
 * @param endTime the shift's end time
 * @param totalWorkingMinutes the shift's backend-computed net working minutes
 * @param effectiveFrom the first date the assignment takes effect, inclusive
 * @param effectiveTo the last date the assignment is in effect, inclusive, or null while open-ended
 * @param status the assignment's lifecycle status
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ShiftAssignmentResponseDTO(
    UUID id,
    UUID workerId,
    String employeeId,
    String workerName,
    UUID shiftId,
    String shiftCode,
    String shiftName,
    java.time.LocalTime startTime,
    java.time.LocalTime endTime,
    Integer totalWorkingMinutes,
    LocalDate effectiveFrom,
    LocalDate effectiveTo,
    ShiftAssignmentStatusEnum status
) {}
