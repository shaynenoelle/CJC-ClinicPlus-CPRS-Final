package com.example.cjcschoolclinicmanagementsystem.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {
    // This points to the physical file on your Dell Latitude
    private static final String URL = "jdbc:sqlite:cjclinic.db";

    // Method to check if the user exists in the 'Users' table
    public static boolean validateLogin(String username, String password) {
        String sql = "SELECT * FROM Users WHERE username = ? AND password_hash = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Returns true if a record matches

        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
            return false;
        }
    }
}