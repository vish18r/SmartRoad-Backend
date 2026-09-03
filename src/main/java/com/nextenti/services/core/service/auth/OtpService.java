package com.nextenti.services.core.service.auth;

import com.nextenti.services.common.enums.OtpFlow;
import com.nextenti.services.common.exception.SmartRoadException;
import com.nextenti.services.common.exception.ApplicationLayer;
import com.nextenti.services.common.exception.ErrorCodeMapping;
import com.nextenti.services.core.service.notification.NotificationService;
import com.nextenti.services.domain.entity.OtpEntity;
import com.nextenti.services.domain.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * @author Vishal
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final OtpRepository otpRepository;
    private final NotificationService notificationService;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a 6-digit OTP for email verification.
     * Creates OTP record with 10-minute expiration, saves to database,
     * and sends via email notification.
     *
     * @param email the email address to send OTP to
     * @param flow the OTP flow type (e.g., SIGNUP_VERIFICATION, PASSWORD_RESET)
     * @return the generated 6-digit OTP string
     * @throws SmartRoadException if OTP delivery fails
     */
    public String generateOtp(String email, OtpFlow flow) throws SmartRoadException {
        String otp = String.format("%0" + OTP_LENGTH + "d", secureRandom.nextInt(1000000));
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        OtpEntity otpEntity = new OtpEntity();
        otpEntity.setEmailId(email);
        otpEntity.setEmailOtp(otp);
        otpEntity.setFlow(flow);
        otpEntity.setExpiresAt(expiresAt);
        otpEntity.setActive(true);
        otpEntity.setRetryCount(0);
        otpEntity.setCreatedBy(SYSTEM_USER_ID);
        otpEntity.setModifiedBy(SYSTEM_USER_ID);

        otpRepository.saveAndFlush(otpEntity);
        log.info("OTP generated and saved for email: {} with flow: {}", email, flow);

        notificationService.sendOtpViaEmail(email, otp);

        return otp;
    }

    /**
     * Generates a 6-digit OTP for phone number verification.
     * Creates OTP record with 10-minute expiration, saves to database,
     * and sends via SMS notification.
     *
     * @param phoneNumber the phone number to send OTP to
     * @param flow the OTP flow type (e.g., SIGNUP_VERIFICATION, PASSWORD_RESET)
     * @return the generated 6-digit OTP string
     * @throws SmartRoadException if OTP delivery fails
     */
    @Transactional
    public String generateOtpForPhone(String phoneNumber, OtpFlow flow) throws SmartRoadException {
        String otp = String.format("%0" + OTP_LENGTH + "d", secureRandom.nextInt(1000000));
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        OtpEntity otpEntity = new OtpEntity();
        otpEntity.setPhoneNumber(phoneNumber);
        otpEntity.setPhoneOtp(otp);
        otpEntity.setFlow(flow);
        otpEntity.setExpiresAt(expiresAt);
        otpEntity.setActive(true);
        otpEntity.setRetryCount(0);
        otpEntity.setCreatedBy(SYSTEM_USER_ID);
        otpEntity.setModifiedBy(SYSTEM_USER_ID);

        otpRepository.saveAndFlush(otpEntity);
        log.info("OTP generated and saved for phone: {} with flow: {}", phoneNumber, flow);

        notificationService.sendOtpViaSms(phoneNumber, otp);

        return otp;
    }

    /**
     * Verifies OTP for email-based authentication.
     * Validates OTP code, checks expiration and retry attempts,
     * marks OTP as used upon successful verification.
     *
     * @param email the email address associated with the OTP
     * @param otp the OTP code to verify
     * @param flow the OTP flow type
     * @throws SmartRoadException if OTP invalid, expired, or max retry attempts exceeded
     */
    @Transactional
    public void verifyOtp(String email, String otp, OtpFlow flow) throws SmartRoadException {
        OffsetDateTime now = OffsetDateTime.now();
        OtpEntity otpEntity = otpRepository.findByEmailIdAndEmailOtpAndFlowAndActiveTrue(email, otp, flow)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "invalid.otp"));

        if (otpEntity.getExpiresAt().isBefore(now)) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "otp.has.expired");
        }

        if (otpEntity.getRetryCount() >= MAX_RETRY_ATTEMPTS) {
            otpEntity.setActive(false);
            otpRepository.save(otpEntity);
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "maximum.retry.attempts.exceeded");
        }

        otpEntity.setActive(false);
        otpRepository.save(otpEntity);

        log.info("OTP verified successfully for email: {} with flow: {}", email, flow);
    }

    /**
     * Verifies OTP for phone number-based authentication.
     * Validates OTP code, checks expiration and retry attempts,
     * marks OTP as used upon successful verification.
     *
     * @param phoneNumber the phone number associated with the OTP
     * @param otp the OTP code to verify
     * @param flow the OTP flow type
     * @throws SmartRoadException if OTP invalid, expired, or max retry attempts exceeded
     */
    @Transactional
    public void verifyPhoneOtp(String phoneNumber, String otp, OtpFlow flow) throws SmartRoadException {
        OffsetDateTime now = OffsetDateTime.now();
        OtpEntity otpEntity = otpRepository.findByPhoneNumberAndPhoneOtpAndFlowAndActiveTrue(phoneNumber, otp, flow)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "invalid.otp"));

        if (otpEntity.getExpiresAt().isBefore(now)) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "otp.has.expired");
        }

        if (otpEntity.getRetryCount() >= MAX_RETRY_ATTEMPTS) {
            otpEntity.setActive(false);
            otpRepository.save(otpEntity);
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "maximum.retry.attempts.exceeded");
        }

        otpEntity.setActive(false);
        otpRepository.save(otpEntity);

        log.info("OTP verified successfully for phone: {} with flow: {}", phoneNumber, flow);
    }

    /**
     * Invalidates all previous active OTPs for the given email and flow.
     * Marks existing OTPs as inactive to prevent reuse.
     *
     * @param email the email address to invalidate OTPs for
     * @param flow the OTP flow type
     */
    @Transactional
    public void invalidatePreviousOtps(String email, OtpFlow flow) {
        List<OtpEntity> activeOtps = otpRepository.findByEmailIdAndFlowOrderByDateCreatedDesc(email, flow);
        activeOtps.forEach(otp -> {
            otp.setActive(false);
            otpRepository.save(otp);
        });
    }

    /**
     * Invalidates all previous active OTPs for the given phone number and flow.
     * Marks existing OTPs as inactive to prevent reuse.
     *
     * @param phoneNumber the phone number to invalidate OTPs for
     * @param flow the OTP flow type
     */
    @Transactional
    public void invalidatePreviousPhoneOtps(String phoneNumber, OtpFlow flow) {
        List<OtpEntity> activeOtps = otpRepository.findByPhoneNumberAndFlowOrderByDateCreatedDesc(phoneNumber, flow);
        activeOtps.forEach(otp -> {
            otp.setActive(false);
            otpRepository.save(otp);
        });
    }

    /**
     * Checks if OTP can be resent based on cooldown period.
     * Enforces 1-minute cooldown between OTP resend requests.
     *
     * @param email the email address to check resend eligibility for
     * @param flow the OTP flow type
     * @return true if OTP can be resent, false if cooldown period not elapsed
     */
    public boolean canResendOtp(String email, OtpFlow flow) {
        List<OtpEntity> recentOtps = otpRepository.findByEmailIdAndFlowOrderByDateCreatedDesc(email, flow);
        if (recentOtps.isEmpty()) {
            return true;
        }

        OtpEntity lastOtp = recentOtps.get(0);
        long oneMinuteAgo = System.currentTimeMillis() - (60 * 1000);
        return lastOtp.getDateCreated().getTime() < oneMinuteAgo;
    }
}
