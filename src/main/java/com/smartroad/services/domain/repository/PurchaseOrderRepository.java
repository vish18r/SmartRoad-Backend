package com.smartroad.services.domain.repository;

import com.smartroad.services.common.enums.purchase.PurchaseOrderStatusEnum;
import com.smartroad.services.domain.entity.procurement.PurchaseOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
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

    /**
     * Sums purchase order value for an organization across the given statuses,
     * scoped through each order's project.
     *
     * @param organizationId the organization UUID
     * @param statuses the purchase order statuses to include
     * @return summed order value, or null when no order matches
     */
    @Query("SELECT SUM(po.totalAmount) FROM PurchaseOrderEntity po, ProjectEntity p "
            + "WHERE po.projectId = p.id AND p.organizationId = :organizationId AND p.archived = FALSE "
            + "AND po.status IN :statuses")
    BigDecimal sumAmountByOrganizationAndStatuses(@Param("organizationId") UUID organizationId,
                                                  @Param("statuses") Collection<PurchaseOrderStatusEnum> statuses);

    /**
     * Counts an organization's purchase orders in the given statuses whose expected
     * delivery date has already passed.
     *
     * @param organizationId the organization UUID
     * @param statuses the purchase order statuses to include
     * @param asOf the reference date to compare expected delivery dates against
     * @return count of overdue deliveries
     */
    @Query("SELECT COUNT(po) FROM PurchaseOrderEntity po, ProjectEntity p "
            + "WHERE po.projectId = p.id AND p.organizationId = :organizationId AND p.archived = FALSE "
            + "AND po.status IN :statuses AND po.expectedDeliveryDate IS NOT NULL AND po.expectedDeliveryDate < :asOf")
    long countOverdueDeliveriesByOrganization(@Param("organizationId") UUID organizationId,
                                              @Param("statuses") Collection<PurchaseOrderStatusEnum> statuses,
                                              @Param("asOf") Date asOf);
}
