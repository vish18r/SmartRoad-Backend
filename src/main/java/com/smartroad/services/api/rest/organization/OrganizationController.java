package com.smartroad.services.api.rest.organization;

import com.smartroad.services.api.utils.RequestUtil;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.organization.OrganizationRequestDTO;
import com.smartroad.services.core.dto.organization.OrganizationResponseDTO;
import com.smartroad.services.core.service.organization.OrganizationService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for OrganizationEntity management.
 * Handles HTTP requests for organization CRUD operations, delegating all business logic to {@link OrganizationService}.
 *
 * @author Vishal
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationController {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationController.class);

    private final OrganizationService organizationService;

    /**
     * Constructs the controller with required service dependency.
     *
     * @param organizationService the organization service
     */
    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    /**
     * Creates a new organization.
     *
     * @param request the organization creation request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the created {@link OrganizationResponseDTO}
     * @throws SmartRoadException if creation fails
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createOrganization(@RequestBody @Valid OrganizationRequestDTO request,
                                                     @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside createOrganizationEntity method--");

        UUID userId = RequestUtil.extractUserId();
        OrganizationResponseDTO response = organizationService.create(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all organizations for the authenticated user.
     *
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of {@link OrganizationResponseDTO}
     * @throws SmartRoadException if retrieval fails
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listOrganizations(@RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside listOrganizations method--");

        List<OrganizationResponseDTO> response = organizationService.list();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves user's organizations.
     *
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of {@link OrganizationResponseDTO}
     * @throws SmartRoadException if retrieval fails
     */
    @GetMapping(path = "/my-organizations", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getMyOrganizations(@RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getMyOrganizations method--");

        UUID userId = RequestUtil.extractUserId();
        List<OrganizationResponseDTO> response = organizationService.mine(userId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves an organization by ID.
     *
     * @param id the UUID of the organization
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link OrganizationResponseDTO}
     * @throws SmartRoadException if organization not found
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getOrganization(@PathVariable UUID id,
                                                  @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getOrganizationEntity method--");

        UUID userId = RequestUtil.extractUserId();
        OrganizationResponseDTO response = organizationService.get(userId, id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates an organization.
     *
     * @param id the UUID of the organization
     * @param request the organization update request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link OrganizationResponseDTO}
     * @throws SmartRoadException if organization not found or update fails
     */
    @PutMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateOrganization(@PathVariable UUID id,
                                                     @RequestBody @Valid OrganizationRequestDTO request,
                                                     @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside updateOrganizationEntity method--");

        UUID userId = RequestUtil.extractUserId();
        OrganizationResponseDTO response = organizationService.update(userId, id, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Deletes an organization.
     *
     * @param id the UUID of the organization
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with HTTP 200 OK on successful deletion
     * @throws SmartRoadException if organization not found or deletion fails
     */
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteOrganization(@PathVariable UUID id,
                                                     @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside deleteOrganizationEntity method--");

        UUID userId = RequestUtil.extractUserId();
        organizationService.deactivate(userId, id);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
