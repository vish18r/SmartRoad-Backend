package com.smartroad.services.core.dto.resourceplan;

import java.util.List;

/**
 * Response DTO for a resource plan together with its resolved allocations.
 *
 * @author Vishal
 * @version 1.0
 */
public record ResourcePlanDetailResponseDTO(
        ResourcePlanResponseDTO plan,
        List<ResourceAllocationResponseDTO> allocations
) {
}
