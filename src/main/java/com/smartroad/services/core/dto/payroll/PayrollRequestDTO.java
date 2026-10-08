package com.smartroad.services.core.dto.payroll;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating or updating a payroll record. The backend always recalculates the
 * attendance-derived and monetary figures from the worker's wage configuration and attendance
 * sessions; only the period, worker, hourly settings, allowances and deductions are taken from
 * the caller.
 *
 * @param workerId the worker this payroll is for
 * @param startDate the first day of the payroll period, inclusive
 * @param endDate the last day of the payroll period, inclusive
 * @param regularHoursPerDay the regular working hours per day used to split overtime; defaults
 *                           to 8 when omitted
 * @param overtimeRate the hourly overtime rate to apply; defaults to the worker's configured
 *                     overtime rate when omitted
 * @param allowances the allowance lines to record against this payroll
 * @param deductions the deduction lines to record against this payroll
 */
public record PayrollRequestDTO(
    @NotNull(message = "{worker.id.required}")
    UUID workerId,

    @NotNull(message = "{payroll.start.date.required}")
    LocalDate startDate,

    @NotNull(message = "{payroll.end.date.required}")
    LocalDate endDate,

    @DecimalMin(value = "0.01", message = "{payroll.regular.hours.invalid}")
    BigDecimal regularHoursPerDay,

    @DecimalMin(value = "0", message = "{payroll.amount.invalid}")
    BigDecimal overtimeRate,

    @Valid
    List<PayrollAllowanceDTO> allowances,

    @Valid
    List<PayrollDeductionDTO> deductions
) {}
