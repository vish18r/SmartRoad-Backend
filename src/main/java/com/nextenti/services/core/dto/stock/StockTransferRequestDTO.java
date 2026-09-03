package com.nextenti.services.core.dto.stock;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * Request DTO for creating/updating stock transfers.
 *
 * @author Vishal
 * @version 1.0
 */
public record StockTransferRequestDTO(

    @NotNull(message = "{source.project.id.required}")
    UUID sourceProjectId,

    @NotNull(message = "{destination.project.id.required}")
    UUID destinationProjectId,

    @NotNull(message = "{material.id.required}")
    UUID materialId,

    @Positive(message = "{quantity.positive}")
    BigDecimal quantityRequested,

    Date transferDate
) {}
