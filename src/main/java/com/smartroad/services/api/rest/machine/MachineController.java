package com.smartroad.services.api.rest.machine;

import com.smartroad.services.api.utils.RequestUtil;
import com.smartroad.services.common.enums.machine.MachineStatusEnum;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.common.util.NextentiConstants;
import com.smartroad.services.core.dto.machine.MachinePageResponseDTO;
import com.smartroad.services.core.dto.machine.MachineRequestDTO;
import com.smartroad.services.core.dto.machine.MachineResponseDTO;
import com.smartroad.services.core.service.machine.MachineService;
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

import java.util.UUID;

/**
 * REST controller for machine/plant asset management.
 * Handles HTTP requests for machine CRUD operations, delegating all business logic to {@link MachineService}.
 *
 * @author Vishal
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/machines")
public class MachineController {

    private static final Logger logger = LoggerFactory.getLogger(MachineController.class);

    private final MachineService machineService;

    /**
     * Constructs the controller with required service dependency.
     *
     * @param machineService the machine service
     */
    public MachineController(MachineService machineService) {
        this.machineService = machineService;
    }

    /**
     * Creates a new machine.
     *
     * @param request the machine creation request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the created {@link MachineResponseDTO}
     * @throws SmartRoadException if creation fails
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createMachine(@RequestBody @Valid MachineRequestDTO request,
                                                @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside createMachine method--");

        UUID userId = RequestUtil.extractUserId();
        MachineResponseDTO response = machineService.create(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lists a page of an organization's machines, optionally filtered by status and current site.
     *
     * @param organizationId the organization UUID
     * @param status the optional status filter
     * @param currentSiteId the optional current site filter
     * @param page the optional one-based page number
     * @param limit the optional page size
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link MachinePageResponseDTO}
     * @throws SmartRoadException if retrieval fails
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listMachines(@RequestParam UUID organizationId,
                                               @RequestParam(required = false) MachineStatusEnum status,
                                               @RequestParam(required = false) UUID currentSiteId,
                                               @RequestParam(required = false) Integer page,
                                               @RequestParam(required = false) Integer limit,
                                               @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside listMachines method--");

        int resolvedPage = page != null ? page : NextentiConstants.DEFAULT_PAGE;
        int resolvedLimit = limit != null ? limit : NextentiConstants.DEFAULT_LIMIT;
        MachinePageResponseDTO response = machineService.list(organizationId, status, currentSiteId,
                resolvedPage, resolvedLimit);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves a machine by ID.
     *
     * @param id the machine UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link MachineResponseDTO}
     * @throws SmartRoadException if the machine is not found
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getMachine(@PathVariable UUID id,
                                             @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getMachine method--");

        MachineResponseDTO response = machineService.getById(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates a machine.
     *
     * @param id the machine UUID
     * @param request the machine update request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link MachineResponseDTO}
     * @throws SmartRoadException if update fails
     */
    @PutMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateMachine(@PathVariable UUID id,
                                                @RequestBody @Valid MachineRequestDTO request,
                                                @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside updateMachine method--");

        UUID userId = RequestUtil.extractUserId();
        MachineResponseDTO response = machineService.update(userId, id, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Soft-deletes a machine.
     *
     * @param id the machine UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with no content
     * @throws SmartRoadException if delete fails
     */
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMachine(@PathVariable UUID id,
                                              @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside deleteMachine method--");

        UUID userId = RequestUtil.extractUserId();
        machineService.delete(userId, id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
