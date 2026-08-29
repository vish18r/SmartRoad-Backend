package com.nextenti.services.core.dto.organization;

import java.util.UUID;

public record OrganizationResponse(UUID id, String name, String legalName, String gstNumber,
                                   String email, String phoneNumber, String address, String logoUrl,
                                   boolean active) { }
