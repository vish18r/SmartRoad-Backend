package com.smartroad.services.core.dto.company;

import com.smartroad.services.common.enums.businessprofile.ContactRoleEnum;

import java.util.UUID;

/**
 * Response DTO for a company contact (founder or additional contact) returned
 * to API consumers.
 */
public record CompanyContactResponseDTO(
    UUID id,
    String contactName,
    ContactRoleEnum contactRole,
    String phoneNumber1,
    String phoneNumber2
) {}
