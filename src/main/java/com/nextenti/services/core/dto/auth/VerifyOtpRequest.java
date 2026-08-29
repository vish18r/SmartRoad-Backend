package com.nextenti.services.core.dto.auth;

import com.nextenti.services.common.enums.OtpFlow;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VerifyOtpRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "OTP is required")
    @Size(min = 6, max = 6, message = "OTP must be 6 digits")
    private String otp;

    @NotNull(message = "Flow is required")
    private OtpFlow flow;

    public VerifyOtpRequest() {
    }

    public VerifyOtpRequest(String email, String otp, OtpFlow flow) {
        this.email = email;
        this.otp = otp;
        this.flow = flow;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public OtpFlow getFlow() {
        return flow;
    }

    public void setFlow(OtpFlow flow) {
        this.flow = flow;
    }
}
