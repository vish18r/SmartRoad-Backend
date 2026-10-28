package com.smartroad.services.core.dto.machineutilization;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Internal filter criteria for machine utilization queries, assembled by the controller from
 * query parameters (date range, optional project/machine/type/status filters, pagination) and
 * passed down to {@link com.smartroad.services.core.service.machineutilization.MachineUtilizationService}.
 * Not bound directly from a JSON request body — the utilization endpoints are all GETs.
 *
 * @author Vishal
 * @version 1.0
 */
public record MachineUtilizationRequestDTO(
        UUID organizationId,
        LocalDate startDate,
        LocalDate endDate,
        UUID projectId,
        UUID machineId,
        String machineType,
        String status,
        int page,
        int limit
) {
}
