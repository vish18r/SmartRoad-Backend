package com.nextenti.services.core.dto.road; import jakarta.validation.constraints.*; import java.math.BigDecimal;
public record RoadRequest(@NotBlank String name,@NotNull @DecimalMin("0.01") BigDecimal lengthM,@DecimalMin("0") BigDecimal widthM,@DecimalMin("0") BigDecimal thicknessMm,String startChainage,String endChainage,@DecimalMin("0") BigDecimal completedLengthM){}
