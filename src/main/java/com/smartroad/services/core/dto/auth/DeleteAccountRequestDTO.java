package com.smartroad.services.core.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for self-service account deletion.
 * The caller must re-confirm their current password before the account is deactivated.
 *
 * @author Vishal
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAccountRequestDTO {

    @NotBlank(message = "Password is required")
    private String password;
}
