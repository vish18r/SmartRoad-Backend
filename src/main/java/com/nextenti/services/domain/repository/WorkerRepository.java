package com.nextenti.services.domain.repository;

import com.nextenti.services.domain.entity.WorkerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link WorkerEntity}.
 * Contains only database query definitions — no business logic.
 *
 * @author Vishal
 * @version 1.0
 */
@Repository
public interface WorkerRepository extends JpaRepository<WorkerEntity, UUID> {

    /**
     * Finds all workers in an organization that are not deleted.
     *
     * @param organizationId the organization UUID
     * @return list of matching {@link WorkerEntity} records
     */
    @Query("SELECT w FROM WorkerEntity w WHERE w.organizationId = :organizationId AND w.isDeleted = FALSE ORDER BY w.firstName")
    List<WorkerEntity> findByOrganizationIdAndNotDeleted(@Param("organizationId") UUID organizationId);

    /**
     * Finds workers assigned to a specific site.
     *
     * @param siteId the site UUID
     * @return list of matching {@link WorkerEntity} records
     */
    @Query("SELECT w FROM WorkerEntity w WHERE w.assignedSiteId = :siteId AND w.isDeleted = FALSE")
    List<WorkerEntity> findByAssignedSiteId(@Param("siteId") UUID siteId);

    /**
     * Finds a worker by email address.
     *
     * @param emailId the email address
     * @return optional containing the matching worker
     */
    @Query("SELECT w FROM WorkerEntity w WHERE w.emailId = :emailId AND w.isDeleted = FALSE")
    Optional<WorkerEntity> findByEmailId(@Param("emailId") String emailId);

    /**
     * Checks if a worker with the given email exists in the organization.
     *
     * @param emailId the email address
     * @param organizationId the organization UUID
     * @return true if worker exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN TRUE ELSE FALSE END FROM WorkerEntity w WHERE w.emailId = :emailId AND w.organizationId = :organizationId AND w.isDeleted = FALSE")
    boolean existsByEmailIdAndOrganizationId(@Param("emailId") String emailId, @Param("organizationId") UUID organizationId);

    /**
     * Counts active workers in an organization.
     *
     * @param organizationId the organization UUID
     * @return count of active workers
     */
    @Query("SELECT COUNT(w) FROM WorkerEntity w WHERE w.organizationId = :organizationId AND w.isDeleted = FALSE")
    long countByOrganizationId(@Param("organizationId") UUID organizationId);
}
