package com.smartroad.services.domain.repository.shift;

import com.smartroad.services.common.enums.shift.ShiftAssignmentStatusEnum;
import com.smartroad.services.domain.entity.shift.WorkerShiftAssignmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link WorkerShiftAssignmentEntity}.
 * Contains only database query definitions — no business logic.
 *
 * @author Vishal
 * @version 1.0
 */
@Repository
public interface WorkerShiftAssignmentRepository extends JpaRepository<WorkerShiftAssignmentEntity, UUID> {

    /**
     * Finds every ACTIVE assignment of a worker whose date range overlaps a candidate range,
     * excluding a given assignment (used when creating or updating an assignment). An open-ended
     * candidate range (null effectiveTo) is treated as extending to infinity.
     *
     * @param workerId the worker UUID
     * @param effectiveFrom the candidate range's start date
     * @param effectiveTo the candidate range's end date, or null for open-ended
     * @param excludeId an assignment id to leave out of the check, or null to check against all
     * @return the overlapping ACTIVE assignments
     */
    @Query("SELECT a FROM WorkerShiftAssignmentEntity a WHERE a.workerId = :workerId AND a.status = 'ACTIVE' "
            + "AND (:excludeId IS NULL OR a.id <> :excludeId) "
            + "AND a.effectiveFrom <= COALESCE(:effectiveTo, a.effectiveFrom) "
            + "AND (a.effectiveTo IS NULL OR a.effectiveTo >= :effectiveFrom)")
    List<WorkerShiftAssignmentEntity> findOverlapping(@Param("workerId") UUID workerId,
                                                       @Param("effectiveFrom") LocalDate effectiveFrom,
                                                       @Param("effectiveTo") LocalDate effectiveTo,
                                                       @Param("excludeId") UUID excludeId);

    /**
     * Finds the assignment of a worker that is in effect on a given date, if any. An assignment
     * is in effect when the date falls within its effective range and the row is not ENDED.
     *
     * @param workerId the worker UUID
     * @param date the date to resolve the shift for
     * @return the matching assignments (at most one is expected under the overlap rule)
     */
    @Query("SELECT a FROM WorkerShiftAssignmentEntity a WHERE a.workerId = :workerId "
            + "AND a.status <> 'ENDED' AND a.effectiveFrom <= :date "
            + "AND (a.effectiveTo IS NULL OR a.effectiveTo >= :date) ORDER BY a.effectiveFrom DESC")
    List<WorkerShiftAssignmentEntity> findInEffectOn(@Param("workerId") UUID workerId, @Param("date") LocalDate date);

    /**
     * Finds a worker's full assignment history, most recently started first.
     *
     * @param workerId the worker UUID
     * @return the worker's assignments, newest effective-from date first
     */
    List<WorkerShiftAssignmentEntity> findByWorkerIdOrderByEffectiveFromDesc(UUID workerId);

    /**
     * Finds a page of assignments matching the optional filters, most recently started first.
     * Any filter left null matches every value for that field.
     *
     * @param workerId the worker UUID filter, or null
     * @param shiftId the shift UUID filter, or null
     * @param status the assignment status filter, or null
     * @param effectiveDate a date the assignment's range must cover, or null
     * @param pageable the page request
     * @return the matching page of assignments
     */
    @Query("SELECT a FROM WorkerShiftAssignmentEntity a WHERE "
            + "(:workerId IS NULL OR a.workerId = :workerId) "
            + "AND (:shiftId IS NULL OR a.shiftId = :shiftId) "
            + "AND (:status IS NULL OR a.status = :status) "
            + "AND (:effectiveDate IS NULL OR (a.effectiveFrom <= :effectiveDate "
            + "AND (a.effectiveTo IS NULL OR a.effectiveTo >= :effectiveDate))) "
            + "ORDER BY a.effectiveFrom DESC")
    Page<WorkerShiftAssignmentEntity> search(@Param("workerId") UUID workerId, @Param("shiftId") UUID shiftId,
                                             @Param("status") ShiftAssignmentStatusEnum status,
                                             @Param("effectiveDate") LocalDate effectiveDate, Pageable pageable);

    /**
     * Finds a page of an organization's assignments matching the optional filters, most recently
     * started first. The organization is resolved through the assigned shift template, since an
     * assignment row holds only ids. Any filter left null matches every value for that field.
     *
     * @param organizationId the organization UUID the assigned shift template must belong to
     * @param workerId the worker UUID filter, or null
     * @param shiftId the shift UUID filter, or null
     * @param status the assignment status filter, or null
     * @param effectiveDate a date the assignment's range must cover, or null
     * @param pageable the page request
     * @return the matching page of assignments
     */
    @Query("SELECT a FROM WorkerShiftAssignmentEntity a WHERE "
            + "a.shiftId IN (SELECT s.id FROM ShiftTemplateEntity s WHERE s.organizationId = :organizationId) "
            + "AND (:workerId IS NULL OR a.workerId = :workerId) "
            + "AND (:shiftId IS NULL OR a.shiftId = :shiftId) "
            + "AND (:status IS NULL OR a.status = :status) "
            + "AND (:effectiveDate IS NULL OR (a.effectiveFrom <= :effectiveDate "
            + "AND (a.effectiveTo IS NULL OR a.effectiveTo >= :effectiveDate))) "
            + "ORDER BY a.effectiveFrom DESC")
    Page<WorkerShiftAssignmentEntity> searchByOrganization(@Param("organizationId") UUID organizationId,
                                                           @Param("workerId") UUID workerId, @Param("shiftId") UUID shiftId,
                                                           @Param("status") ShiftAssignmentStatusEnum status,
                                                           @Param("effectiveDate") LocalDate effectiveDate, Pageable pageable);

    /**
     * Counts the distinct workers that hold an ACTIVE assignment covering today, for the
     * "workers assigned" dashboard figure.
     *
     * @param date the date to check coverage against, normally today
     * @return the count of distinct currently-assigned workers
     */
    @Query("SELECT COUNT(DISTINCT a.workerId) FROM WorkerShiftAssignmentEntity a WHERE a.status = 'ACTIVE' "
            + "AND a.effectiveFrom <= :date AND (a.effectiveTo IS NULL OR a.effectiveTo >= :date)")
    long countDistinctWorkersAssignedOn(@Param("date") LocalDate date);

    /**
     * Lists the distinct worker ids that hold an ACTIVE assignment covering a date, used to
     * derive the complementary "unassigned workers" set.
     *
     * @param date the date to check coverage against, normally today
     * @return the distinct assigned worker ids
     */
    @Query("SELECT DISTINCT a.workerId FROM WorkerShiftAssignmentEntity a WHERE a.status = 'ACTIVE' "
            + "AND a.effectiveFrom <= :date AND (a.effectiveTo IS NULL OR a.effectiveTo >= :date)")
    List<UUID> findDistinctWorkerIdsAssignedOn(@Param("date") LocalDate date);

    /**
     * Counts the assignments referencing a shift template, used to block deleting a shift that
     * still has assignment history.
     *
     * @param shiftId the shift UUID
     * @return the count of assignments referencing the shift
     */
    long countByShiftId(UUID shiftId);
}
