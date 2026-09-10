package com.smartroad.services.app.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Supplies the current user to Spring Data JPA auditing, populating the
 * {@code createdBy} and {@code modifiedBy} columns on every audited entity.
 *
 * <p>{@code @EnableJpaAuditing} only fills {@code @CreatedBy} / {@code @LastModifiedBy}
 * when an {@link AuditorAware} bean is present. Without this bean those columns stay
 * null, which fails outright on the tables whose Flyway migrations declare
 * {@code created_by UUID NOT NULL}.
 *
 * @author Vishal
 * @version 1.0
 */
@Component("auditorAware")
public class AuditorAwareImpl implements AuditorAware<UUID> {

    /**
     * Resolves the authenticated user's ID from the security context.
     * Returns empty for unauthenticated flows — such as the signup that creates the
     * very first user — so that auditing never blocks a legitimate anonymous write.
     *
     * @return the current user's UUID, or empty when no authenticated user is present
     */
    @Override
    @NonNull
    public Optional<UUID> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(userDetails.getUsername()));
        } catch (IllegalArgumentException e) {
            // The principal's username is normally the user's UUID; anything else is not an auditor.
            return Optional.empty();
        }
    }
}
