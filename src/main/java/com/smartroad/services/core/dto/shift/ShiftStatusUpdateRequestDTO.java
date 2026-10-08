package com.smartroad.services.core.dto.shift;

import com.smartroad.services.common.enums.shift.ShiftStatusEnum;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for patching a shift template's lifecycle status.
 *
 * @param status the status to move the shift template to
 */
public record ShiftStatusUpdateRequestDTO(
    @NotNull(message = "{status.required}")
    ShiftStatusEnum status
) {}
