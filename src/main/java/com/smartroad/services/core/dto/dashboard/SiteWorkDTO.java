package com.smartroad.services.core.dto.dashboard;

import java.math.BigDecimal;

/**
 * Physical progress on a site: how much carriageway is built against how much was planned.
 *
 * @author Vishal
 * @version 1.0
 */
public record SiteWorkDTO(
    String projectStatus,
    BigDecimal projectProgress,
    long totalRoads,
    long completedRoads,
    BigDecimal totalLengthM,
    BigDecimal completedLengthM,
    BigDecimal remainingLengthM,
    BigDecimal completionPercentage
) {}
