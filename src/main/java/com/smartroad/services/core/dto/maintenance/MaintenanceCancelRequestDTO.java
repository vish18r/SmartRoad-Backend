package com.smartroad.services.core.dto.maintenance;

import jakarta.validation.constraints.Size;

/**
 * Optional request DTO for cancelling a maintenance record, carrying the cancellation reason.
 *
 * @author Vishal
 * @version 1.0
 */
public record MaintenanceCancelRequestDTO(
        @Size(max = 1000, message = "{maintenance.cancel.reason.size}")
        String reason
) {
}
