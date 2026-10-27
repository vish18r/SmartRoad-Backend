package com.smartroad.services.domain.repository;

import com.smartroad.services.common.enums.machine.MachineStatusEnum;
import com.smartroad.services.domain.entity.machine.MachineEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link MachineEntity}.
 * Contains only database query definitions — no business logic.
 *
 * @author Vishal
 * @version 1.0
 */
@Repository
public interface MachineRepository extends JpaRepository<MachineEntity, UUID> {

    /**
     * Finds all non-deleted machines for an organization.
     *
     * @param organizationId the organization UUID
     * @return list of matching machines
     */
    List<MachineEntity> findByOrganizationIdAndIsDeletedFalse(UUID organizationId);

    /**
     * Finds a non-deleted machine by ID.
     *
     * @param id the machine UUID
     * @return the matching machine, if any
     */
    Optional<MachineEntity> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Counts non-deleted machines for an organization.
     *
     * @param organizationId the organization UUID
     * @return count of matching machines
     */
    long countByOrganizationIdAndIsDeletedFalse(UUID organizationId);

    /**
     * Searches an organization's non-deleted machines by optional status and current site filters.
     *
     * @param organizationId the organization UUID
     * @param status the status filter, or null to match any status
     * @param currentSiteId the current site filter, or null to match any site
     * @param pageable the page request
     * @return the matching page of machines
     */
    @Query("SELECT m FROM MachineEntity m WHERE m.organizationId = :organizationId AND m.isDeleted = FALSE "
            + "AND (:status IS NULL OR m.status = :status) "
            + "AND (:currentSiteId IS NULL OR m.currentSiteId = :currentSiteId) ORDER BY m.name")
    Page<MachineEntity> search(@Param("organizationId") UUID organizationId,
                               @Param("status") MachineStatusEnum status,
                               @Param("currentSiteId") UUID currentSiteId,
                               Pageable pageable);

    /**
     * Finds every non-deleted ACTIVE machine of an organization, for availability checks.
     *
     * @param organizationId the organization UUID
     * @param status the status filter
     * @return list of matching machines
     */
    List<MachineEntity> findByOrganizationIdAndStatusAndIsDeletedFalse(UUID organizationId, MachineStatusEnum status);
}
