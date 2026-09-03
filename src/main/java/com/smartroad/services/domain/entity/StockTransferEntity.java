package com.smartroad.services.domain.entity;

import com.smartroad.services.common.enums.stock.StockTransferStatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * JPA entity representing a stock transfer between projects stored in the sr_stock_transfers table.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_stock_transfers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class StockTransferEntity extends SmartRoadBaseEntity {

    @Column(name = "source_project_id", nullable = false)
    private UUID sourceProjectId;

    @Column(name = "destination_project_id", nullable = false)
    private UUID destinationProjectId;

    @Column(name = "material_id", nullable = false)
    private UUID materialId;

    @Column(name = "quantity_requested", nullable = false)
    private BigDecimal quantityRequested;

    @Column(name = "quantity_transferred")
    private BigDecimal quantityTransferred;

    @Column(name = "transfer_date")
    private Date transferDate;

    @Column(name = "requested_by")
    private UUID requestedBy;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private StockTransferStatusEnum status;
}
