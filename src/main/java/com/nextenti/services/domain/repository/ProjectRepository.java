package com.nextenti.services.domain.repository;

import com.nextenti.services.common.enums.ProjectStatus;
import com.nextenti.services.domain.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link ProjectEntity}.
 * Contains only database query definitions — no business logic.
 */
@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {

    /**
     * Finds all non-archived projects for a given organization.
     *
     * @param organizationId the organization UUID
     * @return list of matching project entities
     */
    List<ProjectEntity> findByOrganizationIdAndArchivedFalse(UUID organizationId);

    /**
     * Finds a specific project by ID and organization, excluding archived projects.
     *
     * @param id the project UUID
     * @param organizationId the organization UUID
     * @return optional containing the matching project, or empty if not found
     */
    Optional<ProjectEntity> findByIdAndOrganizationIdAndArchivedFalse(UUID id, UUID organizationId);

    /**
     * Counts active (non-completed, non-cancelled) projects in an organization.
     *
     * @param organizationId the organization UUID
     * @return count of active projects
     */
    @Query("SELECT COUNT(p) FROM ProjectEntity p WHERE p.organizationId = :organizationId AND p.archived = FALSE AND p.status IN ('ACTIVE', 'ON_HOLD', 'DRAFT')")
    long countActiveProjectsByOrganization(@Param("organizationId") UUID organizationId);

    /**
     * Counts completed projects in an organization.
     *
     * @param organizationId the organization UUID
     * @return count of completed projects
     */
    @Query("SELECT COUNT(p) FROM ProjectEntity p WHERE p.organizationId = :organizationId AND p.archived = FALSE AND p.status = 'COMPLETED'")
    long countCompletedProjectsByOrganization(@Param("organizationId") UUID organizationId);
}
