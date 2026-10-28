package com.smartroad.services.core.dto.machineutilization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Project-wise machine usage comparison row: aggregated running/idle hours, utilization, and
 * machine count for a single project over the requested reporting period.
 *
 * @author Vishal
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMachineUtilizationDTO {

    private UUID projectId;
    private String projectName;
    private long machinesUsed;
    private BigDecimal runningHours;
    private BigDecimal idleHours;

    private boolean utilizationAvailable;
    private BigDecimal utilizationPercent;

    private boolean fuelCostAvailable;
    private BigDecimal fuelCost;
}
