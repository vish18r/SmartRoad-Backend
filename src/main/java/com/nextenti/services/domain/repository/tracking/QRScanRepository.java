package com.nextenti.services.domain.repository.tracking;

import com.nextenti.services.domain.entity.tracking.QRScanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for QRScanEntity.
 */
@Repository
public interface QRScanRepository extends JpaRepository<QRScanEntity, UUID> {

    @Query("SELECT q FROM QRScanEntity q WHERE q.qrCodeId = :qrCodeId ORDER BY q.scanTime DESC")
    List<QRScanEntity> findScanHistory(@Param("qrCodeId") UUID qrCodeId);

    @Query("SELECT q FROM QRScanEntity q WHERE q.projectId = :projectId AND q.scanTime >= :startTime ORDER BY q.scanTime DESC")
    List<QRScanEntity> findByProjectAndTime(@Param("projectId") UUID projectId, @Param("startTime") OffsetDateTime startTime);
}
