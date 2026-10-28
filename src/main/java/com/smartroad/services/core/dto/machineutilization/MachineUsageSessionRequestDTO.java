package com.smartroad.services.core.dto.machineutilization;

import com.smartroad.services.common.enums.machineutilization.UsageSourceEnum;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Request DTO for starting a new machine usage session.
 *
 * @author Vishal
 * @version 1.0
 */
public record MachineUsageSessionRequestDTO(
        @NotNull(message = "{machine.id.required}")
        UUID machineId,

        UUID projectId,

        @NotNull(message = "{machineutilization.start.time.required}")
        OffsetDateTime startTime,

        UUID operatorWorkerId,

        String operatorName,

        UsageSourceEnum source,

        String notes
) {
}
