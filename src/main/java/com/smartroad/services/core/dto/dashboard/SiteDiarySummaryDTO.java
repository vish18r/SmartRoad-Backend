package com.smartroad.services.core.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Summary of the day's site diary entry shown on the supervisor's dashboard.
 *
 * @author Vishal
 * @version 1.0
 */
public record SiteDiarySummaryDTO(
    UUID id,
    LocalDate diaryDate,
    String weather,
    BigDecimal temperatureCelsius,
    String siteConditions,
    String workSummary,
    String issues,
    String safetyNotes,
    String notes
) {}
