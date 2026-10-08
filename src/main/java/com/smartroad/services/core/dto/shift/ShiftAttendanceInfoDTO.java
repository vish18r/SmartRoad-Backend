package com.smartroad.services.core.dto.shift;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalTime;
import java.util.UUID;

/**
 * Additive, read-only enrichment of an attendance day with the worker's scheduled shift for
 * that date. Computed alongside {@code AttendanceDTO.DailySummary} by resolving the worker's
 * active {@code WorkerShiftAssignmentEntity} for the date; never changes how check-in/check-out
 * sessions themselves are recorded.
 *
 * @param shiftId the resolved shift template's UUID
 * @param shiftCode the resolved shift template's unique code
 * @param shiftName the resolved shift template's display name
 * @param scheduledStartTime the shift's scheduled start time
 * @param scheduledEndTime the shift's scheduled end time
 * @param scheduledWorkingMinutes the shift's backend-computed net working minutes
 * @param lateArrival true when the day's first check-in was after the scheduled start time
 * @param earlyCheckout true when the day's last check-out was before the scheduled end time
 * @param overtimeMinutes minutes worked beyond the shift's scheduled working minutes, never negative
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ShiftAttendanceInfoDTO(
    UUID shiftId,
    String shiftCode,
    String shiftName,
    LocalTime scheduledStartTime,
    LocalTime scheduledEndTime,
    Integer scheduledWorkingMinutes,
    boolean lateArrival,
    boolean earlyCheckout,
    long overtimeMinutes
) {}
