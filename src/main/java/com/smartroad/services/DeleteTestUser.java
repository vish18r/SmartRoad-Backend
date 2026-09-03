package com.smartroad.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DeleteTestUser {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        String url = "jdbc:postgresql://localhost:5432/nextenti_db";
        String user = "postgres";
        String password = "Vish@l2003";
        String phoneNumber = "7676966391";

        Class.forName("org.postgresql.Driver");
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String[] queries = {
                "DELETE FROM sr_otp WHERE phone_number = '" + phoneNumber + "'",
                "DELETE FROM sr_user_audit_logs WHERE user_id NOT IN (SELECT id FROM sr_users)",
                "DELETE FROM sr_users WHERE phone_number = '" + phoneNumber + "'",
                "DELETE FROM sr_sessions WHERE user_id NOT IN (SELECT id FROM sr_users)"
            };

            for (String query : queries) {
                int rows = conn.createStatement().executeUpdate(query);
                System.out.println("✓ Deleted " + rows + " rows: " + query);
            }
            System.out.println("\n✅ User " + phoneNumber + " deleted from database!");
        }
    }
}
