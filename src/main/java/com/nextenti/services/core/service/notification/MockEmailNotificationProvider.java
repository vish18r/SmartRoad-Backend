package com.nextenti.services.core.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Mock Email provider for development/testing.
 * Logs OTP to console/file instead of actually sending email.
 * Activate with: notification.email.provider=mock
 */
@Component
@ConditionalOnProperty(name = "notification.email.provider", havingValue = "mock", matchIfMissing = true)
@Slf4j
public class MockEmailNotificationProvider implements EmailNotificationProvider {

    @Override
    public void sendOtp(String email, String otp) throws Exception {
        log.warn("\n" +
                "========================================\n" +
                "EMAIL OTP FOR TESTING\n" +
                "========================================\n" +
                "Email: {}\n" +
                "OTP: {}\n" +
                "========================================\n", email, otp);
    }
}
