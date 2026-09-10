package com.smartroad.services.core.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

/**
 * Dashboard statistics DTO containing overview metrics for a single organization.
 * Covers project counts, financial totals, road progress, workforce headcount, and a
 * per-project progress breakdown for the dashboard's project widget.
 *
 * @author Vishal
 * @version 1.0
 */
public record DashboardStatsDTO(
    long totalProjects,
    long activeProjects,
    long completedProjects,
    long onHoldProjects,
    BigDecimal totalBudget,
    BigDecimal actualCost,
    long totalRoads,
    long completedRoads,
    long totalWorkers,
    long activeWorkers,
    double totalExpenses,
    String status,
    List<DashboardProjectDTO> projectsData
) {}
