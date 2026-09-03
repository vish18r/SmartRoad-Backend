package com.smartroad.services.domain.repository.tracking;

import com.smartroad.services.domain.entity.tracking.QRCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for QRCodeEntity.
 */
@Repository
public interface QRCodeRepository extends JpaRepository<QRCodeEntity, UUID> {

    Optional<QRCodeEntity> findByQrCode(String qrCode);

    List<QRCodeEntity> findByProjectIdAndIsActiveTrue(UUID projectId);

    List<QRCodeEntity> findByReferenceTypeAndReferenceId(String referenceType, UUID referenceId);
}
