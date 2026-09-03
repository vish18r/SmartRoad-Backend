package com.smartroad.services.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_boq_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BoqItemEntity extends SmartRoadBaseEntity {

    @Column(name = "boq_id", nullable = false)
    private UUID boqId;

    @Column(name = "item_code")
    private String itemCode;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String unit;

    @Column(name = "estimated_quantity", nullable = false)
    private BigDecimal estimatedQuantity;

    @Column(nullable = false)
    private BigDecimal rate;

    @Column(name = "actual_quantity", nullable = false)
    private BigDecimal actualQuantity = BigDecimal.ZERO;

    @Column(name = "actual_rate")
    private BigDecimal actualRate;
}

