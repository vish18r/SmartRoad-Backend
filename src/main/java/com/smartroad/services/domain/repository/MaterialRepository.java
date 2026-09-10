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

    /**
     * Searches an organization's materials by code, name, or category.
     * Matching is case-insensitive and partial.
     *
     * @param organizationId the organization UUID
     * @param term the lowercased search term, already wrapped in wildcards
     * @return list of matching materials
     */
    @Query("SELECT m FROM MaterialEntity m WHERE m.organizationId = :organizationId "
            + "AND (LOWER(m.materialCode) LIKE :term OR LOWER(m.materialName) LIKE :term "
            + "OR LOWER(m.category) LIKE :term) ORDER BY m.materialName")
    List<MaterialEntity> search(@Param("organizationId") UUID organizationId, @Param("term") String term);

    /**
     * Finds an organization's materials whose total available stock has fallen to or below their
     * configured reorder threshold. Materials with no threshold set are never reported.
     *
     * @param organizationId the organization UUID
     * @return list of materials at or below their reorder threshold
     */
    @Query("SELECT m FROM MaterialEntity m WHERE m.organizationId = :organizationId "
            + "AND m.minimumStock IS NOT NULL "
            + "AND (SELECT COALESCE(SUM(s.quantityAvailable), 0) FROM MaterialStockEntity s "
            + "WHERE s.materialId = m.id) <= m.minimumStock ORDER BY m.materialName")
    List<MaterialEntity> findLowStock(@Param("organizationId") UUID organizationId);

    /**
     * Finds an organization's materials whose available stock on one project has fallen to or below
     * their configured reorder threshold. Materials with no threshold set are never reported.
     *
     * @param organizationId the organization UUID
     * @param projectId the project UUID to measure stock against
     * @return list of materials at or below their reorder threshold on that project
     */
    @Query("SELECT m FROM MaterialEntity m WHERE m.organizationId = :organizationId "
            + "AND m.minimumStock IS NOT NULL "
            + "AND (SELECT COALESCE(SUM(s.quantityAvailable), 0) FROM MaterialStockEntity s "
            + "WHERE s.materialId = m.id AND s.projectId = :projectId) <= m.minimumStock ORDER BY m.materialName")
    List<MaterialEntity> findLowStockByProject(@Param("organizationId") UUID organizationId,
                                               @Param("projectId") UUID projectId);
}
