package com.nextenti.services.api.rest.workers;

import com.nextenti.services.api.utils.RequestUtil;
import com.nextenti.services.common.exception.SmartRoadException;
import com.nextenti.services.core.dto.workers.WorkerRequestDTO;
import com.nextenti.services.core.dto.workers.WorkerResponseDTO;
import com.nextenti.services.core.service.workers.WorkerService;
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

    /**
     * Constructs the controller with required service dependency.
     *
     * @param workerService the worker service
     */
    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
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
    public ResponseEntity<Object> listWorkers(@RequestParam UUID organizationId,
                                              @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside listWorkers method--");

        UUID userId = RequestUtil.extractUserId();
        List<WorkerResponseDTO> response = workerService.listByOrganization(userId, organizationId);

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
}
