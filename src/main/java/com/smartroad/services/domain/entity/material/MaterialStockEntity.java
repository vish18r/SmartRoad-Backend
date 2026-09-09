package com.smartroad.services.domain.entity.material;

import com.smartroad.services.domain.entity.SmartRoadBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * JPA entity representing material stock per project stored in the sr_material_stock table.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_material_stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class MaterialStockEntity extends SmartRoadBaseEntity {

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "material_id", nullable = false)
    private UUID materialId;

    @Column(name = "quantity_available", nullable = false)
    private BigDecimal quantityAvailable;

    @Column(name = "quantity_reserved")
    private BigDecimal quantityReserved;

    @Column(name = "quantity_consumed")
    private BigDecimal quantityConsumed;

    @Column(name = "unit_rate")
    private BigDecimal unitRate;

    @Column(name = "total_value")
    private BigDecimal totalValue;
}
