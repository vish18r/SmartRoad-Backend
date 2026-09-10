package com.smartroad.services.core.dto.dashboard;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Supervisor-facing view of a single site (project) on a single day.
 * Groups today's physical progress, the workforce on site, material stock held at
 * the site, and the photos captured there.
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
    SitePhotosDTO photos
) {}
