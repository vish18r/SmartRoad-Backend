package com.smartroad.services.core.dto.shift;

import com.smartroad.services.common.enums.shift.ShiftStatusEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.UUID;

/**
 * Request DTO for creating or updating a shift template. The backend always recomputes
 * {@code totalWorkingMinutes} from the start time, end time and break duration; it is never
 * trusted from the client.
 *
 * @param organizationId the organization the shift template belongs to
 * @param shiftCode the unique shift code, e.g. SHIFT-A
 * @param shiftName the display name of the shift
 * @param startTime the shift's start time
 * @param endTime the shift's end time; earlier than startTime means the shift crosses midnight
 * @param breakDurationMinutes the unpaid break duration in minutes
 * @param description an optional free-text description
 * @param status the shift's lifecycle status; defaults to ACTIVE when omitted
 */
public record ShiftRequestDTO(
    @NotNull(message = "{organization.id.required}")
    UUID organizationId,

    @NotBlank(message = "{shift.code.required}")
    String shiftCode,

    @NotBlank(message = "{shift.name.required}")
    String shiftName,

    @NotNull(message = "{shift.start.time.required}")
    LocalTime startTime,

    @NotNull(message = "{shift.end.time.required}")
    LocalTime endTime,

    @NotNull(message = "{shift.break.duration.required}")
    @Min(value = 0, message = "{shift.break.duration.invalid}")
    Integer breakDurationMinutes,

    String description,

    ShiftStatusEnum status
) {}
