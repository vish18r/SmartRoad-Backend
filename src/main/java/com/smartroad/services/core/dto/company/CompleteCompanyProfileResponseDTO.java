package com.smartroad.services.core.dto.company;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for the complete public company profile, including the
 * founder, additional contacts, and services. This is the shape returned by
 * the public, unauthenticated company-profile API consumed by the frontend
 * landing page.
 */
public record CompleteCompanyProfileResponseDTO(
    UUID id,
    String brandName,
    String businessName,
    String businessType,
    String productName,
    String tagline,
    String phone,
    String address,
    CompanyContactResponseDTO founder,
    List<CompanyContactResponseDTO> additionalContacts,
    List<CompanyServiceResponseDTO> services
) {}
