package com.smartroad.services.core.dto.vehicle;

import com.smartroad.services.common.enums.vehicle.VehicleStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO for creating or updating a vehicle.
 *
 * @author Vishal
 * @version 1.0
 */
public record VehicleRequestDTO(
        @NotNull(message = "{organization.id.required}")
        UUID organizationId,

        @NotBlank(message = "{vehicle.registration.number.required}")
        String registrationNumber,

        String vehicleType,

        VehicleStatusEnum status,

        UUID currentSiteId,

        UUID driverWorkerId,

        String capacity,

        String notes
) {
}
