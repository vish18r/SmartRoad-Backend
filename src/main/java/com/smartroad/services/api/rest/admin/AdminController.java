package com.smartroad.services.api.rest.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin controller for database maintenance operations.
 * Handles cleanup and testing utilities.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

  private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

  @Autowired
  private JdbcTemplate jdbcTemplate;

  /**
   * Clears all data from the database (for testing/development only).
   * Truncates all user-related tables and resets sequences.
   *
   * @return ResponseEntity with success message
   */
  @DeleteMapping(path = "/clear-database", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> clearDatabase() {
    logger.info("--Inside clearDatabase method--");

    try {
      String[] truncateSql = {
          "DELETE FROM business_profile_contacts",
          "DELETE FROM business_profile_services",
          "DELETE FROM business_profile",
          "DELETE FROM otp_records",
          "DELETE FROM refresh_tokens",
          "DELETE FROM user_roles",
          "DELETE FROM user_notifications",
          "DELETE FROM projects",
          "DELETE FROM users",
          "DELETE FROM organizations"
      };

      for (String sql : truncateSql) {
        try {
          jdbcTemplate.execute(sql);
          logger.info("✓ Executed: {}", sql);
        } catch (Exception e) {
          logger.warn("⚠ Skipped (table may not exist): {} - {}", sql, e.getMessage());
        }
      }

      String[] resetSeq = {
          "ALTER SEQUENCE users_id_seq RESTART WITH 1",
          "ALTER SEQUENCE organizations_id_seq RESTART WITH 1",
          "ALTER SEQUENCE projects_id_seq RESTART WITH 1",
          "ALTER SEQUENCE business_profile_id_seq RESTART WITH 1",
          "ALTER SEQUENCE otp_records_id_seq RESTART WITH 1"
      };

      for (String sql : resetSeq) {
        try {
          jdbcTemplate.execute(sql);
          logger.info("✓ Reset: {}", sql);
        } catch (Exception e) {
          logger.warn("⚠ Skipped (sequence may not exist): {} - {}", sql, e.getMessage());
        }
      }

      logger.info("All database data cleared successfully!");

      return ResponseEntity.status(HttpStatus.OK)
          .body(new AdminResponse("success", "All database data cleared successfully!"));

    } catch (Exception e) {
      logger.error("Error clearing database: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new AdminResponse("error", "Failed to clear database: " + e.getMessage()));
    }
  }

  /**
   * Simple response DTO for admin operations.
   */
  static class AdminResponse {
    public String status;
    public String message;

    public AdminResponse(String status, String message) {
      this.status = status;
      this.message = message;
    }
  }
}
