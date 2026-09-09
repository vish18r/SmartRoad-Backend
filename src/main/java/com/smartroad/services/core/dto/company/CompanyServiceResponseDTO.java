package com.smartroad.services.core.dto.company;

import java.util.UUID;

/**
 * Response DTO for a company service offering returned to API consumers.
 */
public record CompanyServiceResponseDTO(
    UUID id,
    String serviceName,
    Integer displayOrder
) {}
