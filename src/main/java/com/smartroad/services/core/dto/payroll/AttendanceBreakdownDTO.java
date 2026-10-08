package com.smartroad.services.core.dto.payroll;

import java.math.BigDecimal;

/**
 * The attendance-derived figures a payroll was calculated from, read from the worker
 * attendance sessions for the payroll period.
 *
 * @param presentDays calendar days in the period with at least one closed attendance session
 * @param absentDays calendar days in the period with no attendance session
 * @param paidDays days paid for the period; a day worked under half the regular hours counts
 *                 as a half paid day
 * @param totalWorkedHours the summed length of every closed session in the period, in hours
 * @param regularHours the portion of total worked hours within the regular hours per day
 * @param overtimeHours the portion of total worked hours beyond the regular hours per day
 */
public record AttendanceBreakdownDTO(
    BigDecimal presentDays,
    BigDecimal absentDays,
    BigDecimal paidDays,
    BigDecimal totalWorkedHours,
    BigDecimal regularHours,
    BigDecimal overtimeHours
) {}
