package com.smartroad.services.core.dto.shift;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Read-only shift schedule input for one calendar date, exposed for payroll (or any other
 * consumer) to pull scheduled hours / shift allowance inputs for a worker without this module
 * duplicating payroll's own calculation logic. Payroll remains free to use or ignore this figure;
 * nothing here writes to or changes payroll's existing computation.
 *
 * @param date the calendar date
 * @param shiftId the shift template in effect on that date, or null when the worker has no assignment
 * @param scheduledWorkingMinutes the shift's net scheduled working minutes on that date, or null when unassigned
 */
public record ShiftScheduledMinutesDTO(
    LocalDate date,
    UUID shiftId,
    Integer scheduledWorkingMinutes
) {}
