package com.smartroad.services.domain.repository;

import com.smartroad.services.common.enums.stock.StockTransactionTypeEnum;
import com.smartroad.services.domain.entity.material.MaterialStockLedgerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
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

    /**
     * Finds a project's ledger entries created within a time window, most recent first.
     *
     * @param projectId the project UUID
     * @param start the inclusive start of the window
     * @param end the exclusive end of the window
     * @return the matching ledger entries
     */
    @Query("SELECT l FROM MaterialStockLedgerEntity l WHERE l.projectId = :projectId "
            + "AND l.dateCreated >= :start AND l.dateCreated < :end ORDER BY l.dateCreated DESC")
    List<MaterialStockLedgerEntity> findByProjectIdAndDateCreatedBetween(@Param("projectId") UUID projectId,
                                                                          @Param("start") Date start,
                                                                          @Param("end") Date end);

    /**
     * Sums a project's ledger quantity for one transaction type within a time window.
     *
     * @param projectId the project UUID
     * @param transactionType the transaction type to sum
     * @param start the inclusive start of the window
     * @param end the exclusive end of the window
     * @return the summed quantity, or null when nothing matched
     */
    @Query("SELECT SUM(l.quantity) FROM MaterialStockLedgerEntity l WHERE l.projectId = :projectId "
            + "AND l.transactionType = :transactionType AND l.dateCreated >= :start AND l.dateCreated < :end")
    BigDecimal sumQuantityByProjectIdAndTypeAndDateCreatedBetween(@Param("projectId") UUID projectId,
                                                                   @Param("transactionType") StockTransactionTypeEnum transactionType,
                                                                   @Param("start") Date start,
                                                                   @Param("end") Date end);
}
