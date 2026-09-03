package com.smartroad.services.domain.repository;

import com.smartroad.services.domain.entity.GrnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link GrnEntity}.
 * Contains only database query definitions — no business logic.
 *
 * @author Vishal
 * @version 1.0
 */
@Repository
public interface GrnRepository extends JpaRepository<GrnEntity, UUID> {

    /**
     * Finds all GRN records for a project.
     *
     * @param projectId the project UUID
     * @return list of GRN records
     */
    @Query("SELECT g FROM GrnEntity g WHERE g.projectId = :projectId")
    List<GrnEntity> findByProjectId(@Param("projectId") UUID projectId);

    /**
     * Finds a GRN by GRN number.
     *
     * @param grnNumber the GRN number
     * @return the GRN if found
     */
    @Query("SELECT g FROM GrnEntity g WHERE g.grnNumber = :grnNumber")
    Optional<GrnEntity> findByGrnNumber(@Param("grnNumber") String grnNumber);

    /**
     * Finds all GRN records for a purchase order.
     *
     * @param purchaseOrderId the purchase order UUID
     * @return list of GRN records
     */
    @Query("SELECT g FROM GrnEntity g WHERE g.purchaseOrderId = :purchaseOrderId")
    List<GrnEntity> findByPurchaseOrderId(@Param("purchaseOrderId") UUID purchaseOrderId);
}
