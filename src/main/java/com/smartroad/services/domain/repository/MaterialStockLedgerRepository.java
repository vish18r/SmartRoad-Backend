package com.smartroad.services.domain.repository;

import com.smartroad.services.domain.entity.material.MaterialStockLedgerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for {@link MaterialStockLedgerEntity}.
 * Contains only database query definitions — no business logic.
 *
 * @author Vishal
 * @version 1.0
 */
@Repository
public interface MaterialStockLedgerRepository extends JpaRepository<MaterialStockLedgerEntity, UUID> {

    /**
     * Finds a page of ledger entries for a material, most recent first.
     *
     * @param materialId the material UUID
     * @param pageable the page request
     * @return page of matching ledger entries
     */
    @Query("SELECT l FROM MaterialStockLedgerEntity l WHERE l.materialId = :materialId ORDER BY l.dateCreated DESC")
    Page<MaterialStockLedgerEntity> findByMaterialId(@Param("materialId") UUID materialId, Pageable pageable);
}
