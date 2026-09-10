package com.smartroad.services.api.rest.material;

import com.smartroad.services.api.utils.RequestUtil;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.common.util.NextentiConstants;
import com.smartroad.services.core.dto.material.MaterialRequestDTO;
import com.smartroad.services.core.dto.material.MaterialResponseDTO;
import com.smartroad.services.core.dto.material.StockLedgerPageResponseDTO;
import com.smartroad.services.core.dto.stock.StockTransferRequestDTO;
import com.smartroad.services.core.dto.stock.StockTransferResponseDTO;
import com.smartroad.services.core.service.material.MaterialService;
import com.smartroad.services.core.service.organization.OrganizationService;
import com.smartroad.services.core.service.stock.StockService;
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
    private final StockService stockService;

    /**
     * Constructs the controller with required service dependency.
     *
     * @param materialService the material service
     * @param organizationService the organization service, used to resolve the caller's organization
     * @param stockService the stock service, owning transfers and the stock ledger
     */
    public MaterialController(MaterialService materialService,
                              OrganizationService organizationService,
                              StockService stockService) {
        this.materialService = materialService;
        this.organizationService = organizationService;
        this.stockService = stockService;
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
     * Reports the organization's materials that have fallen to or below their reorder threshold.
     * The optional project filter is accepted either as the projectId query parameter or as the
     * x-project-id header the web client sends; when present, stock is measured on that project alone.
     * The organization is optional and is resolved from the caller when omitted.
     *
     * @param organizationId the optional UUID of the organization
     * @param projectId the optional project UUID supplied as a query parameter
     * @param projectIdHeader the optional project UUID supplied as the x-project-id header
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of low-stock {@link MaterialResponseDTO}
     * @throws SmartRoadException if the caller belongs to no organization or retrieval fails
     */
    @GetMapping(path = "/low-stock", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listLowStockMaterials(@RequestParam(required = false) UUID organizationId,
                                                        @RequestParam(required = false) UUID projectId,
                                                        @RequestHeader(name = NextentiConstants.HEADER_PROJECT_ID, required = false) UUID projectIdHeader,
                                                        @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside listLowStockMaterials method--");

        UUID projectFilter = projectId != null ? projectId : projectIdHeader;
        List<MaterialResponseDTO> response = materialService.listLowStock(resolveOrganization(organizationId), projectFilter);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Moves stock of a material from one project to another and records both legs in the stock ledger.
     *
     * @param request the stock transfer request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the recorded {@link StockTransferResponseDTO}
     * @throws SmartRoadException if the material or source stock is not found, or the source balance
     *                            is insufficient
     */
    @PostMapping(path = "/transfer", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> transferStock(@RequestBody @Valid StockTransferRequestDTO request,
                                                @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside transferStock method--");

        UUID userId = RequestUtil.extractUserId();
        StockTransferResponseDTO response = stockService.transfer(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a page of a material's stock ledger, most recent movement first.
     * Pagination is read from the x-page and x-limit headers the web client sends, falling back to
     * the page and limit request parameters.
     *
     * @param id the material UUID
     * @param page the optional one-based page number supplied as a query parameter
     * @param limit the optional page size supplied as a query parameter
     * @param pageHeader the optional page number supplied as the x-page header
     * @param limitHeader the optional page size supplied as the x-limit header
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link StockLedgerPageResponseDTO}
     * @throws SmartRoadException if the material is not found
     */
    @GetMapping(path = "/{id}/stock-ledger", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getStockLedger(@PathVariable UUID id,
                                                 @RequestParam(required = false) Integer page,
                                                 @RequestParam(required = false) Integer limit,
                                                 @RequestHeader(name = NextentiConstants.HEADER_PAGE, required = false) Integer pageHeader,
                                                 @RequestHeader(name = NextentiConstants.HEADER_LIMIT, required = false) Integer limitHeader,
                                                 @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getStockLedger method--");

        int resolvedPage = firstNonNull(page, pageHeader, NextentiConstants.DEFAULT_PAGE);
        int resolvedLimit = firstNonNull(limit, limitHeader, NextentiConstants.DEFAULT_LIMIT);
        StockLedgerPageResponseDTO response = stockService.getLedger(id, resolvedPage, resolvedLimit);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Returns the first of the supplied pagination values that is present.
     *
     * @param parameter the value from the request parameter, possibly null
     * @param header the value from the request header, possibly null
     * @param fallback the default to use when neither is present
     * @return the resolved value
     */
    private int firstNonNull(Integer parameter, Integer header, int fallback) {
        if (parameter != null) {
            return parameter;
        }
        return header != null ? header : fallback;
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
