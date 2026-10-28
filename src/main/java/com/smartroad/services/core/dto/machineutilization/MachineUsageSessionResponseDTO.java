package com.smartroad.services.core.dto.machineutilization;

import com.smartroad.services.common.enums.machineutilization.UsageSourceEnum;
import com.smartroad.services.common.enums.machineutilization.UsageStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * Response DTO describing one machine usage session returned to API consumers.
 *
 * @author Vishal
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineUsageSessionResponseDTO {

    private UUID id;
    private UUID organizationId;
    private UUID machineId;
    private String machineName;
    private UUID projectId;
    private String projectName;
    private LocalDate usageDate;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private BigDecimal runningHours;
    private BigDecimal idleHours;
    private UUID operatorWorkerId;
    private String operatorName;
    private UsageStatusEnum status;
    private UsageSourceEnum source;
    private BigDecimal fuelConsumed;
    private BigDecimal fuelCost;
    private String notes;
    private boolean open;
    private UUID createdBy;
    private UUID modifiedBy;
    private Date createdDate;
    private Date modifiedDate;
}
