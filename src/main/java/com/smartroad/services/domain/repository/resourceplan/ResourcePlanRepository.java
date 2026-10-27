package com.smartroad.services.domain.repository.resourceplan;

import com.smartroad.services.common.enums.resourceplan.ResourcePlanStatusEnum;
import com.smartroad.services.domain.entity.resourceplan.ResourcePlanEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link ResourcePlanEntity}.
 * Contains only database query definitions — no business logic.
 *
 * @author Vishal
 * @version 1.0
 */
@Repository
public interface ResourcePlanRepository extends JpaRepository<ResourcePlanEntity, UUID> {

    /**
     * Finds a plan by ID scoped to an organization.
     *
     * @param id the plan UUID
     * @param organizationId the organization UUID
     * @return the matching plan, if any
     */
    Optional<ResourcePlanEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);

    /**
     * Searches an organization's plans by optional project, date range, and status filters,
     * most recent plan date first.
     *
     * @param organizationId the organization UUID
     * @param projectId the optional project filter
     * @param dateFrom the optional inclusive start of the plan-date window
     * @param dateTo the optional inclusive end of the plan-date window
     * @param status the optional status filter
     * @param pageable the page request
     * @return the matching page of plans
     */
    @Query("SELECT p FROM ResourcePlanEntity p WHERE p.organizationId = :organizationId "
            + "AND (:projectId IS NULL OR p.projectId = :projectId) "
            + "AND (:dateFrom IS NULL OR p.planDate >= :dateFrom) "
            + "AND (:dateTo IS NULL OR p.planDate <= :dateTo) "
            + "AND (:status IS NULL OR p.status = :status) ORDER BY p.planDate DESC")
    Page<ResourcePlanEntity> search(@Param("organizationId") UUID organizationId,
                                    @Param("projectId") UUID projectId,
                                    @Param("dateFrom") LocalDate dateFrom,
                                    @Param("dateTo") LocalDate dateTo,
                                    @Param("status") ResourcePlanStatusEnum status,
                                    Pageable pageable);

    /**
     * Counts an organization's plans for a project on a given plan date.
     *
     * @param organizationId the organization UUID
     * @param projectId the project UUID, or null for all projects
     * @param planDate the plan date
     * @return count of matching plans
     */
    @Query("SELECT COUNT(p) FROM ResourcePlanEntity p WHERE p.organizationId = :organizationId "
            + "AND (:projectId IS NULL OR p.projectId = :projectId) AND p.planDate = :planDate")
    long countByOrganizationIdAndProjectIdAndPlanDate(@Param("organizationId") UUID organizationId,
                                                      @Param("projectId") UUID projectId,
                                                      @Param("planDate") LocalDate planDate);
}
