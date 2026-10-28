package com.smartroad.services.core.dto.maintenance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Dashboard metrics for the maintenance page. Snapshot counts ({@code scheduled}, {@code dueSoon},
 * {@code overdue}, {@code inProgress}, {@code machinesUnderMaintenance}) describe the organization's
 * current state regardless of period; period metrics ({@code totalRecords}, {@code completed},
 * {@code cancelled}, costs, downtime) cover records whose scheduled start falls inside
 * [{@code periodStart}, {@code periodEnd}]. {@code totalCostInPeriod} is the sum of actual cost
 * where recorded, falling back to estimated cost for records with no actual cost yet (cancelled
 * records excluded); the two halves are also returned separately.
 *
 * @author Vishal
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceSummaryDTO {

    private LocalDate periodStart;
    private LocalDate periodEnd;

    private long totalMachines;

    private long scheduled;
    private long dueSoon;
    private long overdue;
    private long inProgress;
    private long machinesUnderMaintenance;

    private long totalRecords;
    private long completed;
    private long cancelled;

    private BigDecimal totalCostInPeriod;
    private BigDecimal actualCostInPeriod;
    private BigDecimal estimatedCostFallbackInPeriod;
    private long totalDowntimeMinutesInPeriod;
}
