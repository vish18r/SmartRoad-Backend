package com.smartroad.services.domain.repository;

import com.smartroad.services.domain.entity.material.MaterialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link MaterialEntity}.
 * Contains only database query definitions — no business logic.
 *
 * @author Vishal
 * @version 1.0
 */
@Repository
public interface MaterialRepository extends JpaRepository<MaterialEntity, UUID> {

    /**
     * Finds all materials by organization ID.
     *
     * @param organizationId the organization UUID
     * @return list of materials for the organization
     */
    @Query("SELECT m FROM MaterialEntity m WHERE m.organizationId = :organizationId")
    List<MaterialEntity> findByOrganizationId(@Param("organizationId") UUID organizationId);

    /**
     * Finds a material by code.
     *
     * @param materialCode the material code
     * @return the material if found
     */
    @Query("SELECT m FROM MaterialEntity m WHERE m.materialCode = :materialCode")
    Optional<MaterialEntity> findByMaterialCode(@Param("materialCode") String materialCode);
}
