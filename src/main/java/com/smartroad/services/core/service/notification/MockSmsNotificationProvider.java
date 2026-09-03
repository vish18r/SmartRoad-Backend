package com.smartroad.services.core.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Mock SMS provider for development/testing.
 * Logs OTP to console/file instead of actually sending SMS.
 * Activate with: notification.sms.provider=mock
 */
@Component
@ConditionalOnProperty(name = "notification.sms.provider", havingValue = "mock", matchIfMissing = true)
@Slf4j
public class MockSmsNotificationProvider implements SmsNotificationProvider {

    @Override
    public void sendOtp(String phoneNumber, String otp) throws Exception {
        log.warn("\n" +
                "========================================\n" +
                "SMS OTP FOR TESTING\n" +
                "========================================\n" +
                "Phone: {}\n" +
                "OTP: {}\n" +
                "========================================\n", phoneNumber, otp);
    }
}
