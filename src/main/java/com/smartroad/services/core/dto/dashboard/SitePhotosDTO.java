package com.smartroad.services.core.dto.dashboard;

import java.util.List;

/**
 * Photographic record captured on the site today.
 *
 * @author Vishal
 * @version 1.0
 */
public record SitePhotosDTO(
    long photosToday,
    List<SitePhotoDTO> recent
) {}
