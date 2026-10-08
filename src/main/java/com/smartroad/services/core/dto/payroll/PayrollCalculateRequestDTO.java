package com.smartroad.services.core.dto.payroll;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for previewing a payroll calculation. Nothing is persisted; the backend loads
 * the worker's wage configuration and attendance sessions and returns the computed figures for
 * the caller to review before saving.
 *
 * @param workerId the worker to calculate payroll for
 * @param startDate the first day of the payroll period, inclusive
 * @param endDate the last day of the payroll period, inclusive
 * @param regularHoursPerDay the regular working hours per day used to split overtime; defaults
 *                           to 8 when omitted
 * @param overtimeRate the hourly overtime rate to apply; defaults to the worker's configured
 *                     overtime rate when omitted
 * @param allowances the proposed allowance lines to include in the preview
 * @param deductions the proposed deduction lines to include in the preview
 */
public record PayrollCalculateRequestDTO(
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
