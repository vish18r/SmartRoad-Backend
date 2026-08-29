package com.nextenti.services.domain.repository;

import com.nextenti.services.domain.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<OrganizationEntity, UUID> { }
