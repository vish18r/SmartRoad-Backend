package com.smartroad.services.core.mapper.machineutilization;

import com.smartroad.services.core.dto.machineutilization.MachineUsageSessionResponseDTO;
import com.smartroad.services.core.dto.machineutilization.MachineUtilizationResponseDTO;
import com.smartroad.services.core.dto.machineutilization.ProjectMachineUtilizationDTO;
import com.smartroad.services.domain.entity.machine.MachineEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Assembles the per-machine {@link MachineUtilizationResponseDTO} from the machine entity and the
 * already-computed KPI figures. Written as a plain Spring component (manual field mapping) rather
 * than a MapStruct mapper, matching the hand-written mapping convention used by the sibling
 * machine and resource-planning modules in this codebase.
 *
 * @author Vishal
 * @version 1.0
 */
@Component
public class MachineUtilizationMapper {

    /**
     * Builds the per-machine utilization response DTO.
     *
     * @param machine the machine entity
     * @param periodStart the reporting period's inclusive start date
     * @param periodEnd the reporting period's inclusive end date
     * @param runningHours the computed total running hours, never null (zero when no sessions)
     * @param idleHours the computed total idle hours, never null (zero when no sessions)
     * @param availableHours the computed available hours, never null (zero when no sessions)
     * @param utilizationAvailable whether the utilization percentage could be computed
     * @param utilizationUnavailableReason the reason utilization is unavailable, or null when available
     * @param utilizationPercent the computed utilization percentage, or null when unavailable
     * @param fuelAvailable whether fuel figures could be computed
     * @param fuelUnavailableReason the reason fuel figures are unavailable, or null when available
     * @param fuelConsumed the computed total fuel consumed, or null when unavailable
     * @param fuelCost the computed total fuel cost, or null when unavailable
     * @param maintenanceCostAvailable whether maintenance cost is available
     * @param maintenanceCostUnavailableReason the reason maintenance cost is unavailable, or null when available
     * @param maintenanceCost the maintenance cost figure, or null when unavailable
     * @param operatingCostPerHourAvailable whether operating cost per hour could be computed
     * @param operatingCostPerHourUnavailableReason the reason it is unavailable, or null when available
     * @param operatingCostPerHour the computed operating cost per hour, or null when unavailable
     * @param maintenanceHistoryUnavailableReason the reason maintenance history is unavailable (always set; no history table exists)
     * @param projectUsage the project-wise usage breakdown for this machine
     * @param usageHistory the machine's usage sessions in the period
     * @return the assembled response DTO
     */
    public MachineUtilizationResponseDTO toResponseDTO(MachineEntity machine, LocalDate periodStart,
            LocalDate periodEnd, BigDecimal runningHours, BigDecimal idleHours, BigDecimal availableHours,
            boolean utilizationAvailable, String utilizationUnavailableReason, BigDecimal utilizationPercent,
            boolean fuelAvailable, String fuelUnavailableReason, BigDecimal fuelConsumed, BigDecimal fuelCost,
            boolean maintenanceCostAvailable, String maintenanceCostUnavailableReason, BigDecimal maintenanceCost,
            boolean operatingCostPerHourAvailable, String operatingCostPerHourUnavailableReason,
            BigDecimal operatingCostPerHour, String maintenanceHistoryUnavailableReason,
            List<ProjectMachineUtilizationDTO> projectUsage, List<MachineUsageSessionResponseDTO> usageHistory) {

        return MachineUtilizationResponseDTO.builder()
                .machineId(machine.getId())
                .machineName(machine.getName())
                .machineType(machine.getType())
                .status(machine.getStatus())
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .runningHours(runningHours)
                .idleHours(idleHours)
                .availableHours(availableHours)
                .utilizationAvailable(utilizationAvailable)
                .utilizationUnavailableReason(utilizationUnavailableReason)
                .utilizationPercent(utilizationPercent)
                .fuelAvailable(fuelAvailable)
                .fuelUnavailableReason(fuelUnavailableReason)
                .fuelConsumed(fuelConsumed)
                .fuelCost(fuelCost)
                .maintenanceCostAvailable(maintenanceCostAvailable)
                .maintenanceCostUnavailableReason(maintenanceCostUnavailableReason)
                .maintenanceCost(maintenanceCost)
                .operatingCostPerHourAvailable(operatingCostPerHourAvailable)
                .operatingCostPerHourUnavailableReason(operatingCostPerHourUnavailableReason)
                .operatingCostPerHour(operatingCostPerHour)
                .maintenanceHistoryAvailable(false)
                .maintenanceHistoryUnavailableReason(maintenanceHistoryUnavailableReason)
                .projectUsage(projectUsage)
                .usageHistory(usageHistory)
                .build();
    }
}
