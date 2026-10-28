package com.smartroad.services.core.dto.maintenance;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Request DTO for completing an in-progress maintenance record. {@code actualEnd} defaults to the
 * current time; downtime is always computed server-side from the actual start/end timestamps and is
 * therefore not accepted from the client.
 *
 * @author Vishal
 * @version 1.0
 */
public record MaintenanceCompleteRequestDTO(
        OffsetDateTime actualEnd,

        @PositiveOrZero(message = "{maintenance.cost.nonnegative}")
        BigDecimal actualCost,

        @Size(max = 5000, message = "{maintenance.text.size}")
        String partsUsed,

        @Size(max = 5000, message = "{maintenance.text.size}")
        String notes
) {
}
