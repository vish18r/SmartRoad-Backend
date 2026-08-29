package com.nextenti.services.domain.repository;

import com.nextenti.services.domain.entity.OAuthStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthStateRepository extends JpaRepository<OAuthStateEntity, UUID> {

    Optional<OAuthStateEntity> findByState(String state);

    Optional<OAuthStateEntity> findByStateAndUsedFalseAndExpiresAtAfter(String state, OffsetDateTime now);

    void deleteByState(String state);
}
