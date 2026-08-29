package com.nextenti.services.api.rest.boq;

import com.nextenti.services.common.exception.NextentiException;
import com.nextenti.services.core.dto.boq.BoqItemRequest;
import com.nextenti.services.core.dto.boq.BoqItemResponse;
import com.nextenti.services.core.dto.boq.BoqRequest;
import com.nextenti.services.core.dto.boq.BoqResponse;
import com.nextenti.services.core.service.boq.BoqService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Bill of Quantities (BOQ) management.
 * Handles HTTP requests for BOQ and BOQ items, delegating all business logic to {@link BoqService}.
 *
 * @author Vishal
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1")
public class BoqController {

    private static final Logger logger = LoggerFactory.getLogger(BoqController.class);

    private final BoqService boqService;

    /**
     * Constructs the controller with required service dependency.
     *
     * @param boqService the BOQ service
     */
    public BoqController(BoqService boqService) {
        this.boqService = boqService;
    }

    /**
     * Creates a new BOQ for a project.
     *
     * @param projectId the UUID of the project
     * @param request the BOQ creation request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the created {@link BoqResponse}
     * @throws NextentiException if project not found or creation fails
     */
    @PostMapping(path = "/projects/{projectId}/boq", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createBoq(@PathVariable UUID projectId,
                                            @RequestBody @Valid BoqRequest request,
                                            @RequestHeader HttpHeaders headers) throws NextentiException {
        logger.info("--Inside createBoq method--");

        BoqResponse response = boqService.create(projectId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all BOQs for a project.
     *
     * @param projectId the UUID of the project
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of {@link BoqResponse}
     * @throws NextentiException if project not found or retrieval fails
     */
    @GetMapping(path = "/projects/{projectId}/boq", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listBoqs(@PathVariable UUID projectId,
                                           @RequestHeader HttpHeaders headers) throws NextentiException {
        logger.info("--Inside listBoqs method--");

        List<BoqResponse> response = boqService.list(projectId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Adds an item to a BOQ.
     *
     * @param boqId the UUID of the BOQ
     * @param request the BOQ item creation request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the created {@link BoqItemResponse}
     * @throws NextentiException if BOQ not found or creation fails
     */
    @PostMapping(path = "/boq/{boqId}/items", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> addBoqItem(@PathVariable UUID boqId,
                                             @RequestBody @Valid BoqItemRequest request,
                                             @RequestHeader HttpHeaders headers) throws NextentiException {
        logger.info("--Inside addBoqItem method--");

        BoqItemResponse response = boqService.addItem(boqId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all items for a BOQ.
     *
     * @param boqId the UUID of the BOQ
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of {@link BoqItemResponse}
     * @throws NextentiException if BOQ not found or retrieval fails
     */
    @GetMapping(path = "/boq/{boqId}/items", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listBoqItems(@PathVariable UUID boqId,
                                               @RequestHeader HttpHeaders headers) throws NextentiException {
        logger.info("--Inside listBoqItems method--");

        List<BoqItemResponse> response = boqService.listItems(boqId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
