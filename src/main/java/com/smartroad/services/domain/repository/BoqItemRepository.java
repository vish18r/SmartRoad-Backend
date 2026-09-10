package com.smartroad.services.domain.repository;

import com.smartroad.services.domain.entity.project.BoqItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
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

    /**
     * Finds a single BOQ item scoped to its parent BOQ.
     *
     * @param id the BOQ item UUID
     * @param boqId the BOQ UUID
     * @return optional containing the matching item, or empty if not found
     */
    Optional<BoqItemEntity> findByIdAndBoqId(UUID id, UUID boqId);

    /**
     * Deletes every item belonging to a BOQ.
     *
     * @param boqId the BOQ UUID
     */
    void deleteByBoqId(UUID boqId);
}
