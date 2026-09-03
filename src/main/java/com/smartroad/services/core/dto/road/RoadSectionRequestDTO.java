package com.smartroad.services.core.dto.road;import jakarta.validation.constraints.*;import java.math.BigDecimal;
public record RoadSectionRequestDTO(@NotBlank String startChainage,@NotBlank String endChainage,@NotNull @DecimalMin("0.01") BigDecimal lengthM,@DecimalMin("0") BigDecimal completedLengthM){}
