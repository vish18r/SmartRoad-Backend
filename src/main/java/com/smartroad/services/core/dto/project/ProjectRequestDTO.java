package com.smartroad.services.core.dto.project;
import com.smartroad.services.common.enums.ProjectStatus; import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.time.LocalDate; import java.util.UUID;
public record ProjectRequestDTO(@NotNull UUID organizationId, UUID clientId,@Size(max=100) String code,@NotBlank @Size(max=255) String name,String description,String location,@NotNull ProjectStatus status,@NotNull @DecimalMin("0") BigDecimal budget,@DecimalMin("0") @DecimalMax("100") BigDecimal progress,LocalDate startDate,LocalDate endDate){}
