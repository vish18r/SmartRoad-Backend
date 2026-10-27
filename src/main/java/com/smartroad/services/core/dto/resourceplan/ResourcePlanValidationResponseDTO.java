package com.smartroad.services.core.dto.resourceplan;

import java.util.List;

/**
 * Response DTO reporting the conflicts found while validating or confirming a resource plan.
 *
 * @author Vishal
 * @version 1.0
 */
public record ResourcePlanValidationResponseDTO(
        List<ResourceConflictDTO> conflicts,
        boolean hasBlockingConflicts
) {
}
