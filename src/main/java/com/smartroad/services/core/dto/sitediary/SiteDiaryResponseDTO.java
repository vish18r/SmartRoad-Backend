package com.smartroad.services.core.dto.sitediary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

/**
 * Response DTO for a daily site diary entry returned to API consumers.
 */
public record SiteDiaryResponseDTO(
    UUID id,
    UUID projectId,
    LocalDate diaryDate,
    String weather,
    BigDecimal temperatureCelsius,
    String siteConditions,
    String workSummary,
    String issues,
    String safetyNotes,
    String notes,
    Date dateCreated,
    Date dateModified
) {}
