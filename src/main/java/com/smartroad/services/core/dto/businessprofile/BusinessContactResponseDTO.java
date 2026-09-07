package com.smartroad.services.core.dto.businessprofile;

import com.smartroad.services.common.enums.businessprofile.ContactRoleEnum;

import java.util.UUID;

/**
 * Response DTO for business contact data returned to API consumers.
 */
public record BusinessContactResponseDTO(
    UUID id,
    UUID businessProfileId,
    String contactName,
    ContactRoleEnum contactRole,
    String phoneNumber1,
    String phoneNumber2
) {}
