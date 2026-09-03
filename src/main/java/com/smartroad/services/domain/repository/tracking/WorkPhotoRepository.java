package com.smartroad.services.domain.repository.tracking;

import com.smartroad.services.domain.entity.tracking.WorkPhotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for WorkPhotoEntity.
 */
@Repository
public interface WorkPhotoRepository extends JpaRepository<WorkPhotoEntity, UUID> {

    @Query("SELECT w FROM WorkPhotoEntity w WHERE w.projectId = :projectId AND CAST(w.photoDate AS date) = :date ORDER BY w.photoDate DESC")
    List<WorkPhotoEntity> findByProjectAndDate(@Param("projectId") UUID projectId, @Param("date") LocalDate date);

    @Query("SELECT w FROM WorkPhotoEntity w WHERE w.projectId = :projectId AND w.photoType = :type ORDER BY w.photoDate DESC")
    List<WorkPhotoEntity> findByProjectAndType(@Param("projectId") UUID projectId, @Param("type") String type);
}
