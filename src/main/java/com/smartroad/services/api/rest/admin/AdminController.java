package com.smartroad.services.api.rest.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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
   * Lists all users currently in the database (for testing/development only).
   *
   * @return ResponseEntity with all rows in sr_users
   */
  @GetMapping(path = "/list-users", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> listUsers() {
    logger.info("--Inside listUsers method--");

    try {
      List<Map<String, Object>> rows = jdbcTemplate.queryForList(
          "SELECT id, email_id, phone_number, status, email_verified_yn, date_created FROM sr_users ORDER BY date_created DESC");
      logger.info("Found {} total user row(s)", rows.size());
      return ResponseEntity.status(HttpStatus.OK).body(rows);
    } catch (Exception e) {
      logger.error("Error listing users: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new AdminResponse("error", "Failed to list users: " + e.getMessage()));
    }
  }

  /**
   * Looks up the exact user record for an email (for testing/development only).
   * Table/column names must match the real schema: sr_users(email_id).
   *
   * @param email the email address to look up
   * @return ResponseEntity with the matching row(s), or an empty list if none exist
   */
  @GetMapping(path = "/find-user-by-email", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findUserByEmail(@RequestParam String email) {
    logger.info("--Inside findUserByEmail method--");

    try {
      List<Map<String, Object>> rows = jdbcTemplate.queryForList(
          "SELECT id, email_id, phone_number, status, email_verified_yn, date_created FROM sr_users WHERE email_id = ?",
          email);
      logger.info("Found {} row(s) for email lookup", rows.size());
      return ResponseEntity.status(HttpStatus.OK).body(rows);
    } catch (Exception e) {
      logger.error("Error looking up user: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new AdminResponse("error", "Failed to look up user: " + e.getMessage()));
    }
  }

  /**
   * Deletes a single user by email (for testing/development only).
   * Targets the real schema table/column: sr_users(email_id).
   *
   * @param email the email address of the user to delete
   * @return ResponseEntity with success message
   */
  @DeleteMapping(path = "/delete-user-by-email", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> deleteUserByEmail(@RequestParam String email) {
    logger.info("--Inside deleteUserByEmail method--");

    try {
      int rowsDeleted = jdbcTemplate.update("DELETE FROM sr_users WHERE email_id = ?", email);

      if (rowsDeleted > 0) {
        logger.info("✓ Deleted user with email: {}", email);
        return ResponseEntity.status(HttpStatus.OK)
            .body(new AdminResponse("success", "User with email " + email + " deleted successfully!"));
      }

      logger.warn("No user found with email: {}", email);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new AdminResponse("error", "No user found with email: " + email));

    } catch (Exception e) {
      logger.error("Error deleting user: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new AdminResponse("error", "Failed to delete user: " + e.getMessage()));
    }
  }

  /**
   * Clears all data from the database (for testing/development only).
   * Deletes rows from every real schema table (sr_ prefix) and resets sequences.
   *
   * @return ResponseEntity with success message
   */
  @DeleteMapping(path = "/clear-database", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> clearDatabase() {
    logger.info("--Inside clearDatabase method--");

    try {
      String[] deleteSql = {
          "DELETE FROM sr_sessions",
          "DELETE FROM sr_otps",
          "DELETE FROM sr_user_audit_logs",
          "DELETE FROM sr_business_profiles",
          "DELETE FROM sr_projects",
          "DELETE FROM sr_users",
          "DELETE FROM sr_organizations"
      };

      for (String sql : deleteSql) {
        try {
          int count = jdbcTemplate.update(sql);
          logger.info("✓ Executed: {} ({} rows)", sql, count);
        } catch (Exception e) {
          logger.warn("⚠ Skipped (table may not exist): {} - {}", sql, e.getMessage());
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
