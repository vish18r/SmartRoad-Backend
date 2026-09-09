package com.smartroad.services.domain.repository;

import com.smartroad.services.domain.entity.company.CompanyProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for {@link CompanyProfileEntity}.
 * Contains only database query definitions — no business logic.
 */
public interface CompanyProfileRepository extends JpaRepository<CompanyProfileEntity, UUID> {
}
