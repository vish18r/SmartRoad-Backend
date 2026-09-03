package com.smartroad.services.core.dto.grn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.smartroad.services.common.enums.grn.GrnStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * Response DTO for GRN (Goods Received Note) data returned to API consumers.
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
public class GrnResponseDTO {

    private UUID id;

    private String grnNumber;

    private UUID purchaseOrderId;

    private UUID projectId;

    private UUID materialId;

    private UUID vendorId;

    private BigDecimal orderedQuantity;

    private BigDecimal receivedQuantity;

    private BigDecimal rejectedQuantity;

    private Date receivingDate;

    private String qualityStatus;

    private String receivingNotes;

    private GrnStatusEnum status;

    private UUID createdBy;

    private UUID modifiedBy;

    private Date createdDate;

    private Date modifiedDate;
}
