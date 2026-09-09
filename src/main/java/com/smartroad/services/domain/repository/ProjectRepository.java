package com.smartroad.services.domain.repository;

import com.smartroad.services.common.enums.ProjectStatus;
import com.smartroad.services.domain.entity.project.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
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

    /**
     * Counts on-hold projects in an organization.
     *
     * @param organizationId the organization UUID
     * @return count of on-hold projects
     */
    @Query("SELECT COUNT(p) FROM ProjectEntity p WHERE p.organizationId = :organizationId AND p.archived = FALSE AND p.status = 'ON_HOLD'")
    long countOnHoldProjectsByOrganization(@Param("organizationId") UUID organizationId);

    /**
     * Counts all non-archived projects in an organization.
     *
     * @param organizationId the organization UUID
     * @return total count of projects
     */
    @Query("SELECT COUNT(p) FROM ProjectEntity p WHERE p.organizationId = :organizationId AND p.archived = FALSE")
    long countProjectsByOrganization(@Param("organizationId") UUID organizationId);

    /**
     * Sums the approved budget across all non-archived projects in an organization.
     *
     * @param organizationId the organization UUID
     * @return summed budget, or null when no project carries a budget
     */
    @Query("SELECT SUM(p.budget) FROM ProjectEntity p WHERE p.organizationId = :organizationId AND p.archived = FALSE")
    BigDecimal sumBudgetByOrganization(@Param("organizationId") UUID organizationId);

    /**
     * Sums the incurred cost across all non-archived projects in an organization.
     *
     * @param organizationId the organization UUID
     * @return summed actual cost, or null when no cost has been recorded
     */
    @Query("SELECT SUM(p.actualCost) FROM ProjectEntity p WHERE p.organizationId = :organizationId AND p.archived = FALSE")
    BigDecimal sumActualCostByOrganization(@Param("organizationId") UUID organizationId);

    /**
     * Averages reported progress across projects that are not yet completed.
     *
     * @param organizationId the organization UUID
     * @return average progress percentage, or null when no in-flight project exists
     */
    @Query("SELECT AVG(p.progress) FROM ProjectEntity p WHERE p.organizationId = :organizationId AND p.archived = FALSE AND p.status <> 'COMPLETED'")
    Double averageProgressByOrganization(@Param("organizationId") UUID organizationId);

    /**
     * Counts projects whose incurred cost has exceeded their approved budget.
     *
     * @param organizationId the organization UUID
     * @return count of over-budget projects
     */
    @Query("SELECT COUNT(p) FROM ProjectEntity p WHERE p.organizationId = :organizationId AND p.archived = FALSE AND p.budget IS NOT NULL AND p.actualCost IS NOT NULL AND p.actualCost > p.budget")
    long countOverBudgetProjectsByOrganization(@Param("organizationId") UUID organizationId);

    /**
     * Counts incomplete projects whose end date has already passed.
     *
     * @param organizationId the organization UUID
     * @param asOf the reference date to compare end dates against
     * @return count of overdue projects
     */
    @Query("SELECT COUNT(p) FROM ProjectEntity p WHERE p.organizationId = :organizationId AND p.archived = FALSE AND p.status <> 'COMPLETED' AND p.endDate IS NOT NULL AND p.endDate < :asOf")
    long countOverdueProjectsByOrganization(@Param("organizationId") UUID organizationId, @Param("asOf") Date asOf);

    /**
     * Counts incomplete projects whose end date falls inside the given window.
     *
     * @param organizationId the organization UUID
     * @param from the inclusive start of the window
     * @param to the inclusive end of the window
     * @return count of projects due within the window
     */
    @Query("SELECT COUNT(p) FROM ProjectEntity p WHERE p.organizationId = :organizationId AND p.archived = FALSE AND p.status <> 'COMPLETED' AND p.endDate BETWEEN :from AND :to")
    long countProjectsDueBetween(@Param("organizationId") UUID organizationId,
                                 @Param("from") Date from,
                                 @Param("to") Date to);
}
