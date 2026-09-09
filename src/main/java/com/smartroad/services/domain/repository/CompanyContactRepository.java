package com.smartroad.services.domain.repository;

import com.smartroad.services.common.enums.businessprofile.ContactRoleEnum;
import com.smartroad.services.domain.entity.company.CompanyContactEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link CompanyContactEntity}.
 * Contains only database query definitions — no business logic.
 */
public interface CompanyContactRepository extends JpaRepository<CompanyContactEntity, UUID> {

    /**
     * Finds all contacts for a given company profile.
     *
     * @param companyProfileId the company profile ID
     * @return list of contacts for the profile
     */
    List<CompanyContactEntity> findByCompanyProfileId(UUID companyProfileId);

    /**
     * Finds a contact by company profile ID and contact role.
     *
     * @param companyProfileId the company profile ID
     * @param contactRole the contact role
     * @return optional containing the contact if found
     */
    Optional<CompanyContactEntity> findByCompanyProfileIdAndContactRole(UUID companyProfileId, ContactRoleEnum contactRole);
}
