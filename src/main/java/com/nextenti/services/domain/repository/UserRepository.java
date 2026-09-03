package com.nextenti.services.domain.repository;

import com.nextenti.services.domain.entity.UserEntity;
import com.nextenti.services.common.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmailId(String emailId);

    Optional<UserEntity> findByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM UserEntity u WHERE u.emailId = :identifier OR u.phoneNumber = :identifier OR CAST(u.id AS string) = :identifier")
    Optional<UserEntity> findByIdentifier(@Param("identifier") String identifier);

    Optional<UserEntity> findByEmailIdAndStatus(String emailId, UserStatus status);

    Optional<UserEntity> findByPhoneNumberAndStatus(String phoneNumber, UserStatus status);

    Optional<UserEntity> findByOauthSigninId(String oauthSigninId);

    boolean existsByEmailId(String emailId);

    boolean existsByPhoneNumber(String phoneNumber);
}
