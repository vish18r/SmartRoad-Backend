package com.smartroad.services.domain.repository.tracking;

import com.smartroad.services.domain.entity.tracking.GeofenceZoneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for GeofenceZoneEntity.
 */
@Repository
public interface GeofenceZoneRepository extends JpaRepository<GeofenceZoneEntity, UUID> {

    List<GeofenceZoneEntity> findByProjectIdAndIsActiveTrue(UUID projectId);
}
