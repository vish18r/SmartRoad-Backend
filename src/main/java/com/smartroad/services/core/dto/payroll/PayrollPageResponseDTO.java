package com.smartroad.services.core.dto.payroll;

import com.smartroad.services.core.dto.material.PaginationMetaDTO;

import java.util.List;

/**
 * Paged response wrapper for a payroll list. Shaped to match the web client's paged list
 * contract: the rows under {@code data}, with the page counters grouped under
 * {@code pagination}.
 *
 * @param data the payroll records for the requested page
 * @param pagination the page counters
 */
public record PayrollPageResponseDTO(
    List<PayrollResponseDTO> data,
    PaginationMetaDTO pagination
) {}
