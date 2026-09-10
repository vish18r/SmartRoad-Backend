package com.smartroad.services.core.service.stock;

import com.smartroad.services.common.enums.stock.StockTransactionTypeEnum;
import com.smartroad.services.common.enums.stock.StockTransferStatusEnum;
import com.smartroad.services.common.exception.ApplicationLayer;
import com.smartroad.services.common.exception.ErrorCodeMapping;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.material.StockLedgerEntryResponseDTO;
import com.smartroad.services.core.dto.material.StockLedgerPageResponseDTO;
import com.smartroad.services.core.dto.stock.StockTransferRequestDTO;
import com.smartroad.services.core.dto.stock.StockTransferResponseDTO;
import com.smartroad.services.domain.entity.material.MaterialEntity;
import com.smartroad.services.domain.entity.material.MaterialStockEntity;
import com.smartroad.services.domain.entity.material.MaterialStockLedgerEntity;
import com.smartroad.services.domain.entity.procurement.StockTransferEntity;
import com.smartroad.services.domain.repository.MaterialRepository;
import com.smartroad.services.domain.repository.MaterialStockLedgerRepository;
import com.smartroad.services.domain.repository.MaterialStockRepository;
import com.smartroad.services.domain.repository.StockTransferRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * Service layer for material stock movements.
 * Owns project-to-project stock transfers and the append-only stock ledger that records them.
 *
 * @author Vishal
 * @version 1.0
 */
@Service
public class StockService {

    private final MaterialRepository materialRepository;
    private final MaterialStockRepository stockRepository;
    private final MaterialStockLedgerRepository ledgerRepository;
    private final StockTransferRepository transferRepository;

    /**
     * Constructs a StockService with required dependencies.
     *
     * @param materialRepository the material repository
     * @param stockRepository the per-project material stock repository
     * @param ledgerRepository the stock ledger repository
     * @param transferRepository the stock transfer repository
     */
    public StockService(MaterialRepository materialRepository,
                        MaterialStockRepository stockRepository,
                        MaterialStockLedgerRepository ledgerRepository,
                        StockTransferRepository transferRepository) {
        this.materialRepository = materialRepository;
        this.stockRepository = stockRepository;
        this.ledgerRepository = ledgerRepository;
        this.transferRepository = transferRepository;
    }

    /**
     * Moves stock of a material from one project to another.
     * The whole movement is atomic: the source balance is decremented, the destination balance is
     * credited, a completed transfer record is written, and both legs are recorded in the ledger.
     *
     * @param userId the UUID of the user performing the transfer
     * @param request the transfer request
     * @return the recorded {@link StockTransferResponseDTO}
     * @throws SmartRoadException if the material or source stock is not found, the source and
     *                            destination projects are the same, or the source balance is insufficient
     */
    @Transactional
    public StockTransferResponseDTO transfer(UUID userId, StockTransferRequestDTO request) throws SmartRoadException {
        MaterialEntity material = requireMaterial(request.materialId());
        BigDecimal quantity = requirePositiveQuantity(request.quantityRequested());

        if (request.sourceProjectId().equals(request.destinationProjectId())) {
            throw invalid("stock.transfer.same.project");
        }

        debitSource(userId, material.getId(), request.sourceProjectId(), quantity);
        creditDestination(userId, material.getId(), request.destinationProjectId(), quantity);

        StockTransferEntity transfer = transferRepository.save(StockTransferEntity.builder()
                .sourceProjectId(request.sourceProjectId())
                .destinationProjectId(request.destinationProjectId())
                .materialId(material.getId())
                .quantityRequested(quantity)
                .quantityTransferred(quantity)
                .transferDate(request.transferDate() == null ? new Date() : request.transferDate())
                .requestedBy(userId)
                .approvedBy(userId)
                .status(StockTransferStatusEnum.COMPLETED)
                .build());

        String reference = transfer.getId().toString();
        record(userId, material.getId(), request.sourceProjectId(), quantity.negate(), reference);
        record(userId, material.getId(), request.destinationProjectId(), quantity, reference);

        return toTransferResponse(transfer);
    }

    /**
     * Retrieves a page of a material's stock ledger, most recent movement first.
     *
     * @param materialId the material UUID
     * @param page the one-based page number
     * @param limit the page size
     * @return the {@link StockLedgerPageResponseDTO} for the requested page
     * @throws SmartRoadException if the material is not found
     */
    @Transactional(readOnly = true)
    public StockLedgerPageResponseDTO getLedger(UUID materialId, int page, int limit) throws SmartRoadException {
        requireMaterial(materialId);

        int pageIndex = Math.max(page, 1) - 1;
        int pageSize = Math.max(limit, 1);
        Page<MaterialStockLedgerEntity> results =
                ledgerRepository.findByMaterialId(materialId, PageRequest.of(pageIndex, pageSize));

        return new StockLedgerPageResponseDTO(
                results.getContent().stream().map(this::toLedgerResponse).toList(),
                pageIndex + 1,
                pageSize,
                results.getTotalElements(),
                results.getTotalPages());
    }

    /**
     * Decrements a project's available balance for a material, rejecting the movement when the
     * project holds no stock record or not enough stock.
     *
     * @param userId the UUID of the user performing the transfer
     * @param materialId the material UUID
     * @param projectId the source project UUID
     * @param quantity the quantity to remove
     * @throws SmartRoadException if the source stock record is missing or the balance is insufficient
     */
    private void debitSource(UUID userId, UUID materialId, UUID projectId, BigDecimal quantity) throws SmartRoadException {
        MaterialStockEntity source = stockRepository.findByProjectIdAndMaterialId(projectId, materialId)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER,
                        ErrorCodeMapping.DAO_NOT_FOUND, "stock.source.not.found"));

        BigDecimal available = orZero(source.getQuantityAvailable());
        if (available.compareTo(quantity) < 0) {
            throw invalid("stock.insufficient");
        }

        source.setQuantityAvailable(available.subtract(quantity));
        source.setTotalValue(valueOf(source));
        source.setModifiedBy(userId);
        stockRepository.save(source);
    }

    /**
     * Credits a project's available balance for a material, opening a stock record when the project
     * has never held the material before.
     *
     * @param userId the UUID of the user performing the transfer
     * @param materialId the material UUID
     * @param projectId the destination project UUID
     * @param quantity the quantity to add
     */
    private void creditDestination(UUID userId, UUID materialId, UUID projectId, BigDecimal quantity) {
        MaterialStockEntity destination = stockRepository.findByProjectIdAndMaterialId(projectId, materialId)
                .orElseGet(() -> MaterialStockEntity.builder()
                        .projectId(projectId)
                        .materialId(materialId)
                        .quantityAvailable(BigDecimal.ZERO)
                        .quantityReserved(BigDecimal.ZERO)
                        .quantityConsumed(BigDecimal.ZERO)
                        .build());

        destination.setQuantityAvailable(orZero(destination.getQuantityAvailable()).add(quantity));
        destination.setTotalValue(valueOf(destination));
        destination.setModifiedBy(userId);
        stockRepository.save(destination);
    }

    /**
     * Appends one transfer leg to the stock ledger.
     *
     * @param userId the UUID of the user performing the transfer
     * @param materialId the material UUID
     * @param projectId the project the movement applies to
     * @param quantity the signed quantity, negative for the outgoing leg
     * @param reference the transfer record ID this movement belongs to
     */
    private void record(UUID userId, UUID materialId, UUID projectId, BigDecimal quantity, String reference) {
        MaterialStockLedgerEntity entry = MaterialStockLedgerEntity.builder()
                .materialId(materialId)
                .projectId(projectId)
                .transactionType(StockTransactionTypeEnum.TRANSFER)
                .quantity(quantity)
                .referenceNumber(reference)
                .build();
        entry.setModifiedBy(userId);
        ledgerRepository.save(entry);
    }

    /**
     * Loads a material by ID.
     *
     * @param materialId the material UUID
     * @return the {@link MaterialEntity}
     * @throws SmartRoadException if the material is not found
     */
    private MaterialEntity requireMaterial(UUID materialId) throws SmartRoadException {
        return materialRepository.findById(materialId)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER,
                        ErrorCodeMapping.DAO_NOT_FOUND, "material.not.found"));
    }

    /**
     * Validates that a transfer quantity is present and greater than zero.
     *
     * @param quantity the requested quantity
     * @return the validated quantity
     * @throws SmartRoadException if the quantity is missing or not positive
     */
    private BigDecimal requirePositiveQuantity(BigDecimal quantity) throws SmartRoadException {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw invalid("stock.transfer.quantity.invalid");
        }
        return quantity;
    }

    /**
     * Recomputes a stock record's total value from its available quantity and unit rate.
     *
     * @param stock the stock record
     * @return the recomputed total value, or null when no unit rate is set
     */
    private BigDecimal valueOf(MaterialStockEntity stock) {
        return stock.getUnitRate() == null
                ? stock.getTotalValue()
                : orZero(stock.getQuantityAvailable()).multiply(stock.getUnitRate());
    }

    /**
     * Substitutes zero for a null quantity.
     *
     * @param value the quantity, possibly null
     * @return the value, or {@link BigDecimal#ZERO} when null
     */
    private BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * Builds a bad-request exception for the given message key.
     *
     * @param messageKey the message key to report
     * @return the {@link SmartRoadException} to throw
     */
    private SmartRoadException invalid(String messageKey) {
        return new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_INVALID_INPUT, messageKey);
    }

    /**
     * Maps a stock transfer entity to its response DTO.
     *
     * @param entity the stock transfer entity
     * @return the stock transfer response DTO
     */
    private StockTransferResponseDTO toTransferResponse(StockTransferEntity entity) {
        return StockTransferResponseDTO.builder()
                .id(entity.getId())
                .sourceProjectId(entity.getSourceProjectId())
                .destinationProjectId(entity.getDestinationProjectId())
                .materialId(entity.getMaterialId())
                .quantityRequested(entity.getQuantityRequested())
                .quantityTransferred(entity.getQuantityTransferred())
                .transferDate(entity.getTransferDate())
                .requestedBy(entity.getRequestedBy())
                .approvedBy(entity.getApprovedBy())
                .status(entity.getStatus())
                .createdBy(entity.getCreatedBy())
                .modifiedBy(entity.getModifiedBy())
                .createdDate(entity.getDateCreated())
                .modifiedDate(entity.getDateModified())
                .build();
    }

    /**
     * Maps a stock ledger entity to its response DTO.
     *
     * @param entity the stock ledger entity
     * @return the stock ledger entry response DTO
     */
    private StockLedgerEntryResponseDTO toLedgerResponse(MaterialStockLedgerEntity entity) {
        return new StockLedgerEntryResponseDTO(entity.getId(), entity.getMaterialId(), entity.getProjectId(),
                entity.getTransactionType(), entity.getQuantity(), entity.getReferenceNumber(),
                entity.getNotes(), entity.getDateCreated());
    }
}
