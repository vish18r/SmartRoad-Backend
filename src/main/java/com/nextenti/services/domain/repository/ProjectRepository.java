package com.nextenti.services.domain.repository;

import com.nextenti.services.domain.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
