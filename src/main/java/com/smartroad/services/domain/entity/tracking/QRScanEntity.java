package com.smartroad.services.domain.entity.tracking;

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
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity representing QR code scans in the sr_qr_scans table.
 * Records every QR code scan with timestamp and location.
 */
@Entity
@Table(name = "sr_qr_scans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class QRScanEntity extends SmartRoadBaseEntity {

    @Column(name = "qr_code_id", nullable = false)
    private UUID qrCodeId;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "scanned_by", nullable = false)
    private UUID scannedBy;

    @Column(name = "scan_latitude")
    private BigDecimal scanLatitude;

    @Column(name = "scan_longitude")
    private BigDecimal scanLongitude;

    @Column(name = "scan_time", nullable = false)
    private OffsetDateTime scanTime;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "notes")
    private String notes;
}
