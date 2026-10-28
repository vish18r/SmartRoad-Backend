package com.smartroad.services.core.dto.maintenance;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Filter and pagination criteria for listing maintenance records, assembled by the controller from
 * query parameters. {@code status}, {@code priority} and {@code type} are raw strings parsed
 * case-insensitively (by enum name or wire value) in the service so an unknown value yields a clear
 * 400 instead of a type-conversion failure.
 *
 * @author Vishal
 * @version 1.0
 */
public record MaintenanceFilterDTO(
        UUID organizationId,
        UUID machineId,
        UUID projectId,
        String status,
        String priority,
        String type,
        LocalDate dateFrom,
        LocalDate dateTo,
        boolean upcoming,
        boolean overdue,
        int page,
        int limit
) {
}
