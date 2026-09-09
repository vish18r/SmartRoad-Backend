package com.smartroad.services.core.dto.company;

import java.util.UUID;

/**
 * Response DTO for the company profile's core fields, without nested
 * contacts or services. See {@link CompleteCompanyProfileResponseDTO} for the
 * full public-facing shape.
 */
public record CompanyProfileResponseDTO(
    UUID id,
    String brandName,
    String businessName,
    String businessType,
    String productName,
    String tagline,
    String phone,
    String address
) {}
