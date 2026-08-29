package com.nextenti.services.domain.repository;

import com.nextenti.services.domain.entity.SessionEntity;
import com.nextenti.services.common.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, UUID> {

    Optional<SessionEntity> findByToken(String token);

    List<SessionEntity> findByUserIdAndStatus(UUID userId, SessionStatus status);

    List<SessionEntity> findByUserId(UUID userId);

    Optional<SessionEntity> findByUserIdAndStatusAndExpiresAtAfter(UUID userId, SessionStatus status, OffsetDateTime now);

    void deleteByUserId(UUID userId);

    void deleteByToken(String token);
}
