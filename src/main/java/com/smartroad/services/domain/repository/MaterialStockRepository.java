package com.smartroad.services.domain.repository;

import com.smartroad.services.domain.entity.material.MaterialStockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link MaterialStockEntity}.
 * Contains only database query definitions — no business logic.
 *
 * @author Vishal
 * @version 1.0
 */
@Repository
public interface MaterialStockRepository extends JpaRepository<MaterialStockEntity, UUID> {

    /**
     * Finds all stock records for a project.
     *
     * @param projectId the project UUID
     * @return list of stock records
     */
    @Query("SELECT s FROM MaterialStockEntity s WHERE s.projectId = :projectId")
    List<MaterialStockEntity> findByProjectId(@Param("projectId") UUID projectId);

    /**
     * Finds stock for a specific project and material.
     *
     * @param projectId the project UUID
     * @param materialId the material UUID
     * @return the stock record if found
     */
    @Query("SELECT s FROM MaterialStockEntity s WHERE s.projectId = :projectId AND s.materialId = :materialId")
    Optional<MaterialStockEntity> findByProjectIdAndMaterialId(@Param("projectId") UUID projectId,
                                                               @Param("materialId") UUID materialId);
}
