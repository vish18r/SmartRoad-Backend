package com.nextenti.services.core.dto.auth;

import com.nextenti.services.common.enums.OtpFlow;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public class SendOtpRequest {
    @Email(message = "Invalid email format")
    private String email;
    private String phoneNumber;
    @NotNull(message = "Flow is required")
    private OtpFlow flow;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public OtpFlow getFlow() { return flow; }
    public void setFlow(OtpFlow flow) { this.flow = flow; }
}
