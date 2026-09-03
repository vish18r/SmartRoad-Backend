package com.smartroad.services.api.rest.vendor;

import com.smartroad.services.api.utils.RequestUtil;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.vendor.VendorRequestDTO;
import com.smartroad.services.core.dto.vendor.VendorResponseDTO;
import com.smartroad.services.core.service.vendor.VendorService;
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
 * REST controller for vendor/supplier management.
 * Handles HTTP requests for vendor CRUD operations, delegating all business logic to {@link VendorService}.
 *
 * @author Vishal
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/vendors")
public class VendorController {

    private static final Logger logger = LoggerFactory.getLogger(VendorController.class);

    private final VendorService vendorService;

    /**
     * Constructs the controller with required service dependency.
     *
     * @param vendorService the vendor service
     */
    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    /**
     * Creates a new vendor.
     *
     * @param request the vendor creation request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the created {@link VendorResponseDTO}
     * @throws SmartRoadException if creation fails
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createVendor(@RequestBody @Valid VendorRequestDTO request,
                                               @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside createVendor method--");

        UUID userId = RequestUtil.extractUserId();
        UUID organizationId = RequestUtil.extractUserId();
        VendorResponseDTO response = vendorService.create(organizationId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all vendors for an organization.
     *
     * @param organizationId the organization UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of {@link VendorResponseDTO}
     * @throws SmartRoadException if retrieval fails
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listVendors(@RequestParam UUID organizationId,
                                              @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside listVendors method--");

        UUID userId = RequestUtil.extractUserId();
        List<VendorResponseDTO> response = vendorService.listByOrganization(organizationId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves a vendor by ID.
     *
     * @param id the vendor UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link VendorResponseDTO}
     * @throws SmartRoadException if vendor not found
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getVendor(@PathVariable UUID id,
                                            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getVendor method--");

        UUID userId = RequestUtil.extractUserId();
        VendorResponseDTO response = vendorService.getById(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates a vendor.
     *
     * @param id the vendor UUID
     * @param request the vendor update request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link VendorResponseDTO}
     * @throws SmartRoadException if update fails
     */
    @PutMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateVendor(@PathVariable UUID id,
                                               @RequestBody @Valid VendorRequestDTO request,
                                               @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside updateVendor method--");

        UUID userId = RequestUtil.extractUserId();
        VendorResponseDTO response = vendorService.update(id, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Deletes a vendor.
     *
     * @param id the vendor UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with no content
     * @throws SmartRoadException if delete fails
     */
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVendor(@PathVariable UUID id,
                                             @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside deleteVendor method--");

        UUID userId = RequestUtil.extractUserId();
        vendorService.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
