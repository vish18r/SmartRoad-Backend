package com.smartroad.services.core.dto.dashboard;

import java.math.BigDecimal;

/**
 * Material stock held at the site, and how much of it needs reordering.
 *
 * @author Vishal
 * @version 1.0
 */
public record SiteMaterialsDTO(
    long trackedMaterials,
    long lowStockMaterials,
    BigDecimal stockValue
) {}
