package com.smartroad.services.core.dto.purchase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * Request DTO for creating/updating purchase orders.
 *
 * @author Vishal
 * @version 1.0
 */
public record PurchaseOrderRequestDTO(

    @NotNull(message = "{project.id.required}")
    UUID projectId,

    @NotBlank(message = "{po.number.required}")
    String poNumber,

    @NotNull(message = "{vendor.id.required}")
    UUID vendorId,

    @NotNull(message = "{material.id.required}")
    UUID materialId,

    @Positive(message = "{quantity.positive}")
    BigDecimal quantity,

    @NotBlank(message = "{unit.required}")
    String unit,

    @Positive(message = "{rate.positive}")
    BigDecimal rate,

    BigDecimal taxPercentage,

    Date orderDate,

    Date expectedDeliveryDate
) {}
