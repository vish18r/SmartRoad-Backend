package com.smartroad.services.core.service.notification;

/**
 * Builds the subject and HTML body for OTP delivery emails.
 * Centralizes the OTP email markup so every {@link EmailNotificationProvider}
 * implementation renders a consistent, non-hardcoded message with the OTP
 * and expiry duration injected at send time.
 *
 * @author Vishal
 * @version 1.0
 */
public final class OtpEmailTemplate {

    private static final String SUBJECT = "Your SmartRoad verification code";

    private OtpEmailTemplate() {
    }

    /**
     * Returns the subject line used for OTP delivery emails.
     *
     * @return the email subject
     */
    public static String subject() {
        return SUBJECT;
    }

    /**
     * Builds the HTML body for an OTP delivery email.
     *
     * @param otp the generated OTP code to embed in the email
     * @param expiryMinutes the number of minutes before the OTP expires
     * @return the rendered HTML email body
     */
    public static String buildHtml(String otp, int expiryMinutes) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<body style=\"margin:0;padding:0;background-color:#f4f5f7;font-family:Arial,Helvetica,sans-serif;\">"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"padding:24px 0;\">"
                + "<tr><td align=\"center\">"
                + "<table role=\"presentation\" width=\"480\" cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"background-color:#ffffff;border-radius:8px;padding:32px;\">"
                + "<tr><td>"
                + "<h2 style=\"margin:0 0 16px 0;color:#1f2937;\">SmartRoad Verification Code</h2>"
                + "<p style=\"margin:0 0 24px 0;color:#4b5563;font-size:14px;\">"
                + "Use the code below to complete your verification. This code is valid for "
                + expiryMinutes + " minute" + (expiryMinutes == 1 ? "" : "s") + "."
                + "</p>"
                + "<div style=\"text-align:center;margin:0 0 24px 0;\">"
                + "<span style=\"display:inline-block;padding:12px 24px;font-size:28px;letter-spacing:6px;"
                + "font-weight:bold;color:#111827;background-color:#f3f4f6;border-radius:6px;\">"
                + otp
                + "</span>"
                + "</div>"
                + "<p style=\"margin:0;color:#9ca3af;font-size:12px;\">"
                + "If you did not request this code, you can safely ignore this email."
                + "</p>"
                + "</td></tr>"
                + "</table>"
                + "</td></tr>"
                + "</table>"
                + "</body>"
                + "</html>";
    }
}
