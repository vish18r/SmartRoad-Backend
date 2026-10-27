package com.smartroad.services.domain.repository.resourceplan;

import com.smartroad.services.common.enums.resourceplan.AllocationStatusEnum;
import com.smartroad.services.common.enums.resourceplan.ResourceTypeEnum;
import com.smartroad.services.domain.entity.resourceplan.ResourceAllocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link ResourceAllocationEntity}.
 * Contains only database query definitions — no business logic.
 *
 * @author Vishal
 * @version 1.0
 */
@Repository
public interface ResourceAllocationRepository extends JpaRepository<ResourceAllocationEntity, UUID> {

    /**
     * Finds every allocation belonging to a plan, excluding cancelled ones.
     *
     * @param resourcePlanId the plan UUID
     * @return the plan's non-cancelled allocations
     */
    @Query("SELECT a FROM ResourceAllocationEntity a WHERE a.resourcePlanId = :resourcePlanId "
            + "AND a.status <> com.smartroad.services.common.enums.resourceplan.AllocationStatusEnum.CANCELLED")
    List<ResourceAllocationEntity> findByResourcePlanId(@Param("resourcePlanId") UUID resourcePlanId);

    /**
     * Finds every allocation of a resource whose date range overlaps a candidate range, on a plan
     * other than the one being validated, excluding cancelled allocations and cancelled plans.
     *
     * @param resourceType the resource type to check
     * @param resourceId the resource UUID to check
     * @param startDate the candidate range's start date
     * @param endDate the candidate range's end date
     * @param excludeAllocationId an allocation id to leave out of the check, or null to check against all
     * @return the overlapping allocations, each joined to its owning plan's id, name, project and status
     */
    @Query("SELECT a FROM ResourceAllocationEntity a WHERE a.resourceType = :resourceType "
            + "AND a.resourceId = :resourceId "
            + "AND a.status <> com.smartroad.services.common.enums.resourceplan.AllocationStatusEnum.CANCELLED "
            + "AND (:excludeAllocationId IS NULL OR a.id <> :excludeAllocationId) "
            + "AND a.allocationStartDate <= :endDate "
            + "AND COALESCE(a.allocationEndDate, a.allocationStartDate) >= :startDate")
    List<ResourceAllocationEntity> findOverlapping(@Param("resourceType") ResourceTypeEnum resourceType,
                                                   @Param("resourceId") UUID resourceId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate,
                                                   @Param("excludeAllocationId") UUID excludeAllocationId);

    /**
     * Sums the planned quantity of MATERIAL allocations for a material across all plans for a
     * project whose allocation range covers a given date, excluding cancelled allocations and a
     * given allocation (used to avoid double-counting the allocation being validated).
     *
     * @param resourceId the material UUID
     * @param projectId the project UUID the owning plans must belong to
     * @param date the date the allocation must cover
     * @param excludeAllocationId an allocation id to leave out of the sum, or null to include all
     * @return the summed planned quantity, or null when none is allocated
     */
    @Query("SELECT SUM(a.plannedQuantity) FROM ResourceAllocationEntity a "
            + "JOIN ResourcePlanEntity p ON a.resourcePlanId = p.id "
            + "WHERE a.resourceType = com.smartroad.services.common.enums.resourceplan.ResourceTypeEnum.MATERIAL "
            + "AND a.resourceId = :resourceId AND p.projectId = :projectId "
            + "AND a.status <> com.smartroad.services.common.enums.resourceplan.AllocationStatusEnum.CANCELLED "
            + "AND p.status <> com.smartroad.services.common.enums.resourceplan.ResourcePlanStatusEnum.CANCELLED "
            + "AND (:excludeAllocationId IS NULL OR a.id <> :excludeAllocationId) "
            + "AND a.allocationStartDate <= :date AND COALESCE(a.allocationEndDate, a.allocationStartDate) >= :date")
    BigDecimal sumPlannedQuantity(@Param("resourceId") UUID resourceId,
                                  @Param("projectId") UUID projectId,
                                  @Param("date") LocalDate date,
                                  @Param("excludeAllocationId") UUID excludeAllocationId);

    /**
     * Counts a plan's non-cancelled allocations of a given resource type, used to compute the
     * plan's workers/machines/materials/vehicles counts.
     *
     * @param resourcePlanId the plan UUID
     * @param resourceType the resource type to count
     * @return count of matching allocations
     */
    @Query("SELECT COUNT(a) FROM ResourceAllocationEntity a WHERE a.resourcePlanId = :resourcePlanId "
            + "AND a.resourceType = :resourceType "
            + "AND a.status <> com.smartroad.services.common.enums.resourceplan.AllocationStatusEnum.CANCELLED")
    long countByResourcePlanIdAndResourceType(@Param("resourcePlanId") UUID resourcePlanId,
                                              @Param("resourceType") ResourceTypeEnum resourceType);

    /**
     * Counts an organization's non-cancelled allocations of a given resource type for a project on
     * a given plan date, across every non-cancelled plan.
     *
     * @param organizationId the organization UUID
     * @param projectId the project UUID, or null for all projects
     * @param planDate the plan date
     * @param resourceType the resource type to count
     * @return count of matching allocations
     */
    @Query("SELECT COUNT(a) FROM ResourceAllocationEntity a JOIN ResourcePlanEntity p ON a.resourcePlanId = p.id "
            + "WHERE p.organizationId = :organizationId AND (:projectId IS NULL OR p.projectId = :projectId) "
            + "AND p.planDate = :planDate AND a.resourceType = :resourceType "
            + "AND a.status <> com.smartroad.services.common.enums.resourceplan.AllocationStatusEnum.CANCELLED "
            + "AND p.status <> com.smartroad.services.common.enums.resourceplan.ResourcePlanStatusEnum.CANCELLED")
    long countByOrganizationAndProjectAndDateAndType(@Param("organizationId") UUID organizationId,
                                                     @Param("projectId") UUID projectId,
                                                     @Param("planDate") LocalDate planDate,
                                                     @Param("resourceType") ResourceTypeEnum resourceType);
}
