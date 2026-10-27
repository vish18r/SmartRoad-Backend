package com.smartroad.services.core.dto.resourceplan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for creating or updating a resource plan's details.
 *
 * @author Vishal
 * @version 1.0
 */
public record ResourcePlanRequestDTO(
        @NotNull(message = "{organization.id.required}")
        UUID organizationId,

        @NotNull(message = "{resourceplan.project.id.required}")
        UUID projectId,

        @NotBlank(message = "{resourceplan.plan.name.required}")
        String planName,

        @NotNull(message = "{resourceplan.plan.date.required}")
        LocalDate planDate,

        LocalDate startDate,

        LocalDate endDate,

        UUID supervisorWorkerId,

        String description,

        String notes
) {
}
