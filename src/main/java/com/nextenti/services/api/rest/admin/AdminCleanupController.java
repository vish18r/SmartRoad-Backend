package com.nextenti.services.api.rest.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin utility controller for development/testing cleanup.
 * Delete test users and related data.
 *
 * WARNING: This endpoint should be removed in production!
 */
@RestController
@RequestMapping("/api/v1/admin/cleanup")
@RequiredArgsConstructor
@Slf4j
public class AdminCleanupController {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Deletes a test user by phone number along with all related data.
     * Development only - remove in production!
     *
     * @param phoneNumber the phone number to delete
     * @return ResponseEntity with success message
     */
    @DeleteMapping(path = "/user/phone/{phoneNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Object> deleteUserByPhone(@PathVariable String phoneNumber) {
        log.warn("Deleting test user with phone: {}", phoneNumber);

        try {
            jdbcTemplate.execute("DELETE FROM sr_otp WHERE phone_number = '" + phoneNumber + "'");
            jdbcTemplate.execute("DELETE FROM sr_user_audit_logs WHERE user_id NOT IN (SELECT id FROM sr_users)");
            jdbcTemplate.execute("DELETE FROM sr_users WHERE phone_number = '" + phoneNumber + "'");
            jdbcTemplate.execute("DELETE FROM sr_sessions WHERE user_id NOT IN (SELECT id FROM sr_users)");

            log.info("Successfully deleted test user: {}", phoneNumber);
            return ResponseEntity.ok().body(new Object() {
                public String message = "User deleted successfully";
                public String phoneNumber_deleted = phoneNumber;
            });
        } catch (Exception e) {
            log.error("Failed to delete user {}: {}", phoneNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Object() {
                        public String error = e.getMessage();
                    });
        }
    }
}
