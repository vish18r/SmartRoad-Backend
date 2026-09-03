package com.nextenti.services.core.service.notification;

import com.nextenti.services.common.exception.SmartRoadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for sending notifications via multiple channels (Email, SMS, WhatsApp).
 * Delegates to provider-specific implementations based on configuration.
 */
@Service
@Slf4j
public class NotificationService {

    private final SmsNotificationProvider smsProvider;
    private final EmailNotificationProvider emailProvider;

    public NotificationService(SmsNotificationProvider smsProvider, EmailNotificationProvider emailProvider) {
        this.smsProvider = smsProvider;
        this.emailProvider = emailProvider;
    }

    /**
     * Sends an OTP via SMS to the specified phone number.
     *
     * @param phoneNumber the recipient phone number
     * @param otp the OTP code to send
     * @throws SmartRoadException if SMS sending fails
     */
    public void sendOtpViaSms(String phoneNumber, String otp) throws SmartRoadException {
        log.info("Sending OTP via SMS to: {}", maskPhoneNumber(phoneNumber));
        try {
            smsProvider.sendOtp(phoneNumber, otp);
            log.info("OTP successfully sent via SMS to: {}", maskPhoneNumber(phoneNumber));
        } catch (Exception e) {
            log.error("Failed to send OTP via SMS to {}: {}", maskPhoneNumber(phoneNumber), e.getMessage());
            throw new SmartRoadException(
                com.nextenti.services.common.exception.ApplicationLayer.SERVICE_LAYER,
                com.nextenti.services.common.exception.ErrorCodeMapping.SERVICE_INTERNAL_ERROR,
                "otp.delivery.failed",
                "SMS"
            );
        }
    }

    /**
     * Sends an OTP via Email to the specified email address.
     *
     * @param email the recipient email address
     * @param otp the OTP code to send
     * @throws SmartRoadException if email sending fails
     */
    public void sendOtpViaEmail(String email, String otp) throws SmartRoadException {
        log.info("Sending OTP via Email to: {}", maskEmail(email));
        try {
            emailProvider.sendOtp(email, otp);
            log.info("OTP successfully sent via Email to: {}", maskEmail(email));
        } catch (Exception e) {
            log.error("Failed to send OTP via Email to {}: {}", maskEmail(email), e.getMessage());
            throw new SmartRoadException(
                com.nextenti.services.common.exception.ApplicationLayer.SERVICE_LAYER,
                com.nextenti.services.common.exception.ErrorCodeMapping.SERVICE_INTERNAL_ERROR,
                "otp.delivery.failed",
                "EMAIL"
            );
        }
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) return phoneNumber;
        return "***" + phoneNumber.substring(phoneNumber.length() - 4);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        int atIndex = email.indexOf("@");
        if (atIndex <= 2) return email;
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }
}
