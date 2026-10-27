package com.smartroad.services.core.dto.dashboard;

import java.math.BigDecimal;

/**
 * Material receipt and consumption quantities recorded on the site today.
 *
 * @author Vishal
 * @version 1.0
 */
public record SiteMaterialsTodayDTO(
    BigDecimal receivedQuantity,
    BigDecimal consumedQuantity
) {}
