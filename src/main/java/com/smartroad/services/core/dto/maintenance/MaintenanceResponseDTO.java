package com.smartroad.services.core.dto.maintenance;

import com.smartroad.services.common.enums.maintenance.MaintenanceCategoryEnum;
import com.smartroad.services.common.enums.maintenance.MaintenancePriorityEnum;
import com.smartroad.services.common.enums.maintenance.MaintenanceRecurrenceEnum;
import com.smartroad.services.common.enums.maintenance.MaintenanceStatusEnum;
import com.smartroad.services.common.enums.maintenance.MaintenanceTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * Response DTO describing one maintenance record returned to API consumers. {@code status} is the
 * effective (derived) status - DUE/OVERDUE are computed at read time - while the {@code can*} flags
 * tell clients which workflow actions the server will currently accept.
 *
 * @author Vishal
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceResponseDTO {

    private UUID id;
    private UUID organizationId;
    private String maintenanceReference;
    private UUID machineId;
    private String machineName;
    private String machineType;
    private String machineRegistrationNumber;
    private String machineMaintenanceStatus;
    private UUID projectId;
    private String projectName;
    private String title;
    private MaintenanceTypeEnum maintenanceType;
    private MaintenanceCategoryEnum category;
    private String description;
    private MaintenancePriorityEnum priority;
    private MaintenanceStatusEnum status;
    private OffsetDateTime scheduledStart;
    private OffsetDateTime scheduledEnd;
    private OffsetDateTime dueBy;
    private OffsetDateTime actualStart;
    private OffsetDateTime actualEnd;
    private OffsetDateTime reportedAt;
    private UUID technicianWorkerId;
    private String technicianName;
    private BigDecimal estimatedCost;
    private BigDecimal actualCost;
    private Long downtimeMinutes;
    private String partsUsed;
    private String notes;
    private String cancellationReason;
    private MaintenanceRecurrenceEnum recurrenceType;
    private UUID parentMaintenanceId;
    private boolean canStart;
    private boolean canComplete;
    private boolean canCancel;
    private boolean editable;
    private UUID createdBy;
    private UUID modifiedBy;
    private Date createdDate;
    private Date modifiedDate;
}
