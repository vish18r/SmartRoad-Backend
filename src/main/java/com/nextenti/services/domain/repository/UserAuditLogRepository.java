package com.nextenti.services.domain.repository;

import com.nextenti.services.domain.entity.UserAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserAuditLogRepository extends JpaRepository<UserAuditLogEntity, UUID> {

    List<UserAuditLogEntity> findByActionDoneForUserIdOrderByDateCreatedDesc(UUID userId);

    List<UserAuditLogEntity> findByRequestedByOrderByDateCreatedDesc(UUID requestedBy);
}
