package com.smartroad.services.core.dto.machineutilization;

import com.smartroad.services.core.dto.material.PaginationMetaDTO;

import java.util.List;

/**
 * Response DTO for a paged list of machine usage sessions.
 *
 * @author Vishal
 * @version 1.0
 */
public record MachineUsageSessionPageResponseDTO(
        List<MachineUsageSessionResponseDTO> items,
        PaginationMetaDTO pagination
) {
}
