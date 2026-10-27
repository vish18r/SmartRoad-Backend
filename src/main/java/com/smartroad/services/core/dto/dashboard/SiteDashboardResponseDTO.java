package com.smartroad.services.core.dto.dashboard;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Supervisor-facing view of a single site (project) on a single day.
 * Groups today's physical progress, the workforce on site, material stock held at
 * the site, the photos captured there, the day's diary entry, today's material
 * receipt/consumption, and a merged recent-activity feed.
 *
 * @author Vishal
 * @version 1.0
 */
public record SiteDashboardResponseDTO(
    UUID projectId,
    String projectName,
    String projectCode,
    LocalDate date,
    SiteWorkDTO work,
    SiteWorkforceDTO workforce,
    SiteMaterialsDTO materials,
    SitePhotosDTO photos,
    SiteDiarySummaryDTO diary,
    SiteMaterialsTodayDTO materialsToday,
    List<SiteActivityDTO> activity
) {}
