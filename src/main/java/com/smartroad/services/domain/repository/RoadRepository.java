package com.smartroad.services.domain.repository;

import com.smartroad.services.domain.entity.RoadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link RoadEntity}.
 * Contains only database query definitions — no business logic.
 */
@Repository
public interface RoadRepository extends JpaRepository<RoadEntity, UUID> {

    /**
     * Finds all roads for a given project.
     *
     * @param projectId the project UUID
     * @return list of matching road entities
     */
    List<RoadEntity> findByProjectId(UUID projectId);
}
