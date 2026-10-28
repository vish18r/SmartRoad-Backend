package com.smartroad.services.core.dto.maintenance;

import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Request DTO for starting work on a maintenance record (SCHEDULED/DUE/OVERDUE to IN_PROGRESS).
 * Every field is optional; {@code actualStart} defaults to the current time.
 *
 * @author Vishal
 * @version 1.0
 */
public record MaintenanceStartRequestDTO(
        OffsetDateTime actualStart,

        UUID technicianWorkerId,

        @Size(max = 255, message = "{maintenance.technician.name.size}")
        String technicianName,

        @Size(max = 5000, message = "{maintenance.text.size}")
        String notes
) {
}
