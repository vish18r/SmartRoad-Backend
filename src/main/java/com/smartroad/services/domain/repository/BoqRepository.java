package com.smartroad.services.domain.repository;

import com.smartroad.services.domain.entity.BoqEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link BoqEntity}.
 * Contains only database query definitions — no business logic.
 */
@Repository
public interface BoqRepository extends JpaRepository<BoqEntity, UUID> {

    /**
     * Finds all BOQs for a given project.
     *
     * @param projectId the project UUID
     * @return list of matching BOQ entities
     */
    List<BoqEntity> findByProjectId(UUID projectId);
}
