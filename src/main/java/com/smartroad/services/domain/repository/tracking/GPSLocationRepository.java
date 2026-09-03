package com.smartroad.services.domain.repository.tracking;

import com.smartroad.services.domain.entity.tracking.GPSLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for GPSLocationEntity.
 */
@Repository
public interface GPSLocationRepository extends JpaRepository<GPSLocationEntity, UUID> {

    @Query("SELECT g FROM GPSLocationEntity g WHERE g.projectId = :projectId AND g.timestamp >= :startTime ORDER BY g.timestamp DESC")
    List<GPSLocationEntity> findByProjectIdAndTimeRange(@Param("projectId") UUID projectId, @Param("startTime") OffsetDateTime startTime);

    @Query("SELECT g FROM GPSLocationEntity g WHERE g.workerId = :workerId AND g.timestamp >= :startTime ORDER BY g.timestamp DESC")
    List<GPSLocationEntity> findByWorkerIdRecent(@Param("workerId") UUID workerId, @Param("startTime") OffsetDateTime startTime);
}
