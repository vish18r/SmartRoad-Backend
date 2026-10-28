package com.smartroad.services.api.rest.machineutilization;

import com.smartroad.services.api.utils.RequestUtil;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.common.util.NextentiConstants;
import com.smartroad.services.core.dto.machineutilization.MachineUsageSessionCompleteRequestDTO;
import com.smartroad.services.core.dto.machineutilization.MachineUsageSessionPageResponseDTO;
import com.smartroad.services.core.dto.machineutilization.MachineUsageSessionRequestDTO;
import com.smartroad.services.core.dto.machineutilization.MachineUsageSessionResponseDTO;
import com.smartroad.services.core.dto.machineutilization.MachineUtilizationPageResponseDTO;
import com.smartroad.services.core.dto.machineutilization.MachineUtilizationRequestDTO;
import com.smartroad.services.core.dto.machineutilization.MachineUtilizationResponseDTO;
import com.smartroad.services.core.dto.machineutilization.MachineUtilizationSummaryDTO;
import com.smartroad.services.core.dto.machineutilization.ProjectMachineUtilizationDTO;
import com.smartroad.services.core.service.machineutilization.MachineUtilizationService;
import com.smartroad.services.core.service.organization.OrganizationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for machine utilization analytics: KPI summaries, per-machine detail analytics,
 * project-wise usage comparison, and the machine usage session start/complete workflow. Handles
 * HTTP concerns only, delegating all business logic to {@link MachineUtilizationService}.
 *
 * @author Vishal
 * @version 1.0
 */
@RestController
public class MachineUtilizationController {

    private static final Logger logger = LoggerFactory.getLogger(MachineUtilizationController.class);

    private final MachineUtilizationService machineUtilizationService;
    private final OrganizationService organizationService;

    /**
     * Constructs the controller with required service dependencies.
     *
     * @param machineUtilizationService the machine utilization service
     * @param organizationService the organization service, used to resolve the caller's default organization
     */
    public MachineUtilizationController(MachineUtilizationService machineUtilizationService,
                                        OrganizationService organizationService) {
        this.machineUtilizationService = machineUtilizationService;
        this.organizationService = organizationService;
    }

    /**
     * Computes machine utilization KPI summary figures for a reporting period.
     *
     * @param organizationId the optional organization UUID, resolved from the caller when omitted
     * @param startDate the optional inclusive start of the reporting period (defaults to endDate/today)
     * @param endDate the optional inclusive end of the reporting period (defaults to today)
     * @param projectId the optional project filter
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link MachineUtilizationSummaryDTO}
     * @throws SmartRoadException if the date range is invalid
     */
    @GetMapping(path = "/api/v1/machine-utilization/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getSummary(@RequestParam(required = false) UUID organizationId,
                                             @RequestParam(required = false) LocalDate startDate,
                                             @RequestParam(required = false) LocalDate endDate,
                                             @RequestParam(required = false) UUID projectId,
                                             @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getSummary method--");

        MachineUtilizationSummaryDTO response = machineUtilizationService.getSummary(
                resolveOrganization(organizationId), startDate, endDate, projectId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Lists a page of per-machine utilization rows matching the requested filters.
     *
     * @param organizationId the optional organization UUID, resolved from the caller when omitted
     * @param startDate the optional inclusive start of the reporting period
     * @param endDate the optional inclusive end of the reporting period
     * @param projectId the optional project filter
     * @param machineId the optional machine filter
     * @param machineType the optional machine type filter
     * @param status the optional machine status filter
     * @param page the optional one-based page number
     * @param limit the optional page size
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link MachineUtilizationPageResponseDTO}
     * @throws SmartRoadException if the date range is invalid
     */
    @GetMapping(path = "/api/v1/machine-utilization", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listUtilization(@RequestParam(required = false) UUID organizationId,
                                                  @RequestParam(required = false) LocalDate startDate,
                                                  @RequestParam(required = false) LocalDate endDate,
                                                  @RequestParam(required = false) UUID projectId,
                                                  @RequestParam(required = false) UUID machineId,
                                                  @RequestParam(required = false) String machineType,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(required = false) Integer page,
                                                  @RequestParam(required = false) Integer limit,
                                                  @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside listUtilization method--");

        int resolvedPage = page != null ? page : NextentiConstants.DEFAULT_PAGE;
        int resolvedLimit = limit != null ? limit : NextentiConstants.DEFAULT_LIMIT;
        MachineUtilizationRequestDTO filter = new MachineUtilizationRequestDTO(resolveOrganization(organizationId),
                startDate, endDate, projectId, machineId, machineType, status, resolvedPage, resolvedLimit);

        MachineUtilizationPageResponseDTO response = machineUtilizationService.list(filter);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves detailed utilization analytics for a single machine over a reporting period.
     *
     * @param machineId the machine UUID
     * @param organizationId the optional organization UUID, resolved from the caller when omitted
     * @param startDate the optional inclusive start of the reporting period
     * @param endDate the optional inclusive end of the reporting period
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link MachineUtilizationResponseDTO}
     * @throws SmartRoadException if the machine is not found or the date range is invalid
     */
    @GetMapping(path = "/api/v1/machine-utilization/{machineId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getMachineUtilization(@PathVariable UUID machineId,
                                                        @RequestParam(required = false) UUID organizationId,
                                                        @RequestParam(required = false) LocalDate startDate,
                                                        @RequestParam(required = false) LocalDate endDate,
                                                        @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getMachineUtilization method--");

        MachineUtilizationResponseDTO response = machineUtilizationService.getMachineDetail(
                resolveOrganization(organizationId), machineId, startDate, endDate);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Builds the project-wise machine usage comparison for a reporting period.
     *
     * @param organizationId the optional organization UUID, resolved from the caller when omitted
     * @param startDate the optional inclusive start of the reporting period
     * @param endDate the optional inclusive end of the reporting period
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the list of {@link ProjectMachineUtilizationDTO}
     * @throws SmartRoadException if the date range is invalid
     */
    @GetMapping(path = "/api/v1/machine-utilization/projects", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getProjectUsage(@RequestParam(required = false) UUID organizationId,
                                                  @RequestParam(required = false) LocalDate startDate,
                                                  @RequestParam(required = false) LocalDate endDate,
                                                  @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getProjectUsage method--");

        List<ProjectMachineUtilizationDTO> response = machineUtilizationService.getProjectUsage(
                resolveOrganization(organizationId), startDate, endDate);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Lists a page of raw usage sessions for a single machine over a reporting period.
     *
     * @param machineId the machine UUID
     * @param organizationId the optional organization UUID, resolved from the caller when omitted
     * @param startDate the optional inclusive start of the reporting period
     * @param endDate the optional inclusive end of the reporting period
     * @param page the optional one-based page number
     * @param limit the optional page size
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link MachineUsageSessionPageResponseDTO}
     * @throws SmartRoadException if the machine is not found or the date range is invalid
     */
    @GetMapping(path = "/api/v1/machines/{machineId}/usage", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getMachineUsage(@PathVariable UUID machineId,
                                                  @RequestParam(required = false) UUID organizationId,
                                                  @RequestParam(required = false) LocalDate startDate,
                                                  @RequestParam(required = false) LocalDate endDate,
                                                  @RequestParam(required = false) Integer page,
                                                  @RequestParam(required = false) Integer limit,
                                                  @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getMachineUsage method--");

        int resolvedPage = page != null ? page : NextentiConstants.DEFAULT_PAGE;
        int resolvedLimit = limit != null ? limit : NextentiConstants.DEFAULT_LIMIT;
        MachineUsageSessionPageResponseDTO response = machineUtilizationService.getMachineUsageHistory(
                resolveOrganization(organizationId), machineId, startDate, endDate, resolvedPage, resolvedLimit);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Starts a new machine usage session.
     *
     * @param request the session start request
     * @param organizationId the optional organization UUID, resolved from the caller when omitted
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the created {@link MachineUsageSessionResponseDTO}
     * @throws SmartRoadException if the machine/project is not found or the session overlaps an existing one
     */
    @PostMapping(path = "/api/v1/machine-utilization/sessions/start", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> startSession(@RequestBody @Valid MachineUsageSessionRequestDTO request,
                                               @RequestParam(required = false) UUID organizationId,
                                               @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside startSession method--");

        UUID userId = RequestUtil.extractUserId();
        MachineUsageSessionResponseDTO response = machineUtilizationService.startSession(userId,
                resolveOrganization(organizationId), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Completes an open machine usage session.
     *
     * @param sessionId the session UUID
     * @param request the session completion request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the completed {@link MachineUsageSessionResponseDTO}
     * @throws SmartRoadException if the session is not found, already completed, or the new window overlaps another session
     */
    @PostMapping(path = "/api/v1/machine-utilization/sessions/{sessionId}/complete",
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> completeSession(@PathVariable UUID sessionId,
                                                  @RequestBody @Valid MachineUsageSessionCompleteRequestDTO request,
                                                  @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside completeSession method--");

        UUID userId = RequestUtil.extractUserId();
        MachineUsageSessionResponseDTO response = machineUtilizationService.completeSession(userId, sessionId, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Returns the supplied organization, or resolves the caller's own organization when none was supplied.
     *
     * @param organizationId the organization UUID from the request, possibly null
     * @return the organization UUID to operate on
     * @throws SmartRoadException if no organization was supplied and the caller belongs to none
     */
    private UUID resolveOrganization(UUID organizationId) throws SmartRoadException {
        if (organizationId != null) {
            return organizationId;
        }
        return organizationService.resolveDefaultOrganizationId(RequestUtil.extractUserId());
    }
}
