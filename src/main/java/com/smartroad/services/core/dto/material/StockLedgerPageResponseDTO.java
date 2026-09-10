package com.smartroad.services.core.dto.material;

import java.util.List;

/**
 * Paged response wrapper for a material's stock ledger.
 *
 * @author Vishal
 * @version 1.0
 */
public record StockLedgerPageResponseDTO(
    List<StockLedgerEntryResponseDTO> data,
    int page,
    int limit,
    long total,
    int totalPages
) {}
