package com.smartroad.services.core.dto.businessprofile;

import java.util.UUID;

/**
 * Response DTO for business profile data returned to API consumers.
 */
public record BusinessProfileResponseDTO(
    UUID id,
    UUID organizationId,
    String businessName,
    String businessType,
    String addressStreet,
    String addressCity,
    String addressPinCode
) {}
