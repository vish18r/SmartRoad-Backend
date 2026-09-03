package com.smartroad.services.domain.repository;
import com.smartroad.services.domain.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ClientRepository extends JpaRepository<ClientEntity, UUID> {
    List<ClientEntity> findByOrganizationIdAndActiveTrue(UUID organizationId);
    Optional<ClientEntity> findByIdAndOrganizationIdAndActiveTrue(UUID id, UUID organizationId);
}
