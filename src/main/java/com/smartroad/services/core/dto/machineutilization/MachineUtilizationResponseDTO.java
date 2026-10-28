package com.smartroad.services.core.dto.machineutilization;

import com.smartroad.services.common.enums.machine.MachineStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Per-machine utilization analytics for a reporting period: running/idle/available hours,
 * utilization percentage (when computable), fuel and maintenance cost figures (when available),
 * operating cost per hour, project-wise usage split, and usage history. Any metric without real
 * source data is returned as null with an {@code *Available = false} flag and a reason, rather
 * than a fabricated value.
 *
 * @author Vishal
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineUtilizationResponseDTO {

    private UUID machineId;
    private String machineName;
    private String machineType;
    private MachineStatusEnum status;

    private LocalDate periodStart;
    private LocalDate periodEnd;

    private BigDecimal runningHours;
    private BigDecimal idleHours;
    private BigDecimal availableHours;

    private boolean utilizationAvailable;
    private String utilizationUnavailableReason;
    private BigDecimal utilizationPercent;

    private boolean fuelAvailable;
    private String fuelUnavailableReason;
    private BigDecimal fuelConsumed;
    private BigDecimal fuelCost;

    private boolean maintenanceCostAvailable;
    private String maintenanceCostUnavailableReason;
    private BigDecimal maintenanceCost;

    private boolean operatingCostPerHourAvailable;
    private String operatingCostPerHourUnavailableReason;
    private BigDecimal operatingCostPerHour;

    private boolean maintenanceHistoryAvailable;
    private String maintenanceHistoryUnavailableReason;

    private List<ProjectMachineUtilizationDTO> projectUsage;
    private List<MachineUsageSessionResponseDTO> usageHistory;
}
