package com.smartroad.services.core.dto.material;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for recording a direct material receipt onto a project's stock.
 *
 * @author Vishal
 * @version 1.0
 */
public record MaterialReceiptRequestDTO(

    @NotNull(message = "{project.id.required}")
    UUID projectId,

    @Positive(message = "{quantity.positive}")
    BigDecimal quantity,

    BigDecimal unitRate,

    String referenceNumber,

    String notes
) {}
