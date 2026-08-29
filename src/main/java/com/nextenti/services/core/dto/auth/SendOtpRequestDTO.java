package com.nextenti.services.core.dto.auth;

import com.nextenti.services.common.enums.OtpFlow;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendOtpRequestDTO {

    @Email(message = "Invalid email format")
    private String email;
    private String phoneNumber;
    @NotNull(message = "Flow is required")
    private OtpFlow flow;

}
