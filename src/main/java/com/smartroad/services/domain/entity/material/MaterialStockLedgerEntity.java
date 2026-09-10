package com.smartroad.services.domain.entity.material;

import com.smartroad.services.common.enums.stock.StockTransactionTypeEnum;
import com.smartroad.services.domain.entity.SmartRoadBaseEntity;
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
import java.util.UUID;

/**
 * JPA entity representing one append-only stock movement stored in the sr_material_stock_ledger table.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_material_stock_ledger")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class MaterialStockLedgerEntity extends SmartRoadBaseEntity {

    @Column(name = "material_id", nullable = false)
    private UUID materialId;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "transaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private StockTransactionTypeEnum transactionType;

    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;

    @Column(name = "reference_number")
    private String referenceNumber;

    @Column(name = "notes")
    private String notes;
}
