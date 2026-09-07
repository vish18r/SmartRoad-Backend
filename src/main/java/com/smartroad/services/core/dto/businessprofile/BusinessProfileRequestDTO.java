package com.smartroad.services.core.dto.businessprofile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Request DTO for creating or updating a business profile.
 */
public record BusinessProfileRequestDTO(
    @NotNull(message = "{organization.required}")
    UUID organizationId,
    @NotBlank(message = "{business.name.required}")
    @Size(max = 255, message = "{business.name.size}")
    String businessName,
    @Size(max = 100, message = "{business.type.size}")
    String businessType,
    String addressStreet,
    @Size(max = 100, message = "{address.city.size}")
    String addressCity,
    @Size(max = 10, message = "{address.pin.size}")
    String addressPinCode
) {}
