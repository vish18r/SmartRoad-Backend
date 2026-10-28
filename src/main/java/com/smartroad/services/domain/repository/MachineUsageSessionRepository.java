package com.smartroad.services.domain.repository;

import com.smartroad.services.domain.entity.machineutilization.MachineUsageSessionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link MachineUsageSessionEntity}.
 * Contains only database query definitions — no business logic. Aggregate KPI queries are
 * expressed as JPQL projections so summary figures are computed by the database rather than by
 * loading every session row into Java.
 *
 * @author Vishal
 * @version 1.0
 */
public interface MachineUsageSessionRepository extends JpaRepository<MachineUsageSessionEntity, UUID> {

    /**
     * Finds a non-deleted session by ID.
     *
     * @param id the session UUID
     * @return the matching session, if any
     */
    Optional<MachineUsageSessionEntity> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Finds sessions for a machine that overlap a candidate time window, used to reject
     * overlapping/duplicate usage sessions at the service layer (works identically on PostgreSQL
     * and the H2 test database, unlike a DB-level EXCLUDE constraint).
     *
     * @param machineId the machine UUID
     * @param candidateStart the candidate session's start time
     * @param candidateEnd the candidate session's end time, or a far-future sentinel when the
     *                      candidate session is still open
     * @param excludeSessionId a session ID to exclude from the check (the session being completed
     *                         itself), or null when checking a brand-new session
     * @return list of existing sessions that overlap the candidate window
     */
    @Query("SELECT s FROM MachineUsageSessionEntity s WHERE s.machineId = :machineId AND s.isDeleted = FALSE "
            + "AND (:excludeSessionId IS NULL OR s.id <> :excludeSessionId) "
            + "AND s.startTime < :candidateEnd "
            + "AND (s.endTime IS NULL OR s.endTime > :candidateStart)")
    List<MachineUsageSessionEntity> findOverlapping(@Param("machineId") UUID machineId,
                                                     @Param("candidateStart") OffsetDateTime candidateStart,
                                                     @Param("candidateEnd") OffsetDateTime candidateEnd,
                                                     @Param("excludeSessionId") UUID excludeSessionId);

    /**
     * Finds the currently open (not yet completed) session for a machine, if any.
     *
     * @param machineId the machine UUID
     * @return the open session, if any
     */
    @Query("SELECT s FROM MachineUsageSessionEntity s WHERE s.machineId = :machineId AND s.isDeleted = FALSE "
            + "AND s.endTime IS NULL")
    Optional<MachineUsageSessionEntity> findOpenSession(@Param("machineId") UUID machineId);

    /**
     * Searches sessions by optional organization/project/machine/status/date-range filters, paginated.
     *
     * @param organizationId the organization UUID
     * @param projectId the optional project filter
     * @param machineId the optional machine filter
     * @param startDate the inclusive start of the usage-date window
     * @param endDate the inclusive end of the usage-date window
     * @param pageable the page request
     * @return the matching page of sessions
     */
    @Query("SELECT s FROM MachineUsageSessionEntity s WHERE s.organizationId = :organizationId "
            + "AND s.isDeleted = FALSE "
            + "AND (:projectId IS NULL OR s.projectId = :projectId) "
            + "AND (:machineId IS NULL OR s.machineId = :machineId) "
            + "AND s.usageDate BETWEEN :startDate AND :endDate "
            + "ORDER BY s.usageDate DESC, s.startTime DESC")
    Page<MachineUsageSessionEntity> search(@Param("organizationId") UUID organizationId,
                                           @Param("projectId") UUID projectId,
                                           @Param("machineId") UUID machineId,
                                           @Param("startDate") java.time.LocalDate startDate,
                                           @Param("endDate") java.time.LocalDate endDate,
                                           Pageable pageable);

    /**
     * Finds all non-deleted sessions for one machine within a usage-date window, oldest first.
     *
     * @param machineId the machine UUID
     * @param startDate the inclusive start of the usage-date window
     * @param endDate the inclusive end of the usage-date window
     * @return list of matching sessions
     */
    @Query("SELECT s FROM MachineUsageSessionEntity s WHERE s.machineId = :machineId AND s.isDeleted = FALSE "
            + "AND s.usageDate BETWEEN :startDate AND :endDate ORDER BY s.usageDate, s.startTime")
    List<MachineUsageSessionEntity> findForMachineInRange(@Param("machineId") UUID machineId,
                                                          @Param("startDate") java.time.LocalDate startDate,
                                                          @Param("endDate") java.time.LocalDate endDate);

    /**
     * Counts distinct machines that have at least one non-deleted session in a date window for an
     * organization (optionally scoped to a project) — the "machines used in selected period" KPI.
     *
     * @param organizationId the organization UUID
     * @param projectId the optional project filter
     * @param startDate the inclusive start of the usage-date window
     * @param endDate the inclusive end of the usage-date window
     * @return count of distinct machines used
     */
    @Query("SELECT COUNT(DISTINCT s.machineId) FROM MachineUsageSessionEntity s "
            + "WHERE s.organizationId = :organizationId AND s.isDeleted = FALSE "
            + "AND (:projectId IS NULL OR s.projectId = :projectId) "
            + "AND s.usageDate BETWEEN :startDate AND :endDate")
    long countDistinctMachinesUsed(@Param("organizationId") UUID organizationId,
                                   @Param("projectId") UUID projectId,
                                   @Param("startDate") java.time.LocalDate startDate,
                                   @Param("endDate") java.time.LocalDate endDate);

    /**
     * Sums running hours for an organization (optionally scoped to a project) in a date window.
     *
     * @param organizationId the organization UUID
     * @param projectId the optional project filter
     * @param startDate the inclusive start of the usage-date window
     * @param endDate the inclusive end of the usage-date window
     * @return the summed running hours, or null if no matching sessions have recorded hours
     */
    @Query("SELECT SUM(s.runningHours) FROM MachineUsageSessionEntity s "
            + "WHERE s.organizationId = :organizationId AND s.isDeleted = FALSE "
            + "AND (:projectId IS NULL OR s.projectId = :projectId) "
            + "AND s.usageDate BETWEEN :startDate AND :endDate")
    BigDecimal sumRunningHours(@Param("organizationId") UUID organizationId,
                              @Param("projectId") UUID projectId,
                              @Param("startDate") java.time.LocalDate startDate,
                              @Param("endDate") java.time.LocalDate endDate);

    /**
     * Sums idle hours for an organization (optionally scoped to a project) in a date window.
     *
     * @param organizationId the organization UUID
     * @param projectId the optional project filter
     * @param startDate the inclusive start of the usage-date window
     * @param endDate the inclusive end of the usage-date window
     * @return the summed idle hours, or null if no matching sessions have recorded hours
     */
    @Query("SELECT SUM(s.idleHours) FROM MachineUsageSessionEntity s "
            + "WHERE s.organizationId = :organizationId AND s.isDeleted = FALSE "
            + "AND (:projectId IS NULL OR s.projectId = :projectId) "
            + "AND s.usageDate BETWEEN :startDate AND :endDate")
    BigDecimal sumIdleHours(@Param("organizationId") UUID organizationId,
                           @Param("projectId") UUID projectId,
                           @Param("startDate") java.time.LocalDate startDate,
                           @Param("endDate") java.time.LocalDate endDate);

    /**
     * Sums recorded fuel consumed for an organization (optionally scoped to a project) in a date window.
     *
     * @param organizationId the organization UUID
     * @param projectId the optional project filter
     * @param startDate the inclusive start of the usage-date window
     * @param endDate the inclusive end of the usage-date window
     * @return the summed fuel consumed, or null if no matching sessions have recorded fuel data
     */
    @Query("SELECT SUM(s.fuelConsumed) FROM MachineUsageSessionEntity s "
            + "WHERE s.organizationId = :organizationId AND s.isDeleted = FALSE "
            + "AND (:projectId IS NULL OR s.projectId = :projectId) "
            + "AND s.usageDate BETWEEN :startDate AND :endDate")
    BigDecimal sumFuelConsumed(@Param("organizationId") UUID organizationId,
                              @Param("projectId") UUID projectId,
                              @Param("startDate") java.time.LocalDate startDate,
                              @Param("endDate") java.time.LocalDate endDate);

    /**
     * Sums recorded fuel cost for an organization (optionally scoped to a project) in a date window.
     *
     * @param organizationId the organization UUID
     * @param projectId the optional project filter
     * @param startDate the inclusive start of the usage-date window
     * @param endDate the inclusive end of the usage-date window
     * @return the summed fuel cost, or null if no matching sessions have recorded fuel data
     */
    @Query("SELECT SUM(s.fuelCost) FROM MachineUsageSessionEntity s "
            + "WHERE s.organizationId = :organizationId AND s.isDeleted = FALSE "
            + "AND (:projectId IS NULL OR s.projectId = :projectId) "
            + "AND s.usageDate BETWEEN :startDate AND :endDate")
    BigDecimal sumFuelCost(@Param("organizationId") UUID organizationId,
                          @Param("projectId") UUID projectId,
                          @Param("startDate") java.time.LocalDate startDate,
                          @Param("endDate") java.time.LocalDate endDate);

    /**
     * Counts non-deleted sessions for an organization (optionally scoped to a project) in a date
     * window that have at least one non-null fuel field, used to decide whether fuel analytics
     * are available at all for the requested scope.
     *
     * @param organizationId the organization UUID
     * @param projectId the optional project filter
     * @param startDate the inclusive start of the usage-date window
     * @param endDate the inclusive end of the usage-date window
     * @return count of sessions with recorded fuel data
     */
    @Query("SELECT COUNT(s) FROM MachineUsageSessionEntity s "
            + "WHERE s.organizationId = :organizationId AND s.isDeleted = FALSE "
            + "AND (:projectId IS NULL OR s.projectId = :projectId) "
            + "AND s.usageDate BETWEEN :startDate AND :endDate "
            + "AND (s.fuelConsumed IS NOT NULL OR s.fuelCost IS NOT NULL)")
    long countSessionsWithFuelData(@Param("organizationId") UUID organizationId,
                                   @Param("projectId") UUID projectId,
                                   @Param("startDate") java.time.LocalDate startDate,
                                   @Param("endDate") java.time.LocalDate endDate);

    /**
     * Returns distinct project IDs that have at least one non-deleted session in a date window for
     * an organization — used to build the project-wise usage comparison.
     *
     * @param organizationId the organization UUID
     * @param startDate the inclusive start of the usage-date window
     * @param endDate the inclusive end of the usage-date window
     * @return list of distinct project UUIDs (nulls excluded)
     */
    @Query("SELECT DISTINCT s.projectId FROM MachineUsageSessionEntity s "
            + "WHERE s.organizationId = :organizationId AND s.isDeleted = FALSE "
            + "AND s.projectId IS NOT NULL AND s.usageDate BETWEEN :startDate AND :endDate")
    List<UUID> findDistinctProjectIds(@Param("organizationId") UUID organizationId,
                                      @Param("startDate") java.time.LocalDate startDate,
                                      @Param("endDate") java.time.LocalDate endDate);
}
