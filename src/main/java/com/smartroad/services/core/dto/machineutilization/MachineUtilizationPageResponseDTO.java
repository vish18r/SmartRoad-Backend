package com.smartroad.services.core.dto.machineutilization;

import com.smartroad.services.core.dto.material.PaginationMetaDTO;

import java.util.List;

/**
 * Response DTO for a paged list of per-machine utilization rows.
 *
 * @author Vishal
 * @version 1.0
 */
public record MachineUtilizationPageResponseDTO(
        List<MachineUtilizationResponseDTO> items,
        PaginationMetaDTO pagination
) {
}
