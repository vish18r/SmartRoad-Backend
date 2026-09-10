package com.smartroad.services.domain.repository;

import com.smartroad.services.domain.entity.material.MaterialStockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    /**
     * Finds all stock records for a material across every project.
     *
     * @param materialId the material UUID
     * @return list of stock records
     */
    @Query("SELECT s FROM MaterialStockEntity s WHERE s.materialId = :materialId")
    List<MaterialStockEntity> findByMaterialId(@Param("materialId") UUID materialId);

    /**
     * Counts the distinct materials a project holds stock records for.
     *
     * @param projectId the project UUID
     * @return count of tracked materials
     */
    @Query("SELECT COUNT(s) FROM MaterialStockEntity s WHERE s.projectId = :projectId")
    long countByProjectId(@Param("projectId") UUID projectId);

    /**
     * Sums the valued stock a project is holding.
     *
     * @param projectId the project UUID
     * @return summed stock value, or null when nothing is valued
     */
    @Query("SELECT SUM(s.totalValue) FROM MaterialStockEntity s WHERE s.projectId = :projectId")
    BigDecimal sumStockValueByProjectId(@Param("projectId") UUID projectId);

    /**
     * Counts a project's materials whose available stock has fallen to or below the
     * material's reorder threshold. Materials with no threshold set are not counted.
     *
     * @param projectId the project UUID
     * @return count of materials at or below their reorder threshold
     */
    @Query("SELECT COUNT(s) FROM MaterialStockEntity s, MaterialEntity m "
            + "WHERE s.materialId = m.id AND s.projectId = :projectId "
            + "AND m.minimumStock IS NOT NULL AND s.quantityAvailable <= m.minimumStock")
    long countLowStockByProjectId(@Param("projectId") UUID projectId);
}
