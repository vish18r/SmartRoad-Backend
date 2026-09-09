package com.smartroad.services.domain.repository;

import com.smartroad.services.domain.entity.auth.OtpEntity;
import com.smartroad.services.common.enums.OtpFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRepository extends JpaRepository<OtpEntity, UUID> {

    Optional<OtpEntity> findByEmailIdAndFlowAndActiveTrueAndExpiresAtAfter(
            String emailId, OtpFlow flow, OffsetDateTime now);

    Optional<OtpEntity> findByPhoneNumberAndFlowAndActiveTrueAndExpiresAtAfter(
            String phoneNumber, OtpFlow flow, OffsetDateTime now);

    @Query("SELECT o FROM OtpEntity o WHERE o.emailId = :identifier OR o.phoneNumber = :identifier")
    List<OtpEntity> findByIdentifier(@Param("identifier") String identifier);

    Optional<OtpEntity> findByEmailIdAndEmailOtpAndFlowAndActiveTrue(
            String emailId, String emailOtp, OtpFlow flow);

    Optional<OtpEntity> findByPhoneNumberAndPhoneOtpAndFlowAndActiveTrue(
            String phoneNumber, String phoneOtp, OtpFlow flow);

    List<OtpEntity> findByEmailIdAndFlowOrderByDateCreatedDesc(String emailId, OtpFlow flow);

    List<OtpEntity> findByPhoneNumberAndFlowOrderByDateCreatedDesc(String phoneNumber, OtpFlow flow);
}
