package com.nextenti.services.core.dto.purchase;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nextenti.services.common.enums.purchase.PurchaseOrderStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * Response DTO for purchase order data returned to API consumers.
 *
 * @author Vishal
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurchaseOrderResponseDTO {

    private UUID id;

    private UUID projectId;

    private String poNumber;

    private UUID vendorId;

    private UUID materialId;

    private BigDecimal quantity;

    private String unit;

    private BigDecimal rate;

    private BigDecimal taxPercentage;

    private BigDecimal totalAmount;

    private Date orderDate;

    private Date expectedDeliveryDate;

    private PurchaseOrderStatusEnum status;

    private UUID createdBy;

    private UUID modifiedBy;

    private Date createdDate;

    private Date modifiedDate;
}
