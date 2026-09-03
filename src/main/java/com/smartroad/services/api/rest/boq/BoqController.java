package com.smartroad.services.api.rest.boq;

import com.smartroad.services.api.utils.RequestUtil;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.boq.BoqItemRequestDTO;
import com.smartroad.services.core.dto.boq.BoqItemResponseDTO;
import com.smartroad.services.core.dto.boq.BoqRequestDTO;
import com.smartroad.services.core.dto.boq.BoqResponseDTO;
import com.smartroad.services.core.service.boq.BoqService;
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
     * @return {@link ResponseEntity} containing the created {@link BoqResponseDTO}
     * @throws SmartRoadException if project not found or creation fails
     */
    @PostMapping(path = "/projects/{projectId}/boq", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createBoq(@PathVariable UUID projectId,
                                            @RequestBody @Valid BoqRequestDTO request,
                                            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside createBoq method--");

        UUID userId = RequestUtil.extractUserId();
        BoqResponseDTO response = boqService.create(userId, projectId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all BOQs for a project.
     *
     * @param projectId the UUID of the project
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of {@link BoqResponseDTO}
     * @throws SmartRoadException if project not found or retrieval fails
     */
    @GetMapping(path = "/projects/{projectId}/boq", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listBoqs(@PathVariable UUID projectId,
                                           @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside listBoqs method--");

        UUID userId = RequestUtil.extractUserId();
        List<BoqResponseDTO> response = boqService.list(userId, projectId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Adds an item to a BOQ.
     *
     * @param boqId the UUID of the BOQ
     * @param request the BOQ item creation request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the created {@link BoqItemResponseDTO}
     * @throws SmartRoadException if BOQ not found or creation fails
     */
    @PostMapping(path = "/boq/{boqId}/items", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> addBoqItem(@PathVariable UUID boqId,
                                             @RequestBody @Valid BoqItemRequestDTO request,
                                             @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside addBoqItem method--");

        UUID userId = RequestUtil.extractUserId();
        BoqItemResponseDTO response = boqService.addItem(userId, boqId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all items for a BOQ.
     *
     * @param boqId the UUID of the BOQ
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of {@link BoqItemResponseDTO}
     * @throws SmartRoadException if BOQ not found or retrieval fails
     */
    @GetMapping(path = "/boq/{boqId}/items", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listBoqItems(@PathVariable UUID boqId,
                                               @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside listBoqItems method--");

        UUID userId = RequestUtil.extractUserId();
        List<BoqItemResponseDTO> response = boqService.listItems(userId, boqId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
