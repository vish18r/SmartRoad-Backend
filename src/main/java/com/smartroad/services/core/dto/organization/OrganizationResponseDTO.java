package com.smartroad.services.core.dto.organization;

import java.util.UUID;

public record OrganizationResponseDTO(UUID id, String name, String legalName, String gstNumber,
                                   String email, String phoneNumber, String address, String logoUrl,
                                   boolean active) { }
