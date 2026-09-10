package com.smartroad.services.core.dto.material;

import com.smartroad.services.common.enums.stock.StockTransactionTypeEnum;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * Response DTO for a single stock ledger entry returned to API consumers.
 *
 * @author Vishal
 * @version 1.0
 */
public record StockLedgerEntryResponseDTO(
    UUID id,
    UUID materialId,
    UUID projectId,
    StockTransactionTypeEnum transactionType,
    BigDecimal quantity,
    String referenceNumber,
    String notes,
    Date createdDate
) {}
