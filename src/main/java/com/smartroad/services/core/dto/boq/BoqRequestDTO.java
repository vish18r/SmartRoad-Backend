package com.smartroad.services.core.dto.boq;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating a Bill of Quantities (BoQ).
 */
public record BoqRequestDTO(
    @NotBlank(message = "BoQ name is required")
    String name,
    String description
) {}
