package com.nextenti.services.core.dto.auth;

import com.nextenti.services.common.enums.OtpFlow;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ResendOtpRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Flow is required")
    private OtpFlow flow;

    public ResendOtpRequest() {
    }

    public ResendOtpRequest(String email, OtpFlow flow) {
        this.email = email;
        this.flow = flow;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public OtpFlow getFlow() {
        return flow;
    }

    public void setFlow(OtpFlow flow) {
        this.flow = flow;
    }
}
