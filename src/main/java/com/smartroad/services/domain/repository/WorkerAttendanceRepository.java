package com.smartroad.services.domain.repository;

import com.smartroad.services.common.enums.workers.AttendanceStatusEnum;
import com.smartroad.services.domain.entity.worker.WorkerAttendanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link WorkerAttendanceEntity}.
 * Contains only database query definitions — no business logic.
 *
 * @author Vishal
 * @version 1.0
 */
@Repository
public interface WorkerAttendanceRepository extends JpaRepository<WorkerAttendanceEntity, UUID> {

    /**
     * Finds a worker's attendance record for a specific project and day.
     * Backs the upsert performed when attendance is marked twice for the same day.
     *
     * @param workerId the worker UUID
     * @param projectId the project UUID
     * @param attendanceDate the attendance date
     * @return optional containing the matching record, or empty if not found
     */
    @Query("SELECT a FROM WorkerAttendanceEntity a WHERE a.workerId = :workerId "
            + "AND a.projectId = :projectId AND a.attendanceDate = :attendanceDate")
    Optional<WorkerAttendanceEntity> findByWorkerIdAndProjectIdAndAttendanceDate(@Param("workerId") UUID workerId,
                                                                                 @Param("projectId") UUID projectId,
                                                                                 @Param("attendanceDate") LocalDate attendanceDate);

    /**
     * Finds a worker's attendance records falling inside a date range, most recent first.
     *
     * @param workerId the worker UUID
     * @param from the inclusive start of the range
     * @param to the inclusive end of the range
     * @return list of matching attendance records
     */
    @Query("SELECT a FROM WorkerAttendanceEntity a WHERE a.workerId = :workerId "
            + "AND a.attendanceDate BETWEEN :from AND :to ORDER BY a.attendanceDate DESC")
    List<WorkerAttendanceEntity> findByWorkerIdAndDateRange(@Param("workerId") UUID workerId,
                                                             @Param("from") LocalDate from,
                                                             @Param("to") LocalDate to);

    /**
     * Finds all of a worker's attendance records, most recent first.
     *
     * @param workerId the worker UUID
     * @return list of matching attendance records
     */
    @Query("SELECT a FROM WorkerAttendanceEntity a WHERE a.workerId = :workerId ORDER BY a.attendanceDate DESC")
    List<WorkerAttendanceEntity> findByWorkerId(@Param("workerId") UUID workerId);

    /**
     * Counts a project's attendance records for one day that hold a given status.
     *
     * @param projectId the project UUID
     * @param attendanceDate the day to report on
     * @param status the attendance status to filter by
     * @return count of matching records
     */
    @Query("SELECT COUNT(a) FROM WorkerAttendanceEntity a WHERE a.projectId = :projectId "
            + "AND a.attendanceDate = :attendanceDate AND a.status = :status")
    long countByProjectAndDateAndStatus(@Param("projectId") UUID projectId,
                                        @Param("attendanceDate") LocalDate attendanceDate,
                                        @Param("status") AttendanceStatusEnum status);

    /**
     * Sums the hours recorded against a project for one day.
     *
     * @param projectId the project UUID
     * @param attendanceDate the day to report on
     * @return summed hours, or null when nothing was recorded
     */
    @Query("SELECT SUM(a.hoursWorked) FROM WorkerAttendanceEntity a WHERE a.projectId = :projectId "
            + "AND a.attendanceDate = :attendanceDate")
    BigDecimal sumHoursByProjectAndDate(@Param("projectId") UUID projectId,
                                        @Param("attendanceDate") LocalDate attendanceDate);
}
