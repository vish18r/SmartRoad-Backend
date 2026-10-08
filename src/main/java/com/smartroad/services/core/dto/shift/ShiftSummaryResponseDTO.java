package com.smartroad.services.core.dto.shift;

/**
 * Aggregated shift management figures for the dashboard summary cards. Every figure is computed
 * from persisted rows at request time; nothing here is cached or estimated.
 *
 * @param totalShiftTemplates the count of shift templates in the organization, any status
 * @param activeShifts the count of ACTIVE shift templates
 * @param inactiveShifts the count of INACTIVE shift templates
 * @param workersAssigned the count of distinct workers holding an ACTIVE assignment that covers today
 * @param unassignedWorkers the count of active organization workers with no ACTIVE assignment covering today
 */
public record ShiftSummaryResponseDTO(
    long totalShiftTemplates,
    long activeShifts,
    long inactiveShifts,
    long workersAssigned,
    long unassignedWorkers
) {}
