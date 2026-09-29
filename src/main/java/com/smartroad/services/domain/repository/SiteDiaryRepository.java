package com.smartroad.services.domain.repository;

import com.smartroad.services.domain.entity.sitediary.SiteDiaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link SiteDiaryEntity}.
 * Contains only database query definitions — no business logic.
 */
@Repository
public interface SiteDiaryRepository extends JpaRepository<SiteDiaryEntity, UUID> {

    /**
     * Finds the diary entry for a specific project and date.
     *
     * @param projectId the project UUID
     * @param diaryDate the diary date
     * @return optional containing the matching entry, or empty if not found
     */
    @Query("SELECT s FROM SiteDiaryEntity s WHERE s.projectId = :projectId AND s.diaryDate = :diaryDate")
    Optional<SiteDiaryEntity> findByProjectIdAndDiaryDate(@Param("projectId") UUID projectId,
                                                           @Param("diaryDate") LocalDate diaryDate);

    /**
     * Finds all diary entries for a project ordered most-recent first.
     *
     * @param projectId the project UUID
     * @return list of diary entries
     */
    @Query("SELECT s FROM SiteDiaryEntity s WHERE s.projectId = :projectId ORDER BY s.diaryDate DESC")
    List<SiteDiaryEntity> findByProjectIdOrderByDiaryDateDesc(@Param("projectId") UUID projectId);

    /**
     * Finds diary entries for a project within a date range, most-recent first.
     *
     * @param projectId the project UUID
     * @param from inclusive start date
     * @param to inclusive end date
     * @return list of matching diary entries
     */
    @Query("SELECT s FROM SiteDiaryEntity s WHERE s.projectId = :projectId AND s.diaryDate BETWEEN :from AND :to ORDER BY s.diaryDate DESC")
    List<SiteDiaryEntity> findByProjectIdAndDateRange(@Param("projectId") UUID projectId,
                                                       @Param("from") LocalDate from,
                                                       @Param("to") LocalDate to);
}
