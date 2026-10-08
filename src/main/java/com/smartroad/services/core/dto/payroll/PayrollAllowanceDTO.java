package com.smartroad.services.core.dto.payroll;

import com.smartroad.services.common.enums.payroll.AllowanceTypeEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request and response shape for one itemized payroll allowance line (Travel, Food,
 * Accommodation, Site, Performance or Other). The same shape is used inbound, where
 * {@code id} is ignored, and outbound, where it identifies the persisted row.
 *
 * @param id the allowance line UUID, present only in responses
 * @param allowanceType the allowance category
 * @param description a free-text note on the allowance, or null
 * @param amount the allowance amount, never negative
 */
public record PayrollAllowanceDTO(
    UUID id,

    @NotNull(message = "{payroll.allowance.type.required}")
    AllowanceTypeEnum allowanceType,

    String description,

    @NotNull(message = "{payroll.amount.required}")
    @DecimalMin(value = "0", message = "{payroll.amount.invalid}")
    BigDecimal amount
) {}
