package com.smartroad.services.domain.repository;

import com.smartroad.services.common.enums.vehicle.VehicleStatusEnum;
import com.smartroad.services.domain.entity.vehicle.VehicleEntity;
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
 * Spring Data JPA repository for {@link VehicleEntity}.
 * Contains only database query definitions — no business logic.
 *
 * @author Vishal
 * @version 1.0
 */
@Repository
public interface VehicleRepository extends JpaRepository<VehicleEntity, UUID> {

    /**
     * Finds all non-deleted vehicles for an organization.
     *
     * @param organizationId the organization UUID
     * @return list of matching vehicles
     */
    List<VehicleEntity> findByOrganizationIdAndIsDeletedFalse(UUID organizationId);

    /**
     * Finds a non-deleted vehicle by ID.
     *
     * @param id the vehicle UUID
     * @return the matching vehicle, if any
     */
    Optional<VehicleEntity> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Counts non-deleted vehicles for an organization.
     *
     * @param organizationId the organization UUID
     * @return count of matching vehicles
     */
    long countByOrganizationIdAndIsDeletedFalse(UUID organizationId);

    /**
     * Searches an organization's non-deleted vehicles by optional status and current site filters.
     *
     * @param organizationId the organization UUID
     * @param status the status filter, or null to match any status
     * @param currentSiteId the current site filter, or null to match any site
     * @param pageable the page request
     * @return the matching page of vehicles
     */
    @Query("SELECT v FROM VehicleEntity v WHERE v.organizationId = :organizationId AND v.isDeleted = FALSE "
            + "AND (:status IS NULL OR v.status = :status) "
            + "AND (:currentSiteId IS NULL OR v.currentSiteId = :currentSiteId) ORDER BY v.registrationNumber")
    Page<VehicleEntity> search(@Param("organizationId") UUID organizationId,
                               @Param("status") VehicleStatusEnum status,
                               @Param("currentSiteId") UUID currentSiteId,
                               Pageable pageable);

    /**
     * Finds every non-deleted ACTIVE vehicle of an organization, for availability checks.
     *
     * @param organizationId the organization UUID
     * @param status the status filter
     * @return list of matching vehicles
     */
    List<VehicleEntity> findByOrganizationIdAndStatusAndIsDeletedFalse(UUID organizationId, VehicleStatusEnum status);
}
