package com.smartroad.services.core.dto.resourceplan;

import com.smartroad.services.common.enums.resourceplan.ResourceTypeEnum;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Request DTO for creating or updating a single resource allocation within a plan.
 *
 * @author Vishal
 * @version 1.0
 */
public record ResourceAllocationRequestDTO(
        @NotNull(message = "{resourceplan.allocation.resource.type.required}")
        ResourceTypeEnum resourceType,

        @NotNull(message = "{resourceplan.allocation.resource.id.required}")
        UUID resourceId,

        LocalTime startTime,

        LocalTime endTime,

        @NotNull(message = "{resourceplan.allocation.start.date.required}")
        LocalDate allocationStartDate,

        LocalDate allocationEndDate,

        BigDecimal plannedQuantity,

        String unit,

        UUID assignedWorkerId,

        String taskDescription,

        String notes
) {
}
