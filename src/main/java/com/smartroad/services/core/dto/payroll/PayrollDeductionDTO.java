package com.smartroad.services.core.dto.payroll;

import com.smartroad.services.common.enums.payroll.DeductionTypeEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request and response shape for one itemized payroll deduction line (Salary Advance, Loan,
 * Late, Absence or Other). The same shape is used inbound, where {@code id} is ignored, and
 * outbound, where it identifies the persisted row.
 *
 * @param id the deduction line UUID, present only in responses
 * @param deductionType the deduction category
 * @param description a free-text note on the deduction, or null
 * @param amount the deduction amount, never negative
 */
public record PayrollDeductionDTO(
    UUID id,

    @NotNull(message = "{payroll.deduction.type.required}")
    DeductionTypeEnum deductionType,

    String description,

    @NotNull(message = "{payroll.amount.required}")
    @DecimalMin(value = "0", message = "{payroll.amount.invalid}")
    BigDecimal amount
) {}
