package com.smartroad.services.core.dto.machineutilization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Organization/project-level machine utilization KPI summary for a reporting period. Every
 * numeric metric whose source data does not exist is reported as a null value accompanied by an
 * {@code *Available = false} flag and a {@code *UnavailableReason} explaining why, instead of a
 * fabricated number.
 *
 * @author Vishal
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineUtilizationSummaryDTO {

    private LocalDate periodStart;
    private LocalDate periodEnd;
    private UUID projectId;

    private long totalMachines;
    private long machinesUsedInPeriod;

    private BigDecimal totalRunningHours;
    private BigDecimal totalIdleHours;

    private boolean utilizationAvailable;
    private String utilizationUnavailableReason;
    private BigDecimal averageUtilizationPercent;

    private boolean fuelAvailable;
    private String fuelUnavailableReason;
    private BigDecimal totalFuelConsumed;
    private BigDecimal totalFuelCost;

    private boolean maintenanceCostAvailable;
    private String maintenanceCostUnavailableReason;
    private BigDecimal totalMaintenanceCost;

    private boolean operatingCostPerHourAvailable;
    private String operatingCostPerHourUnavailableReason;
    private BigDecimal averageOperatingCostPerHour;
}
