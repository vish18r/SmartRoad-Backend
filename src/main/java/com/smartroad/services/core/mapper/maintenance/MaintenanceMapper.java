package com.smartroad.services.core.mapper.maintenance;

import com.smartroad.services.common.enums.maintenance.MaintenanceReminderTypeEnum;
import com.smartroad.services.common.enums.maintenance.MaintenanceStatusEnum;
import com.smartroad.services.core.dto.maintenance.MaintenanceReminderDTO;
import com.smartroad.services.core.dto.maintenance.MaintenanceResponseDTO;
import com.smartroad.services.domain.entity.machine.MachineEntity;
import com.smartroad.services.domain.entity.maintenance.MaintenanceRecordEntity;
import org.springframework.stereotype.Component;

/**
 * Converts {@link MaintenanceRecordEntity} rows to maintenance response/reminder DTOs. Written as a
 * plain Spring component (manual field mapping) rather than a MapStruct mapper to match the
 * hand-written mapping convention used by the sibling machine and machine-utilization modules, and
 * because the response needs the derived effective status, workflow flags, and resolved machine and
 * project display data that do not exist on the entity itself.
 *
 * @author Vishal
 * @version 1.0
 */
@Component
public class MaintenanceMapper {

    /**
     * Converts a maintenance entity to its response DTO.
     *
     * @param entity the source entity
     * @param effectiveStatus the derived status (DUE/OVERDUE computed from the schedule and current time)
     * @param machine the referenced machine, or null if it can no longer be resolved
     * @param projectName the resolved project name, or null if the record has no project or it is not resolvable
     * @return the mapped response DTO
     */
    public MaintenanceResponseDTO toResponseDTO(MaintenanceRecordEntity entity, MaintenanceStatusEnum effectiveStatus,
                                                MachineEntity machine, String projectName) {
        boolean terminal = entity.isTerminal();
        boolean inProgress = entity.getStatus() == MaintenanceStatusEnum.IN_PROGRESS;
        return MaintenanceResponseDTO.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganizationId())
                .maintenanceReference(entity.getMaintenanceReference())
                .machineId(entity.getMachineId())
                .machineName(machine == null ? null : machine.getName())
                .machineType(machine == null ? null : machine.getType())
                .machineRegistrationNumber(machine == null ? null : machine.getRegistrationNumber())
                .machineMaintenanceStatus(machine == null ? null : machine.getMaintenanceStatus())
                .projectId(entity.getProjectId())
                .projectName(projectName)
                .title(entity.getTitle())
                .maintenanceType(entity.getMaintenanceType())
                .category(entity.getCategory())
                .description(entity.getDescription())
                .priority(entity.getPriority())
                .status(effectiveStatus)
                .scheduledStart(entity.getScheduledStart())
                .scheduledEnd(entity.getScheduledEnd())
                .dueBy(entity.getDueBy())
                .actualStart(entity.getActualStart())
                .actualEnd(entity.getActualEnd())
                .reportedAt(entity.getReportedAt())
                .technicianWorkerId(entity.getTechnicianWorkerId())
                .technicianName(entity.getTechnicianName())
                .estimatedCost(entity.getEstimatedCost())
                .actualCost(entity.getActualCost())
                .downtimeMinutes(entity.getDowntimeMinutes())
                .partsUsed(entity.getPartsUsed())
                .notes(entity.getNotes())
                .cancellationReason(entity.getCancellationReason())
                .recurrenceType(entity.getRecurrenceType())
                .parentMaintenanceId(entity.getParentMaintenanceId())
                .canStart(entity.getStatus() == MaintenanceStatusEnum.SCHEDULED)
                .canComplete(inProgress && entity.getActualStart() != null)
                .canCancel(!terminal)
                .editable(!terminal)
                .createdBy(entity.getCreatedBy())
                .modifiedBy(entity.getModifiedBy())
                .createdDate(entity.getDateCreated())
                .modifiedDate(entity.getDateModified())
                .build();
    }

    /**
     * Converts a maintenance entity to a computed reminder DTO.
     *
     * @param entity the source entity
     * @param reminderType the reminder event type
     * @param machineName the resolved machine name, or null if not resolvable
     * @param message the human-readable reminder message
     * @return the mapped reminder DTO
     */
    public MaintenanceReminderDTO toReminderDTO(MaintenanceRecordEntity entity, MaintenanceReminderTypeEnum reminderType,
                                                String machineName, String message) {
        return MaintenanceReminderDTO.builder()
                .reminderType(reminderType)
                .maintenanceId(entity.getId())
                .maintenanceReference(entity.getMaintenanceReference())
                .machineId(entity.getMachineId())
                .machineName(machineName)
                .title(entity.getTitle())
                .priority(entity.getPriority())
                .scheduledStart(entity.getScheduledStart())
                .dueBy(entity.getDueBy())
                .technicianName(entity.getTechnicianName())
                .message(message)
                .build();
    }
}
