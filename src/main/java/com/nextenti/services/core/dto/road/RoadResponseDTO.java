package com.nextenti.services.core.dto.road;import java.math.BigDecimal;import java.util.UUID;
public record RoadResponseDTO(UUID id,UUID projectId,String name,BigDecimal lengthM,BigDecimal widthM,BigDecimal thicknessMm,String startChainage,String endChainage,BigDecimal completedLengthM,BigDecimal remainingLengthM,BigDecimal completionPercentage){}
