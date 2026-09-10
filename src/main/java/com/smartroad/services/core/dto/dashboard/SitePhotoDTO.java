package com.smartroad.services.core.dto.dashboard;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A single site photo shown in the supervisor's photo strip.
 *
 * @author Vishal
 * @version 1.0
 */
public record SitePhotoDTO(
    UUID id,
    String photoType,
    String photoUrl,
    OffsetDateTime photoDate,
    String workArea,
    String description
) {}
