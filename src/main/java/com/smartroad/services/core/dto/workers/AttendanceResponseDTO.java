package com.smartroad.services.core.dto.workers;

import com.smartroad.services.common.enums.workers.AttendanceStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Response DTO for a worker's daily attendance record returned to API consumers.
 *
 * @author Vishal
 * @version 1.0
 */
public record AttendanceResponseDTO(
    UUID id,
    UUID workerId,
    UUID projectId,
    LocalDate attendanceDate,
    AttendanceStatusEnum status,
    BigDecimal hoursWorked,
    String notes
) {}
