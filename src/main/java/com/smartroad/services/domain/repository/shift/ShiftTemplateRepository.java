package com.smartroad.services.domain.repository.shift;

import com.smartroad.services.common.enums.shift.ShiftStatusEnum;
import com.smartroad.services.domain.entity.shift.ShiftTemplateEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link ShiftTemplateEntity}.
 * Contains only database query definitions — no business logic.
 *
 * @author Vishal
 * @version 1.0
 */
@Repository
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplateEntity, UUID> {

    /**
     * Finds a shift template by its unique code, ignoring case.
     *
     * @param shiftCode the shift code
     * @return optional containing the matching shift template
     */
    @Query("SELECT s FROM ShiftTemplateEntity s WHERE LOWER(s.shiftCode) = LOWER(:shiftCode)")
    Optional<ShiftTemplateEntity> findByShiftCodeIgnoreCase(@Param("shiftCode") String shiftCode);

    /**
     * Checks whether another shift template already uses a code within an organization.
     *
     * @param shiftCode the shift code
     * @param organizationId the organization UUID
     * @param excludeId a shift id to leave out of the check, or null to check against all
     * @return true if another shift template already holds the code
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM ShiftTemplateEntity s "
            + "WHERE LOWER(s.shiftCode) = LOWER(:shiftCode) AND s.organizationId = :organizationId "
            + "AND (:excludeId IS NULL OR s.id <> :excludeId)")
    boolean existsByShiftCodeIgnoreCaseAndOrganizationId(@Param("shiftCode") String shiftCode,
                                                         @Param("organizationId") UUID organizationId,
                                                         @Param("excludeId") UUID excludeId);

    /**
     * Finds a page of an organization's shift templates matching the optional filters.
     * Any filter left null matches every value for that field.
     *
     * @param organizationId the organization UUID
     * @param status the shift status filter, or null
     * @param search a free-text term matched against shift name or shift code, or null
     * @param pageable the page request
     * @return the matching page of shift templates
     */
    @Query("SELECT s FROM ShiftTemplateEntity s WHERE s.organizationId = :organizationId "
            + "AND (:status IS NULL OR s.status = :status) "
            + "AND (:search IS NULL OR LOWER(s.shiftName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) "
            + "OR LOWER(s.shiftCode) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) "
            + "ORDER BY s.shiftName")
    Page<ShiftTemplateEntity> search(@Param("organizationId") UUID organizationId,
                                     @Param("status") ShiftStatusEnum status,
                                     @Param("search") String search, Pageable pageable);

    /**
     * Counts an organization's shift templates, optionally narrowed to a status.
     *
     * @param organizationId the organization UUID
     * @param status the shift status filter, or null to count every status
     * @return the count of matching shift templates
     */
    @Query("SELECT COUNT(s) FROM ShiftTemplateEntity s WHERE s.organizationId = :organizationId "
            + "AND (:status IS NULL OR s.status = :status)")
    long countByOrganizationIdAndOptionalStatus(@Param("organizationId") UUID organizationId,
                                                @Param("status") ShiftStatusEnum status);
}
