package com.smartroad.services.core.dto.resourceplan;

import com.smartroad.services.common.enums.resourceplan.ResourcePlanStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

/**
 * Response DTO describing a resource plan returned to API consumers.
 *
 * @author Vishal
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourcePlanResponseDTO {

    private UUID id;
    private UUID organizationId;
    private UUID projectId;
    private String projectName;
    private String planName;
    private LocalDate planDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private UUID supervisorWorkerId;
    private String supervisorName;
    private String description;
    private ResourcePlanStatusEnum status;
    private String notes;
    private boolean hasConflicts;
    private int workersCount;
    private int machinesCount;
    private int materialsCount;
    private int vehiclesCount;
    private UUID createdBy;
    private UUID modifiedBy;
    private Date createdDate;
    private Date modifiedDate;
}
