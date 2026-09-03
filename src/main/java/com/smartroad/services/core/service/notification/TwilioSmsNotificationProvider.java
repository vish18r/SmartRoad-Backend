package com.smartroad.services.core.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Twilio SMS provider for real SMS delivery.
 * Activate with: notification.sms.provider=twilio
 *
 * Required properties:
 * - twilio.account-sid
 * - twilio.auth-token
 * - twilio.phone-number
 */
@Component
@ConditionalOnProperty(name = "notification.sms.provider", havingValue = "twilio")
@Slf4j
public class TwilioSmsNotificationProvider implements SmsNotificationProvider {

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.phone-number:}")
    private String fromPhoneNumber;

    @Override
    public void sendOtp(String phoneNumber, String otp) throws Exception {
        if (accountSid.isEmpty() || authToken.isEmpty() || fromPhoneNumber.isEmpty()) {
            throw new Exception("Twilio credentials not configured. Set: twilio.account-sid, twilio.auth-token, twilio.phone-number");
        }

        // TODO: Integrate with Twilio SDK
        // For now, just log
        log.info("Twilio SMS would be sent to: {} with OTP: {}", phoneNumber, otp);
    }
}
