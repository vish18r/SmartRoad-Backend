package com.nextenti.services.domain.repository;

import com.nextenti.services.domain.entity.BoqItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link BoqItemEntity}.
 * Contains only database query definitions — no business logic.
 */
@Repository
public interface BoqItemRepository extends JpaRepository<BoqItemEntity, UUID> {

    /**
     * Finds all BOQ items for a given BOQ.
     *
     * @param boqId the BOQ UUID
     * @return list of matching BOQ item entities
     */
    List<BoqItemEntity> findByBoqId(UUID boqId);
}
