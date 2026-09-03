package com.smartroad.services.domain.repository;

import com.smartroad.services.domain.entity.VendorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link VendorEntity}.
 * Contains only database query definitions — no business logic.
 *
 * @author Vishal
 * @version 1.0
 */
@Repository
public interface VendorRepository extends JpaRepository<VendorEntity, UUID> {

    /**
     * Finds all vendors by organization ID.
     *
     * @param organizationId the organization UUID
     * @return list of vendors for the organization
     */
    @Query("SELECT v FROM VendorEntity v WHERE v.organizationId = :organizationId AND v.isActive = true")
    List<VendorEntity> findByOrganizationId(@Param("organizationId") UUID organizationId);

    /**
     * Finds a vendor by GST number.
     *
     * @param gstNumber the GST number
     * @return the vendor if found
     */
    @Query("SELECT v FROM VendorEntity v WHERE v.gstNumber = :gstNumber")
    Optional<VendorEntity> findByGstNumber(@Param("gstNumber") String gstNumber);

    /**
     * Finds vendors by organization and active status.
     *
     * @param organizationId the organization UUID
     * @param isActive the active status
     * @return list of matching vendors
     */
    @Query("SELECT v FROM VendorEntity v WHERE v.organizationId = :organizationId AND v.isActive = :isActive")
    List<VendorEntity> findByOrganizationIdAndIsActive(@Param("organizationId") UUID organizationId,
                                                       @Param("isActive") Boolean isActive);
}
