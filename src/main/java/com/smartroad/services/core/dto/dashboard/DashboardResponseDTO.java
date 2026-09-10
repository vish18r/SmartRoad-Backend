package com.smartroad.services.core.dto.dashboard;

import java.util.List;

/**
 * Aggregate dashboard payload combining the organization's statistics with a recent activity feed.
 * Returned by the dashboard landing endpoint.
 *
 * @author Vishal
 * @version 1.0
 */
public record DashboardResponseDTO(
    DashboardStatsDTO stats,
    List<DashboardActivityDTO> recentActivities
) {}
