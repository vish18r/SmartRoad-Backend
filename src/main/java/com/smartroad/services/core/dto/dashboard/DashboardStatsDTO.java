package com.smartroad.services.core.dto.dashboard;

/**
 * Dashboard statistics DTO containing overview metrics.
 *
 * @author Vishal
 * @version 1.0
 */
public record DashboardStatsDTO(
    long activeProjects,
    long completedProjects,
    long activeWorkers,
    double totalExpenses,
    String status
) {}
