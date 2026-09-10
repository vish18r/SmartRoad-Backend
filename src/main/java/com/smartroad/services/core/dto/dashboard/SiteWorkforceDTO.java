package com.smartroad.services.core.dto.dashboard;

import java.math.BigDecimal;

/**
 * Who is on site today: the posted headcount, the attendance register for the day,
 * and live check-in state from GPS tracking.
 *
 * @author Vishal
 * @version 1.0
 */
public record SiteWorkforceDTO(
    long assignedWorkers,
    long present,
    long absent,
    long halfDay,
    long onLeave,
    long marked,
    long checkedIn,
    long checkedOut,
    long stillOnSite,
    BigDecimal hoursWorked
) {}
