package com.smartroad.services.api.rest.material;

import com.smartroad.services.api.utils.RequestUtil;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.common.util.NextentiConstants;
import com.smartroad.services.core.dto.material.MaterialRequestDTO;
import com.smartroad.services.core.dto.material.MaterialResponseDTO;
import com.smartroad.services.core.service.material.MaterialService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for material/inventory management.
 * Handles HTTP requests for material CRUD operations, delegating all business logic to {@link MaterialService}.
 *
 * @author Vishal
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/materials")
public class MaterialController {

    private static final Logger logger = LoggerFactory.getLogger(MaterialController.class);

    private final MaterialService materialService;
    private final OrganizationService organizationService;

    /**
     * Constructs the controller with required service dependency.
     *
     * @param materialService the material service
     * @param organizationService the organization service, used to resolve the caller's organization
     */
    public MaterialController(MaterialService materialService, OrganizationService organizationService) {
        this.materialService = materialService;
        this.organizationService = organizationService;
    }

    /**
     * Creates a new material.
     * The organization is optional; when omitted it is resolved from the caller's membership.
     *
     * @param request the material creation request
     * @param organizationId the optional UUID of the organization to create the material under
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the created {@link MaterialResponseDTO}
     * @throws SmartRoadException if the caller belongs to no organization or creation fails
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createMaterial(@RequestBody @Valid MaterialRequestDTO request,
                                                 @RequestParam(required = false) UUID organizationId,
                                                 @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside createMaterial method--");

        MaterialResponseDTO response = materialService.create(resolveOrganization(organizationId), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all materials for an organization.
     *
     * @param organizationId the organization UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of {@link MaterialResponseDTO}
     * @throws SmartRoadException if retrieval fails
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listMaterials(@RequestParam(required = false) UUID organizationId,
                                                @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside listMaterials method--");

        List<MaterialResponseDTO> response = materialService.listByOrganization(resolveOrganization(organizationId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Searches the organization's materials by code, name, or category.
     * The term is read from the x-search header the web client sends, falling back to the
     * query request parameter. The organization is optional and is resolved from the caller
     * when omitted.
     *
     * @param query the optional search term supplied as a query parameter
     * @param searchHeader the optional search term supplied as the x-search header
     * @param organizationId the optional UUID of the organization
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of matching {@link MaterialResponseDTO}
     * @throws SmartRoadException if the caller belongs to no organization or the search fails
     */
    @GetMapping(path = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> searchMaterials(@RequestParam(required = false) String query,
                                                  @RequestHeader(name = NextentiConstants.HEADER_SEARCH, required = false) String searchHeader,
                                                  @RequestParam(required = false) UUID organizationId,
                                                  @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside searchMaterials method--");

        String term = query != null ? query : searchHeader;
        List<MaterialResponseDTO> response = materialService.search(resolveOrganization(organizationId), term);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves a material by ID.
     *
     * @param id the material UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link MaterialResponseDTO}
     * @throws SmartRoadException if material not found
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getMaterial(@PathVariable UUID id,
                                              @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getMaterial method--");

        UUID userId = RequestUtil.extractUserId();
        MaterialResponseDTO response = materialService.getById(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates a material.
     *
     * @param id the material UUID
     * @param request the material update request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link MaterialResponseDTO}
     * @throws SmartRoadException if update fails
     */
    @PutMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateMaterial(@PathVariable UUID id,
                                                 @RequestBody @Valid MaterialRequestDTO request,
                                                 @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside updateMaterial method--");

        UUID userId = RequestUtil.extractUserId();
        MaterialResponseDTO response = materialService.update(id, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Deletes a material.
     *
     * @param id the material UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with no content
     * @throws SmartRoadException if delete fails
     */
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMaterial(@PathVariable UUID id,
                                               @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside deleteMaterial method--");

        UUID userId = RequestUtil.extractUserId();
        materialService.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
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
