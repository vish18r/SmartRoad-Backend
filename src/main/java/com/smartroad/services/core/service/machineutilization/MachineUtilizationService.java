package com.smartroad.services.core.service.machineutilization;

import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.machineutilization.MachineUsageSessionCompleteRequestDTO;
import com.smartroad.services.core.dto.machineutilization.MachineUsageSessionPageResponseDTO;
import com.smartroad.services.core.dto.machineutilization.MachineUsageSessionRequestDTO;
import com.smartroad.services.core.dto.machineutilization.MachineUsageSessionResponseDTO;
import com.smartroad.services.core.dto.machineutilization.MachineUtilizationPageResponseDTO;
import com.smartroad.services.core.dto.machineutilization.MachineUtilizationRequestDTO;
import com.smartroad.services.core.dto.machineutilization.MachineUtilizationResponseDTO;
import com.smartroad.services.core.dto.machineutilization.MachineUtilizationSummaryDTO;
import com.smartroad.services.core.dto.machineutilization.ProjectMachineUtilizationDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service layer contract for machine utilization analytics: organization/project-level KPI
 * summaries, per-machine detail analytics, project-wise usage comparison, and the machine usage
 * session create/complete workflow.
 *
 * @author Vishal
 * @version 1.0
 */
public interface MachineUtilizationService {

    /**
     * Computes organization (optionally project-scoped) utilization KPI summary figures for a
     * reporting period.
     *
     * @param organizationId the organization UUID
     * @param startDate the inclusive start of the reporting period
     * @param endDate the inclusive end of the reporting period
     * @param projectId the optional project filter
     * @return the {@link MachineUtilizationSummaryDTO}
     * @throws SmartRoadException if the date range is invalid
     */
    MachineUtilizationSummaryDTO getSummary(UUID organizationId, LocalDate startDate, LocalDate endDate,
                                            UUID projectId) throws SmartRoadException;

    /**
     * Lists a page of per-machine utilization rows matching the requested filters.
     *
     * @param filter the assembled filter/pagination criteria
     * @return the {@link MachineUtilizationPageResponseDTO}
     * @throws SmartRoadException if the date range is invalid
     */
    MachineUtilizationPageResponseDTO list(MachineUtilizationRequestDTO filter) throws SmartRoadException;

    /**
     * Retrieves detailed utilization analytics for a single machine over a reporting period.
     *
     * @param organizationId the organization UUID
     * @param machineId the machine UUID
     * @param startDate the inclusive start of the reporting period
     * @param endDate the inclusive end of the reporting period
     * @return the {@link MachineUtilizationResponseDTO}
     * @throws SmartRoadException if the machine is not found or the date range is invalid
     */
    MachineUtilizationResponseDTO getMachineDetail(UUID organizationId, UUID machineId, LocalDate startDate,
                                                   LocalDate endDate) throws SmartRoadException;

    /**
     * Builds the project-wise machine usage comparison for an organization over a reporting period.
     *
     * @param organizationId the organization UUID
     * @param startDate the inclusive start of the reporting period
     * @param endDate the inclusive end of the reporting period
     * @return list of {@link ProjectMachineUtilizationDTO} rows, one per project with recorded usage
     * @throws SmartRoadException if the date range is invalid
     */
    List<ProjectMachineUtilizationDTO> getProjectUsage(UUID organizationId, LocalDate startDate, LocalDate endDate)
            throws SmartRoadException;

    /**
     * Lists a page of raw usage sessions for a single machine over a reporting period.
     *
     * @param organizationId the organization UUID
     * @param machineId the machine UUID
     * @param startDate the inclusive start of the reporting period
     * @param endDate the inclusive end of the reporting period
     * @param page the one-based page number
     * @param limit the page size
     * @return the {@link MachineUsageSessionPageResponseDTO}
     * @throws SmartRoadException if the machine is not found or the date range is invalid
     */
    MachineUsageSessionPageResponseDTO getMachineUsageHistory(UUID organizationId, UUID machineId,
            LocalDate startDate, LocalDate endDate, int page, int limit) throws SmartRoadException;

    /**
     * Starts a new machine usage session.
     *
     * @param userId the UUID of the user starting the session
     * @param organizationId the organization UUID
     * @param request the session start request
     * @return the created {@link MachineUsageSessionResponseDTO}
     * @throws SmartRoadException if the machine/project is not found or the session overlaps an existing one
     */
    MachineUsageSessionResponseDTO startSession(UUID userId, UUID organizationId,
            MachineUsageSessionRequestDTO request) throws SmartRoadException;

    /**
     * Completes an open machine usage session.
     *
     * @param userId the UUID of the user completing the session
     * @param sessionId the session UUID
     * @param request the session completion request
     * @return the completed {@link MachineUsageSessionResponseDTO}
     * @throws SmartRoadException if the session is not found, already completed, or the new window overlaps another session
     */
    MachineUsageSessionResponseDTO completeSession(UUID userId, UUID sessionId,
            MachineUsageSessionCompleteRequestDTO request) throws SmartRoadException;
}
