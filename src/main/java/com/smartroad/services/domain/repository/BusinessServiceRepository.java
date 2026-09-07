package com.smartroad.services.domain.repository;

import com.smartroad.services.domain.entity.BusinessServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link BusinessServiceEntity}.
 * Contains only database query definitions — no business logic.
 */
public interface BusinessServiceRepository extends JpaRepository<BusinessServiceEntity, UUID> {

    /**
     * Finds all services for a given business profile.
     *
     * @param businessProfileId the business profile ID
     * @return list of services for the profile
     */
    List<BusinessServiceEntity> findByBusinessProfileId(UUID businessProfileId);

    /**
     * Finds a service by business profile ID and service name.
     *
     * @param businessProfileId the business profile ID
     * @param serviceName the service name
     * @return optional containing the service if found
     */
    Optional<BusinessServiceEntity> findByBusinessProfileIdAndServiceName(UUID businessProfileId, String serviceName);
}
