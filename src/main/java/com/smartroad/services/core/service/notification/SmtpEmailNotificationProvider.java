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

    @Value("${otp.mail.from:}")
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
     * Never logs the OTP value or SMTP credentials.
     *
     * @param email the recipient email address
     * @param otp the OTP code to send
     * @throws Exception if SMTP credentials are missing or sending fails
     */
    @Override
    public void sendOtp(String email, String otp) throws Exception {
        if (fromAddress == null || fromAddress.isBlank()) {
            log.error("SMTP send aborted: MAIL_FROM/MAIL_USERNAME is not configured");
            throw new IllegalStateException(
                    "SMTP email credentials not configured. Set environment variables: MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD, MAIL_FROM");
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
        helper.setFrom(fromAddress);
        helper.setTo(email);
        helper.setSubject(OtpEmailTemplate.subject());
        helper.setText(OtpEmailTemplate.buildHtml(otp, otpExpiryMinutes), true);
        log.info("OTP email template rendered, subject and body ready");

        log.info("Dispatching OTP email via SMTP host");
        mailSender.send(message);
        log.info("OTP email accepted by SMTP server for delivery");
    }
}
