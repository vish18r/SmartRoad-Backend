package com.smartroad.services.domain.entity.procurement;

import com.smartroad.services.common.enums.grn.GrnStatusEnum;
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
import java.util.Date;
import java.util.UUID;

/**
 * JPA entity representing a Goods Received Note (GRN) stored in the sr_grn table.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_grn")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class GrnEntity extends SmartRoadBaseEntity {

    @Column(name = "grn_number", nullable = false, unique = true)
    private String grnNumber;

    @Column(name = "purchase_order_id", nullable = false)
    private UUID purchaseOrderId;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "material_id", nullable = false)
    private UUID materialId;

    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;

    @Column(name = "ordered_quantity", nullable = false)
    private BigDecimal orderedQuantity;

    @Column(name = "received_quantity", nullable = false)
    private BigDecimal receivedQuantity;

    @Column(name = "rejected_quantity")
    private BigDecimal rejectedQuantity;

    @Column(name = "receiving_date", nullable = false)
    private Date receivingDate;

    @Column(name = "quality_status")
    private String qualityStatus;

    @Column(name = "receiving_notes", columnDefinition = "TEXT")
    private String receivingNotes;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private GrnStatusEnum status;
}
