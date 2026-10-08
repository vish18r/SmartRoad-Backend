package com.smartroad.services.core.dto.shift;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.smartroad.services.common.enums.shift.ShiftStatusEnum;

import java.time.LocalTime;
import java.util.UUID;

/**
 * Response DTO for a shift template.
 *
 * @param id the shift template UUID
 * @param organizationId the organization that owns the shift template
 * @param shiftCode the unique shift code
 * @param shiftName the display name of the shift
 * @param startTime the shift's start time
 * @param endTime the shift's end time
 * @param breakDurationMinutes the unpaid break duration in minutes
 * @param totalWorkingMinutes the backend-computed net working minutes, overnight-aware
 * @param description the free-text description, or null
 * @param status the shift's lifecycle status
 * @param assignedWorkersCount the count of workers currently ACTIVE-assigned to this shift,
 *                             populated on list and detail reads; null where not computed
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ShiftResponseDTO(
    UUID id,
    UUID organizationId,
    String shiftCode,
    String shiftName,
    LocalTime startTime,
    LocalTime endTime,
    Integer breakDurationMinutes,
    Integer totalWorkingMinutes,
    String description,
    ShiftStatusEnum status,
    Long assignedWorkersCount
) {}
