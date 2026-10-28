package com.smartroad.services.domain.repository;

import com.smartroad.services.common.enums.maintenance.MaintenancePriorityEnum;
import com.smartroad.services.common.enums.maintenance.MaintenanceStatusEnum;
import com.smartroad.services.common.enums.maintenance.MaintenanceTypeEnum;
import com.smartroad.services.domain.entity.maintenance.MaintenanceRecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link MaintenanceRecordEntity}.
 * Contains only database query definitions - no business logic. Date-range bounds are always
 * passed as concrete (never null) timestamps; the service substitutes wide sentinel bounds when a
 * filter is not requested, so no nullable timestamp parameter ever reaches PostgreSQL untyped.
 *
 * @author Vishal
 * @version 1.0
 */
@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecordEntity, UUID> {

    /**
     * Shared JPQL filter clause used by {@link #search} and {@link #countFiltered}.
     */
    String FILTER_WHERE = "WHERE m.organizationId = :organizationId "
            + "AND (:machineId IS NULL OR m.machineId = :machineId) "
            + "AND (:projectId IS NULL OR m.projectId = :projectId) "
            + "AND (:maintenanceType IS NULL OR m.maintenanceType = :maintenanceType) "
            + "AND (:priority IS NULL OR m.priority = :priority) "
            + "AND (:status IS NULL OR m.status = :status) "
            + "AND m.scheduledStart >= :startFrom AND m.scheduledStart < :startBefore "
            + "AND m.dueBy >= :dueByFrom AND m.dueBy < :dueByBefore";

    /**
     * Searches an organization's maintenance records by optional machine/project/type/priority/
     * persisted-status filters and mandatory half-open scheduled-start and due-by windows, paginated.
     * Sorting is supplied by the {@link Pageable}.
     *
     * @param organizationId the organization UUID
     * @param machineId the machine filter, or null for any machine
     * @param projectId the project filter, or null for any project
     * @param maintenanceType the type filter, or null for any type
     * @param priority the priority filter, or null for any priority
     * @param status the persisted status filter, or null for any status
     * @param startFrom inclusive lower bound on scheduled start
     * @param startBefore exclusive upper bound on scheduled start
     * @param dueByFrom inclusive lower bound on the due-by deadline
     * @param dueByBefore exclusive upper bound on the due-by deadline
     * @param pageable the page request (including sort)
     * @return the matching page of records
     */
    @Query("SELECT m FROM MaintenanceRecordEntity m " + FILTER_WHERE)
    Page<MaintenanceRecordEntity> search(@Param("organizationId") UUID organizationId,
                                         @Param("machineId") UUID machineId,
                                         @Param("projectId") UUID projectId,
                                         @Param("maintenanceType") MaintenanceTypeEnum maintenanceType,
                                         @Param("priority") MaintenancePriorityEnum priority,
                                         @Param("status") MaintenanceStatusEnum status,
                                         @Param("startFrom") OffsetDateTime startFrom,
                                         @Param("startBefore") OffsetDateTime startBefore,
                                         @Param("dueByFrom") OffsetDateTime dueByFrom,
                                         @Param("dueByBefore") OffsetDateTime dueByBefore,
                                         Pageable pageable);

    /**
     * Counts an organization's maintenance records matching the same filters as {@link #search}.
     *
     * @param organizationId the organization UUID
     * @param machineId the machine filter, or null for any machine
     * @param projectId the project filter, or null for any project
     * @param maintenanceType the type filter, or null for any type
     * @param priority the priority filter, or null for any priority
     * @param status the persisted status filter, or null for any status
     * @param startFrom inclusive lower bound on scheduled start
     * @param startBefore exclusive upper bound on scheduled start
     * @param dueByFrom inclusive lower bound on the due-by deadline
     * @param dueByBefore exclusive upper bound on the due-by deadline
     * @return the number of matching records
     */
    @Query("SELECT COUNT(m) FROM MaintenanceRecordEntity m " + FILTER_WHERE)
    long countFiltered(@Param("organizationId") UUID organizationId,
                       @Param("machineId") UUID machineId,
                       @Param("projectId") UUID projectId,
                       @Param("maintenanceType") MaintenanceTypeEnum maintenanceType,
                       @Param("priority") MaintenancePriorityEnum priority,
                       @Param("status") MaintenanceStatusEnum status,
                       @Param("startFrom") OffsetDateTime startFrom,
                       @Param("startBefore") OffsetDateTime startBefore,
                       @Param("dueByFrom") OffsetDateTime dueByFrom,
                       @Param("dueByBefore") OffsetDateTime dueByBefore);

    /**
     * Finds every maintenance record of an organization whose scheduled start falls in a half-open
     * window, oldest first - used by the calendar view.
     *
     * @param organizationId the organization UUID
     * @param startFrom inclusive lower bound on scheduled start
     * @param startBefore exclusive upper bound on scheduled start
     * @return list of matching records ordered by scheduled start
     */
    @Query("SELECT m FROM MaintenanceRecordEntity m WHERE m.organizationId = :organizationId "
            + "AND m.scheduledStart >= :startFrom AND m.scheduledStart < :startBefore "
            + "ORDER BY m.scheduledStart ASC")
    List<MaintenanceRecordEntity> findForCalendar(@Param("organizationId") UUID organizationId,
                                                  @Param("startFrom") OffsetDateTime startFrom,
                                                  @Param("startBefore") OffsetDateTime startBefore);

    /**
     * Counts the distinct machines of an organization that currently have at least one maintenance
     * record in the given persisted status.
     *
     * @param organizationId the organization UUID
     * @param status the persisted status (IN_PROGRESS for "machines under maintenance")
     * @return the number of distinct machines
     */
    @Query("SELECT COUNT(DISTINCT m.machineId) FROM MaintenanceRecordEntity m "
            + "WHERE m.organizationId = :organizationId AND m.status = :status")
    long countDistinctMachinesInStatus(@Param("organizationId") UUID organizationId,
                                       @Param("status") MaintenanceStatusEnum status);

    /**
     * Sums the recorded actual cost of an organization's non-cancelled records scheduled in a window.
     *
     * @param organizationId the organization UUID
     * @param startFrom inclusive lower bound on scheduled start
     * @param startBefore exclusive upper bound on scheduled start
     * @param excludedStatus the persisted status to exclude (CANCELLED)
     * @return the summed actual cost, or null when no record in scope has an actual cost
     */
    @Query("SELECT SUM(m.actualCost) FROM MaintenanceRecordEntity m "
            + "WHERE m.organizationId = :organizationId AND m.status <> :excludedStatus "
            + "AND m.actualCost IS NOT NULL "
            + "AND m.scheduledStart >= :startFrom AND m.scheduledStart < :startBefore")
    BigDecimal sumActualCost(@Param("organizationId") UUID organizationId,
                             @Param("startFrom") OffsetDateTime startFrom,
                             @Param("startBefore") OffsetDateTime startBefore,
                             @Param("excludedStatus") MaintenanceStatusEnum excludedStatus);

    /**
     * Sums the estimated cost of an organization's non-cancelled records scheduled in a window that
     * have no actual cost recorded yet (the fallback half of "total cost in period").
     *
     * @param organizationId the organization UUID
     * @param startFrom inclusive lower bound on scheduled start
     * @param startBefore exclusive upper bound on scheduled start
     * @param excludedStatus the persisted status to exclude (CANCELLED)
     * @return the summed estimated cost, or null when no such record has an estimate
     */
    @Query("SELECT SUM(m.estimatedCost) FROM MaintenanceRecordEntity m "
            + "WHERE m.organizationId = :organizationId AND m.status <> :excludedStatus "
            + "AND m.actualCost IS NULL AND m.estimatedCost IS NOT NULL "
            + "AND m.scheduledStart >= :startFrom AND m.scheduledStart < :startBefore")
    BigDecimal sumEstimatedCostWithoutActual(@Param("organizationId") UUID organizationId,
                                             @Param("startFrom") OffsetDateTime startFrom,
                                             @Param("startBefore") OffsetDateTime startBefore,
                                             @Param("excludedStatus") MaintenanceStatusEnum excludedStatus);

    /**
     * Sums recorded downtime minutes of an organization's completed records scheduled in a window.
     *
     * @param organizationId the organization UUID
     * @param startFrom inclusive lower bound on scheduled start
     * @param startBefore exclusive upper bound on scheduled start
     * @param status the persisted status to include (COMPLETED)
     * @return the summed downtime minutes, or null when none is recorded
     */
    @Query("SELECT SUM(m.downtimeMinutes) FROM MaintenanceRecordEntity m "
            + "WHERE m.organizationId = :organizationId AND m.status = :status "
            + "AND m.scheduledStart >= :startFrom AND m.scheduledStart < :startBefore")
    Long sumDowntimeMinutes(@Param("organizationId") UUID organizationId,
                            @Param("startFrom") OffsetDateTime startFrom,
                            @Param("startBefore") OffsetDateTime startBefore,
                            @Param("status") MaintenanceStatusEnum status);

    /**
     * Counts records for a machine in a given persisted status, excluding one record.
     *
     * @param machineId the machine UUID
     * @param status the persisted status
     * @param excludeId the record UUID to exclude
     * @return the number of other matching records
     */
    @Query("SELECT COUNT(m) FROM MaintenanceRecordEntity m WHERE m.machineId = :machineId "
            + "AND m.status = :status AND m.id <> :excludeId")
    long countOtherForMachineInStatus(@Param("machineId") UUID machineId,
                                      @Param("status") MaintenanceStatusEnum status,
                                      @Param("excludeId") UUID excludeId);

    /**
     * Returns whether any record was generated from the given parent record (recurrence chain).
     *
     * @param parentMaintenanceId the parent record UUID
     * @return true when a child occurrence exists
     */
    boolean existsByParentMaintenanceId(UUID parentMaintenanceId);

    /**
     * Draws the next value from the maintenance reference sequence (PostgreSQL sequence created by
     * V29), used to build human-readable references such as {@code MNT-000123}.
     *
     * @return the next sequence number
     */
    @Query(value = "SELECT nextval('sr_maintenance_ref_seq')", nativeQuery = true)
    long nextReferenceNumber();
}
