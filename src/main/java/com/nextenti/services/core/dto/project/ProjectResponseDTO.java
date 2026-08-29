package com.nextenti.services.core.dto.project;
import com.nextenti.services.common.enums.ProjectStatus; import java.math.BigDecimal; import java.time.LocalDate; import java.util.UUID;
public record ProjectResponseDTO(UUID id,UUID organizationId,UUID clientId,String code,String name,String description,String location,ProjectStatus status,BigDecimal budget,BigDecimal actualCost,BigDecimal progress,LocalDate startDate,LocalDate endDate,boolean archived){}
