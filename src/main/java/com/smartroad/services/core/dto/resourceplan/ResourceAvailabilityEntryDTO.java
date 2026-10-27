package com.smartroad.services.core.dto.resourceplan;

import java.util.UUID;

/**
 * Describes a single resource's availability for a date/time window.
 *
 * @author Vishal
 * @version 1.0
 */
public record ResourceAvailabilityEntryDTO(
        UUID id,
        String label,
        boolean available,
        String conflictReason
) {
}
