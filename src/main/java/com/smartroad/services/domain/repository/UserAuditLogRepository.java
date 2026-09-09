package com.smartroad.services.domain.repository;

import com.smartroad.services.domain.entity.user.UserAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserAuditLogRepository extends JpaRepository<UserAuditLogEntity, UUID> {

    List<UserAuditLogEntity> findByUserIdOrderByDateCreatedDesc(UUID userId);

    List<UserAuditLogEntity> findByRequestedByOrderByDateCreatedDesc(String requestedBy);
}
