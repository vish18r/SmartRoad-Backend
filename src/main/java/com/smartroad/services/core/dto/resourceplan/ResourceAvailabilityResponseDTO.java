package com.smartroad.services.core.dto.resourceplan;

import java.util.List;

/**
 * Response DTO listing each resource type's availability for a project/date/time window.
 * A resource-type list is populated only when that type was requested (or no filter was given).
 *
 * @author Vishal
 * @version 1.0
 */
public record ResourceAvailabilityResponseDTO(
        List<ResourceAvailabilityEntryDTO> workers,
        List<ResourceAvailabilityEntryDTO> machines,
        List<ResourceAvailabilityEntryDTO> materials,
        List<ResourceAvailabilityEntryDTO> vehicles
) {
}
