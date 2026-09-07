package com.smartroad.services.core.dto.businessprofile;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO containing the complete business profile with all nested entities.
 * Includes business details, contacts, services, and address information.
 */
public record CompleteBusinessProfileResponseDTO(
    UUID id,
    UUID organizationId,
    String businessName,
    String businessType,
    String addressStreet,
    String addressCity,
    String addressPinCode,
    List<BusinessContactResponseDTO> contacts,
    List<BusinessServiceResponseDTO> services
) {}
