package com.smartroad.services.core.dto.dashboard;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A single entry in the site's recent-activity feed, merged from check-ins, material
 * movements, photo uploads, and diary updates.
 *
 * @author Vishal
 * @version 1.0
 */
public record SiteActivityDTO(
    String id,
    String type,
    String description,
    OffsetDateTime timestamp,
    UUID actorId
) {}
