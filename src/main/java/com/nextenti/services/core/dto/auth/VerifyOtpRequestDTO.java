package com.nextenti.services.core.dto.auth;

import com.nextenti.services.common.enums.OtpFlow;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequestDTO {

    @Email(message = "Invalid email format")
    private String email;
    @Size(min = 6, max = 6, message = "OTP must be 6 digits")
    private String otp;
    @NotNull(message = "Flow is required")
    private OtpFlow flow;

}
