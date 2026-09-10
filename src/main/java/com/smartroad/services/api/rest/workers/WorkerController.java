package com.smartroad.services.api.rest.workers;

import com.smartroad.services.api.utils.RequestUtil;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.common.util.NextentiConstants;
import com.smartroad.services.core.dto.workers.AttendanceRequestDTO;
import com.smartroad.services.core.dto.workers.AttendanceResponseDTO;
import com.smartroad.services.core.dto.workers.WorkerRequestDTO;
import com.smartroad.services.core.dto.workers.WorkerResponseDTO;
import com.smartroad.services.core.service.organization.OrganizationService;
import com.smartroad.services.core.service.workers.WorkerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing workers.
 * Handles HTTP requests and delegates all business logic to {@link WorkerService}.
 *
 * @author Vishal
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/workers")
public class WorkerController {

    private static final Logger logger = LoggerFactory.getLogger(WorkerController.class);

    private final WorkerService workerService;
    private final OrganizationService organizationService;

    /**
     * Constructs the controller with required service dependencies.
     *
     * @param workerService the worker service
     * @param organizationService the organization service, used to resolve the caller's organization
     */
    public WorkerController(WorkerService workerService, OrganizationService organizationService) {
        this.workerService = workerService;
        this.organizationService = organizationService;
    }

    /**
     * Creates a new worker.
     *
     * @param request the worker creation request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the created {@link WorkerResponseDTO}
     * @throws SmartRoadException if creation fails
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createWorker(@RequestBody @Valid WorkerRequestDTO request,
                                               @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside createWorker method--");

        UUID userId = RequestUtil.extractUserId();
        WorkerResponseDTO response = workerService.create(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all workers for an organization.
     *
     * @param organizationId the UUID of the organization
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of {@link WorkerResponseDTO}
     * @throws SmartRoadException if retrieval fails
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listWorkers(@RequestParam(required = false) UUID organizationId,
                                              @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside listWorkers method--");

        UUID userId = RequestUtil.extractUserId();
        List<WorkerResponseDTO> response = workerService.listByOrganization(userId, resolveOrganization(organizationId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Searches the organization's workers by name, phone number, or email.
     * The term is read from the x-search header the web client sends, falling back to the
     * query request parameter. The organization is optional and is resolved from the caller
     * when omitted.
     *
     * @param query the optional search term supplied as a query parameter
     * @param searchHeader the optional search term supplied as the x-search header
     * @param organizationId the optional UUID of the organization
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of matching {@link WorkerResponseDTO}
     * @throws SmartRoadException if the caller belongs to no organization or the search fails
     */
    @GetMapping(path = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> searchWorkers(@RequestParam(required = false) String query,
                                                @RequestHeader(name = NextentiConstants.HEADER_SEARCH, required = false) String searchHeader,
                                                @RequestParam(required = false) UUID organizationId,
                                                @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside searchWorkers method--");

        UUID userId = RequestUtil.extractUserId();
        String term = query != null ? query : searchHeader;
        List<WorkerResponseDTO> response = workerService.search(userId, resolveOrganization(organizationId), term);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves workers assigned to a site.
     *
     * @param siteId the UUID of the site
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of {@link WorkerResponseDTO}
     * @throws SmartRoadException if retrieval fails
     */
    @GetMapping(path = "/by-site/{siteId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listWorkersBySite(@PathVariable UUID siteId,
                                                    @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside listWorkersBySite method--");

        List<WorkerResponseDTO> response = workerService.listBySite(siteId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves a worker by ID.
     *
     * @param id the UUID of the worker
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link WorkerResponseDTO}
     * @throws SmartRoadException if worker not found
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getWorker(@PathVariable UUID id,
                                            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getWorker method--");

        WorkerResponseDTO response = workerService.getById(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates a worker.
     *
     * @param id the UUID of the worker
     * @param request the worker update request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link WorkerResponseDTO}
     * @throws SmartRoadException if worker not found or update fails
     */
    @PutMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateWorker(@PathVariable UUID id,
                                               @RequestBody @Valid WorkerRequestDTO request,
                                               @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside updateWorker method--");

        UUID userId = RequestUtil.extractUserId();
        WorkerResponseDTO response = workerService.update(userId, id, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Deletes a worker.
     *
     * @param id the UUID of the worker
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with HTTP 200 OK
     * @throws SmartRoadException if worker not found or delete fails
     */
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteWorker(@PathVariable UUID id,
                                               @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside deleteWorker method--");

        UUID userId = RequestUtil.extractUserId();
        workerService.delete(userId, id);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Marks a worker's attendance for a project on a given day.
     * Re-marking the same worker, project, and date updates the existing record instead of duplicating it.
     *
     * @param request the attendance request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the persisted {@link AttendanceResponseDTO}
     * @throws SmartRoadException if the worker is not found or the caller is not authorized
     */
    @PostMapping(path = "/attendance", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> markAttendance(@RequestBody @Valid AttendanceRequestDTO request,
                                                 @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside markAttendance method--");

        UUID userId = RequestUtil.extractUserId();
        AttendanceResponseDTO response = workerService.markAttendance(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a worker's attendance records, optionally narrowed to a single calendar month.
     * The month is read from the x-month header the web client sends (in yyyy-MM form), falling
     * back to the month request parameter; when neither is present the full history is returned.
     *
     * @param workerId the UUID of the worker
     * @param month the optional month supplied as a query parameter
     * @param monthHeader the optional month supplied as the x-month header
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of {@link AttendanceResponseDTO}
     * @throws SmartRoadException if the worker is not found, the caller is not authorized,
     *                            or the month cannot be parsed
     */
    @GetMapping(path = "/{workerId}/attendance", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listAttendance(@PathVariable UUID workerId,
                                                 @RequestParam(required = false) String month,
                                                 @RequestHeader(name = NextentiConstants.HEADER_MONTH, required = false) String monthHeader,
                                                 @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside listAttendance method--");

        UUID userId = RequestUtil.extractUserId();
        List<AttendanceResponseDTO> response = workerService.listAttendance(userId, workerId,
                month != null ? month : monthHeader);

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
