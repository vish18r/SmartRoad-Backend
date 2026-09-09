package com.smartroad.services.domain.repository;

import com.smartroad.services.domain.entity.organization.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<OrganizationEntity, UUID> { }
