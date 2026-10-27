package com.smartroad.services.core.dto.resourceplan;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for the no-persistence conflict-validation endpoint.
 *
 * @author Vishal
 * @version 1.0
 */
public record ResourcePlanValidateRequestDTO(
        @NotNull(message = "{resourceplan.project.id.required}")
        UUID projectId,

        @NotNull(message = "{resourceplan.plan.date.required}")
        LocalDate planDate,

        UUID excludePlanId,

        @NotEmpty(message = "{resourceplan.allocations.required}")
        @Valid
        List<ResourceAllocationRequestDTO> allocations
) {
}
