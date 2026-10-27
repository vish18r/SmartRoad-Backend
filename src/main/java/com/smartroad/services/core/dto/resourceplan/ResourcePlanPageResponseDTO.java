package com.smartroad.services.core.dto.resourceplan;

import com.smartroad.services.core.dto.material.PaginationMetaDTO;

import java.util.List;

/**
 * Response DTO for a paged list of resource plans.
 *
 * @author Vishal
 * @version 1.0
 */
public record ResourcePlanPageResponseDTO(
        List<ResourcePlanResponseDTO> items,
        PaginationMetaDTO pagination
) {
}
