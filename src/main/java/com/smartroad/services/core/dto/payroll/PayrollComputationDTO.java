package com.smartroad.services.core.dto.payroll;

import java.math.BigDecimal;

/**
 * The full set of figures {@code PayrollCalculationService} derives for one worker and payroll
 * period. Internal to the payroll service layer; never returned directly from a controller.
 *
 * @param presentDays calendar days in the period with at least one closed attendance session
 * @param absentDays calendar days in the period with no attendance session
 * @param paidDays days paid for the period, with half-day granularity
 * @param totalWorkedHours the summed length of every closed session in the period, in hours
 * @param regularHours the portion of total worked hours within the regular hours per day
 * @param overtimeHours the portion of total worked hours beyond the regular hours per day
 * @param regularHoursPerDay the regular working hours per day used to split overtime
 * @param overtimeRate the hourly overtime rate applied
 * @param basicWage the basic wage for the period
 * @param overtimeAmount the overtime pay for the period
 * @param totalAllowances the sum of all allowance amounts
 * @param grossPay basic wage plus overtime pay plus total allowances
 * @param totalDeductions the sum of all deduction amounts
 * @param netPay gross pay minus total deductions
 */
public record PayrollComputationDTO(
    BigDecimal presentDays,
    BigDecimal absentDays,
    BigDecimal paidDays,
    BigDecimal totalWorkedHours,
    BigDecimal regularHours,
    BigDecimal overtimeHours,
    BigDecimal regularHoursPerDay,
    BigDecimal overtimeRate,
    BigDecimal basicWage,
    BigDecimal overtimeAmount,
    BigDecimal totalAllowances,
    BigDecimal grossPay,
    BigDecimal totalDeductions,
    BigDecimal netPay
) {}
