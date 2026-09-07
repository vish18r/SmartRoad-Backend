package com.smartroad.services.core.dto.businessprofile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Request DTO for creating or updating a business service.
 */
public record BusinessServiceRequestDTO(
    @NotNull(message = "{business.profile.required}")
    UUID businessProfileId,
    @NotBlank(message = "{service.name.required}")
    @Size(max = 255, message = "{service.name.size}")
    String serviceName
) {}
