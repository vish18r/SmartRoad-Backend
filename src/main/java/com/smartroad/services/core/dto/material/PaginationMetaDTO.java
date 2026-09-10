package com.smartroad.services.core.dto.material;

/**
 * Pagination metadata accompanying a paged list response.
 *
 * @author Vishal
 * @version 1.0
 */
public record PaginationMetaDTO(
    int currentPage,
    int pageSize,
    long totalRecords,
    int totalPages,
    boolean hasNextPage,
    boolean hasPreviousPage
) {}
