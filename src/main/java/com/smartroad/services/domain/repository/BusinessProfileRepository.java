package com.smartroad.services.domain.repository;

import com.smartroad.services.domain.entity.business.BusinessProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link BusinessProfileEntity}.
 * Contains only database query definitions — no business logic.
 */
public interface BusinessProfileRepository extends JpaRepository<BusinessProfileEntity, UUID> {

    /**
     * Finds a business profile by organization ID.
     *
     * @param organizationId the organization ID
     * @return optional containing the business profile if found
     */
    Optional<BusinessProfileEntity> findByOrganizationId(UUID organizationId);
}
