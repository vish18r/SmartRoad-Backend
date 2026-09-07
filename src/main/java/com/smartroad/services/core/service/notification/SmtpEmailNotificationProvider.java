package com.smartroad.services.core.service.notification;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * Real Email provider that sends OTP emails over SMTP (e.g. Gmail SMTP)
 * using Spring Mail's {@link JavaMailSender}.
 * Activate with: notification.email.provider=smtp
 *
 * Required configuration (supplied via environment variables, never hardcoded):
 * - MAIL_HOST (defaults to smtp.gmail.com)
 * - MAIL_PORT (defaults to 587)
 * - MAIL_USERNAME (the sending Gmail address)
 * - MAIL_PASSWORD (a Gmail App Password, not the account password)
 */
@Component
@ConditionalOnProperty(name = "notification.email.provider", havingValue = "smtp")
@Slf4j
public class SmtpEmailNotificationProvider implements EmailNotificationProvider {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Value("${otp.expiry-minutes:10}")
    private int otpExpiryMinutes;

    /**
     * Constructs the provider with the Spring-managed mail sender.
     *
     * @param mailSender the configured {@link JavaMailSender} bean
     */
    public SmtpEmailNotificationProvider(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends the OTP to the given email address using the configured SMTP server.
     *
     * @param email the recipient email address
     * @param otp the OTP code to send
     * @throws Exception if SMTP credentials are missing or sending fails
     */
    @Override
    public void sendOtp(String email, String otp) throws Exception {
        if (fromAddress == null || fromAddress.isBlank()) {
            throw new IllegalStateException(
                    "SMTP email credentials not configured. Set environment variables: MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD");
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
        helper.setFrom(fromAddress);
        helper.setTo(email);
        helper.setSubject(OtpEmailTemplate.subject());
        helper.setText(OtpEmailTemplate.buildHtml(otp, otpExpiryMinutes), true);

        mailSender.send(message);
        log.info("OTP email dispatched via SMTP to recipient");
    }
}
