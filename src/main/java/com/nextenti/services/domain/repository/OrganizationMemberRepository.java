package com.nextenti.services.domain.repository;

import com.nextenti.services.domain.entity.OrganizationMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMemberEntity, UUID> {
    List<OrganizationMemberEntity> findByUserIdAndActiveTrue(UUID userId);
    List<OrganizationMemberEntity> findByOrganizationIdAndActiveTrue(UUID organizationId);
    Optional<OrganizationMemberEntity> findByOrganizationIdAndUserIdAndActiveTrue(UUID organizationId, UUID userId);
}
