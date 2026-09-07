package com.smartroad.services.domain.repository;

import com.smartroad.services.common.enums.businessprofile.ContactRoleEnum;
import com.smartroad.services.domain.entity.BusinessContactEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link BusinessContactEntity}.
 * Contains only database query definitions — no business logic.
 */
public interface BusinessContactRepository extends JpaRepository<BusinessContactEntity, UUID> {

    /**
     * Finds all contacts for a given business profile.
     *
     * @param businessProfileId the business profile ID
     * @return list of contacts for the profile
     */
    List<BusinessContactEntity> findByBusinessProfileId(UUID businessProfileId);

    /**
     * Finds a contact by business profile ID and contact role.
     *
     * @param businessProfileId the business profile ID
     * @param contactRole the contact role
     * @return optional containing the contact if found
     */
    Optional<BusinessContactEntity> findByBusinessProfileIdAndContactRole(UUID businessProfileId, ContactRoleEnum contactRole);
}
