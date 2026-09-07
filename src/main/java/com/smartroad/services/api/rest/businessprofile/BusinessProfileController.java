package com.smartroad.services.api.rest.businessprofile;

import com.smartroad.services.api.utils.RequestUtil;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.businessprofile.BusinessContactRequestDTO;
import com.smartroad.services.core.dto.businessprofile.BusinessContactResponseDTO;
import com.smartroad.services.core.dto.businessprofile.BusinessProfileRequestDTO;
import com.smartroad.services.core.dto.businessprofile.BusinessProfileResponseDTO;
import com.smartroad.services.core.dto.businessprofile.BusinessServiceRequestDTO;
import com.smartroad.services.core.dto.businessprofile.BusinessServiceResponseDTO;
import com.smartroad.services.core.dto.businessprofile.CompleteBusinessProfileResponseDTO;
import com.smartroad.services.core.service.businessprofile.BusinessProfileService;
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
 * REST controller for managing business profiles.
 * Handles HTTP requests for business profile CRUD operations and related entities,
 * delegating all business logic to {@link BusinessProfileService}.
 *
 * @author Vishal
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/business-profiles")
public class BusinessProfileController {

    private static final Logger logger = LoggerFactory.getLogger(BusinessProfileController.class);

    private final BusinessProfileService businessProfileService;

    /**
     * Constructs the controller with required service dependency.
     *
     * @param businessProfileService the business profile service
     */
    public BusinessProfileController(BusinessProfileService businessProfileService) {
        this.businessProfileService = businessProfileService;
    }

    /**
     * Creates a new business profile.
     *
     * @param request the business profile creation request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the created {@link BusinessProfileResponseDTO}
     * @throws SmartRoadException if creation fails
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createBusinessProfile(
            @RequestBody @Valid BusinessProfileRequestDTO request,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside createBusinessProfile method--");

        UUID userId = RequestUtil.extractUserId();
        BusinessProfileResponseDTO response = businessProfileService.createBusinessProfile(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a business profile by ID.
     *
     * @param id the UUID of the business profile
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link BusinessProfileResponseDTO}
     * @throws SmartRoadException if business profile not found
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getBusinessProfile(
            @PathVariable UUID id,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getBusinessProfile method--");

        BusinessProfileResponseDTO response = businessProfileService.getBusinessProfile(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves a business profile by organization ID.
     *
     * @param organizationId the UUID of the organization
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link BusinessProfileResponseDTO}
     * @throws SmartRoadException if business profile not found
     */
    @GetMapping(path = "/organization/{organizationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getBusinessProfileByOrganization(
            @PathVariable UUID organizationId,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getBusinessProfileByOrganization method--");

        BusinessProfileResponseDTO response = businessProfileService.getBusinessProfileByOrganization(organizationId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves the complete business profile with all nested data (contacts and services).
     *
     * @param id the UUID of the business profile
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link CompleteBusinessProfileResponseDTO}
     * @throws SmartRoadException if business profile not found
     */
    @GetMapping(path = "/{id}/complete", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getCompleteBusinessProfile(
            @PathVariable UUID id,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getCompleteBusinessProfile method--");

        CompleteBusinessProfileResponseDTO response = businessProfileService.getCompleteBusinessProfile(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates a business profile.
     *
     * @param id the UUID of the business profile
     * @param request the business profile update request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link BusinessProfileResponseDTO}
     * @throws SmartRoadException if business profile not found or update fails
     */
    @PutMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateBusinessProfile(
            @PathVariable UUID id,
            @RequestBody @Valid BusinessProfileRequestDTO request,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside updateBusinessProfile method--");

        UUID userId = RequestUtil.extractUserId();
        BusinessProfileResponseDTO response = businessProfileService.updateBusinessProfile(id, userId, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Deletes a business profile and all associated contacts and services.
     *
     * @param id the UUID of the business profile
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with HTTP 200 OK on successful deletion
     * @throws SmartRoadException if business profile not found or deletion fails
     */
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteBusinessProfile(
            @PathVariable UUID id,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside deleteBusinessProfile method--");

        businessProfileService.deleteBusinessProfile(id);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Adds a new contact to a business profile.
     *
     * @param request the business contact creation request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the created {@link BusinessContactResponseDTO}
     * @throws SmartRoadException if business profile not found or contact creation fails
     */
    @PostMapping(path = "/contacts", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> addBusinessContact(
            @RequestBody @Valid BusinessContactRequestDTO request,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside addBusinessContact method--");

        UUID userId = RequestUtil.extractUserId();
        BusinessContactResponseDTO response = businessProfileService.addBusinessContact(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all contacts for a business profile.
     *
     * @param profileId the UUID of the business profile
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of {@link BusinessContactResponseDTO}
     */
    @GetMapping(path = "/{profileId}/contacts", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getBusinessContacts(
            @PathVariable UUID profileId,
            @RequestHeader HttpHeaders headers) {
        logger.info("--Inside getBusinessContacts method--");

        List<BusinessContactResponseDTO> response = businessProfileService.getBusinessContacts(profileId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates an existing business contact.
     *
     * @param contactId the UUID of the contact
     * @param request the business contact update request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link BusinessContactResponseDTO}
     * @throws SmartRoadException if contact not found or update fails
     */
    @PutMapping(path = "/contacts/{contactId}", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateBusinessContact(
            @PathVariable UUID contactId,
            @RequestBody @Valid BusinessContactRequestDTO request,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside updateBusinessContact method--");

        UUID userId = RequestUtil.extractUserId();
        BusinessContactResponseDTO response = businessProfileService.updateBusinessContact(contactId, userId, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Deletes a business contact.
     *
     * @param contactId the UUID of the contact
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with HTTP 200 OK on successful deletion
     * @throws SmartRoadException if contact not found or deletion fails
     */
    @DeleteMapping(path = "/contacts/{contactId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteBusinessContact(
            @PathVariable UUID contactId,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside deleteBusinessContact method--");

        businessProfileService.deleteBusinessContact(contactId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Adds a new service to a business profile.
     *
     * @param request the business service creation request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the created {@link BusinessServiceResponseDTO}
     * @throws SmartRoadException if business profile not found or service creation fails
     */
    @PostMapping(path = "/services", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> addBusinessService(
            @RequestBody @Valid BusinessServiceRequestDTO request,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside addBusinessService method--");

        UUID userId = RequestUtil.extractUserId();
        BusinessServiceResponseDTO response = businessProfileService.addBusinessService(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all services for a business profile.
     *
     * @param profileId the UUID of the business profile
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of {@link BusinessServiceResponseDTO}
     */
    @GetMapping(path = "/{profileId}/services", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getBusinessServices(
            @PathVariable UUID profileId,
            @RequestHeader HttpHeaders headers) {
        logger.info("--Inside getBusinessServices method--");

        List<BusinessServiceResponseDTO> response = businessProfileService.getBusinessServices(profileId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates an existing business service.
     *
     * @param serviceId the UUID of the service
     * @param request the business service update request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link BusinessServiceResponseDTO}
     * @throws SmartRoadException if service not found or update fails
     */
    @PutMapping(path = "/services/{serviceId}", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateBusinessService(
            @PathVariable UUID serviceId,
            @RequestBody @Valid BusinessServiceRequestDTO request,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside updateBusinessService method--");

        UUID userId = RequestUtil.extractUserId();
        BusinessServiceResponseDTO response = businessProfileService.updateBusinessService(serviceId, userId, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Deletes a business service.
     *
     * @param serviceId the UUID of the service
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with HTTP 200 OK on successful deletion
     * @throws SmartRoadException if service not found or deletion fails
     */
    @DeleteMapping(path = "/services/{serviceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteBusinessService(
            @PathVariable UUID serviceId,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside deleteBusinessService method--");

        businessProfileService.deleteBusinessService(serviceId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
