package com.smartroad.services.core.dto.maintenance;

import com.smartroad.services.core.dto.material.PaginationMetaDTO;

import java.util.List;

/**
 * Response DTO for a paged list of maintenance records.
 *
 * @author Vishal
 * @version 1.0
 */
public record MaintenancePageResponseDTO(
        List<MaintenanceResponseDTO> items,
        PaginationMetaDTO pagination
) {
}
