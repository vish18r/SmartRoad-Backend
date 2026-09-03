package com.smartroad.services.domain.repository.tracking;

import com.smartroad.services.domain.entity.tracking.DailyPhotoTimelineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for DailyPhotoTimelineEntity.
 */
@Repository
public interface DailyPhotoTimelineRepository extends JpaRepository<DailyPhotoTimelineEntity, UUID> {

    Optional<DailyPhotoTimelineEntity> findByProjectIdAndTimelineDate(UUID projectId, LocalDate timelineDate);

    List<DailyPhotoTimelineEntity> findByProjectIdOrderByTimelineDateDesc(UUID projectId);
}
