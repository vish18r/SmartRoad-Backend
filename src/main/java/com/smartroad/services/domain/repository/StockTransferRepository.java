package com.smartroad.services.domain.repository;

import com.smartroad.services.domain.entity.procurement.StockTransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link StockTransferEntity}.
 * Contains only database query definitions — no business logic.
 *
 * @author Vishal
 * @version 1.0
 */
@Repository
public interface StockTransferRepository extends JpaRepository<StockTransferEntity, UUID> {

    /**
     * Finds all stock transfers for a source project.
     *
     * @param sourceProjectId the source project UUID
     * @return list of stock transfers
     */
    @Query("SELECT s FROM StockTransferEntity s WHERE s.sourceProjectId = :sourceProjectId")
    List<StockTransferEntity> findBySourceProjectId(@Param("sourceProjectId") UUID sourceProjectId);

    /**
     * Finds all stock transfers for a destination project.
     *
     * @param destinationProjectId the destination project UUID
     * @return list of stock transfers
     */
    @Query("SELECT s FROM StockTransferEntity s WHERE s.destinationProjectId = :destinationProjectId")
    List<StockTransferEntity> findByDestinationProjectId(@Param("destinationProjectId") UUID destinationProjectId);

    /**
     * Finds all stock transfers for a material.
     *
     * @param materialId the material UUID
     * @return list of stock transfers
     */
    @Query("SELECT s FROM StockTransferEntity s WHERE s.materialId = :materialId")
    List<StockTransferEntity> findByMaterialId(@Param("materialId") UUID materialId);
}
