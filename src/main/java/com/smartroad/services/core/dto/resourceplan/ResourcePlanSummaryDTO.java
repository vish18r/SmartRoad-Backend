package com.smartroad.services.core.dto.resourceplan;

/**
 * Summary DTO reporting resource-planning figures for a project (or an organization) on a date.
 *
 * @author Vishal
 * @version 1.0
 */
public record ResourcePlanSummaryDTO(
        long totalPlans,
        long workersPlanned,
        long machinesAllocated,
        long vehiclesAllocated,
        long materialsPlanned,
        long plansWithConflicts
) {
}
