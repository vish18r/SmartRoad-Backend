package com.smartroad.services.api.rest.maintenance;

import com.smartroad.services.api.utils.RequestUtil;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.common.util.NextentiConstants;
import com.smartroad.services.core.dto.maintenance.MaintenanceCancelRequestDTO;
import com.smartroad.services.core.dto.maintenance.MaintenanceCompleteRequestDTO;
import com.smartroad.services.core.dto.maintenance.MaintenanceFilterDTO;
import com.smartroad.services.core.dto.maintenance.MaintenancePageResponseDTO;
import com.smartroad.services.core.dto.maintenance.MaintenanceRemindersDTO;
import com.smartroad.services.core.dto.maintenance.MaintenanceRequestDTO;
import com.smartroad.services.core.dto.maintenance.MaintenanceResponseDTO;
import com.smartroad.services.core.dto.maintenance.MaintenanceStartRequestDTO;
import com.smartroad.services.core.dto.maintenance.MaintenanceSummaryDTO;
import com.smartroad.services.core.service.maintenance.MaintenanceService;
import com.smartroad.services.core.service.organization.OrganizationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for maintenance management: preventive/corrective maintenance record CRUD, the
 * start/complete/cancel workflow, per-machine maintenance history, upcoming/overdue lists, the
 * dashboard summary, the calendar feed, and computed reminders. Handles HTTP concerns only,
 * delegating all business logic to {@link MaintenanceService}.
 *
 * @author Vishal
 * @version 1.0
 */
@RestController
public class MaintenanceController {

    private static final Logger logger = LoggerFactory.getLogger(MaintenanceController.class);

    private final MaintenanceService maintenanceService;
    private final OrganizationService organizationService;

    /**
     * Constructs the controller with required service dependencies.
     *
     * @param maintenanceService the maintenance service
     * @param organizationService the organization service, used to resolve the caller's default organization
     */
    public MaintenanceController(MaintenanceService maintenanceService, OrganizationService organizationService) {
        this.maintenanceService = maintenanceService;
        this.organizationService = organizationService;
    }

    /**
     * Creates a maintenance record (and the next occurrence of a recurring preventive record).
     *
     * @param request the creation request
     * @param organizationId the optional organization UUID, resolved from the caller when omitted
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the created {@link MaintenanceResponseDTO}
     * @throws SmartRoadException if the machine/project/technician is not found or validation fails
     */
    @PostMapping(path = "/api/v1/maintenance", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createMaintenance(@RequestBody @Valid MaintenanceRequestDTO request,
                                                    @RequestParam(required = false) UUID organizationId,
                                                    @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside createMaintenance method--");

        UUID userId = RequestUtil.extractUserId();
        MaintenanceResponseDTO response = maintenanceService.create(userId, resolveOrganization(organizationId),
                request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lists a page of maintenance records matching the requested filters.
     *
     * @param organizationId the optional organization UUID, resolved from the caller when omitted
     * @param machineId the optional machine filter
     * @param projectId the optional project/site filter
     * @param status the optional effective status filter (scheduled, due, in_progress, completed, overdue, cancelled)
     * @param priority the optional priority filter
     * @param type the optional maintenance type filter (preventive, corrective)
     * @param dateFrom the optional inclusive scheduled-start date lower bound
     * @param dateTo the optional inclusive scheduled-start date upper bound
     * @param upcoming when true, only scheduled work starting within the next 7 days
     * @param overdue when true, only overdue work
     * @param page the optional one-based page number
     * @param limit the optional page size
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link MaintenancePageResponseDTO}
     * @throws SmartRoadException if a filter value or the date range is invalid
     */
    @GetMapping(path = "/api/v1/maintenance", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listMaintenance(@RequestParam(required = false) UUID organizationId,
                                                  @RequestParam(required = false) UUID machineId,
                                                  @RequestParam(required = false) UUID projectId,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(required = false) String priority,
                                                  @RequestParam(required = false) String type,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                                                  @RequestParam(required = false) Boolean upcoming,
                                                  @RequestParam(required = false) Boolean overdue,
                                                  @RequestParam(required = false) Integer page,
                                                  @RequestParam(required = false) Integer limit,
                                                  @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside listMaintenance method--");

        MaintenanceFilterDTO filter = new MaintenanceFilterDTO(resolveOrganization(organizationId), machineId,
                projectId, status, priority, type, dateFrom, dateTo, Boolean.TRUE.equals(upcoming),
                Boolean.TRUE.equals(overdue), resolvePage(page), resolveLimit(limit));
        MaintenancePageResponseDTO response = maintenanceService.list(filter);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Computes the maintenance dashboard summary.
     *
     * @param organizationId the optional organization UUID, resolved from the caller when omitted
     * @param startDate the optional inclusive period start (defaults to the first day of the current month)
     * @param endDate the optional inclusive period end (defaults to the last day of the current month)
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link MaintenanceSummaryDTO}
     * @throws SmartRoadException if the date range is invalid
     */
    @GetMapping(path = "/api/v1/maintenance/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getSummary(@RequestParam(required = false) UUID organizationId,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                             @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getSummary method--");

        MaintenanceSummaryDTO response = maintenanceService.getSummary(resolveOrganization(organizationId),
                startDate, endDate);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Lists a page of upcoming scheduled maintenance.
     *
     * @param organizationId the optional organization UUID, resolved from the caller when omitted
     * @param days the optional look-ahead window in days (default 7, max 90)
     * @param page the optional one-based page number
     * @param limit the optional page size
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link MaintenancePageResponseDTO}
     * @throws SmartRoadException if the look-ahead window is invalid
     */
    @GetMapping(path = "/api/v1/maintenance/upcoming", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getUpcoming(@RequestParam(required = false) UUID organizationId,
                                              @RequestParam(required = false) Integer days,
                                              @RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer limit,
                                              @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getUpcoming method--");

        MaintenancePageResponseDTO response = maintenanceService.getUpcoming(resolveOrganization(organizationId),
                days, resolvePage(page), resolveLimit(limit));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Lists a page of overdue maintenance.
     *
     * @param organizationId the optional organization UUID, resolved from the caller when omitted
     * @param page the optional one-based page number
     * @param limit the optional page size
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link MaintenancePageResponseDTO}
     * @throws SmartRoadException if the caller's organization cannot be resolved
     */
    @GetMapping(path = "/api/v1/maintenance/overdue", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getOverdue(@RequestParam(required = false) UUID organizationId,
                                             @RequestParam(required = false) Integer page,
                                             @RequestParam(required = false) Integer limit,
                                             @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getOverdue method--");

        MaintenancePageResponseDTO response = maintenanceService.getOverdue(resolveOrganization(organizationId),
                resolvePage(page), resolveLimit(limit));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Lists every maintenance record scheduled to start in a date range, for the calendar view.
     *
     * @param organizationId the optional organization UUID, resolved from the caller when omitted
     * @param startDate the optional inclusive range start (defaults to the first day of the current month)
     * @param endDate the optional inclusive range end (defaults to the last day of the current month)
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the list of {@link MaintenanceResponseDTO}
     * @throws SmartRoadException if the range is invalid or longer than 92 days
     */
    @GetMapping(path = "/api/v1/maintenance/calendar", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getCalendar(@RequestParam(required = false) UUID organizationId,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                              @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getCalendar method--");

        List<MaintenanceResponseDTO> response = maintenanceService.getCalendar(resolveOrganization(organizationId),
                startDate, endDate);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Computes the due and overdue reminders that would need to be sent right now. Nothing is
     * dispatched by this endpoint.
     *
     * @param organizationId the optional organization UUID, resolved from the caller when omitted
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link MaintenanceRemindersDTO}
     * @throws SmartRoadException if the caller's organization cannot be resolved
     */
    @GetMapping(path = "/api/v1/maintenance/reminders", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getReminders(@RequestParam(required = false) UUID organizationId,
                                               @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getReminders method--");

        MaintenanceRemindersDTO response = maintenanceService.getReminders(resolveOrganization(organizationId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves a maintenance record by ID.
     *
     * @param id the record UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link MaintenanceResponseDTO}
     * @throws SmartRoadException if the record is not found
     */
    @GetMapping(path = "/api/v1/maintenance/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getMaintenance(@PathVariable UUID id,
                                                 @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getMaintenance method--");

        MaintenanceResponseDTO response = maintenanceService.getById(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates a maintenance record.
     *
     * @param id the record UUID
     * @param request the update request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link MaintenanceResponseDTO}
     * @throws SmartRoadException if the record is not found, not editable, or validation fails
     */
    @PutMapping(path = "/api/v1/maintenance/{id}", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateMaintenance(@PathVariable UUID id,
                                                    @RequestBody @Valid MaintenanceRequestDTO request,
                                                    @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside updateMaintenance method--");

        UUID userId = RequestUtil.extractUserId();
        MaintenanceResponseDTO response = maintenanceService.update(userId, id, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Deletes a maintenance record by cancelling it (the row is kept for history).
     *
     * @param id the record UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the cancelled {@link MaintenanceResponseDTO}
     * @throws SmartRoadException if the record is not found, completed, or already cancelled
     */
    @DeleteMapping(path = "/api/v1/maintenance/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteMaintenance(@PathVariable UUID id,
                                                    @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside deleteMaintenance method--");

        UUID userId = RequestUtil.extractUserId();
        MaintenanceResponseDTO response = maintenanceService.delete(userId, id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Starts work on a scheduled/due/overdue maintenance record. The body is optional, so no
     * {@code consumes} restriction is declared (a body-less POST must be accepted).
     *
     * @param id the record UUID
     * @param request the optional start request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link MaintenanceResponseDTO}
     * @throws SmartRoadException if the record is not found or not startable
     */
    @PostMapping(path = "/api/v1/maintenance/{id}/start", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> startMaintenance(@PathVariable UUID id,
                                                   @RequestBody(required = false) @Valid MaintenanceStartRequestDTO request,
                                                   @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside startMaintenance method--");

        UUID userId = RequestUtil.extractUserId();
        MaintenanceResponseDTO response = maintenanceService.start(userId, id, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Completes an in-progress maintenance record. The body is optional, so no {@code consumes}
     * restriction is declared.
     *
     * @param id the record UUID
     * @param request the optional completion request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link MaintenanceResponseDTO}
     * @throws SmartRoadException if the record is not found, not in progress, or the end precedes the start
     */
    @PostMapping(path = "/api/v1/maintenance/{id}/complete", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> completeMaintenance(@PathVariable UUID id,
                                                      @RequestBody(required = false) @Valid MaintenanceCompleteRequestDTO request,
                                                      @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside completeMaintenance method--");

        UUID userId = RequestUtil.extractUserId();
        MaintenanceResponseDTO response = maintenanceService.complete(userId, id, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Cancels a maintenance record. The body is optional, so no {@code consumes} restriction is declared.
     *
     * @param id the record UUID
     * @param request the optional cancellation request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link MaintenanceResponseDTO}
     * @throws SmartRoadException if the record is not found or already completed/cancelled
     */
    @PostMapping(path = "/api/v1/maintenance/{id}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> cancelMaintenance(@PathVariable UUID id,
                                                    @RequestBody(required = false) @Valid MaintenanceCancelRequestDTO request,
                                                    @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside cancelMaintenance method--");

        UUID userId = RequestUtil.extractUserId();
        MaintenanceResponseDTO response = maintenanceService.cancel(userId, id, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Lists a page of one machine's maintenance history, newest first.
     *
     * @param machineId the machine UUID
     * @param organizationId the optional organization UUID, resolved from the caller when omitted
     * @param page the optional one-based page number
     * @param limit the optional page size
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link MaintenancePageResponseDTO}
     * @throws SmartRoadException if the machine is not found
     */
    @GetMapping(path = "/api/v1/machines/{machineId}/maintenance", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getMachineMaintenance(@PathVariable UUID machineId,
                                                        @RequestParam(required = false) UUID organizationId,
                                                        @RequestParam(required = false) Integer page,
                                                        @RequestParam(required = false) Integer limit,
                                                        @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getMachineMaintenance method--");

        MaintenancePageResponseDTO response = maintenanceService.getMachineHistory(
                resolveOrganization(organizationId), machineId, resolvePage(page), resolveLimit(limit));

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

    /**
     * Resolves the page number, defaulting to {@link NextentiConstants#DEFAULT_PAGE}.
     *
     * @param page the requested page, possibly null
     * @return the resolved page number
     */
    private int resolvePage(Integer page) {
        return page != null ? page : NextentiConstants.DEFAULT_PAGE;
    }

    /**
     * Resolves the page size, defaulting to {@link NextentiConstants#DEFAULT_LIMIT}.
     *
     * @param limit the requested page size, possibly null
     * @return the resolved page size
     */
    private int resolveLimit(Integer limit) {
        return limit != null ? limit : NextentiConstants.DEFAULT_LIMIT;
    }
}
