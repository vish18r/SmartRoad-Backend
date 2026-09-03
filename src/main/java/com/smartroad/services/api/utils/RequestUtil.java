package com.smartroad.services.api.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
     * This is a placeholder implementation that uses a random UUID.
     * In production, this should extract the actual user ID from the JWT token or SecurityContext.
     *
     * @return the user ID as UUID
     */
    public static UUID extractUserId() {
        // TODO: Extract the actual user ID from the authenticated principal
        // For now, using a placeholder that generates a random UUID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // In a real implementation, extract userId from JWT claims or principal
            // Example: return UUID.fromString((String) authentication.getPrincipal());
        }
        return UUID.randomUUID();
    }
}
