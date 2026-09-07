package com.smartroad.services.core.dto.businessprofile;

import java.util.UUID;

/**
 * Response DTO for business service data returned to API consumers.
 */
public record BusinessServiceResponseDTO(
    UUID id,
    UUID businessProfileId,
    String serviceName
) {}
