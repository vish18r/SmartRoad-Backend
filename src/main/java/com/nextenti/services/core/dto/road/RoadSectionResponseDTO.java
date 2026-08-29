package com.nextenti.services.core.dto.road;import java.math.BigDecimal;import java.util.UUID;
public record RoadSectionResponseDTO(UUID id,UUID roadId,String startChainage,String endChainage,BigDecimal lengthM,BigDecimal completedLengthM,BigDecimal remainingLengthM,BigDecimal completionPercentage){}
