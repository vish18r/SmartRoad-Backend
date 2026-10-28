package com.smartroad.services.core.dto.maintenance;

import com.smartroad.services.common.enums.maintenance.MaintenanceCategoryEnum;
import com.smartroad.services.common.enums.maintenance.MaintenancePriorityEnum;
import com.smartroad.services.common.enums.maintenance.MaintenanceRecurrenceEnum;
import com.smartroad.services.common.enums.maintenance.MaintenanceTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Request DTO for creating or updating a maintenance record. Used by both POST (create) and PUT
 * (update). Cross-field rules (scheduled end after start, category belongs to type, recurrence only
 * for preventive work, terminal-record edit restrictions) are enforced in the service layer.
 *
 * @author Vishal
 * @version 1.0
 */
public record MaintenanceRequestDTO(
        @NotNull(message = "{machine.id.required}")
        UUID machineId,

        UUID projectId,

        @NotBlank(message = "{maintenance.title.required}")
        @Size(max = 200, message = "{maintenance.title.size}")
        String title,

        @NotNull(message = "{maintenance.type.required}")
        MaintenanceTypeEnum maintenanceType,

        MaintenanceCategoryEnum category,

        @Size(max = 5000, message = "{maintenance.text.size}")
        String description,

        MaintenancePriorityEnum priority,

        @NotNull(message = "{maintenance.scheduled.start.required}")
        OffsetDateTime scheduledStart,

        OffsetDateTime scheduledEnd,

        OffsetDateTime reportedAt,

        UUID technicianWorkerId,

        @Size(max = 255, message = "{maintenance.technician.name.size}")
        String technicianName,

        @PositiveOrZero(message = "{maintenance.cost.nonnegative}")
        BigDecimal estimatedCost,

        @PositiveOrZero(message = "{maintenance.cost.nonnegative}")
        BigDecimal actualCost,

        @Size(max = 5000, message = "{maintenance.text.size}")
        String partsUsed,

        @Size(max = 5000, message = "{maintenance.text.size}")
        String notes,

        MaintenanceRecurrenceEnum recurrenceType
) {
}
