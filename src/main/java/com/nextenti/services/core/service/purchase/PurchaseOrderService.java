package com.nextenti.services.core.service.purchase;

import com.nextenti.services.common.enums.purchase.PurchaseOrderStatusEnum;
import com.nextenti.services.common.exception.ApplicationLayer;
import com.nextenti.services.common.exception.ErrorCodeMapping;
import com.nextenti.services.common.exception.SmartRoadException;
import com.nextenti.services.core.dto.purchase.PurchaseOrderRequestDTO;
import com.nextenti.services.core.dto.purchase.PurchaseOrderResponseDTO;
import com.nextenti.services.domain.entity.PurchaseOrderEntity;
import com.nextenti.services.domain.repository.PurchaseOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for purchase order business logic.
 * Manages purchase order creation, retrieval, updating, and deletion operations.
 *
 * @author Vishal
 * @version 1.0
 */
@Service
@Transactional
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;

    /**
     * Constructs a PurchaseOrderService with required dependencies.
     *
     * @param purchaseOrderRepository the purchase order repository
     */
    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    /**
     * Creates a new purchase order.
     *
     * @param request the purchase order request DTO
     * @return the created purchase order response DTO
     * @throws SmartRoadException if PO number already exists or calculation fails
     */
    public PurchaseOrderResponseDTO create(PurchaseOrderRequestDTO request) throws SmartRoadException {
        try {
            if (purchaseOrderRepository.findByPoNumber(request.poNumber()).isPresent()) {
                throw new SmartRoadException(
                    ApplicationLayer.SERVICE_LAYER,
                    ErrorCodeMapping.SERVICE_INVALID_INPUT,
                    "po.number.exists"
                );
            }

            BigDecimal totalAmount = calculateTotalAmount(request.quantity(), request.rate(), request.taxPercentage());

            PurchaseOrderEntity entity = PurchaseOrderEntity.builder()
                .projectId(request.projectId())
                .poNumber(request.poNumber())
                .vendorId(request.vendorId())
                .materialId(request.materialId())
                .quantity(request.quantity())
                .unit(request.unit())
                .rate(request.rate())
                .taxPercentage(request.taxPercentage())
                .totalAmount(totalAmount)
                .orderDate(request.orderDate())
                .expectedDeliveryDate(request.expectedDeliveryDate())
                .status(PurchaseOrderStatusEnum.DRAFT)
                .build();

            PurchaseOrderEntity saved = purchaseOrderRepository.save(entity);
            return mapToResponseDTO(saved);
        } catch (SmartRoadException e) {
            throw e;
        } catch (Exception e) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.SERVICE_INTERNAL_ERROR,
                "po.create.failed",
                e
            );
        }
    }

    /**
     * Retrieves a purchase order by ID.
     *
     * @param id the purchase order UUID
     * @return the purchase order response DTO
     * @throws SmartRoadException if purchase order not found
     */
    public PurchaseOrderResponseDTO getById(UUID id) throws SmartRoadException {
        return purchaseOrderRepository.findById(id)
            .map(this::mapToResponseDTO)
            .orElseThrow(() -> new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "po.not.found"
            ));
    }

    /**
     * Retrieves all purchase orders for a project.
     *
     * @param projectId the project UUID
     * @return list of purchase order response DTOs
     */
    @Transactional(readOnly = true)
    public List<PurchaseOrderResponseDTO> listByProject(UUID projectId) {
        return purchaseOrderRepository.findByProjectId(projectId)
            .stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }

    /**
     * Updates an existing purchase order.
     *
     * @param id the purchase order UUID
     * @param request the purchase order request DTO
     * @return the updated purchase order response DTO
     * @throws SmartRoadException if purchase order not found
     */
    public PurchaseOrderResponseDTO update(UUID id, PurchaseOrderRequestDTO request) throws SmartRoadException {
        try {
            PurchaseOrderEntity entity = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new SmartRoadException(
                    ApplicationLayer.SERVICE_LAYER,
                    ErrorCodeMapping.DAO_NOT_FOUND,
                    "po.not.found"
                ));

            entity.setVendorId(request.vendorId());
            entity.setMaterialId(request.materialId());
            entity.setQuantity(request.quantity());
            entity.setUnit(request.unit());
            entity.setRate(request.rate());
            entity.setTaxPercentage(request.taxPercentage());
            entity.setTotalAmount(calculateTotalAmount(request.quantity(), request.rate(), request.taxPercentage()));
            entity.setOrderDate(request.orderDate());
            entity.setExpectedDeliveryDate(request.expectedDeliveryDate());

            PurchaseOrderEntity updated = purchaseOrderRepository.save(entity);
            return mapToResponseDTO(updated);
        } catch (SmartRoadException e) {
            throw e;
        } catch (Exception e) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.SERVICE_INTERNAL_ERROR,
                "po.update.failed",
                e
            );
        }
    }

    /**
     * Deletes a purchase order by ID.
     *
     * @param id the purchase order UUID
     * @throws SmartRoadException if purchase order not found
     */
    public void delete(UUID id) throws SmartRoadException {
        try {
            if (!purchaseOrderRepository.existsById(id)) {
                throw new SmartRoadException(
                    ApplicationLayer.SERVICE_LAYER,
                    ErrorCodeMapping.DAO_NOT_FOUND,
                    "po.not.found"
                );
            }
            purchaseOrderRepository.deleteById(id);
        } catch (SmartRoadException e) {
            throw e;
        } catch (Exception e) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.SERVICE_INTERNAL_ERROR,
                "po.delete.failed",
                e
            );
        }
    }

    /**
     * Calculates total amount for purchase order.
     *
     * @param quantity the quantity
     * @param rate the rate per unit
     * @param taxPercentage the tax percentage
     * @return the calculated total amount
     */
    private BigDecimal calculateTotalAmount(BigDecimal quantity, BigDecimal rate, BigDecimal taxPercentage) {
        BigDecimal subtotal = quantity.multiply(rate);
        if (taxPercentage != null && taxPercentage.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal taxAmount = subtotal.multiply(taxPercentage).divide(new BigDecimal(100), 2, java.math.RoundingMode.HALF_UP);
            return subtotal.add(taxAmount);
        }
        return subtotal;
    }

    /**
     * Maps a PurchaseOrderEntity to PurchaseOrderResponseDTO.
     *
     * @param entity the purchase order entity
     * @return the response DTO
     */
    private PurchaseOrderResponseDTO mapToResponseDTO(PurchaseOrderEntity entity) {
        return PurchaseOrderResponseDTO.builder()
            .id(entity.getId())
            .projectId(entity.getProjectId())
            .poNumber(entity.getPoNumber())
            .vendorId(entity.getVendorId())
            .materialId(entity.getMaterialId())
            .quantity(entity.getQuantity())
            .unit(entity.getUnit())
            .rate(entity.getRate())
            .taxPercentage(entity.getTaxPercentage())
            .totalAmount(entity.getTotalAmount())
            .orderDate(entity.getOrderDate())
            .expectedDeliveryDate(entity.getExpectedDeliveryDate())
            .status(entity.getStatus())
            .createdBy(entity.getCreatedBy())
            .modifiedBy(entity.getModifiedBy())
            .createdDate(entity.getDateCreated())
            .modifiedDate(entity.getDateModified())
            .build();
    }
}
