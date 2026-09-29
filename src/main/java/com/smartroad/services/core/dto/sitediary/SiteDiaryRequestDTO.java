package com.smartroad.services.core.dto.sitediary;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for creating or updating a daily site diary entry.
 */
public record SiteDiaryRequestDTO(
    @NotNull UUID projectId,
    @NotNull LocalDate diaryDate,
    String weather,
    BigDecimal temperatureCelsius,
    String siteConditions,
    String workSummary,
    String issues,
    String safetyNotes,
    String notes
) {}
