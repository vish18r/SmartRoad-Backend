package com.smartroad.services.core.dto.maintenance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * The set of maintenance reminders that would need to be dispatched at {@code generatedAt}: records
 * that are due (starting within the due window, not yet past their deadline) and records that are
 * overdue.
 *
 * @author Vishal
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRemindersDTO {

    private OffsetDateTime generatedAt;
    private List<MaintenanceReminderDTO> dueReminders;
    private List<MaintenanceReminderDTO> overdueReminders;
}
