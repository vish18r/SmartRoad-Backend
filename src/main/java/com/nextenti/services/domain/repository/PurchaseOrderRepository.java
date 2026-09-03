package com.nextenti.services.domain.repository;

import com.nextenti.services.domain.entity.PurchaseOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link PurchaseOrderEntity}.
 * Contains only database query definitions — no business logic.
 *
 * @author Vishal
 * @version 1.0
 */
@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrderEntity, UUID> {

    /**
     * Finds all purchase orders for a project.
     *
     * @param projectId the project UUID
     * @return list of purchase orders
     */
    @Query("SELECT p FROM PurchaseOrderEntity p WHERE p.projectId = :projectId")
    List<PurchaseOrderEntity> findByProjectId(@Param("projectId") UUID projectId);

    /**
     * Finds a purchase order by PO number.
     *
     * @param poNumber the PO number
     * @return the purchase order if found
     */
    @Query("SELECT p FROM PurchaseOrderEntity p WHERE p.poNumber = :poNumber")
    Optional<PurchaseOrderEntity> findByPoNumber(@Param("poNumber") String poNumber);

    /**
     * Finds all purchase orders for a vendor.
     *
     * @param vendorId the vendor UUID
     * @return list of purchase orders
     */
    @Query("SELECT p FROM PurchaseOrderEntity p WHERE p.vendorId = :vendorId")
    List<PurchaseOrderEntity> findByVendorId(@Param("vendorId") UUID vendorId);
}
