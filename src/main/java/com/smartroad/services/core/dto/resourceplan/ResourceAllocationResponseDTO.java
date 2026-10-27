package com.smartroad.services.core.dto.resourceplan;

import com.smartroad.services.common.enums.resourceplan.AllocationStatusEnum;
import com.smartroad.services.common.enums.resourceplan.ResourceTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Response DTO describing a single resource allocation returned to API consumers.
 *
 * @author Vishal
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceAllocationResponseDTO {

    private UUID id;
    private UUID resourcePlanId;
    private ResourceTypeEnum resourceType;
    private UUID resourceId;
    private String resourceLabel;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate allocationStartDate;
    private LocalDate allocationEndDate;
    private BigDecimal plannedQuantity;
    private String unit;
    private UUID assignedWorkerId;
    private String taskDescription;
    private AllocationStatusEnum status;
    private String notes;
}
