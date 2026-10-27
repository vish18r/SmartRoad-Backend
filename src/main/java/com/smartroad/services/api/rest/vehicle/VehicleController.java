package com.smartroad.services.api.rest.vehicle;

import com.smartroad.services.api.utils.RequestUtil;
import com.smartroad.services.common.enums.vehicle.VehicleStatusEnum;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.common.util.NextentiConstants;
import com.smartroad.services.core.dto.vehicle.VehiclePageResponseDTO;
import com.smartroad.services.core.dto.vehicle.VehicleRequestDTO;
import com.smartroad.services.core.dto.vehicle.VehicleResponseDTO;
import com.smartroad.services.core.service.vehicle.VehicleService;
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
 * REST controller for vehicle/fleet management.
 * Handles HTTP requests for vehicle CRUD operations, delegating all business logic to {@link VehicleService}.
 *
 * @author Vishal
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

    private static final Logger logger = LoggerFactory.getLogger(VehicleController.class);

    private final VehicleService vehicleService;

    /**
     * Constructs the controller with required service dependency.
     *
     * @param vehicleService the vehicle service
     */
    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    /**
     * Creates a new vehicle.
     *
     * @param request the vehicle creation request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the created {@link VehicleResponseDTO}
     * @throws SmartRoadException if creation fails
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createVehicle(@RequestBody @Valid VehicleRequestDTO request,
                                                @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside createVehicle method--");

        UUID userId = RequestUtil.extractUserId();
        VehicleResponseDTO response = vehicleService.create(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lists a page of an organization's vehicles, optionally filtered by status and current site.
     *
     * @param organizationId the organization UUID
     * @param status the optional status filter
     * @param currentSiteId the optional current site filter
     * @param page the optional one-based page number
     * @param limit the optional page size
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link VehiclePageResponseDTO}
     * @throws SmartRoadException if retrieval fails
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listVehicles(@RequestParam UUID organizationId,
                                               @RequestParam(required = false) VehicleStatusEnum status,
                                               @RequestParam(required = false) UUID currentSiteId,
                                               @RequestParam(required = false) Integer page,
                                               @RequestParam(required = false) Integer limit,
                                               @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside listVehicles method--");

        int resolvedPage = page != null ? page : NextentiConstants.DEFAULT_PAGE;
        int resolvedLimit = limit != null ? limit : NextentiConstants.DEFAULT_LIMIT;
        VehiclePageResponseDTO response = vehicleService.list(organizationId, status, currentSiteId,
                resolvedPage, resolvedLimit);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves a vehicle by ID.
     *
     * @param id the vehicle UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link VehicleResponseDTO}
     * @throws SmartRoadException if the vehicle is not found
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getVehicle(@PathVariable UUID id,
                                             @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getVehicle method--");

        VehicleResponseDTO response = vehicleService.getById(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates a vehicle.
     *
     * @param id the vehicle UUID
     * @param request the vehicle update request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link VehicleResponseDTO}
     * @throws SmartRoadException if update fails
     */
    @PutMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateVehicle(@PathVariable UUID id,
                                                @RequestBody @Valid VehicleRequestDTO request,
                                                @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside updateVehicle method--");

        UUID userId = RequestUtil.extractUserId();
        VehicleResponseDTO response = vehicleService.update(userId, id, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Soft-deletes a vehicle.
     *
     * @param id the vehicle UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with no content
     * @throws SmartRoadException if delete fails
     */
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVehicle(@PathVariable UUID id,
                                              @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside deleteVehicle method--");

        UUID userId = RequestUtil.extractUserId();
        vehicleService.delete(userId, id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
