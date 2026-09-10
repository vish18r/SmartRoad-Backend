package com.smartroad.services.core.dto.material;

import java.util.List;

/**
 * Paged response wrapper for a material's stock ledger.
 * Shaped to match the web client's paged list contract: the rows under {@code data}, with the
 * page counters grouped under {@code pagination}.
 *
 * @author Vishal
 * @version 1.0
 */
public record StockLedgerPageResponseDTO(
    List<StockLedgerEntryResponseDTO> data,
    PaginationMetaDTO pagination
) {}
