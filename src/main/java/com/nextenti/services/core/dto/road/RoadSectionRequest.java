package com.nextenti.services.core.dto.road;import jakarta.validation.constraints.*;import java.math.BigDecimal;
public record RoadSectionRequest(@NotBlank String startChainage,@NotBlank String endChainage,@NotNull @DecimalMin("0.01") BigDecimal lengthM,@DecimalMin("0") BigDecimal completedLengthM){}
