package com.smartroad.services.core.dto.workers;

import com.smartroad.services.common.enums.workers.AttendanceStatusEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for marking a worker's attendance on a project for a single day.
 *
 * @author Vishal
 * @version 1.0
 */
public record AttendanceRequestDTO(
    @NotNull(message = "{worker.id.required}")
    UUID workerId,

    @NotNull(message = "{project.id.required}")
    UUID projectId,

    @NotNull(message = "{attendance.date.required}")
    LocalDate attendanceDate,

    @NotNull(message = "{attendance.status.required}")
    AttendanceStatusEnum status,

    @PositiveOrZero(message = "{hours.worked.invalid}")
    BigDecimal hoursWorked,

    String notes
) {}
