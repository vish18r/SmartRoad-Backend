package com.nextenti.services.core.dto.organization;

import jakarta.validation.constraints.*;

public record OrganizationRequestDTO(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 255) String legalName,
        @Size(max = 32) String gstNumber,
        @Email @Size(max = 255) String email,
        @Size(max = 32) String phoneNumber,
        String address,
        @Size(max = 2048) String logoUrl) { }
