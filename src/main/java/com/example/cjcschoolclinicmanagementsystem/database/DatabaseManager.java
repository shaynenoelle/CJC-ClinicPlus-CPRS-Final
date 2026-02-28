package com.example.cjcschoolclinicmanagementsystem.database;

import com.example.cjcschoolclinicmanagementsystem.model.Patient;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    // Points to the physical SQLite file on your Dell Latitude
    private static final String URL = "jdbc:sqlite:cjclinic.db";

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static boolean validateLogin(String username, String password) {
        String sql = "SELECT * FROM Users WHERE username = ? AND password_hash = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println("Login DB Error: " + e.getMessage());
            return false;
        }
    }

    public static boolean addPatient(String id, String name, String course, String year, String gender, String contact, String bday, String history) {
        // SQL aligns with your 8-column table structure
        String sql = "INSERT INTO Patients(patient_id, full_name, course_dept, year_level, gender, contact_number, birthdate, medical_history) VALUES(?,?,?,?,?,?,?,?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);      // Student ID
            pstmt.setString(2, name);    // Full Name
            pstmt.setString(3, course);  // Course/Dept
            pstmt.setString(4, year);    // Year Level
            pstmt.setString(5, gender);  // Gender
            pstmt.setString(6, contact); // Contact Number
            pstmt.setString(7, bday);    // Birthdate
            pstmt.setString(8, history); // Medical History

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Add Patient DB Error: " + e.getMessage());
            return false;
        }
    }


    public static boolean updatePatient(String id, String name, String course, String year, String gender, String contact, String bday, String history) {
        String sql = "UPDATE Patients SET full_name = ?, course_dept = ?, year_level = ?, gender = ?, contact_number = ?, birthdate = ?, medical_history = ? WHERE patient_id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, course);
            pstmt.setString(3, year);
            pstmt.setString(4, gender);
            pstmt.setString(5, contact);
            pstmt.setString(6, bday);
            pstmt.setString(7, history);
            pstmt.setString(8, id);

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Update Patient DB Error: " + e.getMessage());
            return false;
        }
    }

    public static List<Patient> getAllPatients() {
        List<Patient> list = new ArrayList<>();
        // Selects all columns from Patients table
        String sql = "SELECT patient_id, full_name, course_dept, year_level, gender, contact_number, birthdate, medical_history FROM Patients";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Patient(
                        rs.getString("patient_id"),
                        rs.getString("full_name"),
                        rs.getString("course_dept"),
                        rs.getString("year_level"),
                        rs.getString("gender"),
                        rs.getString("contact_number"),
                        rs.getString("birthdate"),
                        rs.getString("medical_history")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Fetch Patients Error: " + e.getMessage());
        }
        return list;
    }
}