package com.smartroad.services.core.service.notification;

/**
 * Interface for Email notification providers.
 * Implementations can use SendGrid, AWS SES, or other email services.
 */
public interface EmailNotificationProvider {

    /**
     * Sends an OTP via Email.
     *
     * @param email the recipient email address
     * @param otp the OTP code to send
     * @throws Exception if sending fails
     */
    void sendOtp(String email, String otp) throws Exception;
}
