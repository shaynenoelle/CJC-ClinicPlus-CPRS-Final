package com.example.cjcschoolclinicmanagementsystem.database;

import com.example.cjcschoolclinicmanagementsystem.model.Patient;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:cjclinic.db";

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void ensureColumns() {
        String[][] alterStatements = {
                { "Patients", "type TEXT DEFAULT 'Student'" },
                { "Patients", "address TEXT DEFAULT ''" },
                { "Patients", "father_name TEXT DEFAULT ''" },
                { "Patients", "father_contact TEXT DEFAULT ''" },
                { "Patients", "mother_name TEXT DEFAULT ''" },
                { "Patients", "mother_contact TEXT DEFAULT ''" },
                { "Patients", "position TEXT DEFAULT ''" },
                { "Patients", "school_year TEXT DEFAULT ''" },
                { "Visits",   "attended_by TEXT DEFAULT ''" },
                { "Visits",   "respiratory_rate TEXT DEFAULT ''" },
        };
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            for (String[] pair : alterStatements) {
                try {
                    stmt.execute("ALTER TABLE " + pair[0] + " ADD COLUMN " + pair[1]);
                } catch (SQLException e) {
                    if (!e.getMessage().toLowerCase().contains("duplicate column")) {
                        System.err.println("ensureColumns warning: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("ensureColumns Error: " + e.getMessage());
        }
    }

    public static void ensureTypeColumnExists() { ensureColumns(); }

    // ── LOGIN ─────────────────────────────────────────────────────
    public static boolean validateLogin(String username, String password) {
        String sql = "SELECT password_hash FROM Users WHERE username = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) return false;
            String storedHash = rs.getString("password_hash");
            return org.mindrot.jbcrypt.BCrypt.checkpw(password, storedHash);
        } catch (Exception e) {
            System.err.println("Login DB Error: " + e.getMessage());
            return false;
        }
    }

    // ── ADD PATIENT ───────────────────────────────────────────────
    public static boolean addPatient(String patientId, String fullName, String age,
                                     String course, String year, String gender, String college,
                                     String contact, String email, String birthdate, String type,
                                     String emergencyName, String relationship, String emergencyContact,
                                     String allergies, String conditions, String bloodType,
                                     String height, String weight, String medicalRemarks) {
        return addPatientFull(patientId, fullName, age, course, year, gender, college,
                contact, email, birthdate, type, emergencyName, relationship, emergencyContact,
                allergies, conditions, bloodType, height, weight, medicalRemarks,
                "", "", "", "", "", "", "");
    }

    public static boolean addPatientFull(
            String patientId, String fullName, String age,
            String course, String year, String gender, String college,
            String contact, String email, String birthdate, String type,
            String emergencyName, String relationship, String emergencyContact,
            String allergies, String conditions, String bloodType,
            String height, String weight, String medicalRemarks,
            String address, String fatherName, String fatherContact,
            String motherName, String motherContact,
            String position, String schoolYear) {

        ensureColumns();
        String sql = "INSERT INTO Patients(" +
                "patient_id, full_name, age, course_dept, year_level, gender, college, " +
                "contact_number, email, birthdate, type, emergency_name, relationship, " +
                "emergency_contact, allergies, conditions, blood_type, height, weight, " +
                "medical_remarks, address, father_name, father_contact, " +
                "mother_name, mother_contact, position, school_year" +
                ") VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1,  patientId);   p.setString(2,  fullName);
            p.setString(3,  age);         p.setString(4,  course);
            p.setString(5,  year);        p.setString(6,  gender);
            p.setString(7,  college);     p.setString(8,  contact);
            p.setString(9,  email);       p.setString(10, birthdate);
            p.setString(11, type);        p.setString(12, emergencyName);
            p.setString(13, relationship);p.setString(14, emergencyContact);
            p.setString(15, allergies);   p.setString(16, conditions);
            p.setString(17, bloodType);   p.setString(18, height);
            p.setString(19, weight);      p.setString(20, medicalRemarks);
            p.setString(21, address);     p.setString(22, fatherName);
            p.setString(23, fatherContact);p.setString(24, motherName);
            p.setString(25, motherContact);p.setString(26, position);
            p.setString(27, schoolYear);
            p.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Add Patient DB Error: " + e.getMessage());
            return false;
        }
    }

    // ── UPDATE PATIENT ────────────────────────────────────────────
    public static boolean updatePatient(String id, String name, String age,
                                        String course, String year, String gender, String college,
                                        String contact, String email, String birthdate, String type,
                                        String emergencyName, String relationship, String emergencyContact,
                                        String allergies, String conditions, String bloodType,
                                        String height, String weight, String medicalRemarks) {
        return updatePatientFull(id, id, name, age, course, year, gender, college,
                contact, email, birthdate, type, emergencyName, relationship, emergencyContact,
                allergies, conditions, bloodType, height, weight, medicalRemarks,
                "", "", "", "", "", "", "");
    }

    public static boolean updatePatientFull(
            String oldId, String newId, String name, String age,
            String course, String year, String gender, String college,
            String contact, String email, String birthdate, String type,
            String emergencyName, String relationship, String emergencyContact,
            String allergies, String conditions, String bloodType,
            String height, String weight, String medicalRemarks,
            String address, String fatherName, String fatherContact,
            String motherName, String motherContact,
            String position, String schoolYear) {
        String sql = "UPDATE Patients SET " +
                "patient_id=?, full_name=?, age=?, course_dept=?, year_level=?, gender=?, college=?, " +
                "contact_number=?, email=?, birthdate=?, type=?, emergency_name=?, " +
                "relationship=?, emergency_contact=?, allergies=?, conditions=?, blood_type=?, " +
                "height=?, weight=?, medical_remarks=?, address=?, father_name=?, father_contact=?, " +
                "mother_name=?, mother_contact=?, position=?, school_year=? WHERE patient_id=?";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1,  newId);        p.setString(2,  name);
            p.setString(3,  age);          p.setString(4,  course);
            p.setString(5,  year);         p.setString(6,  gender);
            p.setString(7,  college);      p.setString(8,  contact);
            p.setString(9,  email);        p.setString(10, birthdate);
            p.setString(11, type);         p.setString(12, emergencyName);
            p.setString(13, relationship); p.setString(14, emergencyContact);
            p.setString(15, allergies);    p.setString(16, conditions);
            p.setString(17, bloodType);    p.setString(18, height);
            p.setString(19, weight);       p.setString(20, medicalRemarks);
            p.setString(21, address);      p.setString(22, fatherName);
            p.setString(23, fatherContact);p.setString(24, motherName);
            p.setString(25, motherContact);p.setString(26, position);
            p.setString(27, schoolYear);   p.setString(28, oldId);
            int rows = p.executeUpdate();
            if (rows == 0) {
                System.err.println("Update Patient: No rows affected for patient_id='" + oldId + "'.");
                return false;
            }
            // Cascade ID change to Visits table if patient_id was changed
            if (!oldId.equals(newId)) {
                try (PreparedStatement pv = conn.prepareStatement(
                        "UPDATE Visits SET patient_id=? WHERE patient_id=?")) {
                    pv.setString(1, newId); pv.setString(2, oldId);
                    pv.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Update Patient DB Error: " + e.getMessage());
            return false;
        }
    }

    // ── GET PATIENTS ──────────────────────────────────────────────
    public static List<Patient> getAllPatients() {
        List<Patient> list = new ArrayList<>();
        ensureColumns();
        String sql = "SELECT patient_id, full_name, age, course_dept, year_level, gender, college, " +
                "contact_number, email, birthdate, type, emergency_name, relationship, " +
                "emergency_contact, allergies, conditions, blood_type, height, weight, " +
                "medical_remarks, is_active, address, father_name, father_contact, " +
                "mother_name, mother_contact, position, school_year " +
                "FROM Patients WHERE is_active = 1 OR is_active IS NULL";
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(buildPatient(rs));
        } catch (SQLException e) { System.err.println("Fetch Patients Error: " + e.getMessage()); }
        return list;
    }

    public static List<Patient> getAllPatientsWithArchived() {
        List<Patient> list = new ArrayList<>();
        ensureColumns();
        String sql = "SELECT patient_id, full_name, age, course_dept, year_level, gender, college, " +
                "contact_number, email, birthdate, type, emergency_name, relationship, " +
                "emergency_contact, allergies, conditions, blood_type, height, weight, " +
                "medical_remarks, is_active, address, father_name, father_contact, " +
                "mother_name, mother_contact, position, school_year " +
                "FROM Patients ORDER BY full_name ASC";
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(buildPatient(rs));
        } catch (SQLException e) { System.err.println("Fetch All Patients Error: " + e.getMessage()); }
        return list;
    }

    public static List<Patient> searchPatients(String query) {
        List<Patient> list = new ArrayList<>();
        ensureColumns();
        String sql = "SELECT patient_id, full_name, age, course_dept, year_level, gender, college, " +
                "contact_number, email, birthdate, type, emergency_name, relationship, " +
                "emergency_contact, allergies, conditions, blood_type, height, weight, " +
                "medical_remarks, is_active, address, father_name, father_contact, " +
                "mother_name, mother_contact, position, school_year " +
                "FROM Patients WHERE (patient_id LIKE ? OR full_name LIKE ?) " +
                "AND (is_active = 1 OR is_active IS NULL) LIMIT 10";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, "%" + query + "%");
            p.setString(2, "%" + query + "%");
            ResultSet rs = p.executeQuery();
            while (rs.next()) list.add(buildPatient(rs));
        } catch (SQLException e) { System.err.println("Search Error: " + e.getMessage()); }
        return list;
    }

    private static Patient buildPatient(ResultSet rs) throws SQLException {
        Patient p = new Patient(
                rs.getString("patient_id"), rs.getString("full_name"), rs.getString("age"),
                rs.getString("course_dept"), rs.getString("year_level"), rs.getString("gender"),
                rs.getString("college"), rs.getString("contact_number"), rs.getString("email"),
                rs.getString("birthdate"),
                rs.getString("type") != null ? rs.getString("type") : "Student",
                rs.getString("emergency_name"), rs.getString("relationship"),
                rs.getString("emergency_contact"), rs.getString("allergies"),
                rs.getString("conditions"), rs.getString("blood_type"),
                rs.getString("height"), rs.getString("weight"), rs.getString("medical_remarks")
        );
        p.setIsActive(String.valueOf(rs.getInt("is_active")));
        p.setAddress(nvl(rs.getString("address")));
        p.setFatherName(nvl(rs.getString("father_name")));
        p.setFatherContact(nvl(rs.getString("father_contact")));
        p.setMotherName(nvl(rs.getString("mother_name")));
        p.setMotherContact(nvl(rs.getString("mother_contact")));
        p.setPosition(nvl(rs.getString("position")));
        p.setSchoolYear(nvl(rs.getString("school_year")));
        return p;
    }

    public static boolean setPatientActiveStatus(String patientId, int isActive) {
        String sql = "UPDATE Patients SET is_active = ? WHERE patient_id = ?";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, isActive); p.setString(2, patientId);
            p.executeUpdate(); return true;
        } catch (SQLException e) { System.err.println("SetActiveStatus Error: " + e.getMessage()); return false; }
    }

    public static boolean deletePatient(String patientId) {
        try (Connection conn = connect()) {
            try (PreparedStatement p = conn.prepareStatement("DELETE FROM Visits WHERE patient_id = ?")) {
                p.setString(1, patientId); p.executeUpdate();
            }
            try (PreparedStatement p = conn.prepareStatement("DELETE FROM Patients WHERE patient_id = ?")) {
                p.setString(1, patientId); p.executeUpdate();
            }
            return true;
        } catch (SQLException e) { System.err.println("DeletePatient Error: " + e.getMessage()); return false; }
    }

    public static String generateNextPatientId() {
        int year = java.time.LocalDate.now().getYear();
        String prefix = year + "-";
        String sql = "SELECT patient_id FROM Patients WHERE patient_id LIKE ? ORDER BY patient_id DESC LIMIT 1";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, prefix + "%");
            ResultSet rs = p.executeQuery();
            if (rs.next()) {
                String last = rs.getString("patient_id");
                String[] parts = last.split("-");
                try {
                    int seq = Integer.parseInt(parts[parts.length - 1]) + 1;
                    return prefix + String.format("%04d", seq);
                } catch (NumberFormatException ignored) {}
            }
        } catch (SQLException e) { System.err.println("GenerateID Error: " + e.getMessage()); }
        return prefix + "0001";
    }

    // ── VISITS ────────────────────────────────────────────────────
    public static List<java.util.Map<String, String>> getVisitsForPatient(String patientId) {
        List<java.util.Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT visit_id, patient_id, visit_date, time_in, time_out, purpose, chief_complaint, " +
                "diagnosis, treatment, remarks, temperature, blood_pressure, heart_rate, oxygen_sat, " +
                "respiratory_rate, attended_by " +
                "FROM Visits WHERE patient_id = ? ORDER BY visit_date DESC, time_in DESC";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, patientId);
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                java.util.Map<String, String> row = new java.util.LinkedHashMap<>();
                row.put("visit_id",        nvl(rs.getString("visit_id")));
                row.put("patient_id",      nvl(rs.getString("patient_id")));
                row.put("visit_date",      nvl(rs.getString("visit_date")));
                row.put("time_in",         nvl(rs.getString("time_in")));
                row.put("time_out",        nvl(rs.getString("time_out")));
                row.put("purpose",         nvl(rs.getString("purpose")));
                row.put("chief_complaint", nvl(rs.getString("chief_complaint")));
                row.put("diagnosis",       nvl(rs.getString("diagnosis")));
                row.put("treatment",       nvl(rs.getString("treatment")));
                row.put("remarks",         nvl(rs.getString("remarks")));
                row.put("temperature",     nvl(rs.getString("temperature")));
                row.put("blood_pressure",  nvl(rs.getString("blood_pressure")));
                row.put("heart_rate",      nvl(rs.getString("heart_rate")));
                row.put("oxygen_sat",      nvl(rs.getString("oxygen_sat")));
                row.put("respiratory_rate", nvl(rs.getString("respiratory_rate")));
                row.put("attended_by",     nvl(rs.getString("attended_by")));
                list.add(row);
            }
        } catch (SQLException e) { System.err.println("GetVisitsForPatient Error: " + e.getMessage()); }
        return list;
    }

    public static boolean checkInPatient(String patientId, String timeIn, String purpose) {
        return checkInPatient(patientId, timeIn, purpose,
                com.example.cjcschoolclinicmanagementsystem.SessionManager.getCurrentUser());
    }

    public static boolean checkInPatient(String patientId, String timeIn,
                                         String purpose, String attendedBy) {
        String sql = "INSERT INTO Visits(patient_id, visit_date, time_in, purpose, attended_by)" +
                " VALUES(?, DATE('now'), ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, patientId); p.setString(2, timeIn);
            p.setString(3, purpose);   p.setString(4, attendedBy == null ? "" : attendedBy);
            p.executeUpdate(); return true;
        } catch (SQLException e) { System.err.println("CheckIn Error: " + e.getMessage()); return false; }
    }

    public static List<java.util.Map<String, String>> getTodayVisits() {
        List<java.util.Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT v.visit_id, v.patient_id, p.full_name, v.time_in, v.time_out, v.purpose " +
                "FROM Visits v JOIN Patients p ON v.patient_id = p.patient_id " +
                "WHERE v.visit_date = DATE('now') ORDER BY v.time_in DESC";
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
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
        } catch (SQLException e) { System.err.println("TodayVisits Error: " + e.getMessage()); }
        return list;
    }

    public static List<java.util.Map<String, String>> getFilteredVisits(String filter) {
        List<java.util.Map<String, String>> list = new ArrayList<>();
        String dateFilter = switch (filter.toLowerCase()) {
            case "weekly"  -> "WHERE v.visit_date >= DATE('now', '-7 days')";
            case "monthly" -> "WHERE v.visit_date >= DATE('now', '-30 days')";
            default        -> "WHERE v.visit_date = DATE('now')";
        };
        String sql = "SELECT v.visit_id, v.patient_id, p.full_name, v.time_in, v.time_out, v.purpose " +
                "FROM Visits v JOIN Patients p ON v.patient_id = p.patient_id " +
                dateFilter + " ORDER BY v.visit_date DESC, v.time_in DESC";
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
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
        } catch (SQLException e) { System.err.println("FilteredVisits Error: " + e.getMessage()); }
        return list;
    }

    public static boolean checkOutPatient(int visitId, String timeOut) {
        String sql = "UPDATE Visits SET time_out = ? WHERE visit_id = ?";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, timeOut); p.setInt(2, visitId);
            p.executeUpdate(); return true;
        } catch (SQLException e) { System.err.println("CheckOut Error: " + e.getMessage()); return false; }
    }

    public static boolean updateTimeIn(int visitId, String timeIn) {
        String sql = "UPDATE Visits SET time_in = ? WHERE visit_id = ?";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, timeIn); p.setInt(2, visitId);
            p.executeUpdate(); return true;
        } catch (SQLException e) { System.err.println("UpdateTimeIn Error: " + e.getMessage()); return false; }
    }

    public static boolean saveVisitRecord(int visitId, String temperature, String bloodPressure,
                                          String heartRate, String oxygenSat, String respiratoryRate,
                                          String complaint, String diagnosis, String treatment,
                                          String remarks, String attendedBy) {
        String sql = "UPDATE Visits SET temperature=?, blood_pressure=?, heart_rate=?, " +
                "oxygen_sat=?, respiratory_rate=?, chief_complaint=?, diagnosis=?, " +
                "treatment=?, remarks=?, attended_by=? WHERE visit_id=?";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, temperature);    p.setString(2, bloodPressure);
            p.setString(3, heartRate);      p.setString(4, oxygenSat);
            p.setString(5, respiratoryRate);p.setString(6, complaint);
            p.setString(7, diagnosis);      p.setString(8, treatment);
            p.setString(9, remarks);        p.setString(10, attendedBy);
            p.setInt(11, visitId);
            p.executeUpdate(); return true;
        } catch (SQLException e) { System.err.println("SaveVisit Error: " + e.getMessage()); return false; }
    }

    public static List<java.util.Map<String, String>> getAllVisits(String filter) {
        List<java.util.Map<String, String>> list = new ArrayList<>();
        String dateFilter = switch (filter.toLowerCase()) {
            case "daily"   -> "WHERE v.visit_date = DATE('now')";
            case "weekly"  -> "WHERE v.visit_date >= DATE('now', '-7 days')";
            case "monthly" -> "WHERE v.visit_date >= DATE('now', '-30 days')";
            default        -> "";
        };
        String sql = "SELECT v.visit_id, v.patient_id, p.full_name, v.visit_date, " +
                "v.time_in, v.time_out, v.purpose, v.chief_complaint, " +
                "v.diagnosis, v.treatment, v.remarks, v.temperature, " +
                "v.blood_pressure, v.heart_rate, v.oxygen_sat, p.course_dept, v.attended_by, " +
                "p.type, " +
                "(SELECT COUNT(*) FROM FollowUps f " +
                " WHERE f.visit_id = v.visit_id AND f.status = 'done') AS done_recheck_count " +
                "FROM Visits v JOIN Patients p ON v.patient_id = p.patient_id " +
                dateFilter + " ORDER BY v.visit_date DESC, v.time_in DESC";
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(buildVisitRow(rs));
        } catch (SQLException e) { System.err.println("AllVisits Error: " + e.getMessage()); }
        return list;
    }

    public static List<java.util.Map<String, String>> getVisitsByDateRange(String from, String to) {
        List<java.util.Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT v.visit_id, v.patient_id, p.full_name, v.visit_date, " +
                "v.time_in, v.time_out, v.purpose, v.chief_complaint, " +
                "v.diagnosis, v.treatment, v.remarks, v.temperature, " +
                "v.blood_pressure, v.heart_rate, v.oxygen_sat, p.course_dept, v.attended_by, " +
                "p.type, " +
                "(SELECT COUNT(*) FROM FollowUps f " +
                " WHERE f.visit_id = v.visit_id AND f.status = 'done') AS done_recheck_count " +
                "FROM Visits v JOIN Patients p ON v.patient_id = p.patient_id " +
                "WHERE v.visit_date BETWEEN ? AND ? ORDER BY v.visit_date DESC, v.time_in DESC";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, from); p.setString(2, to);
            ResultSet rs = p.executeQuery();
            while (rs.next()) list.add(buildVisitRow(rs));
        } catch (SQLException e) { System.err.println("DateRange Error: " + e.getMessage()); }
        return list;
    }

    public static boolean deleteVisit(int visitId) {
        String sql = "DELETE FROM Visits WHERE visit_id = ?";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, visitId); p.executeUpdate(); return true;
        } catch (SQLException e) { System.err.println("DeleteVisit Error: " + e.getMessage()); return false; }
    }

    /**
     * Fetches a single visit row with ALL columns (including chief_complaint,
     * vitals, treatment, etc.) by visit_id. Used by the dashboard Attend button
     * so the AttendController receives the full record, not the thin queue row.
     */
    public static java.util.Map<String, String> getVisitById(int visitId) {
        String sql = "SELECT v.visit_id, v.patient_id, p.full_name, v.visit_date, " +
                "v.time_in, v.time_out, v.purpose, v.chief_complaint, " +
                "v.diagnosis, v.treatment, v.remarks, v.temperature, " +
                "v.blood_pressure, v.heart_rate, v.oxygen_sat, v.respiratory_rate, " +
                "p.course_dept, v.attended_by, p.type " +
                "FROM Visits v JOIN Patients p ON v.patient_id = p.patient_id " +
                "WHERE v.visit_id = ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, visitId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return buildVisitRow(rs);
        } catch (SQLException e) { System.err.println("getVisitById error: " + e.getMessage()); }
        return null;
    }

    public static int bulkTimeOutToday() {
        String sql = "UPDATE Visits SET time_out = strftime('%H:%M', 'now', 'localtime') " +
                "WHERE date(visit_date) = date('now', 'localtime') " +
                "AND (time_out IS NULL OR time_out = '')";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            return p.executeUpdate();
        } catch (SQLException e) {
            System.err.println("BulkTimeOut Error: " + e.getMessage());
            return 0;
        }
    }

    private static java.util.Map<String, String> buildVisitRow(ResultSet rs) throws SQLException {
        java.util.Map<String, String> row = new java.util.LinkedHashMap<>();
        row.put("visit_id",        nvl(rs.getString("visit_id")));
        row.put("patient_id",      nvl(rs.getString("patient_id")));
        row.put("full_name",       nvl(rs.getString("full_name")));
        row.put("visit_date",      nvl(rs.getString("visit_date")));
        row.put("time_in",         nvl(rs.getString("time_in")));
        row.put("time_out",        nvl(rs.getString("time_out")));
        row.put("purpose",         nvl(rs.getString("purpose")));
        row.put("chief_complaint", nvl(rs.getString("chief_complaint")));
        row.put("diagnosis",       nvl(rs.getString("diagnosis")));
        row.put("treatment",       nvl(rs.getString("treatment")));
        row.put("remarks",         nvl(rs.getString("remarks")));
        row.put("temperature",     nvl(rs.getString("temperature")));
        row.put("blood_pressure",  nvl(rs.getString("blood_pressure")));
        row.put("heart_rate",      nvl(rs.getString("heart_rate")));
        row.put("oxygen_sat",      nvl(rs.getString("oxygen_sat")));
        try { row.put("course_dept",       nvl(rs.getString("course_dept")));       } catch (SQLException ignored) {}
        try { row.put("respiratory_rate",  nvl(rs.getString("respiratory_rate")));  } catch (SQLException ignored) {}
        try { row.put("attended_by",       nvl(rs.getString("attended_by")));       } catch (SQLException ignored) {}
        try { row.put("type",              nvl(rs.getString("type")));              } catch (SQLException ignored) {}
        try { row.put("done_recheck_count",nvl(rs.getString("done_recheck_count")));} catch (SQLException ignored) {}
        return row;
    }

    // ── DASHBOARD ─────────────────────────────────────────────────
    // FIX: now accepts "daily" as alias for "today" so the dashboard dropdown works correctly
    public static int getVisitCount(String period) {
        String condition = switch (period.toLowerCase()) {
            case "today", "daily"   -> "WHERE visit_date = DATE('now')";
            case "weekly", "week"   -> "WHERE visit_date >= DATE('now', '-7 days')";
            case "monthly", "month" -> "WHERE visit_date >= DATE('now', '-30 days')";
            default                 -> "";   // "all" = count every visit
        };
        String sql = "SELECT COUNT(*) FROM Visits " + condition;
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { System.err.println("VisitCount Error: " + e.getMessage()); return 0; }
    }

    public static int getTotalPatients() {
        String sql = "SELECT COUNT(*) FROM Patients WHERE is_active = 1 OR is_active IS NULL";
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { return 0; }
    }

    public static int getUnattendedToday() {
        String sql = "SELECT COUNT(*) FROM Visits WHERE visit_date = DATE('now') " +
                "AND (chief_complaint IS NULL OR chief_complaint = '') " +
                "AND (is_done IS NULL OR is_done = 0)";
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { return 0; }
    }

    public static java.util.List<java.util.Map<String, String>> getQueueToday() {
        java.util.List<java.util.Map<String, String>> queue = new java.util.ArrayList<>();
        // FIX: also select time_out, and exclude patients who already have a time_out
        // (they have been seen and left — should not stay in queue overnight or after checkout)
        String sql = "SELECT v.visit_id, v.patient_id, v.visit_date, v.time_in, v.time_out, p.full_name, " +
                "COALESCE(p.allergies,'') AS allergies, COALESCE(p.conditions,'') AS conditions " +
                "FROM Visits v JOIN Patients p ON v.patient_id = p.patient_id " +
                "WHERE v.visit_date = DATE('now') " +
                "AND (v.is_done IS NULL OR v.is_done = 0) " +
                "AND (v.time_out IS NULL OR v.time_out = '') " +
                "ORDER BY v.time_in ASC";
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                java.util.Map<String, String> row = new java.util.HashMap<>();
                row.put("visit_id",   rs.getString("visit_id")   != null ? rs.getString("visit_id")   : "");
                row.put("patient_id", rs.getString("patient_id") != null ? rs.getString("patient_id") : "");
                row.put("visit_date", rs.getString("visit_date") != null ? rs.getString("visit_date") : "");
                row.put("full_name",  rs.getString("full_name"));
                row.put("time_in",    rs.getString("time_in") != null ? rs.getString("time_in") : "—");
                row.put("time_out",   rs.getString("time_out") != null ? rs.getString("time_out") : "");
                row.put("allergies",  rs.getString("allergies"));
                row.put("conditions", rs.getString("conditions"));
                String timeIn = rs.getString("time_in");
                if (timeIn != null && !timeIn.isBlank()) {
                    try {
                        long mins = java.time.Duration.between(
                                java.time.LocalTime.parse(timeIn), java.time.LocalTime.now()).toMinutes();
                        row.put("waiting", Math.max(0, mins) + " min");
                    } catch (Exception ignored) { row.put("waiting", ""); }
                } else { row.put("waiting", ""); }
                queue.add(row);
            }
        } catch (SQLException e) { System.err.println("getQueueToday error: " + e.getMessage()); }
        return queue;
    }

    public static java.util.Map<String, java.util.Map<String, Integer>> getIllnessTrend() {
        java.util.List<String> top5 = new java.util.ArrayList<>();
        String top5sql = "SELECT diagnosis, COUNT(*) as cnt FROM Visits " +
                "WHERE visit_date >= DATE('now', '-56 days') AND diagnosis IS NOT NULL AND diagnosis != '' " +
                "GROUP BY diagnosis ORDER BY cnt DESC LIMIT 5";
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(top5sql)) {
            while (rs.next()) top5.add(rs.getString("diagnosis"));
        } catch (SQLException e) { return new java.util.LinkedHashMap<>(); }
        if (top5.isEmpty()) return new java.util.LinkedHashMap<>();

        java.util.Map<String, java.util.Map<String, Integer>> result = new java.util.LinkedHashMap<>();
        java.util.List<String> weekLabels = new java.util.ArrayList<>();
        for (int i = 7; i >= 0; i--) weekLabels.add("W-" + (i == 0 ? "now" : i));
        for (String diag : top5) {
            java.util.Map<String, Integer> weekMap = new java.util.LinkedHashMap<>();
            for (String lbl : weekLabels) weekMap.put(lbl, 0);
            for (int i = 7; i >= 0; i--) {
                String lbl = "W-" + (i == 0 ? "now" : i);
                String weekSql = "SELECT COUNT(*) FROM Visits WHERE diagnosis = ? " +
                        "AND visit_date >= DATE('now', '-" + ((i+1)*7) + " days') " +
                        "AND visit_date < DATE('now', '-" + (i*7) + " days')";
                try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(weekSql)) {
                    p.setString(1, diag); ResultSet rs = p.executeQuery();
                    if (rs.next()) weekMap.put(lbl, rs.getInt(1));
                } catch (SQLException ignored) {}
            }
            result.put(diag, weekMap);
        }
        return result;
    }

    public static java.util.Map<String, Integer> getPatientsByCollege() {
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        String sql = "SELECT course_dept, COUNT(*) as cnt FROM Patients " +
                "WHERE (is_active = 1 OR is_active IS NULL) AND (type IS NULL OR type != 'Employee') " +
                "GROUP BY course_dept ORDER BY cnt DESC";
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) map.put(nvl(rs.getString("course_dept")), rs.getInt("cnt"));
        } catch (SQLException e) { System.err.println("CollegeCount Error: " + e.getMessage()); }
        String empSql = "SELECT COUNT(*) FROM Patients WHERE (is_active=1 OR is_active IS NULL) AND type='Employee'";
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(empSql)) {
            if (rs.next()) { int c = rs.getInt(1); if (c > 0) map.put("Employee", c); }
        } catch (SQLException e) { System.err.println("EmployeeCount Error: " + e.getMessage()); }
        return map;
    }

    public static java.util.Map<String, Integer> getTopIllnesses(String period) {
        String condition = switch (period.toLowerCase()) {
            case "daily", "today"   -> "WHERE visit_date = DATE('now')";
            case "weekly", "week"   -> "WHERE visit_date >= DATE('now', '-7 days')";
            case "monthly", "month" -> "WHERE visit_date >= DATE('now', '-30 days')";
            default                 -> "WHERE 1=1";
        };
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        String sql = "SELECT diagnosis, COUNT(*) as cnt FROM Visits " + condition +
                " AND diagnosis IS NOT NULL AND diagnosis != '' GROUP BY diagnosis ORDER BY cnt DESC LIMIT 5";
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) map.put(rs.getString("diagnosis"), rs.getInt("cnt"));
        } catch (SQLException e) { System.err.println("TopIllnesses Error: " + e.getMessage()); }
        return map;
    }

    public static java.util.Map<String, Integer> getVisitsByMonth() {
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        String sql = "SELECT strftime('%m', visit_date) as month, COUNT(*) as cnt " +
                "FROM Visits WHERE visit_date >= DATE('now', '-6 months') GROUP BY month ORDER BY month ASC";
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) map.put(monthName(rs.getString("month")), rs.getInt("cnt"));
        } catch (SQLException e) { System.err.println("VisitsByMonth Error: " + e.getMessage()); }
        return map;
    }

    public static java.util.Map<String, Integer> getVisitsByCollegePeriod(String period) {
        String condition = switch (period.toLowerCase()) {
            case "daily", "today"   -> "AND v.visit_date = DATE('now')";
            case "monthly", "month" -> "AND v.visit_date >= DATE('now', '-30 days')";
            default                 -> "AND v.visit_date >= DATE('now', '-7 days')";
        };
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        String sql = "SELECT p.course_dept, COUNT(*) as cnt FROM Visits v " +
                "JOIN Patients p ON v.patient_id = p.patient_id " +
                "WHERE (p.type IS NULL OR p.type != 'Employee') " + condition +
                " GROUP BY p.course_dept ORDER BY cnt DESC";
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) map.put(nvl(rs.getString("course_dept")), rs.getInt("cnt"));
        } catch (SQLException e) { System.err.println("VisitsByCollegePeriod Error: " + e.getMessage()); }
        String empSql = "SELECT COUNT(*) FROM Visits v JOIN Patients p ON v.patient_id=p.patient_id " +
                "WHERE p.type='Employee' " + condition;
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(empSql)) {
            if (rs.next()) { int c = rs.getInt(1); if (c > 0) map.put("Employees", c); }
        } catch (SQLException e) { System.err.println("VisitsByCollegePeriod Emp Error: " + e.getMessage()); }
        return map;
    }

    public static java.util.Map<String, Integer> getVisitTrendByPeriod(String period) {
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        try (Connection conn = connect()) {
            switch (period.toLowerCase()) {
                case "daily", "today" -> {
                    String sql = "SELECT strftime('%H', time_in) as hr, COUNT(*) as cnt " +
                            "FROM Visits WHERE visit_date = DATE('now') AND time_in IS NOT NULL AND time_in != '' " +
                            "GROUP BY hr ORDER BY hr ASC";
                    try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
                        while (rs.next()) { String hr = rs.getString("hr"); map.put(hr != null ? hr + ":00" : "?", rs.getInt("cnt")); }
                    }
                }
                case "monthly", "month" -> {
                    String sql = "SELECT strftime('%m', visit_date) as mo, COUNT(*) as cnt " +
                            "FROM Visits WHERE strftime('%Y', visit_date) = strftime('%Y', 'now') GROUP BY mo ORDER BY mo ASC";
                    try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
                        while (rs.next()) {
                            String mo = rs.getString("mo");
                            map.put(switch (mo != null ? mo : "") {
                                case "01"->"Jan"; case "02"->"Feb"; case "03"->"Mar"; case "04"->"Apr";
                                case "05"->"May"; case "06"->"Jun"; case "07"->"Jul"; case "08"->"Aug";
                                case "09"->"Sep"; case "10"->"Oct"; case "11"->"Nov"; case "12"->"Dec";
                                default -> mo; }, rs.getInt("cnt"));
                        }
                    }
                }
                default -> {
                    for (int i = 7; i >= 0; i--) {
                        String lbl = (i == 0) ? "W-now" : "W-" + i;
                        String sql = "SELECT COUNT(*) FROM Visits WHERE visit_date >= DATE('now', '-" + ((i+1)*7) + " days') " +
                                "AND visit_date < DATE('now', '-" + (i*7) + " days')";
                        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
                            map.put(lbl, rs.next() ? rs.getInt(1) : 0);
                        }
                    }
                }
            }
        } catch (SQLException e) { System.err.println("VisitTrendByPeriod Error: " + e.getMessage()); }
        return map;
    }

    public static java.util.Map<String, Integer> getVisitPurposeCount() { return getVisitPurposeCount("all"); }

    public static java.util.Map<String, Integer> getVisitPurposeCount(String period) {
        String condition = switch (period.toLowerCase()) {
            case "daily", "today"   -> "WHERE visit_date = DATE('now')";
            case "weekly", "week"   -> "WHERE visit_date >= DATE('now', '-7 days')";
            case "monthly", "month" -> "WHERE visit_date >= DATE('now', '-30 days')";
            default                 -> "";
        };
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        String sql = "SELECT purpose, COUNT(*) as cnt FROM Visits " + condition + " GROUP BY purpose";
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) map.put(nvl(rs.getString("purpose")), rs.getInt("cnt"));
        } catch (SQLException e) { System.err.println("PurposeCount Error: " + e.getMessage()); }
        return map;
    }

    private static String monthName(String month) {
        return switch (month) {
            case "01"->"Jan"; case "02"->"Feb"; case "03"->"Mar"; case "04"->"Apr";
            case "05"->"May"; case "06"->"Jun"; case "07"->"Jul"; case "08"->"Aug";
            case "09"->"Sep"; case "10"->"Oct"; case "11"->"Nov"; case "12"->"Dec";
            default -> month;
        };
    }

    // ── SETTINGS ─────────────────────────────────────────────────
    public static boolean changePassword(String username, String oldPassword, String newPassword) {
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement("SELECT password_hash FROM Users WHERE username=?")) {
            p.setString(1, username);
            ResultSet rs = p.executeQuery();
            if (!rs.next()) return false;
            if (!org.mindrot.jbcrypt.BCrypt.checkpw(oldPassword, rs.getString("password_hash"))) return false;
        } catch (Exception e) { return false; }
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement("UPDATE Users SET password_hash=? WHERE username=?")) {
            p.setString(1, org.mindrot.jbcrypt.BCrypt.hashpw(newPassword, org.mindrot.jbcrypt.BCrypt.gensalt()));
            p.setString(2, username);
            p.executeUpdate(); return true;
        } catch (SQLException e) { System.err.println("ChangePassword Error: " + e.getMessage()); return false; }
    }

    public static List<java.util.Map<String, String>> getAllUsers() {
        List<java.util.Map<String, String>> list = new ArrayList<>();
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT username FROM Users")) {
            while (rs.next()) { java.util.Map<String, String> row = new java.util.LinkedHashMap<>(); row.put("username", rs.getString("username")); list.add(row); }
        } catch (SQLException e) { System.err.println("GetAllUsers Error: " + e.getMessage()); }
        return list;
    }

    public static boolean addUser(String username, String password) {
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement("INSERT INTO Users(username, password_hash, role) VALUES(?,?,'STAFF')")) {
            p.setString(1, username);
            // On account creation — hash before storing:
            p.setString(2, org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt()));
            p.executeUpdate(); return true;
        } catch (SQLException e) { System.err.println("AddUser Error: " + e.getMessage()); return false; }
    }

    public static boolean deleteUser(String username) {
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement("DELETE FROM Users WHERE username=?")) {
            p.setString(1, username); p.executeUpdate(); return true;
        } catch (SQLException e) { System.err.println("DeleteUser Error: " + e.getMessage()); return false; }
    }

    public static boolean resetUserPassword(String username, String newPassword) {
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement("UPDATE Users SET password_hash=? WHERE username=?")) {
            p.setString(1, org.mindrot.jbcrypt.BCrypt.hashpw(newPassword, org.mindrot.jbcrypt.BCrypt.gensalt()));
            p.setString(2, username);
            return p.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("ResetPassword Error: " + e.getMessage()); return false; }
    }

    public static void ensureSettingsTable() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS AppSettings (key TEXT PRIMARY KEY, value TEXT)");
        } catch (SQLException e) { System.err.println("EnsureSettings Error: " + e.getMessage()); }
    }

    public static void saveSetting(String key, String value) {
        ensureSettingsTable();
        String sql = "INSERT INTO AppSettings(key,value) VALUES(?,?) ON CONFLICT(key) DO UPDATE SET value=excluded.value";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, key); p.setString(2, value); p.executeUpdate();
        } catch (SQLException e) { System.err.println("SaveSetting Error: " + e.getMessage()); }
    }

    public static String getSetting(String key, String defaultValue) {
        ensureSettingsTable();
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement("SELECT value FROM AppSettings WHERE key=?")) {
            p.setString(1, key); ResultSet rs = p.executeQuery();
            if (rs.next()) return rs.getString("value");
        } catch (SQLException e) { System.err.println("GetSetting Error: " + e.getMessage()); }
        return defaultValue;
    }

    public static boolean backupDatabase(String destinationPath) {
        try {
            java.nio.file.Files.copy(java.nio.file.Paths.get("cjclinic.db"),
                    java.nio.file.Paths.get(destinationPath), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Exception e) { System.err.println("Backup Error: " + e.getMessage()); return false; }
    }

    public static boolean restoreDatabase(String backupFilePath) {
        try {
            java.nio.file.Files.copy(java.nio.file.Paths.get(backupFilePath),
                    java.nio.file.Paths.get("cjclinic.db"),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Exception e) { System.err.println("Restore Error: " + e.getMessage()); return false; }
    }

    public static boolean exportPatientsToCSV(String filePath) {
        String sql = "SELECT patient_id, full_name, age, course_dept, year_level, gender, college, " +
                "contact_number, email, birthdate, type, emergency_name, relationship, " +
                "emergency_contact, allergies, conditions, blood_type, height, weight, medical_remarks " +
                "FROM Patients WHERE is_active = 1 OR is_active IS NULL";
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(filePath))) {
            pw.println("ID,Full Name,Age,College/Dept,Year Level,Gender,Course,Contact,Email,Birthdate,Type," +
                    "Emergency Name,Relationship,Emergency Contact,Allergies,Conditions,Blood Type,Height,Weight,Medical Remarks");
            while (rs.next()) {
                pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"," +
                                "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        safe(rs.getString("patient_id")), safe(rs.getString("full_name")), safe(rs.getString("age")),
                        safe(rs.getString("course_dept")), safe(rs.getString("year_level")), safe(rs.getString("gender")),
                        safe(rs.getString("college")), safe(rs.getString("contact_number")), safe(rs.getString("email")),
                        safe(rs.getString("birthdate")), safe(rs.getString("type")), safe(rs.getString("emergency_name")),
                        safe(rs.getString("relationship")), safe(rs.getString("emergency_contact")),
                        safe(rs.getString("allergies")), safe(rs.getString("conditions")), safe(rs.getString("blood_type")),
                        safe(rs.getString("height")), safe(rs.getString("weight")), safe(rs.getString("medical_remarks")));
            }
            return true;
        } catch (Exception e) { System.err.println("ExportCSV Error: " + e.getMessage()); return false; }
    }

    public static boolean exportVisitsToCSV(String filePath) {
        String sql = "SELECT v.visit_id, v.patient_id, p.full_name, v.visit_date, v.time_in, v.time_out, " +
                "v.purpose, v.chief_complaint, v.treatment, v.remarks, " +
                "v.temperature, v.blood_pressure, v.heart_rate, v.oxygen_sat " +
                "FROM Visits v JOIN Patients p ON v.patient_id = p.patient_id ORDER BY v.visit_date DESC";
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(filePath))) {
            pw.println("Visit ID,Patient ID,Full Name,Date,Time In,Time Out,Purpose," +
                    "Cues,Treatment,Remarks,Temperature,Blood Pressure,Heart Rate,SpO2");
            while (rs.next()) {
                pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"," +
                                "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        safe(rs.getString("visit_id")), safe(rs.getString("patient_id")), safe(rs.getString("full_name")),
                        safe(rs.getString("visit_date")), safe(rs.getString("time_in")), safe(rs.getString("time_out")),
                        safe(rs.getString("purpose")), safe(rs.getString("chief_complaint")),
                        safe(rs.getString("treatment")), safe(rs.getString("remarks")), safe(rs.getString("temperature")),
                        safe(rs.getString("blood_pressure")), safe(rs.getString("heart_rate")), safe(rs.getString("oxygen_sat")));
            }
            return true;
        } catch (Exception e) { System.err.println("ExportVisitsCSV Error: " + e.getMessage()); return false; }
    }

    public static boolean exportDashboardToCSV(String filePath, String period) {
        String normalized = period == null ? "weekly" : period.toLowerCase();
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(filePath))) {
            pw.println("Dashboard Export"); pw.println("Period," + normalized);
            pw.println("Generated At," + java.time.LocalDateTime.now()); pw.println();
            pw.println("Summary"); pw.println("Metric,Value");
            pw.println("Visits (selected period)," + getVisitCount(normalized));
            pw.println("Total Patients," + getTotalPatients());
            pw.println("Unattended Today," + getUnattendedToday()); pw.println();
            pw.println("Top Cues"); pw.println("Cue,Count");
            java.util.Map<String, Integer> ill = getTopIllnesses(normalized);
            if (ill.isEmpty()) pw.println("No data,0"); else ill.forEach((k,v) -> pw.println("\""+safe(k)+"\","+v));
            pw.println(); pw.println("Visit Rows");
            pw.println("Visit ID,Patient ID,Full Name,Date,Time In,Time Out,Purpose,Cues");
            for (java.util.Map<String, String> row : getAllVisits(normalized)) {
                pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        safe(row.get("visit_id")), safe(row.get("patient_id")), safe(row.get("full_name")),
                        safe(row.get("visit_date")), safe(row.get("time_in")), safe(row.get("time_out")),
                        safe(row.get("purpose")), safe(row.get("chief_complaint")));
            }
            return true;
        } catch (Exception e) { System.err.println("ExportDashboardCSV Error: " + e.getMessage()); return false; }
    }

    private static String safe(String s) { return s == null ? "" : s.replace("\"", "\"\""); }
    private static String nvl(String s)  { return s == null ? "" : s; }

    public static boolean userExists(String username) {
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement("SELECT 1 FROM Users WHERE username=? LIMIT 1")) {
            p.setString(1, username); return p.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    /** Returns true if a patient with the given ID already exists in the database. */
    public static boolean patientExists(String patientId) {
        String sql = "SELECT 1 FROM Patients WHERE patient_id = ? LIMIT 1";
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, patientId);
            return p.executeQuery().next();
        } catch (SQLException e) {
            System.err.println("PatientExists Error: " + e.getMessage());
            return false;
        }
    }

    // ── FOLLOW-UPS ────────────────────────────────────────────────────────────
    public static void ensureFollowUpsTable() {
        // CREATE with the new column included (safe for fresh installs)
        String create = "CREATE TABLE IF NOT EXISTS FollowUps (" +
                "followup_id      INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "visit_id         INTEGER NOT NULL, " +
                "scheduled_time   TEXT NOT NULL, " +
                "actual_time      TEXT DEFAULT '', " +
                "nurse_notes      TEXT DEFAULT '', " +
                "vitals_snapshot  TEXT DEFAULT '', " +
                "recheck_remarks  TEXT DEFAULT '', " +   // ← NEW
                "attended_by      TEXT DEFAULT '', " +
                "status           TEXT DEFAULT 'pending'" +
                ")";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(create);
            // Safe migration for databases where FollowUps already exists without the column
            try {
                stmt.execute("ALTER TABLE FollowUps ADD COLUMN recheck_remarks TEXT DEFAULT ''");
            } catch (SQLException e) {
                // Column already exists — ignore
                if (!e.getMessage().toLowerCase().contains("duplicate column")) {
                    System.err.println("ensureFollowUpsTable migration: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("ensureFollowUpsTable Error: " + e.getMessage());
        }
    }

    /**
     * Schedules a new re-check for an existing visit.
     *
     * @param visitId       the parent visit's ID
     * @param scheduledTime "HH:mm" string — when the nurse wants to re-check
     * @param notes         optional reason / instruction (e.g. "temp still 38.5")
     * @return true on success
     */
    public static boolean scheduleFollowUp(int visitId, String scheduledTime, String notes) {
        ensureFollowUpsTable();
        String sql = "INSERT INTO FollowUps(visit_id, scheduled_time, nurse_notes, status) " +
                "VALUES(?, ?, ?, 'pending')";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, visitId);
            p.setString(2, scheduledTime);
            p.setString(3, notes == null ? "" : notes.trim());
            p.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("scheduleFollowUp Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns all follow-ups for a given visit, ordered by scheduled_time ASC
     * so the nurse sees them chronologically
     */
    public static List<java.util.Map<String, String>> getFollowUpsForVisit(int visitId) {
        ensureFollowUpsTable();
        List<java.util.Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT followup_id, visit_id, scheduled_time, actual_time, " +
                "nurse_notes, vitals_snapshot, recheck_remarks, attended_by, status " +
                "FROM FollowUps WHERE visit_id = ? ORDER BY scheduled_time ASC";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, visitId);
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                java.util.Map<String, String> row = new java.util.LinkedHashMap<>();
                row.put("followup_id",      nvl(rs.getString("followup_id")));
                row.put("visit_id",         nvl(rs.getString("visit_id")));
                row.put("scheduled_time",   nvl(rs.getString("scheduled_time")));
                row.put("actual_time",      nvl(rs.getString("actual_time")));
                row.put("nurse_notes",      nvl(rs.getString("nurse_notes")));
                row.put("vitals_snapshot",  nvl(rs.getString("vitals_snapshot")));
                row.put("recheck_remarks",  nvl(rs.getString("recheck_remarks")));  // ← NEW
                row.put("attended_by",      nvl(rs.getString("attended_by")));
                row.put("status",           nvl(rs.getString("status")));
                list.add(row);
            }
        } catch (SQLException e) {
            System.err.println("getFollowUpsForVisit Error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns all PENDING follow-ups for today across all visits.
     * Used by the Logbook to show "Re-check at HH:mm" badges.
     *
     * Extra keys: full_name, patient_id  (joined from Visits → Patients)
     */
    public static List<java.util.Map<String, String>> getPendingFollowUpsToday() {
        ensureFollowUpsTable();
        List<java.util.Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT f.followup_id, f.visit_id, f.scheduled_time, f.nurse_notes, " +
                "p.full_name, p.patient_id " +
                "FROM FollowUps f " +
                "JOIN Visits v ON f.visit_id = v.visit_id " +
                "JOIN Patients p ON v.patient_id = p.patient_id " +
                "WHERE f.status = 'pending' AND v.visit_date = DATE('now') " +
                "ORDER BY f.scheduled_time ASC";
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                java.util.Map<String, String> row = new java.util.LinkedHashMap<>();
                row.put("followup_id",    nvl(rs.getString("followup_id")));
                row.put("visit_id",       nvl(rs.getString("visit_id")));
                row.put("scheduled_time", nvl(rs.getString("scheduled_time")));
                row.put("nurse_notes",    nvl(rs.getString("nurse_notes")));
                row.put("full_name",      nvl(rs.getString("full_name")));
                row.put("patient_id",     nvl(rs.getString("patient_id")));
                list.add(row);
            }
        } catch (SQLException e) {
            System.err.println("getPendingFollowUpsToday Error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Marks a follow-up as done and records actual time, vitals snapshot,
     * nurse remarks, and who attended.
     */
    public static boolean completeFollowUp(int followUpId, String actualTime,
                                           String vitalsSnapshot, String remarks,
                                           String attendedBy) {
        ensureFollowUpsTable();
        String sql = "UPDATE FollowUps SET status='done', actual_time=?, " +
                "vitals_snapshot=?, recheck_remarks=?, attended_by=? " +
                "WHERE followup_id=?";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, actualTime     == null ? "" : actualTime.trim());
            p.setString(2, vitalsSnapshot == null ? "" : vitalsSnapshot.trim());
            p.setString(3, remarks        == null ? "" : remarks.trim());
            p.setString(4, attendedBy     == null ? "" : attendedBy.trim());
            p.setInt(5, followUpId);
            p.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("completeFollowUp Error: " + e.getMessage());
            return false;
        }
    }


    /**
     * Deletes a single follow-up row.
     * Used by the nurse if they accidentally added one.
     */
    public static boolean deleteFollowUp(int followUpId) {
        ensureFollowUpsTable();
        String sql = "DELETE FROM FollowUps WHERE followup_id = ?";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, followUpId);
            p.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("deleteFollowUp Error: " + e.getMessage());
            return false;
        }
    }

    // ── PRESET LISTS ──────────────────────────────────────────────
    public static void ensurePresetsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Presets (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, category TEXT NOT NULL, " +
                "value TEXT NOT NULL, UNIQUE(category, value))";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) { System.err.println("EnsurePresets Error: " + e.getMessage()); }
    }

    public static List<String> getPresets(String category) {
        ensurePresetsTable();
        List<String> list = new ArrayList<>();
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement("SELECT value FROM Presets WHERE category=? ORDER BY value ASC")) {
            p.setString(1, category); ResultSet rs = p.executeQuery();
            while (rs.next()) list.add(rs.getString("value"));
        } catch (SQLException e) { System.err.println("GetPresets Error: " + e.getMessage()); }
        return list;
    }

    public static boolean addPreset(String category, String value) {
        ensurePresetsTable();
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement("INSERT OR IGNORE INTO Presets(category, value) VALUES(?,?)")) {
            p.setString(1, category); p.setString(2, value.trim());
            return p.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("AddPreset Error: " + e.getMessage()); return false; }
    }

    public static boolean removePreset(String category, String value) {
        ensurePresetsTable();
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement("DELETE FROM Presets WHERE category=? AND value=?")) {
            p.setString(1, category); p.setString(2, value);
            return p.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("RemovePreset Error: " + e.getMessage()); return false; }
    }

    public static String getSchoolYear() { return getSetting("school_year", "2025-2026"); }
    public static void saveSchoolYear(String value) { saveSetting("school_year", value.trim()); }

    /**
     * Returns the set of all existing patient_id values in the database.
     * Used by the Excel importer to detect duplicates before inserting.
     */
    public static Set<String> getAllPatientIds() {
        Set<String> ids = new java.util.HashSet<>();
        String sql = "SELECT patient_id FROM Patients";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) ids.add(rs.getString("patient_id"));
        } catch (SQLException e) {
            System.err.println("getAllPatientIds Error: " + e.getMessage());
        }
        return ids;
    }

    /**
     * Bulk-inserts a list of Patient objects inside a single transaction.
     * Patients whose patient_id already exists are silently skipped (INSERT OR IGNORE).
     *
     * @param patients List of Patient objects parsed from the Excel file.
     * @return         The number of rows actually inserted.
     */
    public static int importPatientsBulk(List<com.example.cjcschoolclinicmanagementsystem.model.Patient> patients) {
        if (patients == null || patients.isEmpty()) return 0;

        ensureColumns();

        // INSERT OR IGNORE so a race-condition duplicate never causes an exception
        String sql = "INSERT OR IGNORE INTO Patients(" +
                "patient_id, full_name, age, course_dept, year_level, gender, college, " +
                "contact_number, email, birthdate, type, emergency_name, relationship, " +
                "emergency_contact, allergies, conditions, blood_type, height, weight, " +
                "medical_remarks, address, father_name, father_contact, " +
                "mother_name, mother_contact, position, school_year" +
                ") VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        int inserted = 0;

        try (Connection conn = connect()) {
            conn.setAutoCommit(false);  // wrap everything in one transaction for speed

            try (PreparedStatement p = conn.prepareStatement(sql)) {
                for (com.example.cjcschoolclinicmanagementsystem.model.Patient pt : patients) {
                    p.setString(1,  pt.getId());
                    p.setString(2,  pt.getName());
                    p.setString(3,  pt.getAge());
                    p.setString(4,  pt.getCourse());       // course_dept
                    p.setString(5,  pt.getYear());         // year_level
                    p.setString(6,  pt.getGender());
                    p.setString(7,  pt.getCollege());      // course/program name
                    p.setString(8,  pt.getContact());
                    p.setString(9,  pt.getEmail());
                    p.setString(10, pt.getBirthdate());
                    p.setString(11, pt.getType().isEmpty() ? "Student" : pt.getType());
                    p.setString(12, pt.getEmergencyName());
                    p.setString(13, pt.getRelationship());
                    p.setString(14, pt.getEmergencyContact());
                    p.setString(15, pt.getAllergies());
                    p.setString(16, pt.getConditions());
                    p.setString(17, pt.getBloodType());
                    p.setString(18, pt.getHeight());
                    p.setString(19, pt.getWeight());
                    p.setString(20, pt.getMedicalRemarks());
                    p.setString(21, pt.getAddress());
                    p.setString(22, pt.getFatherName());
                    p.setString(23, pt.getFatherContact());
                    p.setString(24, pt.getMotherName());
                    p.setString(25, pt.getMotherContact());
                    p.setString(26, pt.getPosition());
                    p.setString(27, pt.getSchoolYear());
                    p.addBatch();
                }

                int[] results = p.executeBatch();
                conn.commit();

                for (int r : results) {
                    if (r > 0 || r == PreparedStatement.SUCCESS_NO_INFO) inserted++;
                }

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("importPatientsBulk Error: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.err.println("importPatientsBulk Connection Error: " + e.getMessage());
        }

        return inserted;
    }
    // ═══════════════════════════════════════════════════════════════
    //  MEDICINE INVENTORY
    // ═══════════════════════════════════════════════════════════════

    /** Creates medicines + dispense_log tables if they don't exist yet. */
    public static void ensureMedicineTables() {
        String medicines =
                "CREATE TABLE IF NOT EXISTS medicines (" +
                        "  medicine_id   INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "  name          TEXT    NOT NULL," +
                        "  description   TEXT    DEFAULT ''," +
                        "  unit          TEXT    DEFAULT 'tablet'," +
                        "  quantity      INTEGER DEFAULT 0," +
                        "  low_stock_qty INTEGER DEFAULT 10," +
                        "  expiry_date   TEXT    DEFAULT ''," +   // YYYY-MM-DD or blank
                        "  is_active     INTEGER DEFAULT 1" +
                        ")";
        String dispenseLog =
                "CREATE TABLE IF NOT EXISTS dispense_log (" +
                        "  log_id        INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "  visit_id      INTEGER," +
                        "  medicine_id   INTEGER," +
                        "  medicine_name TEXT    DEFAULT ''," +   // snapshot
                        "  quantity_used INTEGER DEFAULT 1," +
                        "  unit          TEXT    DEFAULT 'tablet'," +
                        "  dispensed_by  TEXT    DEFAULT ''," +
                        "  dispensed_at  TEXT    DEFAULT ''," +   // YYYY-MM-DD HH:mm
                        "  notes         TEXT    DEFAULT ''" +
                        ")";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(medicines);
            stmt.execute(dispenseLog);
        } catch (SQLException e) {
            System.err.println("ensureMedicineTables Error: " + e.getMessage());
        }
    }

    /** Returns all active medicines ordered by name. */
    public static List<java.util.Map<String, String>> getAllMedicines() {
        ensureMedicineTables();
        List<java.util.Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT * FROM medicines WHERE is_active = 1 ORDER BY name COLLATE NOCASE";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                java.util.Map<String, String> row = new java.util.LinkedHashMap<>();
                row.put("medicine_id",   String.valueOf(rs.getInt("medicine_id")));
                row.put("name",          rs.getString("name"));
                row.put("description",   rs.getString("description"));
                row.put("unit",          rs.getString("unit"));
                row.put("quantity",      String.valueOf(rs.getInt("quantity")));
                row.put("low_stock_qty", String.valueOf(rs.getInt("low_stock_qty")));
                row.put("expiry_date",   rs.getString("expiry_date"));
                row.put("is_active",     String.valueOf(rs.getInt("is_active")));
                list.add(row);
            }
        } catch (SQLException e) {
            System.err.println("getAllMedicines Error: " + e.getMessage());
        }
        return list;
    }

    /** Inserts a new medicine. Returns true on success. */
    public static boolean addMedicineInventory(String name, String description,
                                               String unit, int quantity,
                                               int lowStockQty, String expiryDate) {
        ensureMedicineTables();
        String sql = "INSERT INTO medicines (name, description, unit, quantity, low_stock_qty, expiry_date)" +
                " VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, name);
            p.setString(2, description);
            p.setString(3, unit);
            p.setInt   (4, quantity);
            p.setInt   (5, lowStockQty);
            p.setString(6, expiryDate);
            return p.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("addMedicineInventory Error: " + e.getMessage());
            return false;
        }
    }

    /** Updates an existing medicine record. */
    public static boolean updateMedicineInventory(int medicineId, String name, String description,
                                                  String unit, int quantity,
                                                  int lowStockQty, String expiryDate) {
        ensureMedicineTables();
        String sql = "UPDATE medicines SET name=?, description=?, unit=?, quantity=?," +
                " low_stock_qty=?, expiry_date=? WHERE medicine_id=?";
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, name);
            p.setString(2, description);
            p.setString(3, unit);
            p.setInt   (4, quantity);
            p.setInt   (5, lowStockQty);
            p.setString(6, expiryDate);
            p.setInt   (7, medicineId);
            return p.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("updateMedicineInventory Error: " + e.getMessage());
            return false;
        }
    }

    /** Soft-deletes (deactivates) a medicine. */
    public static boolean deactivateMedicine(int medicineId) {
        ensureMedicineTables();
        String sql = "UPDATE medicines SET is_active = 0 WHERE medicine_id = ?";
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, medicineId);
            return p.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("deactivateMedicine Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Adds quantity to an existing medicine (restocking).
     * Pass a negative value to manually reduce stock.
     */
    public static boolean restockMedicine(int medicineId, int addQty) {
        ensureMedicineTables();
        String sql = "UPDATE medicines SET quantity = MAX(0, quantity + ?) WHERE medicine_id = ?";
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, addQty);
            p.setInt(2, medicineId);
            return p.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("restockMedicine Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Dispenses one or more medicines for a visit.
     * Deducts qty from stock and writes to dispense_log.
     * @param visitId      the visit's ID (0 if not yet saved — should always be saved first)
     * @param medicineId   from medicines.medicine_id
     * @param medicineName snapshot of the name at time of dispensing
     * @param unit         unit snapshot
     * @param qty          how many to dispense
     * @param dispensedBy  nurse username
     * @return true if both deduction and log write succeeded
     */
    public static boolean dispenseMedicine(int visitId, int medicineId,
                                           String medicineName, String unit,
                                           int qty, String dispensedBy) {
        ensureMedicineTables();
        String deduct = "UPDATE medicines SET quantity = MAX(0, quantity - ?) " +
                "WHERE medicine_id = ? AND is_active = 1";
        String log    = "INSERT INTO dispense_log " +
                "(visit_id, medicine_id, medicine_name, quantity_used, unit, dispensed_by, dispensed_at)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?)";
        String now = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement p1 = conn.prepareStatement(deduct);
                 PreparedStatement p2 = conn.prepareStatement(log)) {
                p1.setInt(1, qty);
                p1.setInt(2, medicineId);
                int deducted = p1.executeUpdate();

                p2.setInt   (1, visitId);
                p2.setInt   (2, medicineId);
                p2.setString(3, medicineName);
                p2.setInt   (4, qty);
                p2.setString(5, unit);
                p2.setString(6, dispensedBy);
                p2.setString(7, now);
                p2.executeUpdate();

                conn.commit();
                return deducted > 0;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("dispenseMedicine Error: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("dispenseMedicine Connection Error: " + e.getMessage());
            return false;
        }
    }

    /** Returns dispense log entries for a specific visit. */
    public static List<java.util.Map<String, String>> getDispenseLogForVisit(int visitId) {
        ensureMedicineTables();
        List<java.util.Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT * FROM dispense_log WHERE visit_id = ? ORDER BY log_id";
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, visitId);
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                java.util.Map<String, String> row = new java.util.LinkedHashMap<>();
                row.put("log_id",        String.valueOf(rs.getInt("log_id")));
                row.put("medicine_id",   String.valueOf(rs.getInt("medicine_id")));
                row.put("medicine_name", rs.getString("medicine_name"));
                row.put("quantity_used", String.valueOf(rs.getInt("quantity_used")));
                row.put("unit",          rs.getString("unit"));
                row.put("dispensed_by",  rs.getString("dispensed_by"));
                row.put("dispensed_at",  rs.getString("dispensed_at"));
                list.add(row);
            }
        } catch (SQLException e) {
            System.err.println("getDispenseLogForVisit Error: " + e.getMessage());
        }
        return list;
    }

    /** Returns full dispense log (all visits), most recent first. */
    public static List<java.util.Map<String, String>> getAllDispenseLog() {
        ensureMedicineTables();
        List<java.util.Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT dl.*, m.quantity AS current_stock " +
                "FROM dispense_log dl " +
                "LEFT JOIN medicines m ON dl.medicine_id = m.medicine_id " +
                "ORDER BY dl.log_id DESC";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                java.util.Map<String, String> row = new java.util.LinkedHashMap<>();
                row.put("log_id",        String.valueOf(rs.getInt("log_id")));
                row.put("visit_id",      String.valueOf(rs.getInt("visit_id")));
                row.put("medicine_id",   String.valueOf(rs.getInt("medicine_id")));
                row.put("medicine_name", rs.getString("medicine_name"));
                row.put("quantity_used", String.valueOf(rs.getInt("quantity_used")));
                row.put("unit",          rs.getString("unit"));
                row.put("dispensed_by",  rs.getString("dispensed_by"));
                row.put("dispensed_at",  rs.getString("dispensed_at"));
                list.add(row);
            }
        } catch (SQLException e) {
            System.err.println("getAllDispenseLog Error: " + e.getMessage());
        }
        return list;
    }

    /** Count of medicines whose stock is at or below their low_stock_qty threshold. */
    public static int getLowStockCount() {
        ensureMedicineTables();
        String sql = "SELECT COUNT(*) FROM medicines WHERE is_active=1 AND quantity <= low_stock_qty";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.err.println("getLowStockCount Error: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Count of medicines expiring within the next {@code days} days.
     * Medicines with a blank expiry_date are excluded.
     */
    public static int getExpiringSoonCount(int days) {
        ensureMedicineTables();
        String today  = java.time.LocalDate.now().toString();
        String cutoff = java.time.LocalDate.now().plusDays(days).toString();
        String sql = "SELECT COUNT(*) FROM medicines WHERE is_active=1" +
                " AND expiry_date != '' AND expiry_date <= ? AND expiry_date >= ?";
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, cutoff);
            p.setString(2, today);
            ResultSet rs = p.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.err.println("getExpiringSoonCount Error: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Exports current medicine inventory + dispense log to two CSV files.
     * @param inventoryPath  path for the inventory CSV
     * @param dispensePath   path for the dispense log CSV
     */
    public static boolean exportMedicineCSV(String inventoryPath, String dispensePath) {
        ensureMedicineTables();
        try {
            // ── Inventory CSV ──────────────────────────────────────
            try (java.io.PrintWriter pw = new java.io.PrintWriter(
                    new java.io.FileWriter(inventoryPath))) {
                pw.println("Medicine ID,Name,Description,Unit,Quantity,Low Stock Threshold,Expiry Date,Status");
                for (java.util.Map<String, String> m : getAllMedicines()) {
                    int qty      = Integer.parseInt(m.get("quantity"));
                    int lowStock = Integer.parseInt(m.get("low_stock_qty"));
                    String expiry = m.get("expiry_date");
                    String status = "OK";
                    if (qty <= lowStock) status = "Low Stock";
                    if (!expiry.isEmpty()) {
                        java.time.LocalDate exp = java.time.LocalDate.parse(expiry);
                        if (!exp.isAfter(java.time.LocalDate.now())) status = "Expired";
                        else if (!exp.isAfter(java.time.LocalDate.now().plusDays(30)))
                            status = "Expiring Soon";
                    }
                    pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",%s,%s,\"%s\",\"%s\"%n",
                            m.get("medicine_id"), escape(m.get("name")),
                            escape(m.get("description")), escape(m.get("unit")),
                            qty, lowStock, expiry, status);
                }
            }
            // ── Dispense Log CSV ───────────────────────────────────
            try (java.io.PrintWriter pw = new java.io.PrintWriter(
                    new java.io.FileWriter(dispensePath))) {
                pw.println("Log ID,Visit ID,Medicine,Qty Used,Unit,Dispensed By,Date/Time");
                for (java.util.Map<String, String> d : getAllDispenseLog()) {
                    pw.printf("%s,%s,\"%s\",%s,\"%s\",\"%s\",\"%s\"%n",
                            d.get("log_id"), d.get("visit_id"),
                            escape(d.get("medicine_name")), d.get("quantity_used"),
                            escape(d.get("unit")), escape(d.get("dispensed_by")),
                            d.get("dispensed_at"));
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("exportMedicineCSV Error: " + e.getMessage());
            return false;
        }
    }

    /** CSV-escapes double-quotes in a string. */
    private static String escape(String s) {
        return s == null ? "" : s.replace("\"", "\"\"");
    }
}