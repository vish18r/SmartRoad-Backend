package com.smartroad.services.core.mapper.machineutilization;

import com.smartroad.services.core.dto.machineutilization.MachineUsageSessionResponseDTO;
import com.smartroad.services.domain.entity.machineutilization.MachineUsageSessionEntity;
import org.springframework.stereotype.Component;

/**
 * Converts {@link MachineUsageSessionEntity} rows to {@link MachineUsageSessionResponseDTO}.
 * Written as a plain Spring component (manual field mapping) rather than a MapStruct mapper to
 * match the hand-written mapping convention already used by the sibling machine and
 * resource-planning modules in this codebase, and because the DTO needs resolved machine/project
 * display names that do not exist on the entity itself.
 *
 * @author Vishal
 * @version 1.0
 */
@Component
public class MachineUsageSessionMapper {

    /**
     * Converts a session entity to its response DTO.
     *
     * @param entity the source entity
     * @param machineName the resolved machine name, or null if not resolvable
     * @param projectName the resolved project name, or null if the session has no project or it is not resolvable
     * @return the mapped response DTO
     */
    public MachineUsageSessionResponseDTO toResponseDTO(MachineUsageSessionEntity entity, String machineName,
                                                        String projectName) {
        return MachineUsageSessionResponseDTO.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganizationId())
                .machineId(entity.getMachineId())
                .machineName(machineName)
                .projectId(entity.getProjectId())
                .projectName(projectName)
                .usageDate(entity.getUsageDate())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .runningHours(entity.getRunningHours())
                .idleHours(entity.getIdleHours())
                .operatorWorkerId(entity.getOperatorWorkerId())
                .operatorName(entity.getOperatorName())
                .status(entity.getStatus())
                .source(entity.getSource())
                .fuelConsumed(entity.getFuelConsumed())
                .fuelCost(entity.getFuelCost())
                .notes(entity.getNotes())
                .open(entity.isOpen())
                .createdBy(entity.getCreatedBy())
                .modifiedBy(entity.getModifiedBy())
                .createdDate(entity.getDateCreated())
                .modifiedDate(entity.getDateModified())
                .build();
    }
}
