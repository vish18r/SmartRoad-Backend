package com.smartroad.services.core.service.notification;

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
        log.warn("MOCK email provider active - no real email will be sent. "
                + "Set NOTIFICATION_EMAIL_PROVIDER=smtp with MAIL_HOST/MAIL_USERNAME/MAIL_PASSWORD/MAIL_FROM "
                + "to deliver real OTP emails. (recipient masked, OTP not logged)");
    }
}
