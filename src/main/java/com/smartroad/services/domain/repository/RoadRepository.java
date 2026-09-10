package com.smartroad.services.domain.repository;

import com.smartroad.services.domain.entity.road.RoadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Counts roads belonging to an organization, scoped through their project.
     *
     * @param organizationId the organization UUID
     * @return total count of roads
     */
    @Query("SELECT COUNT(r) FROM RoadEntity r, ProjectEntity p "
            + "WHERE r.projectId = p.id AND p.organizationId = :organizationId AND p.archived = FALSE")
    long countByOrganization(@Param("organizationId") UUID organizationId);

    /**
     * Counts an organization's roads whose completed length has reached their full length.
     *
     * @param organizationId the organization UUID
     * @return count of fully completed roads
     */
    @Query("SELECT COUNT(r) FROM RoadEntity r, ProjectEntity p "
            + "WHERE r.projectId = p.id AND p.organizationId = :organizationId AND p.archived = FALSE "
            + "AND r.completedLengthM >= r.lengthM")
    long countCompletedByOrganization(@Param("organizationId") UUID organizationId);
}
