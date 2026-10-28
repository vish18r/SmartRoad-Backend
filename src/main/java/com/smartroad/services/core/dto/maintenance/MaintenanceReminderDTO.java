package com.smartroad.services.core.dto.maintenance;

import com.smartroad.services.common.enums.maintenance.MaintenancePriorityEnum;
import com.smartroad.services.common.enums.maintenance.MaintenanceReminderTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * One computed reminder describing a notification that would need to be sent for a maintenance
 * record right now. Reminders are computed on demand; no scheduler dispatches them (see
 * docs/MAINTENANCE_MANAGEMENT.md).
 *
 * @author Vishal
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceReminderDTO {

    private MaintenanceReminderTypeEnum reminderType;
    private UUID maintenanceId;
    private String maintenanceReference;
    private UUID machineId;
    private String machineName;
    private String title;
    private MaintenancePriorityEnum priority;
    private OffsetDateTime scheduledStart;
    private OffsetDateTime dueBy;
    private String technicianName;
    private String message;
}
