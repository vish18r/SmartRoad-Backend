package com.smartroad.services.domain.repository.tracking;

import com.smartroad.services.domain.entity.tracking.WorkerCheckinEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for WorkerCheckinEntity.
 */
@Repository
public interface WorkerCheckinRepository extends JpaRepository<WorkerCheckinEntity, UUID> {

    @Query("SELECT w FROM WorkerCheckinEntity w WHERE w.projectId = :projectId AND w.workerId = :workerId AND CAST(w.checkInTime AS date) = :date AND w.status = 'ACTIVE'")
    Optional<WorkerCheckinEntity> findTodayActiveCheckin(@Param("projectId") UUID projectId, @Param("workerId") UUID workerId, @Param("date") LocalDate date);

    @Query("SELECT w FROM WorkerCheckinEntity w WHERE w.projectId = :projectId AND CAST(w.checkInTime AS date) = :date ORDER BY w.checkInTime DESC")
    List<WorkerCheckinEntity> findByProjectAndDate(@Param("projectId") UUID projectId, @Param("date") LocalDate date);

    /**
     * Finds a worker's sessions that started inside a time window, earliest first.
     *
     * @param workerId the worker UUID
     * @param from the inclusive start of the window
     * @param to the exclusive end of the window
     * @return the matching sessions
     */
    @Query("SELECT w FROM WorkerCheckinEntity w WHERE w.workerId = :workerId AND w.checkInTime >= :from AND w.checkInTime < :to ORDER BY w.checkInTime ASC")
    List<WorkerCheckinEntity> findByWorkerAndPeriod(@Param("workerId") UUID workerId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);
}
