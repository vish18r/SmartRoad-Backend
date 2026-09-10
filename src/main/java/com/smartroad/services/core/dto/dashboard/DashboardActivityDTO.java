package com.smartroad.services.core.dto.dashboard;

import java.util.Date;
import java.util.UUID;

/**
 * A single entry in the dashboard's recent activity feed.
 *
 * @author Vishal
 * @version 1.0
 */
public record DashboardActivityDTO(
    UUID id,
    String type,
    String description,
    Date timestamp
) {}
