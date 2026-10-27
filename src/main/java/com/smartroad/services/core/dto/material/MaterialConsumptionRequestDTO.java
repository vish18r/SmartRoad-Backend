package com.smartroad.services.core.dto.material;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for recording direct material consumption from a project's stock.
 *
 * @author Vishal
 * @version 1.0
 */
public record MaterialConsumptionRequestDTO(

    @NotNull(message = "{project.id.required}")
    UUID projectId,

    @Positive(message = "{quantity.positive}")
    BigDecimal quantity,

    String referenceNumber,

    String notes
) {}
