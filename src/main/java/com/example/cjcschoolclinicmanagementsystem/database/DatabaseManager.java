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

    // ── Ensure all optional columns exist (safe to call repeatedly) ──
    // Called on startup so the app never crashes on an older DB file
    public static void ensureColumns() {
        String[][] alterStatements = {
                // Original columns that may be missing in older DBs
                { "Patients", "type TEXT DEFAULT 'Student'" },
                // NEW columns added for updated patient form
                { "Patients", "address TEXT DEFAULT ''" },
                { "Patients", "father_name TEXT DEFAULT ''" },
                { "Patients", "father_contact TEXT DEFAULT ''" },
                { "Patients", "mother_name TEXT DEFAULT ''" },
                { "Patients", "mother_contact TEXT DEFAULT ''" },
                { "Patients", "position TEXT DEFAULT ''" },      // employee position
                { "Patients", "school_year TEXT DEFAULT ''" },
        };
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            for (String[] pair : alterStatements) {
                try {
                    stmt.execute("ALTER TABLE " + pair[0] + " ADD COLUMN " + pair[1]);
                } catch (SQLException e) {
                    // "duplicate column" is expected when column already exists — ignore
                    if (!e.getMessage().toLowerCase().contains("duplicate column")) {
                        System.err.println("ensureColumns warning: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("ensureColumns Error: " + e.getMessage());
        }
    }

    // Keep old method name so existing code that calls it still compiles
    public static void ensureTypeColumnExists() {
        ensureColumns();
    }

    // ── LOGIN ─────────────────────────────────────────────────────
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

    // ── ADD PATIENT ───────────────────────────────────────────────
    // FIX 1: blood_type param 17 was MISSING in original — now correctly set
    // FIX 2: now also saves new fields (address, father/mother, position, school_year)
    public static boolean addPatient(String patientId, String fullName, String age,
                                     String course, String year, String gender, String college,
                                     String contact, String email, String birthdate, String type,
                                     String emergencyName, String relationship, String emergencyContact,
                                     String allergies, String conditions, String bloodType,
                                     String height, String weight, String medicalRemarks) {
        // Call with empty extended fields for backward compatibility
        return addPatientFull(patientId, fullName, age, course, year, gender, college,
                contact, email, birthdate, type, emergencyName, relationship, emergencyContact,
                allergies, conditions, bloodType, height, weight, medicalRemarks,
                "", "", "", "", "", "", "");
    }

    // Full version used by the new AddPatientController
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

        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1,  patientId);
            p.setString(2,  fullName);
            p.setString(3,  age);
            p.setString(4,  course);        // course_dept = college abbreviation e.g. CCIS
            p.setString(5,  year);          // year_level
            p.setString(6,  gender);
            p.setString(7,  college);       // college col = course name e.g. BSCS
            p.setString(8,  contact);
            p.setString(9,  email);
            p.setString(10, birthdate);
            p.setString(11, type);
            p.setString(12, emergencyName);
            p.setString(13, relationship);
            p.setString(14, emergencyContact);
            p.setString(15, allergies);
            p.setString(16, conditions);
            p.setString(17, bloodType);     // FIX: was missing in original!
            p.setString(18, height);
            p.setString(19, weight);
            p.setString(20, medicalRemarks);
            p.setString(21, address);
            p.setString(22, fatherName);
            p.setString(23, fatherContact);
            p.setString(24, motherName);
            p.setString(25, motherContact);
            p.setString(26, position);
            p.setString(27, schoolYear);
            p.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Add Patient DB Error: " + e.getMessage());
            return false;
        }
    }

    // ── UPDATE PATIENT ────────────────────────────────────────────
    // Original 20-param version kept for compatibility
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

    // Full version used by the new AddPatientController (edit mode)
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
                "mother_name=?, mother_contact=?, position=?, school_year=? " +
                "WHERE patient_id=?";

        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1,  name);
            p.setString(2,  age);
            p.setString(3,  course);
            p.setString(4,  year);
            p.setString(5,  gender);
            p.setString(6,  college);
            p.setString(7,  contact);
            p.setString(8,  email);
            p.setString(9,  birthdate);
            p.setString(10, type);
            p.setString(11, emergencyName);
            p.setString(12, relationship);
            p.setString(13, emergencyContact);
            p.setString(14, allergies);
            p.setString(15, conditions);
            p.setString(16, bloodType);
            p.setString(17, height);
            p.setString(18, weight);
            p.setString(19, medicalRemarks);
            p.setString(20, address);
            p.setString(21, fatherName);
            p.setString(22, fatherContact);
            p.setString(23, motherName);
            p.setString(24, motherContact);
            p.setString(25, position);
            p.setString(26, schoolYear);
            p.setString(27, id);            // WHERE patient_id = ?
            p.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Update Patient DB Error: " + e.getMessage());
            return false;
        }
    }

    // ── GET ALL PATIENTS (active only) ───────────────────────────
    // FIX: removed stray `";` at end of SQL string that caused a crash
    // FIX: now also reads new fields and calls p.setIsActive()
    public static List<Patient> getAllPatients() {
        List<Patient> list = new ArrayList<>();
        ensureColumns();
        String sql = "SELECT patient_id, full_name, age, course_dept, year_level, gender, college, " +
                "contact_number, email, birthdate, type, emergency_name, relationship, " +
                "emergency_contact, allergies, conditions, blood_type, height, weight, " +
                "medical_remarks, is_active, address, father_name, father_contact, " +
                "mother_name, mother_contact, position, school_year " +
                "FROM Patients WHERE is_active = 1 OR is_active IS NULL";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Patient p = buildPatient(rs);
                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Fetch Patients Error: " + e.getMessage());
        }
        return list;
    }

    // ── GET ALL PATIENTS including archived ───────────────────────
    public static List<Patient> getAllPatientsWithArchived() {
        List<Patient> list = new ArrayList<>();
        ensureColumns();
        String sql = "SELECT patient_id, full_name, age, course_dept, year_level, gender, college, " +
                "contact_number, email, birthdate, type, emergency_name, relationship, " +
                "emergency_contact, allergies, conditions, blood_type, height, weight, " +
                "medical_remarks, is_active, address, father_name, father_contact, " +
                "mother_name, mother_contact, position, school_year " +
                "FROM Patients ORDER BY full_name ASC";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(buildPatient(rs));
            }
        } catch (SQLException e) {
            System.err.println("Fetch All Patients Error: " + e.getMessage());
        }
        return list;
    }

    // ── SEARCH patients (used by Logbook check-in) ────────────────
    // FIX: now also reads new fields via buildPatient()
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
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, "%" + query + "%");
            p.setString(2, "%" + query + "%");
            ResultSet rs = p.executeQuery();
            while (rs.next()) list.add(buildPatient(rs));
        } catch (SQLException e) {
            System.err.println("Search Error: " + e.getMessage());
        }
        return list;
    }

    // ── Shared helper: build a Patient from a ResultSet row ───────
    // One place to update when Patient fields change
    private static Patient buildPatient(ResultSet rs) throws SQLException {
        Patient p = new Patient(
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
        );
        // New fields — use nvl() because columns may be null in older records
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

    // ── SET ACTIVE / ARCHIVE STATUS ───────────────────────────────
    public static boolean setPatientActiveStatus(String patientId, int isActive) {
        String sql = "UPDATE Patients SET is_active = ? WHERE patient_id = ?";
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, isActive);
            p.setString(2, patientId);
            p.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("SetActiveStatus Error: " + e.getMessage());
            return false;
        }
    }

    // ── DELETE PATIENT (and all their visits) ─────────────────────
    public static boolean deletePatient(String patientId) {
        try (Connection conn = connect()) {
            try (PreparedStatement p = conn.prepareStatement(
                    "DELETE FROM Visits WHERE patient_id = ?")) {
                p.setString(1, patientId); p.executeUpdate();
            }
            try (PreparedStatement p = conn.prepareStatement(
                    "DELETE FROM Patients WHERE patient_id = ?")) {
                p.setString(1, patientId); p.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            System.err.println("DeletePatient Error: " + e.getMessage());
            return false;
        }
    }

    // ── AUTO-GENERATE NEXT PATIENT ID ────────────────────────────
    // Format: YYYY-XXXX  e.g. 2026-0001
    public static String generateNextPatientId() {
        int year = java.time.LocalDate.now().getYear();
        String prefix = year + "-";
        String sql = "SELECT patient_id FROM Patients WHERE patient_id LIKE ? " +
                "ORDER BY patient_id DESC LIMIT 1";
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
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
        } catch (SQLException e) {
            System.err.println("GenerateID Error: " + e.getMessage());
        }
        return prefix + "0001";
    }

    // ── GET VISIT HISTORY FOR ONE PATIENT ────────────────────────
    public static List<java.util.Map<String, String>> getVisitsForPatient(String patientId) {
        List<java.util.Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT visit_id, patient_id, visit_date, time_in, time_out, purpose, chief_complaint, " +
                "diagnosis, treatment, remarks, temperature, blood_pressure, " +
                "heart_rate, oxygen_sat " +
                "FROM Visits WHERE patient_id = ? ORDER BY visit_date DESC, time_in DESC";
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
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
        } catch (SQLException e) {
            System.err.println("GetVisitsForPatient Error: " + e.getMessage());
        }
        return list;
    }

    // ── LOGBOOK: Check-in ─────────────────────────────────────────
    public static boolean checkInPatient(String patientId, String timeIn, String purpose) {
        String sql = "INSERT INTO Visits(patient_id, visit_date, time_in, purpose) " +
                "VALUES(?, DATE('now'), ?, ?)";
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

    // ── LOGBOOK: Get today's visitors ─────────────────────────────
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

    // ── LOGBOOK: Get filtered visitors (today/weekly/monthly) ─────
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
            System.err.println("FilteredVisits Error: " + e.getMessage());
        }
        return list;
    }

    // ── ATTEND: Check out ─────────────────────────────────────────
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

    // ── ATTEND: Save full visit record ────────────────────────────
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

    // ── VISIT HISTORY: Get all visits with period filter ──────────
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
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(buildVisitRow(rs));
        } catch (SQLException e) {
            System.err.println("AllVisits Error: " + e.getMessage());
        }
        return list;
    }

    // ── VISIT HISTORY: Get by date range ──────────────────────────
    public static List<java.util.Map<String, String>> getVisitsByDateRange(String from, String to) {
        List<java.util.Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT v.visit_id, v.patient_id, p.full_name, v.visit_date, " +
                "v.time_in, v.time_out, v.purpose, v.chief_complaint, " +
                "v.diagnosis, v.treatment, v.remarks, v.temperature, " +
                "v.blood_pressure, v.heart_rate, v.oxygen_sat, p.course_dept " +
                "FROM Visits v JOIN Patients p ON v.patient_id = p.patient_id " +
                "WHERE v.visit_date BETWEEN ? AND ? " +
                "ORDER BY v.visit_date DESC, v.time_in DESC";
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, from);
            p.setString(2, to);
            ResultSet rs = p.executeQuery();
            while (rs.next()) list.add(buildVisitRow(rs));
        } catch (SQLException e) {
            System.err.println("DateRange Error: " + e.getMessage());
        }
        return list;
    }

    // ── VISIT HISTORY: Delete a visit ─────────────────────────────
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

    // Shared helper for visit rows
    private static java.util.Map<String, String> buildVisitRow(ResultSet rs) throws SQLException {
        java.util.Map<String, String> row = new java.util.LinkedHashMap<>();
        row.put("visit_id",       nvl(rs.getString("visit_id")));
        row.put("patient_id",     nvl(rs.getString("patient_id")));
        row.put("full_name",      nvl(rs.getString("full_name")));
        row.put("visit_date",     nvl(rs.getString("visit_date")));
        row.put("time_in",        nvl(rs.getString("time_in")));
        row.put("time_out",       nvl(rs.getString("time_out")));
        row.put("purpose",        nvl(rs.getString("purpose")));
        row.put("chief_complaint",nvl(rs.getString("chief_complaint")));
        row.put("diagnosis",      nvl(rs.getString("diagnosis")));
        row.put("treatment",      nvl(rs.getString("treatment")));
        row.put("remarks",        nvl(rs.getString("remarks")));
        row.put("temperature",    nvl(rs.getString("temperature")));
        row.put("blood_pressure", nvl(rs.getString("blood_pressure")));
        row.put("heart_rate",     nvl(rs.getString("heart_rate")));
        row.put("oxygen_sat",     nvl(rs.getString("oxygen_sat")));
        // course_dept for VisitHistory search filter
        try { row.put("course_dept", nvl(rs.getString("course_dept"))); }
        catch (SQLException ignored) {}
        return row;
    }

    // ── DASHBOARD: Visit counts ───────────────────────────────────
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
        String sql = "SELECT COUNT(*) FROM Patients WHERE is_active = 1 OR is_active IS NULL";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { return 0; }
    }

    // ── DASHBOARD: Unattended patients today (checked in, no complaint recorded) ──
    public static int getUnattendedToday() {
        String sql = "SELECT COUNT(*) FROM Visits " +
                "WHERE visit_date = DATE('now') " +
                "AND (chief_complaint IS NULL OR chief_complaint = '') " +
                "AND (is_done IS NULL OR is_done = 0)";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { return 0; }
    }

    // ── DASHBOARD: Illness trend — top 5 diagnoses over past 8 weeks, by week ──
    public static java.util.Map<String, java.util.Map<String, Integer>> getIllnessTrend() {
        // Returns: { "Asthma" -> { "Week 1" -> 2, "Week 2" -> 1, ... }, ... }
        // Step 1: find top 5 diagnoses in last 8 weeks
        java.util.List<String> top5 = new java.util.ArrayList<>();
        String top5sql = "SELECT diagnosis, COUNT(*) as cnt FROM Visits " +
                "WHERE visit_date >= DATE('now', '-56 days') " +
                "AND diagnosis IS NOT NULL AND diagnosis != '' " +
                "GROUP BY diagnosis ORDER BY cnt DESC LIMIT 5";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(top5sql)) {
            while (rs.next()) top5.add(rs.getString("diagnosis"));
        } catch (SQLException e) {
            System.err.println("IllnessTrend top5 Error: " + e.getMessage());
            return new java.util.LinkedHashMap<>();
        }
        if (top5.isEmpty()) return new java.util.LinkedHashMap<>();

        // Step 2: for each of those diagnoses, count per week label
        java.util.Map<String, java.util.Map<String, Integer>> result = new java.util.LinkedHashMap<>();
        // Build week labels: W-7 oldest, W-1 newest
        java.util.List<String> weekLabels = new java.util.ArrayList<>();
        for (int i = 7; i >= 0; i--) weekLabels.add("W-" + (i == 0 ? "now" : i));

        for (String diag : top5) {
            java.util.Map<String, Integer> weekMap = new java.util.LinkedHashMap<>();
            for (String lbl : weekLabels) weekMap.put(lbl, 0);
            // week offset: W-7 = 56-49 days ago, ..., W-now = 0-7 days ago
            for (int i = 7; i >= 0; i--) {
                String lbl = "W-" + (i == 0 ? "now" : i);
                int daysStart = (i + 1) * 7;
                int daysEnd   = i * 7;
                String weekSql = "SELECT COUNT(*) FROM Visits " +
                        "WHERE diagnosis = ? " +
                        "AND visit_date >= DATE('now', '-" + daysStart + " days') " +
                        "AND visit_date < DATE('now', '-" + daysEnd + " days')";
                try (Connection conn = connect();
                     PreparedStatement p = conn.prepareStatement(weekSql)) {
                    p.setString(1, diag);
                    ResultSet rs = p.executeQuery();
                    if (rs.next()) weekMap.put(lbl, rs.getInt(1));
                } catch (SQLException e) { /* keep 0 */ }
            }
            result.put(diag, weekMap);
        }
        return result;
    }

    public static java.util.Map<String, Integer> getPatientsByCollege() {
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        // Students grouped by course_dept
        String sql = "SELECT course_dept, COUNT(*) as cnt FROM Patients " +
                "WHERE (is_active = 1 OR is_active IS NULL) AND (type IS NULL OR type != 'Employee') " +
                "GROUP BY course_dept ORDER BY cnt DESC";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                map.put(nvl(rs.getString("course_dept")), rs.getInt("cnt"));
        } catch (SQLException e) {
            System.err.println("CollegeCount Error: " + e.getMessage());
        }
        // Employees as a single separate category
        String empSql = "SELECT COUNT(*) FROM Patients " +
                "WHERE (is_active = 1 OR is_active IS NULL) AND type = 'Employee'";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(empSql)) {
            if (rs.next()) {
                int empCount = rs.getInt(1);
                if (empCount > 0) map.put("Employee", empCount);
            }
        } catch (SQLException e) {
            System.err.println("EmployeeCount Error: " + e.getMessage());
        }
        return map;
    }

    public static java.util.Map<String, Integer> getTopIllnesses(String period) {
        String condition = switch (period.toLowerCase()) {
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

    /**
     * Returns visit counts grouped by college/dept for a given period.
     * This is different from getPatientsByCollege() which counts registered
     * patients — this counts actual VISITS joined to the patient's dept.
     *
     * Period values: "daily", "weekly", "monthly"
     */
    public static java.util.Map<String, Integer> getVisitsByCollegePeriod(String period) {
        String condition = switch (period.toLowerCase()) {
            case "daily"   -> "AND v.visit_date = DATE('now')";
            case "monthly" -> "AND v.visit_date >= DATE('now', '-30 days')";
            default        -> "AND v.visit_date >= DATE('now', '-7 days')";
        };
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        // Students — grouped by course_dept
        String sql = "SELECT p.course_dept, COUNT(*) as cnt " +
                "FROM Visits v JOIN Patients p ON v.patient_id = p.patient_id " +
                "WHERE (p.type IS NULL OR p.type != 'Employee') " + condition +
                " GROUP BY p.course_dept ORDER BY cnt DESC";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                map.put(nvl(rs.getString("course_dept")), rs.getInt("cnt"));
        } catch (SQLException e) {
            System.err.println("VisitsByCollegePeriod Error: " + e.getMessage());
        }
        // Employees — counted separately
        String empSql = "SELECT COUNT(*) FROM Visits v " +
                "JOIN Patients p ON v.patient_id = p.patient_id " +
                "WHERE p.type = 'Employee' " + condition;
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(empSql)) {
            if (rs.next()) {
                int cnt = rs.getInt(1);
                if (cnt > 0) map.put("Employees", cnt);
            }
        } catch (SQLException e) {
            System.err.println("VisitsByCollegePeriod Emp Error: " + e.getMessage());
        }
        return map;
    }

    /**
     * Returns a time-series of visit counts for the trend line chart.
     *
     * - "daily"   → counts per hour (00–23) for today
     * - "weekly"  → counts per week label (W-7 … W-now) over past 8 weeks
     * - "monthly" → counts per month (Jan–Dec) for the current year
     */
    public static java.util.Map<String, Integer> getVisitTrendByPeriod(String period) {
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        try (Connection conn = connect()) {
            switch (period.toLowerCase()) {

                case "daily": {
                    // Hours 00–23 today
                    String sql = "SELECT strftime('%H', time_in) as hr, COUNT(*) as cnt " +
                            "FROM Visits WHERE visit_date = DATE('now') " +
                            "AND time_in IS NOT NULL AND time_in != '' " +
                            "GROUP BY hr ORDER BY hr ASC";
                    try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
                        while (rs.next()) {
                            String hr = rs.getString("hr");
                            map.put(hr != null ? hr + ":00" : "?", rs.getInt("cnt"));
                        }
                    }
                    break;
                }

                case "monthly": {
                    // Jan–Dec this year
                    String sql = "SELECT strftime('%m', visit_date) as mo, COUNT(*) as cnt " +
                            "FROM Visits WHERE strftime('%Y', visit_date) = strftime('%Y', 'now') " +
                            "GROUP BY mo ORDER BY mo ASC";
                    try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
                        while (rs.next()) {
                            String mo = rs.getString("mo");
                            String name = switch (mo != null ? mo : "") {
                                case "01"->"Jan"; case "02"->"Feb"; case "03"->"Mar";
                                case "04"->"Apr"; case "05"->"May"; case "06"->"Jun";
                                case "07"->"Jul"; case "08"->"Aug"; case "09"->"Sep";
                                case "10"->"Oct"; case "11"->"Nov"; case "12"->"Dec";
                                default -> mo;
                            };
                            map.put(name, rs.getInt("cnt"));
                        }
                    }
                    break;
                }

                default: { // weekly — 8-week window
                    for (int i = 7; i >= 0; i--) {
                        String lbl      = (i == 0) ? "W-now" : "W-" + i;
                        int daysStart   = (i + 1) * 7;
                        int daysEnd     = i * 7;
                        String sql = "SELECT COUNT(*) FROM Visits " +
                                "WHERE visit_date >= DATE('now', '-" + daysStart + " days') " +
                                "AND visit_date < DATE('now', '-" + daysEnd + " days')";
                        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
                            map.put(lbl, rs.next() ? rs.getInt(1) : 0);
                        }
                    }
                    break;
                }
            }
        } catch (SQLException e) {
            System.err.println("VisitTrendByPeriod Error: " + e.getMessage());
        }
        return map;
    }

    public static java.util.Map<String, Integer> getVisitPurposeCount() {
        return getVisitPurposeCount("all");
    }

    public static java.util.Map<String, Integer> getVisitPurposeCount(String period) {
        String condition = switch (period.toLowerCase()) {
            case "daily"   -> "WHERE visit_date = DATE('now')";
            case "weekly"  -> "WHERE visit_date >= DATE('now', '-7 days')";
            case "monthly" -> "WHERE visit_date >= DATE('now', '-30 days')";
            default        -> "";
        };
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        String sql = "SELECT purpose, COUNT(*) as cnt FROM Visits " +
                condition + " GROUP BY purpose";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                map.put(nvl(rs.getString("purpose")), rs.getInt("cnt"));
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

    // ── SETTINGS ─────────────────────────────────────────────────
    public static boolean changePassword(String username, String oldPassword, String newPassword) {
        String verifySql = "SELECT * FROM Users WHERE username = ? AND password_hash = ?";
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(verifySql)) {
            p.setString(1, username); p.setString(2, oldPassword);
            if (!p.executeQuery().next()) return false;
        } catch (SQLException e) { return false; }

        String updateSql = "UPDATE Users SET password_hash = ? WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(updateSql)) {
            p.setString(1, newPassword); p.setString(2, username);
            p.executeUpdate(); return true;
        } catch (SQLException e) {
            System.err.println("ChangePassword Error: " + e.getMessage()); return false;
        }
    }

    public static List<java.util.Map<String, String>> getAllUsers() {
        List<java.util.Map<String, String>> list = new ArrayList<>();
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT username FROM Users")) {
            while (rs.next()) {
                java.util.Map<String, String> row = new java.util.LinkedHashMap<>();
                row.put("username", rs.getString("username")); list.add(row);
            }
        } catch (SQLException e) { System.err.println("GetAllUsers Error: " + e.getMessage()); }
        return list;
    }

    public static boolean addUser(String username, String password) {
        // Users table has a role column — default to STAFF
        String sql = "INSERT INTO Users(username, password_hash, role) VALUES(?, ?, 'STAFF')";
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, username); p.setString(2, password);
            p.executeUpdate(); return true;
        } catch (SQLException e) {
            System.err.println("AddUser Error: " + e.getMessage()); return false;
        }
    }

    public static boolean deleteUser(String username) {
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(
                     "DELETE FROM Users WHERE username = ?")) {
            p.setString(1, username); p.executeUpdate(); return true;
        } catch (SQLException e) {
            System.err.println("DeleteUser Error: " + e.getMessage()); return false;
        }
    }

    public static boolean resetUserPassword(String username, String newPassword) {
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(
                     "UPDATE Users SET password_hash = ? WHERE username = ?")) {
            p.setString(1, newPassword); p.setString(2, username);
            return p.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ResetPassword Error: " + e.getMessage()); return false;
        }
    }

    public static void ensureSettingsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS AppSettings (key TEXT PRIMARY KEY, value TEXT)";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) { System.err.println("EnsureSettings Error: " + e.getMessage()); }
    }

    public static void saveSetting(String key, String value) {
        ensureSettingsTable();
        String sql = "INSERT INTO AppSettings(key,value) VALUES(?,?) " +
                "ON CONFLICT(key) DO UPDATE SET value=excluded.value";
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, key); p.setString(2, value); p.executeUpdate();
        } catch (SQLException e) { System.err.println("SaveSetting Error: " + e.getMessage()); }
    }

    public static String getSetting(String key, String defaultValue) {
        ensureSettingsTable();
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(
                     "SELECT value FROM AppSettings WHERE key = ?")) {
            p.setString(1, key);
            ResultSet rs = p.executeQuery();
            if (rs.next()) return rs.getString("value");
        } catch (SQLException e) { System.err.println("GetSetting Error: " + e.getMessage()); }
        return defaultValue;
    }

    // ── BACKUP ───────────────────────────────────────────────────
    public static boolean backupDatabase(String destinationPath) {
        try {
            java.nio.file.Files.copy(
                    java.nio.file.Paths.get("cjclinic.db"),
                    java.nio.file.Paths.get(destinationPath),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Exception e) { System.err.println("Backup Error: " + e.getMessage()); return false; }
    }

    // ── EXPORT CSV ───────────────────────────────────────────────
    public static boolean exportPatientsToCSV(String filePath) {
        String sql = "SELECT patient_id, full_name, age, course_dept, year_level, gender, college, " +
                "contact_number, email, birthdate, type, emergency_name, relationship, " +
                "emergency_contact, allergies, conditions, blood_type, height, weight, medical_remarks " +
                "FROM Patients WHERE is_active = 1 OR is_active IS NULL";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(filePath))) {
            pw.println("ID,Full Name,Age,College/Dept,Year Level,Gender,Course," +
                    "Contact,Email,Birthdate,Type,Emergency Name,Relationship," +
                    "Emergency Contact,Allergies,Conditions,Blood Type,Height,Weight,Medical Remarks");
            while (rs.next()) {
                pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"," +
                                "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"," +
                                "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        safe(rs.getString("patient_id")), safe(rs.getString("full_name")),
                        safe(rs.getString("age")),        safe(rs.getString("course_dept")),
                        safe(rs.getString("year_level")), safe(rs.getString("gender")),
                        safe(rs.getString("college")),    safe(rs.getString("contact_number")),
                        safe(rs.getString("email")),      safe(rs.getString("birthdate")),
                        safe(rs.getString("type")),       safe(rs.getString("emergency_name")),
                        safe(rs.getString("relationship")),safe(rs.getString("emergency_contact")),
                        safe(rs.getString("allergies")),  safe(rs.getString("conditions")),
                        safe(rs.getString("blood_type")), safe(rs.getString("height")),
                        safe(rs.getString("weight")),     safe(rs.getString("medical_remarks")));
            }
            return true;
        } catch (Exception e) { System.err.println("ExportCSV Error: " + e.getMessage()); return false; }
    }

    public static boolean exportVisitsToCSV(String filePath) {
        String sql = "SELECT v.visit_id, v.patient_id, p.full_name, v.visit_date, v.time_in, v.time_out, " +
                "v.purpose, v.chief_complaint, v.diagnosis, v.treatment, v.remarks, " +
                "v.temperature, v.blood_pressure, v.heart_rate, v.oxygen_sat " +
                "FROM Visits v JOIN Patients p ON v.patient_id = p.patient_id " +
                "ORDER BY v.visit_date DESC";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(filePath))) {
            pw.println("Visit ID,Patient ID,Full Name,Date,Time In,Time Out,Purpose," +
                    "Chief Complaint,Diagnosis,Treatment,Remarks,Temperature,Blood Pressure,Heart Rate,SpO2");
            while (rs.next()) {
                pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"," +
                                "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        safe(rs.getString("visit_id")),       safe(rs.getString("patient_id")),
                        safe(rs.getString("full_name")),      safe(rs.getString("visit_date")),
                        safe(rs.getString("time_in")),        safe(rs.getString("time_out")),
                        safe(rs.getString("purpose")),        safe(rs.getString("chief_complaint")),
                        safe(rs.getString("diagnosis")),      safe(rs.getString("treatment")),
                        safe(rs.getString("remarks")),        safe(rs.getString("temperature")),
                        safe(rs.getString("blood_pressure")), safe(rs.getString("heart_rate")),
                        safe(rs.getString("oxygen_sat")));
            }
            return true;
        } catch (Exception e) { System.err.println("ExportVisitsCSV Error: " + e.getMessage()); return false; }
    }

    // Dashboard snapshot export (cards + chart data + filtered visit rows)
    public static boolean exportDashboardToCSV(String filePath, String period) {
        String normalized = period == null ? "weekly" : period.toLowerCase();
        String countKey = switch (normalized) {
            case "daily" -> "today";
            case "monthly" -> "monthly";
            default -> "weekly";
        };

        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(filePath))) {
            pw.println("Dashboard Export");
            pw.println("Period," + normalized);
            pw.println("Generated At," + java.time.LocalDateTime.now());
            pw.println();

            // Summary cards
            pw.println("Summary");
            pw.println("Metric,Value");
            pw.println("Visits (selected period)," + getVisitCount(countKey));
            pw.println("Total Patients," + getTotalPatients());
            pw.println("Unattended Today," + getUnattendedToday());
            pw.println();

            // Top diagnoses
            pw.println("Top Diagnoses");
            pw.println("Diagnosis,Count");
            java.util.Map<String, Integer> illnesses = getTopIllnesses(normalized);
            if (illnesses.isEmpty()) pw.println("No data,0");
            else illnesses.forEach((k, v) -> pw.println("\"" + safe(k) + "\"," + v));
            pw.println();

            // Purpose chart
            pw.println("Purpose of Visit");
            pw.println("Purpose,Count");
            java.util.Map<String, Integer> purpose = getVisitPurposeCount(normalized);
            if (purpose.isEmpty()) pw.println("No data,0");
            else purpose.forEach((k, v) -> pw.println("\"" + safe(k) + "\"," + v));
            pw.println();

            // College/Dept chart
            pw.println("Visits by College/Dept");
            pw.println("College/Dept,Count");
            java.util.Map<String, Integer> college = getPatientsByCollege();
            if (college.isEmpty()) pw.println("No data,0");
            else college.forEach((k, v) -> pw.println("\"" + safe(k) + "\"," + v));
            pw.println();

            // Underlying rows used for period
            pw.println("Visit Rows");
            pw.println("Visit ID,Patient ID,Full Name,Date,Time In,Time Out,Purpose,Diagnosis");
            for (java.util.Map<String, String> row : getAllVisits(normalized)) {
                pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        safe(row.get("visit_id")),
                        safe(row.get("patient_id")),
                        safe(row.get("full_name")),
                        safe(row.get("visit_date")),
                        safe(row.get("time_in")),
                        safe(row.get("time_out")),
                        safe(row.get("purpose")),
                        safe(row.get("diagnosis")));
            }
            return true;
        } catch (Exception e) {
            System.err.println("ExportDashboardCSV Error: " + e.getMessage());
            return false;
        }
    }

    // ── Utilities ─────────────────────────────────────────────────
    private static String safe(String s) { return s == null ? "" : s.replace("\"", "\"\""); }
    private static String nvl(String s)  { return s == null ? "" : s; }

    // ── FORGOT PASSWORD ───────────────────────────────────────────
    public static boolean userExists(String username) {
        String sql = "SELECT 1 FROM Users WHERE username = ? LIMIT 1";
        try (Connection conn = connect();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, username);
            ResultSet rs = p.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("UserExists Error: " + e.getMessage());
            return false;
        }
    }

    // ── PRESET LISTS (Diagnosis, Complaint, Medicine, Dept, Course) ──

    /** Ensures the Presets table exists. Called before every preset operation. */
    public static void ensurePresetsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Presets (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "category TEXT NOT NULL, " +
                "value TEXT NOT NULL, " +
                "UNIQUE(category, value))";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("EnsurePresets Error: " + e.getMessage());
        }
    }

    /** Returns all preset values for a given category (e.g. "diagnosis", "complaint", "medicine", "department", "course"). */
    public static List<String> getPresets(String category) {
        ensurePresetsTable();
        List<String> list = new ArrayList<>();
        String sql = "SELECT value FROM Presets WHERE category = ? ORDER BY value ASC";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, category);
            ResultSet rs = p.executeQuery();
            while (rs.next()) list.add(rs.getString("value"));
        } catch (SQLException e) {
            System.err.println("GetPresets Error: " + e.getMessage());
        }
        return list;
    }

    /** Adds a new preset value. Returns true if added, false if duplicate or error. */
    public static boolean addPreset(String category, String value) {
        ensurePresetsTable();
        String sql = "INSERT OR IGNORE INTO Presets(category, value) VALUES(?, ?)";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, category);
            p.setString(2, value.trim());
            return p.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("AddPreset Error: " + e.getMessage());
            return false;
        }
    }

    /** Removes a preset value. Returns true if deleted. */
    public static boolean removePreset(String category, String value) {
        ensurePresetsTable();
        String sql = "DELETE FROM Presets WHERE category = ? AND value = ?";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, category);
            p.setString(2, value);
            return p.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("RemovePreset Error: " + e.getMessage());
            return false;
        }
    }

    // ── SCHOOL YEAR ───────────────────────────────────────────────
    public static String getSchoolYear() {
        return getSetting("school_year", "2025-2026");
    }

    public static void saveSchoolYear(String value) {
        saveSetting("school_year", value.trim());
    }
}