package com.smartroad.services.core.dto.shift;

import com.smartroad.services.core.dto.material.PaginationMetaDTO;

import java.util.List;

/**
 * Paged response wrapper for a shift assignment list. Shaped to match the web client's paged
 * list contract: the rows under {@code data}, with the page counters grouped under
 * {@code pagination}.
 *
 * @param data the assignments for the requested page
 * @param pagination the page counters
 */
public record ShiftAssignmentPageResponseDTO(
    List<ShiftAssignmentResponseDTO> data,
    PaginationMetaDTO pagination
) {}
