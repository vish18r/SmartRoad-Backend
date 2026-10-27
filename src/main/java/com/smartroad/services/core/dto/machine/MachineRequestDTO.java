package com.smartroad.services.core.dto.machine;

import com.smartroad.services.common.enums.machine.MachineStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for creating or updating a machine.
 *
 * @author Vishal
 * @version 1.0
 */
public record MachineRequestDTO(
        @NotNull(message = "{organization.id.required}")
        UUID organizationId,

        @NotBlank(message = "{machine.name.required}")
        String name,

        String type,

        MachineStatusEnum status,

        String registrationNumber,

        LocalDate purchaseDate,

        BigDecimal purchaseCost,

        UUID currentSiteId,

        BigDecimal currentValue,

        BigDecimal workingHours,

        String maintenanceStatus,

        String fuelType,

        String capacity,

        String operatorName
) {
}
