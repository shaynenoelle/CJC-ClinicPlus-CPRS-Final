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

    // ── LOGBOOK: Check-in a patient ──────────────────────────────
    public static boolean checkInPatient(String patientId, String timeIn, String purpose) {
        String sql = "INSERT INTO Visits(patient_id, visit_date, time_in, purpose) VALUES(?, DATE('now'), ?, ?)";
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, patientId);
            p.setString(2, timeIn);
            p.setString(3, purpose);
            p.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("CheckIn Error: " + e.getMessage());
            return false;
        }
    }

    // ── LOGBOOK: Get today's visitors ────────────────────────────
    public static List<java.util.Map<String, String>> getTodayVisits() {
        List<java.util.Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT v.visit_id, v.patient_id, p.full_name, v.time_in, v.time_out, v.purpose " +
                "FROM Visits v JOIN Patients p ON v.patient_id = p.patient_id " +
                "WHERE v.visit_date = DATE('now') ORDER BY v.time_in DESC";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                java.util.Map<String, String> row = new java.util.LinkedHashMap<>();
                row.put("visit_id",   rs.getString("visit_id"));
                row.put("patient_id", rs.getString("patient_id"));
                row.put("full_name",  rs.getString("full_name"));
                row.put("time_in",    rs.getString("time_in"));
                row.put("time_out",   rs.getString("time_out"));
                row.put("purpose",    rs.getString("purpose"));
                list.add(row);
            }
        } catch (SQLException e) {
            System.err.println("TodayVisits Error: " + e.getMessage());
        }
        return list;
    }

    // ── LOGBOOK: Check out a patient ─────────────────────────────
    public static boolean checkOutPatient(int visitId, String timeOut) {
        String sql = "UPDATE Visits SET time_out = ? WHERE visit_id = ?";
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, timeOut);
            p.setInt(2, visitId);
            p.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("CheckOut Error: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateTimeIn(int visitId, String timeIn) {
        String sql = "UPDATE Visits SET time_in = ? WHERE visit_id = ?";
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, timeIn);
            p.setInt(2, visitId);
            p.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("UpdateTimeIn Error: " + e.getMessage());
            return false;
        }
    }

    // ── ATTEND: Save full visit record ───────────────────────────
    public static boolean saveVisitRecord(int visitId, String temperature, String bloodPressure,
                                          String heartRate, String oxygenSat, String complaint,
                                          String diagnosis, String treatment, String remarks) {
        String sql = "UPDATE Visits SET temperature=?, blood_pressure=?, heart_rate=?, " +
                "oxygen_sat=?, chief_complaint=?, diagnosis=?, treatment=?, remarks=? " +
                "WHERE visit_id=?";
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, temperature);
            p.setString(2, bloodPressure);
            p.setString(3, heartRate);
            p.setString(4, oxygenSat);
            p.setString(5, complaint);
            p.setString(6, diagnosis);
            p.setString(7, treatment);
            p.setString(8, remarks);
            p.setInt(9, visitId);
            p.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("SaveVisit Error: " + e.getMessage());
            return false;
        }
    }

    // ── VISIT HISTORY: Get all visits with filters ───────────────
    public static List<java.util.Map<String, String>> getAllVisits(String filter) {
        List<java.util.Map<String, String>> list = new ArrayList<>();
        String dateFilter = switch (filter) {
            case "daily"  -> "WHERE v.visit_date = DATE('now')";
            case "weekly" -> "WHERE v.visit_date >= DATE('now', '-7 days')";
            case "monthly"-> "WHERE v.visit_date >= DATE('now', '-30 days')";
            default       -> "";
        };
        String sql = "SELECT v.visit_id, v.patient_id, p.full_name, v.visit_date, " +
                "v.time_in, v.time_out, v.purpose, v.chief_complaint, " +
                "v.diagnosis, v.treatment, v.remarks, v.temperature, " +
                "v.blood_pressure, v.heart_rate, v.oxygen_sat " +
                "FROM Visits v JOIN Patients p ON v.patient_id = p.patient_id " +
                dateFilter + " ORDER BY v.visit_date DESC, v.time_in DESC";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                java.util.Map<String, String> row = new java.util.LinkedHashMap<>();
                row.put("visit_id",       rs.getString("visit_id"));
                row.put("patient_id",     rs.getString("patient_id"));
                row.put("full_name",      rs.getString("full_name"));
                row.put("visit_date",     rs.getString("visit_date"));
                row.put("time_in",        rs.getString("time_in"));
                row.put("time_out",       rs.getString("time_out"));
                row.put("purpose",        rs.getString("purpose"));
                row.put("chief_complaint",rs.getString("chief_complaint"));
                row.put("diagnosis",      rs.getString("diagnosis"));
                row.put("treatment",      rs.getString("treatment"));
                row.put("remarks",        rs.getString("remarks"));
                row.put("temperature",    rs.getString("temperature"));
                row.put("blood_pressure", rs.getString("blood_pressure"));
                row.put("heart_rate",     rs.getString("heart_rate"));
                row.put("oxygen_sat",     rs.getString("oxygen_sat"));
                list.add(row);
            }
        } catch (SQLException e) {
            System.err.println("AllVisits Error: " + e.getMessage());
        }
        return list;
    }

    // ── SEARCH patients for check-in ────────────────────────────
    public static List<Patient> searchPatients(String query) {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM Patients WHERE patient_id LIKE ? OR full_name LIKE ? LIMIT 10";
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, "%" + query + "%");
            p.setString(2, "%" + query + "%");
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                list.add(new Patient(
                        rs.getString("patient_id"),  // id
                        rs.getString("full_name"),   // name
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
                ));
            }
        } catch (SQLException e) {
            System.err.println("Search Error: " + e.getMessage());
        }
        return list;
    }

    public static boolean deleteVisit(int visitId) {
        String sql = "DELETE FROM Visits WHERE visit_id = ?";
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, visitId);
            p.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("DeleteVisit Error: " + e.getMessage());
            return false;
        }
    }

    public static List<java.util.Map<String, String>> getVisitsByDateRange(
            String from, String to) {
        List<java.util.Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT v.visit_id, v.patient_id, p.full_name, v.visit_date, " +
                "v.time_in, v.time_out, v.purpose, v.chief_complaint, " +
                "v.diagnosis, v.treatment, v.remarks, v.temperature, " +
                "v.blood_pressure, v.heart_rate, v.oxygen_sat " +
                "FROM Visits v JOIN Patients p ON v.patient_id = p.patient_id " +
                "WHERE v.visit_date BETWEEN ? AND ? " +
                "ORDER BY v.visit_date DESC, v.time_in DESC";
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, from);
            p.setString(2, to);
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                java.util.Map<String, String> row = new java.util.LinkedHashMap<>();
                row.put("visit_id",       rs.getString("visit_id"));
                row.put("patient_id",     rs.getString("patient_id"));
                row.put("full_name",      rs.getString("full_name"));
                row.put("visit_date",     rs.getString("visit_date"));
                row.put("time_in",        rs.getString("time_in"));
                row.put("time_out",       rs.getString("time_out"));
                row.put("purpose",        rs.getString("purpose"));
                row.put("chief_complaint",rs.getString("chief_complaint"));
                row.put("diagnosis",      rs.getString("diagnosis"));
                row.put("treatment",      rs.getString("treatment"));
                row.put("remarks",        rs.getString("remarks"));
                row.put("temperature",    rs.getString("temperature"));
                row.put("blood_pressure", rs.getString("blood_pressure"));
                row.put("heart_rate",     rs.getString("heart_rate"));
                row.put("oxygen_sat",     rs.getString("oxygen_sat"));
                list.add(row);
            }
        } catch (SQLException e) {
            System.err.println("DateRange Error: " + e.getMessage());
        }
        return list;
    }

    public static int getVisitCount(String period) {
        String condition = switch (period) {
            case "today"   -> "WHERE visit_date = DATE('now')";
            case "weekly"  -> "WHERE visit_date >= DATE('now', '-7 days')";
            case "monthly" -> "WHERE visit_date >= DATE('now', '-30 days')";
            default        -> "";
        };
        String sql = "SELECT COUNT(*) FROM Visits " + condition;
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.err.println("VisitCount Error: " + e.getMessage());
            return 0;
        }
    }

    public static int getTotalPatients() {
        String sql = "SELECT COUNT(*) FROM Patients WHERE is_active = 1";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    public static java.util.Map<String, Integer> getPatientsByCollege() {
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        String sql = "SELECT college, COUNT(*) as cnt FROM Patients " +
                "WHERE is_active = 1 GROUP BY college ORDER BY cnt DESC";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                map.put(rs.getString("college"), rs.getInt("cnt"));
        } catch (SQLException e) {
            System.err.println("CollegeCount Error: " + e.getMessage());
        }
        return map;
    }

    public static java.util.Map<String, Integer> getTopIllnesses(String period) {
        String condition = switch (period) {
            case "weekly"  -> "WHERE visit_date >= DATE('now', '-7 days')";
            case "monthly" -> "WHERE visit_date >= DATE('now', '-30 days')";
            default        -> "WHERE visit_date = DATE('now')";
        };
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        String sql = "SELECT diagnosis, COUNT(*) as cnt FROM Visits " +
                condition + " AND diagnosis IS NOT NULL AND diagnosis != '' " +
                "GROUP BY diagnosis ORDER BY cnt DESC LIMIT 5";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                map.put(rs.getString("diagnosis"), rs.getInt("cnt"));
        } catch (SQLException e) {
            System.err.println("TopIllnesses Error: " + e.getMessage());
        }
        return map;
    }

    public static java.util.Map<String, Integer> getVisitsByMonth() {
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        String sql = "SELECT strftime('%m', visit_date) as month, COUNT(*) as cnt " +
                "FROM Visits WHERE visit_date >= DATE('now', '-6 months') " +
                "GROUP BY month ORDER BY month ASC";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                map.put(monthName(rs.getString("month")), rs.getInt("cnt"));
        } catch (SQLException e) {
            System.err.println("VisitsByMonth Error: " + e.getMessage());
        }
        return map;
    }

    public static java.util.Map<String, Integer> getVisitPurposeCount() {
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        String sql = "SELECT purpose, COUNT(*) as cnt FROM Visits " +
                "GROUP BY purpose";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                map.put(rs.getString("purpose"), rs.getInt("cnt"));
        } catch (SQLException e) {
            System.err.println("PurposeCount Error: " + e.getMessage());
        }
        return map;
    }

    private static String monthName(String month) {
        return switch (month) {
            case "01" -> "Jan"; case "02" -> "Feb"; case "03" -> "Mar";
            case "04" -> "Apr"; case "05" -> "May"; case "06" -> "Jun";
            case "07" -> "Jul"; case "08" -> "Aug"; case "09" -> "Sep";
            case "10" -> "Oct"; case "11" -> "Nov"; case "12" -> "Dec";
            default   -> month;
        };
    }
}