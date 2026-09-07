package com.smartroad.services;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

public class ClearDatabase {
  public static void clearAllData(DataSource dataSource) throws Exception {
    try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
      String[] truncateSql = {
          "TRUNCATE TABLE user_notifications CASCADE",
          "TRUNCATE TABLE user_roles CASCADE",
          "TRUNCATE TABLE refresh_tokens CASCADE",
          "TRUNCATE TABLE projects CASCADE",
          "TRUNCATE TABLE otp_records CASCADE",
          "TRUNCATE TABLE business_profile_contacts CASCADE",
          "TRUNCATE TABLE business_profile_services CASCADE",
          "TRUNCATE TABLE business_profile CASCADE",
          "TRUNCATE TABLE users CASCADE",
          "TRUNCATE TABLE organizations CASCADE"
      };

      for (String sql : truncateSql) {
        stmt.execute(sql);
        System.out.println("✓ Executed: " + sql);
      }

      String[] resetSeq = {
          "ALTER SEQUENCE users_id_seq RESTART WITH 1",
          "ALTER SEQUENCE organizations_id_seq RESTART WITH 1",
          "ALTER SEQUENCE projects_id_seq RESTART WITH 1",
          "ALTER SEQUENCE business_profile_id_seq RESTART WITH 1",
          "ALTER SEQUENCE otp_records_id_seq RESTART WITH 1"
      };

      for (String sql : resetSeq) {
        stmt.execute(sql);
        System.out.println("✓ Reset: " + sql);
      }

      System.out.println("\n✅ All database data cleared successfully!");
    }
  }
}
