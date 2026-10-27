package com.smartroad.services.core.dto.vehicle;

import com.smartroad.services.core.dto.material.PaginationMetaDTO;

import java.util.List;

/**
 * Response DTO for a paged list of vehicles.
 *
 * @author Vishal
 * @version 1.0
 */
public record VehiclePageResponseDTO(
        List<VehicleResponseDTO> items,
        PaginationMetaDTO pagination
) {
}
