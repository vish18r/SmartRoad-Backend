package com.smartroad.services.core.dto.businessprofile;

import com.smartroad.services.common.enums.businessprofile.ContactRoleEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Request DTO for creating or updating a business contact.
 */
public record BusinessContactRequestDTO(
    @NotNull(message = "{business.profile.required}")
    UUID businessProfileId,
    @NotBlank(message = "{contact.name.required}")
    @Size(max = 255, message = "{contact.name.size}")
    String contactName,
    @NotNull(message = "{contact.role.required}")
    ContactRoleEnum contactRole,
    @Size(max = 20, message = "{phone.size}")
    String phoneNumber1,
    @Size(max = 20, message = "{phone.size}")
    String phoneNumber2
) {}
