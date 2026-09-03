package com.nextenti.services.core.dto.material;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating/updating materials.
 *
 * @author Vishal
 * @version 1.0
 */
public record MaterialRequestDTO(

    @NotBlank(message = "{material.code.required}")
    String materialCode,

    @NotBlank(message = "{material.name.required}")
    String materialName,

    @NotBlank(message = "{material.unit.required}")
    String unit,

    String category,

    String description
) {}
