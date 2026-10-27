package com.smartroad.services.core.dto.machine;

import com.smartroad.services.core.dto.material.PaginationMetaDTO;

import java.util.List;

/**
 * Response DTO for a paged list of machines.
 *
 * @author Vishal
 * @version 1.0
 */
public record MachinePageResponseDTO(
        List<MachineResponseDTO> items,
        PaginationMetaDTO pagination
) {
}
