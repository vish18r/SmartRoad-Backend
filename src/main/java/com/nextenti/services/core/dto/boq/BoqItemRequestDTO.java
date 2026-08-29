package com.nextenti.services.core.dto.boq;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Request DTO for creating a Bill of Quantities (BoQ) item.
 */
public record BoqItemRequestDTO(
    @Size(max = 100)
    String itemCode,
    @NotBlank
    String description,
    @NotBlank
    @Size(max = 32)
    String unit,
    @NotNull
    @DecimalMin("0")
    BigDecimal estimatedQuantity,
    @NotNull
    @DecimalMin("0")
    BigDecimal rate,
    @DecimalMin("0")
    BigDecimal actualQuantity,
    @DecimalMin("0")
    BigDecimal actualRate
) {}
