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

    public static void ensureTypeColumnExists() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            // Try to add the type column if it doesn't exist
            stmt.execute("ALTER TABLE Patients ADD COLUMN type TEXT DEFAULT 'Student'");
        } catch (SQLException e) {
            // Column may already exist, that's fine
            if (!e.getMessage().contains("duplicate column")) {
                System.err.println("Column check error: " + e.getMessage());
            }
        }
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

    public static boolean addPatient(String patientId, String fullName, String age, String course, String year,
                                     String gender, String college, String contact, String email, String birthdate, String type,
                                     String emergencyName, String relationship, String emergencyContact,
                                     String allergies, String conditions, String bloodType, String height, String weight, String medicalRemarks) {
        ensureTypeColumnExists();
        String sql = "INSERT INTO Patients(patient_id, full_name, age, course_dept, year_level, gender, college, contact_number, email, birthdate, type, emergency_name, relationship, emergency_contact, allergies, conditions, blood_type, height, weight, medical_remarks) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, patientId);
            pstmt.setString(2, fullName);
            pstmt.setString(3, age);
            pstmt.setString(4, course);
            pstmt.setString(5, year);
            pstmt.setString(6, gender);
            pstmt.setString(7, college);
            pstmt.setString(8, contact);
            pstmt.setString(9, email);
            pstmt.setString(10, birthdate);
            pstmt.setString(11, type);
            pstmt.setString(12, emergencyName);
            pstmt.setString(13, relationship);
            pstmt.setString(14, emergencyContact);
            pstmt.setString(15, allergies);
            pstmt.setString(16, conditions);
            pstmt.setString(18, height);
            pstmt.setString(19, weight);
            pstmt.setString(20, medicalRemarks);

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Add Patient DB Error: " + e.getMessage());
            return false;
        }
    }


    public static boolean updatePatient(String id, String name, String age, String course, String year,
                                        String gender, String college, String contact, String email,
                                        String birthdate, String type, String emergencyName, String relationship,
                                        String emergencyContact, String allergies, String conditions,
                                        String bloodType, String height, String weight, String medicalRemarks) {
        String sql = "UPDATE Patients SET full_name = ?, age = ?, course_dept = ?, year_level = ?, gender = ?, " +
                     "college = ?, contact_number = ?, email = ?, birthdate = ?, type = ?, emergency_name = ?, " +
                     "relationship = ?, emergency_contact = ?, allergies = ?, conditions = ?, blood_type = ?, " +
                     "height = ?, weight = ?, medical_remarks = ? WHERE patient_id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, age);
            pstmt.setString(3, course);
            pstmt.setString(4, year);
            pstmt.setString(5, gender);
            pstmt.setString(6, college);
            pstmt.setString(7, contact);
            pstmt.setString(8, email);
            pstmt.setString(9, birthdate);
            pstmt.setString(10, type);
            pstmt.setString(11, emergencyName);
            pstmt.setString(12, relationship);
            pstmt.setString(13, emergencyContact);
            pstmt.setString(14, allergies);
            pstmt.setString(15, conditions);
            pstmt.setString(16, bloodType);
            pstmt.setString(17, height);
            pstmt.setString(18, weight);
            pstmt.setString(19, medicalRemarks);
            pstmt.setString(20, id);
            // pstmt.setString(21, address); // for address update to be reviewed later

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Update Patient DB Error: " + e.getMessage());
            return false;
        }
    }

    public static List<Patient> getAllPatients() {
        List<Patient> list = new ArrayList<>();
        ensureTypeColumnExists();
        String sql = "SELECT patient_id, full_name, age, course_dept, year_level, gender, college, contact_number, email, birthdate, type, emergency_name, relationship, emergency_contact, allergies, conditions, blood_type, height, weight, medical_remarks FROM Patients";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Patient(
                        rs.getString("patient_id"),
                        rs.getString("full_name"),
                        rs.getString("age"),
                        rs.getString("course_dept"),
                        rs.getString("year_level"),
                        rs.getString("gender"),
                        rs.getString("college"),
                        rs.getString("contact_number"),
                        rs.getString("email"),
                        rs.getString("birthdate"),
                        rs.getString("type") != null ? rs.getString("type") : "Student",
                        rs.getString("emergency_name"),
                        rs.getString("relationship"),
                        rs.getString("emergency_contact"),
                        rs.getString("allergies"),
                        rs.getString("conditions"),
                        rs.getString("blood_type"),
                        rs.getString("height"),
                        rs.getString("weight"),
                        rs.getString("medical_remarks")
                       // add here for patient address if needed in the future
                ));
            }
        } catch (SQLException e) {
            System.err.println("Fetch Patients Error: " + e.getMessage());
        }
        return list;
    }
}