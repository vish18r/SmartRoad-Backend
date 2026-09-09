package com.smartroad.services.domain.repository;

import com.smartroad.services.domain.entity.company.CompanyServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link CompanyServiceEntity}.
 * Contains only database query definitions — no business logic.
 */
public interface CompanyServiceRepository extends JpaRepository<CompanyServiceEntity, UUID> {

    /**
     * Finds all services for a given company profile, ordered for display.
     *
     * @param companyProfileId the company profile ID
     * @return list of services for the profile, ordered by display order
     */
    List<CompanyServiceEntity> findByCompanyProfileIdOrderByDisplayOrderAsc(UUID companyProfileId);
}
