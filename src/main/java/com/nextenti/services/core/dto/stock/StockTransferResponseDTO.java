package com.nextenti.services.core.dto.stock;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nextenti.services.common.enums.stock.StockTransferStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * Response DTO for stock transfer data returned to API consumers.
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
public class StockTransferResponseDTO {

    private UUID id;

    private UUID sourceProjectId;

    private UUID destinationProjectId;

    private UUID materialId;

    private BigDecimal quantityRequested;

    private BigDecimal quantityTransferred;

    private Date transferDate;

    private UUID requestedBy;

    private UUID approvedBy;

    private StockTransferStatusEnum status;

    private UUID createdBy;

    private UUID modifiedBy;

    private Date createdDate;

    private Date modifiedDate;
}
