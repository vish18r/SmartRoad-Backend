package com.nextenti.services.core.service.notification;

/**
 * Interface for SMS notification providers.
 * Implementations can use Twilio, AWS SNS, or other SMS services.
 */
public interface SmsNotificationProvider {

    /**
     * Sends an OTP via SMS.
     *
     * @param phoneNumber the recipient phone number
     * @param otp the OTP code to send
     * @throws Exception if sending fails
     */
    void sendOtp(String phoneNumber, String otp) throws Exception;
}
