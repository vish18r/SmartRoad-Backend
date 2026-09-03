package com.smartroad.services.core.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequestDTO {

    @Email(message = "Invalid email format")
    private String email;
    @Size(min = 6, max = 6, message = "OTP must be 6 digits")
    private String otp;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Password must contain at least one uppercase, one lowercase, one number, and one special character")
    private String newPassword;
    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;

}
