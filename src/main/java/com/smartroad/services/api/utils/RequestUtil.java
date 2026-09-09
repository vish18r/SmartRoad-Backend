package com.smartroad.services.api.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Utility class for extracting information from HTTP requests and the security context.
 * Provides methods to access authenticated user information.
 */
@Component
public class RequestUtil {

    /**
     * Extracts the user ID from the authenticated principal.
     * {@link com.smartroad.services.core.service.auth.CustomUserDetailsService} builds the
     * principal's username as the user's UUID string, and {@link com.smartroad.services.core.service.auth.JwtService}
     * carries that same value in the JWT subject claim, so the authenticated principal's
     * username is always the current user's ID.
     *
     * @return the authenticated user's ID as UUID
     * @throws IllegalStateException if no authenticated user is present in the security context
     */
    public static UUID extractUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof UserDetails userDetails) {
            return UUID.fromString(userDetails.getUsername());
        }
        throw new IllegalStateException("No authenticated user found in security context");
    }
}
