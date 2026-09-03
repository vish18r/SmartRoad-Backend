package com.smartroad.services.core.dto.contracts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * Request DTO for creating/updating contracts.
 *
 * @author Vishal
 * @version 1.0
 */
public record ContractRequestDTO(

    @NotNull(message = "{project.id.required}")
    UUID projectId,

    @NotBlank(message = "{contract.number.required}")
    String contractNumber,

    UUID clientId,

    UUID contractorId,

    String workOrderNumber,

    String agreementNumber,

    @Positive(message = "{contract.value.positive}")
    BigDecimal contractValue,

    Date startDate,

    Date endDate,

    BigDecimal securityDeposit,

    BigDecimal retentionPercentage,

    String documentReference
) {}
