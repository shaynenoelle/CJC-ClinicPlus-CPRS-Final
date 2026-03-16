package com.example.cjcschoolclinicmanagementsystem.database;

import com.example.cjcschoolclinicmanagementsystem.model.Patient;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
        String sql = "SELECT * FROM Users WHERE username = ? AND password_hash = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            return pstmt.executeQuery().next();
        } catch (SQLException e) {
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
        return updatePatientFull(id, name, age, course, year, gender, college,
                contact, email, birthdate, type, emergencyName, relationship, emergencyContact,
                allergies, conditions, bloodType, height, weight, medicalRemarks,
                "", "", "", "", "", "", "");
    }

    public static boolean updatePatientFull(
            String id, String name, String age,
            String course, String year, String gender, String college,
            String contact, String email, String birthdate, String type,
            String emergencyName, String relationship, String emergencyContact,
            String allergies, String conditions, String bloodType,
            String height, String weight, String medicalRemarks,
            String address, String fatherName, String fatherContact,
            String motherName, String motherContact,
            String position, String schoolYear) {
        String sql = "UPDATE Patients SET " +
                "full_name=?, age=?, course_dept=?, year_level=?, gender=?, college=?, " +
                "contact_number=?, email=?, birthdate=?, type=?, emergency_name=?, " +
                "relationship=?, emergency_contact=?, allergies=?, conditions=?, blood_type=?, " +
                "height=?, weight=?, medical_remarks=?, address=?, father_name=?, father_contact=?, " +
                "mother_name=?, mother_contact=?, position=?, school_year=? WHERE patient_id=?";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1,  name);         p.setString(2,  age);
            p.setString(3,  course);       p.setString(4,  year);
            p.setString(5,  gender);       p.setString(6,  college);
            p.setString(7,  contact);      p.setString(8,  email);
            p.setString(9,  birthdate);    p.setString(10, type);
            p.setString(11, emergencyName);p.setString(12, relationship);
            p.setString(13, emergencyContact);p.setString(14, allergies);
            p.setString(15, conditions);   p.setString(16, bloodType);
            p.setString(17, height);       p.setString(18, weight);
            p.setString(19, medicalRemarks);p.setString(20, address);
            p.setString(21, fatherName);   p.setString(22, fatherContact);
            p.setString(23, motherName);   p.setString(24, motherContact);
            p.setString(25, position);     p.setString(26, schoolYear);
            p.setString(27, id);
            p.executeUpdate();
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
                "diagnosis, treatment, remarks, temperature, blood_pressure, heart_rate, oxygen_sat " +
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
                list.add(row);
            }
        } catch (SQLException e) { System.err.println("GetVisitsForPatient Error: " + e.getMessage()); }
        return list;
    }

    public static boolean checkInPatient(String patientId, String timeIn, String purpose) {
        String sql = "INSERT INTO Visits(patient_id, visit_date, time_in, purpose) VALUES(?, DATE('now'), ?, ?)";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, patientId); p.setString(2, timeIn); p.setString(3, purpose);
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
                                          String heartRate, String oxygenSat, String complaint,
                                          String diagnosis, String treatment, String remarks) {
        String sql = "UPDATE Visits SET temperature=?, blood_pressure=?, heart_rate=?, " +
                "oxygen_sat=?, chief_complaint=?, diagnosis=?, treatment=?, remarks=? WHERE visit_id=?";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, temperature); p.setString(2, bloodPressure);
            p.setString(3, heartRate);   p.setString(4, oxygenSat);
            p.setString(5, complaint);   p.setString(6, diagnosis);
            p.setString(7, treatment);   p.setString(8, remarks);
            p.setInt(9, visitId);
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
                "v.blood_pressure, v.heart_rate, v.oxygen_sat, p.course_dept " +
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
                "v.blood_pressure, v.heart_rate, v.oxygen_sat, p.course_dept " +
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
        try { row.put("course_dept", nvl(rs.getString("course_dept"))); } catch (SQLException ignored) {}
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
        String sql = "SELECT v.visit_id, v.time_in, v.time_out, p.full_name, " +
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
             PreparedStatement p = conn.prepareStatement("SELECT * FROM Users WHERE username=? AND password_hash=?")) {
            p.setString(1, username); p.setString(2, oldPassword);
            if (!p.executeQuery().next()) return false;
        } catch (SQLException e) { return false; }
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement("UPDATE Users SET password_hash=? WHERE username=?")) {
            p.setString(1, newPassword); p.setString(2, username);
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
            p.setString(1, username); p.setString(2, password);
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
            p.setString(1, newPassword); p.setString(2, username);
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
                "v.purpose, v.chief_complaint, v.diagnosis, v.treatment, v.remarks, " +
                "v.temperature, v.blood_pressure, v.heart_rate, v.oxygen_sat " +
                "FROM Visits v JOIN Patients p ON v.patient_id = p.patient_id ORDER BY v.visit_date DESC";
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(filePath))) {
            pw.println("Visit ID,Patient ID,Full Name,Date,Time In,Time Out,Purpose," +
                    "Chief Complaint,Diagnosis,Treatment,Remarks,Temperature,Blood Pressure,Heart Rate,SpO2");
            while (rs.next()) {
                pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"," +
                                "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        safe(rs.getString("visit_id")), safe(rs.getString("patient_id")), safe(rs.getString("full_name")),
                        safe(rs.getString("visit_date")), safe(rs.getString("time_in")), safe(rs.getString("time_out")),
                        safe(rs.getString("purpose")), safe(rs.getString("chief_complaint")), safe(rs.getString("diagnosis")),
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
            pw.println("Top Diagnoses"); pw.println("Diagnosis,Count");
            java.util.Map<String, Integer> ill = getTopIllnesses(normalized);
            if (ill.isEmpty()) pw.println("No data,0"); else ill.forEach((k,v) -> pw.println("\""+safe(k)+"\","+v));
            pw.println(); pw.println("Visit Rows");
            pw.println("Visit ID,Patient ID,Full Name,Date,Time In,Time Out,Purpose,Diagnosis");
            for (java.util.Map<String, String> row : getAllVisits(normalized)) {
                pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        safe(row.get("visit_id")), safe(row.get("patient_id")), safe(row.get("full_name")),
                        safe(row.get("visit_date")), safe(row.get("time_in")), safe(row.get("time_out")),
                        safe(row.get("purpose")), safe(row.get("diagnosis")));
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
}