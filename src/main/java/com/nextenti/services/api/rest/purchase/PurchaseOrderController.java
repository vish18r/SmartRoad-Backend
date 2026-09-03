package com.nextenti.services.api.rest.purchase;

import com.nextenti.services.api.utils.RequestUtil;
import com.nextenti.services.common.exception.SmartRoadException;
import com.nextenti.services.core.dto.purchase.PurchaseOrderRequestDTO;
import com.nextenti.services.core.dto.purchase.PurchaseOrderResponseDTO;
import com.nextenti.services.core.service.purchase.PurchaseOrderService;
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
 * REST controller for purchase order management.
 * Handles HTTP requests for purchase order CRUD operations, delegating all business logic to {@link PurchaseOrderService}.
 *
 * @author Vishal
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/purchase-orders")
public class PurchaseOrderController {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderController.class);

    private final PurchaseOrderService purchaseOrderService;

    /**
     * Constructs the controller with required service dependency.
     *
     * @param purchaseOrderService the purchase order service
     */
    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    /**
     * Creates a new purchase order.
     *
     * @param request the purchase order creation request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the created {@link PurchaseOrderResponseDTO}
     * @throws SmartRoadException if creation fails
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createPurchaseOrder(@RequestBody @Valid PurchaseOrderRequestDTO request,
                                                      @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside createPurchaseOrder method--");

        UUID userId = RequestUtil.extractUserId();
        PurchaseOrderResponseDTO response = purchaseOrderService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all purchase orders for a project.
     *
     * @param projectId the project UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of {@link PurchaseOrderResponseDTO}
     * @throws SmartRoadException if retrieval fails
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listPurchaseOrders(@RequestParam UUID projectId,
                                                     @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside listPurchaseOrders method--");

        UUID userId = RequestUtil.extractUserId();
        List<PurchaseOrderResponseDTO> response = purchaseOrderService.listByProject(projectId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves a purchase order by ID.
     *
     * @param id the purchase order UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link PurchaseOrderResponseDTO}
     * @throws SmartRoadException if purchase order not found
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getPurchaseOrder(@PathVariable UUID id,
                                                   @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getPurchaseOrder method--");

        UUID userId = RequestUtil.extractUserId();
        PurchaseOrderResponseDTO response = purchaseOrderService.getById(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates a purchase order.
     *
     * @param id the purchase order UUID
     * @param request the purchase order update request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link PurchaseOrderResponseDTO}
     * @throws SmartRoadException if update fails
     */
    @PutMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updatePurchaseOrder(@PathVariable UUID id,
                                                      @RequestBody @Valid PurchaseOrderRequestDTO request,
                                                      @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside updatePurchaseOrder method--");

        UUID userId = RequestUtil.extractUserId();
        PurchaseOrderResponseDTO response = purchaseOrderService.update(id, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Deletes a purchase order.
     *
     * @param id the purchase order UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with no content
     * @throws SmartRoadException if delete fails
     */
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePurchaseOrder(@PathVariable UUID id,
                                                    @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside deletePurchaseOrder method--");

        UUID userId = RequestUtil.extractUserId();
        purchaseOrderService.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
