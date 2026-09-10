package com.smartroad.services.domain.repository;

import com.smartroad.services.domain.entity.road.RoadSectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link RoadSectionEntity}.
 * Contains only database query definitions — no business logic.
 */
@Repository
public interface RoadSectionRepository extends JpaRepository<RoadSectionEntity, UUID> {

    /**
     * Finds all road sections for a given road.
     *
     * @param roadId the road UUID
     * @return list of matching road section entities
     */
    List<RoadSectionEntity> findByRoadId(UUID roadId);

    /**
     * Finds a single road section scoped to its parent road.
     *
     * @param id the road section UUID
     * @param roadId the road UUID
     * @return optional containing the matching section, or empty if not found
     */
    Optional<RoadSectionEntity> findByIdAndRoadId(UUID id, UUID roadId);
}
