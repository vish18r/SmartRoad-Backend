package com.smartroad.services.core.dto.resourceplan;

import com.smartroad.services.common.enums.resourceplan.ConflictSeverityEnum;
import com.smartroad.services.common.enums.resourceplan.ConflictTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Describes a single conflict detected while planning or validating resource allocations.
 *
 * @author Vishal
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceConflictDTO {

    private ConflictTypeEnum conflictType;
    private UUID resourceId;
    private String resourceLabel;
    private UUID conflictingPlanId;
    private String conflictingPlanName;
    private UUID conflictingProjectId;
    private String conflictingProjectName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private ConflictSeverityEnum severity;
    private String message;
}
