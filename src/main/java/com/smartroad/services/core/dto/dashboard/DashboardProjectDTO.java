package com.smartroad.services.core.dto.dashboard;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Compact per-project projection used by the dashboard's project progress widget.
 *
 * @author Vishal
 * @version 1.0
 */
public record DashboardProjectDTO(
    UUID id,
    String name,
    BigDecimal progress,
    String status
) {}
