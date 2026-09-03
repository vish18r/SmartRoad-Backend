package com.smartroad.services.core.dto.grn;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * Request DTO for creating/updating GRN (Goods Received Note).
 *
 * @author Vishal
 * @version 1.0
 */
public record GrnRequestDTO(

    @NotBlank(message = "{grn.number.required}")
    String grnNumber,

    @NotNull(message = "{purchase.order.id.required}")
    UUID purchaseOrderId,

    @NotNull(message = "{project.id.required}")
    UUID projectId,

    @NotNull(message = "{material.id.required}")
    UUID materialId,

    @NotNull(message = "{vendor.id.required}")
    UUID vendorId,

    @Positive(message = "{received.quantity.positive}")
    BigDecimal receivedQuantity,

    BigDecimal rejectedQuantity,

    @NotNull(message = "{receiving.date.required}")
    Date receivingDate,

    String qualityStatus,

    String receivingNotes
) {}
