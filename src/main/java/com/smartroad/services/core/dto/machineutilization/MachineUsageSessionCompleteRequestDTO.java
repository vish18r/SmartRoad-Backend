package com.smartroad.services.core.dto.machineutilization;

import com.smartroad.services.common.enums.machineutilization.UsageStatusEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Request DTO for completing an open machine usage session.
 *
 * @author Vishal
 * @version 1.0
 */
public record MachineUsageSessionCompleteRequestDTO(
        @NotNull(message = "{machineutilization.end.time.required}")
        OffsetDateTime endTime,

        @DecimalMin(value = "0", message = "{machineutilization.idle.hours.nonnegative}")
        BigDecimal idleHours,

        UsageStatusEnum status,

        @DecimalMin(value = "0", message = "{machineutilization.fuel.consumed.nonnegative}")
        BigDecimal fuelConsumed,

        @DecimalMin(value = "0", message = "{machineutilization.fuel.cost.nonnegative}")
        BigDecimal fuelCost,

        String notes
) {
}
