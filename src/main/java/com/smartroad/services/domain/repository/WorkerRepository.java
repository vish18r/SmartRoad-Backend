package com.smartroad.services.domain.repository;

import com.smartroad.services.common.enums.workers.WorkerStatusEnum;
import com.smartroad.services.domain.entity.worker.WorkerEntity;
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

    /**
     * Counts workers in an organization that hold a given status.
     *
     * @param organizationId the organization UUID
     * @param status the worker status to filter by
     * @return count of matching workers
     */
    @Query("SELECT COUNT(w) FROM WorkerEntity w WHERE w.organizationId = :organizationId AND w.isDeleted = FALSE AND w.status = :status")
    long countByOrganizationIdAndStatus(@Param("organizationId") UUID organizationId,
                                        @Param("status") WorkerStatusEnum status);

    /**
     * Searches an organization's workers by first name, last name, phone number, or email.
     * Matching is case-insensitive and partial.
     *
     * @param organizationId the organization UUID
     * @param term the lowercased search term, already wrapped in wildcards
     * @return list of matching {@link WorkerEntity} records
     */
    @Query("SELECT w FROM WorkerEntity w WHERE w.organizationId = :organizationId AND w.isDeleted = FALSE "
            + "AND (LOWER(w.firstName) LIKE :term OR LOWER(w.lastName) LIKE :term "
            + "OR LOWER(w.phoneNumber) LIKE :term OR LOWER(w.emailId) LIKE :term) ORDER BY w.firstName")
    List<WorkerEntity> search(@Param("organizationId") UUID organizationId, @Param("term") String term);

    /**
     * Counts the workers posted to a site.
     *
     * @param siteId the site (project) UUID
     * @return count of workers assigned to the site
     */
    @Query("SELECT COUNT(w) FROM WorkerEntity w WHERE w.assignedSiteId = :siteId AND w.isDeleted = FALSE")
    long countByAssignedSiteId(@Param("siteId") UUID siteId);
}
