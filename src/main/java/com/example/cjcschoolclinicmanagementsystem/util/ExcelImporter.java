package com.example.cjcschoolclinicmanagementsystem.util;

import com.example.cjcschoolclinicmanagementsystem.model.Patient;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.util.*;

/**
 * Reads a registrar's .xlsx file and converts each data row into a Patient object.
 *
 * HOW TO USE:
 *   Set<String> existing = DatabaseManager.getAllPatientIds();   // fetch existing IDs
 *   ImportResult result  = ExcelImporter.parse(file, existing);
 *   result.rows()   → List<PatientRow>  (each row has patient + isDuplicate flag + rowNum)
 *   result.errors() → List<String>      (parse-time warnings)
 *
 * SUPPORTED COLUMN HEADERS (case-insensitive, any order):
 *   Patient ID / Student ID / Employee ID / ID
 *   Name / Full Name / Student Name
 *   Age
 *   Course / Program          → stored as "college"  (e.g. BSCS)
 *   Department / Dept         → stored as "course_dept" (e.g. CCIS)
 *   Year / Year Level / Grade
 *   Gender / Sex
 *   Contact / Contact Number / Phone / Mobile
 *   Email
 *   Birthdate / Birth Date / DOB / Birthday
 *   Type                      → "Student" or "Employee" (defaults to Student if blank)
 *   Blood Type / Blood Group
 *   Height, Weight
 *   Address
 *   Father Name / Father's Name
 *   Father Contact
 *   Mother Name / Mother's Name
 *   Mother Contact
 *   Emergency Name / Emergency Contact Name
 *   Relationship
 *   Emergency Contact / Emergency Contact Number
 *   Allergies
 *   Conditions / Medical Conditions
 *   Medical Remarks / Remarks
 *   Position / Job Title      → for Employee
 *   School Year / SY / Academic Year
 */
public class ExcelImporter {

    // ── Result containers ─────────────────────────────────────────

    public record ImportResult(List<PatientRow> rows, List<String> errors) {}

    public record PatientRow(Patient patient, boolean isDuplicate, int rowNum) {}

    // ── Column-header → canonical-field mapping ───────────────────

    private static final Map<String, String> HEADER_MAP = new LinkedHashMap<>();

    static {
        // patient_id
        put("student id",   "patient_id"); put("student_id",    "patient_id");
        put("patient id",   "patient_id"); put("patient_id",    "patient_id");
        put("employee id",  "patient_id"); put("employee_id",   "patient_id");
        put("id",           "patient_id");

        // full_name
        put("name",          "full_name"); put("full name",     "full_name");
        put("full_name",     "full_name"); put("student name",  "full_name");
        put("employee name", "full_name"); put("full_name",     "full_name");

        // age
        put("age", "age");

        // college — the program abbreviation (e.g. BSCS, BSN, Grade 10)
        put("course",         "college"); put("college",        "college");
        put("program",        "college"); put("course program", "college");

        // course_dept — department abbreviation (e.g. CCIS, CAS)
        put("department",  "course_dept"); put("dept",          "course_dept");
        put("course_dept", "course_dept"); put("course dept",   "course_dept");
        put("college dept","course_dept");

        // year_level
        put("year",           "year_level"); put("year level",  "year_level");
        put("year_level",     "year_level"); put("grade",       "year_level");
        put("grade level",    "year_level");

        // gender
        put("gender", "gender"); put("sex", "gender");

        // contact_number
        put("contact",          "contact_number"); put("contact number", "contact_number");
        put("contact_number",   "contact_number"); put("phone",          "contact_number");
        put("mobile",           "contact_number"); put("mobile number",  "contact_number");

        // email
        put("email", "email"); put("email address", "email");

        // birthdate
        put("birthdate",    "birthdate"); put("birth date",    "birthdate");
        put("date of birth","birthdate"); put("dob",           "birthdate");
        put("birthday",     "birthdate");

        // type
        put("type",            "type"); put("patient type",    "type");
        put("student/employee","type"); put("classification",  "type");

        // blood_type
        put("blood type",  "blood_type"); put("blood_type",   "blood_type");
        put("blood group", "blood_type"); put("bloodtype",    "blood_type");

        // height / weight
        put("height", "height"); put("weight", "weight");

        // address
        put("address", "address"); put("home address", "address");

        // parents
        put("father name",    "father_name");    put("father's name", "father_name");
        put("father_name",    "father_name");
        put("father contact", "father_contact"); put("father_contact","father_contact");
        put("mother name",    "mother_name");    put("mother's name", "mother_name");
        put("mother_name",    "mother_name");
        put("mother contact", "mother_contact"); put("mother_contact","mother_contact");

        // emergency
        put("emergency name",           "emergency_name");
        put("emergency_name",           "emergency_name");
        put("emergency contact name",   "emergency_name");
        put("relationship",             "relationship");
        put("emergency contact",        "emergency_contact");
        put("emergency_contact",        "emergency_contact");
        put("emergency contact number", "emergency_contact");

        // medical
        put("allergies",       "allergies");
        put("conditions",      "conditions"); put("medical conditions", "conditions");
        put("medical remarks", "medical_remarks");
        put("medical_remarks", "medical_remarks"); put("remarks", "medical_remarks");

        // position (employee)
        put("position",  "position"); put("job title", "position");
        put("job_title", "position");

        // school_year
        put("school year",   "school_year"); put("school_year",  "school_year");
        put("sy",            "school_year"); put("academic year","school_year");
    }

    private static void put(String header, String field) {
        HEADER_MAP.put(header.toLowerCase(), field);
    }

    // ── Public API ────────────────────────────────────────────────

    /**
     * Parse an .xlsx file.
     *
     * @param file        The Excel file to parse (first sheet used).
     * @param existingIds Set of patient IDs already in the database (used to flag duplicates).
     * @return            An ImportResult containing rows and any parse errors.
     */
    public static ImportResult parse(File file, Set<String> existingIds) {
        List<PatientRow> rows   = new ArrayList<>();
        List<String>     errors = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook   = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                errors.add("The Excel file appears to be empty.");
                return new ImportResult(rows, errors);
            }

            // Map column index → canonical field name
            Map<Integer, String> colMap = new LinkedHashMap<>();
            for (Cell cell : headerRow) {
                String rawHeader = cellToString(cell).toLowerCase().trim();
                String field     = HEADER_MAP.get(rawHeader);
                if (field != null) {
                    colMap.put(cell.getColumnIndex(), field);
                }
            }

            // Validate that at minimum ID + Name columns were found
            if (!colMap.containsValue("patient_id")) {
                errors.add("❌ No ID column found. Expected header: 'Student ID', 'Patient ID', or 'ID'.");
                return new ImportResult(rows, errors);
            }
            if (!colMap.containsValue("full_name")) {
                errors.add("❌ No Name column found. Expected header: 'Name', 'Full Name', or 'Student Name'.");
                return new ImportResult(rows, errors);
            }

            Set<String> seenInFile = new HashSet<>(); // catch duplicates within the same file

            for (int rowIdx = 1; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (isRowBlank(row)) continue;

                // Collect all cell values keyed by canonical field name
                Map<String, String> data = new HashMap<>();
                for (Map.Entry<Integer, String> entry : colMap.entrySet()) {
                    Cell cell = row.getCell(entry.getKey(), Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    data.put(entry.getValue(), cellToString(cell));
                }

                String pid  = data.getOrDefault("patient_id", "").trim();
                String name = data.getOrDefault("full_name",  "").trim();

                if (pid.isEmpty()) continue;  // skip rows with no ID

                if (name.isEmpty()) {
                    errors.add("Row " + (rowIdx + 1) + ": No name for ID '" + pid + "' — skipped.");
                    continue;
                }

                boolean isDuplicate = existingIds.contains(pid) || seenInFile.contains(pid);
                seenInFile.add(pid);

                // Build Patient object (uses the existing 20-param constructor)
                Patient p = new Patient(
                    pid,
                    name,
                    data.getOrDefault("age",               ""),
                    data.getOrDefault("course_dept",       ""),
                    data.getOrDefault("year_level",        ""),
                    data.getOrDefault("gender",            ""),
                    data.getOrDefault("college",           ""),
                    data.getOrDefault("contact_number",    ""),
                    data.getOrDefault("email",             ""),
                    data.getOrDefault("birthdate",         ""),
                    data.getOrDefault("type",              "Student"),
                    data.getOrDefault("emergency_name",    ""),
                    data.getOrDefault("relationship",      ""),
                    data.getOrDefault("emergency_contact", ""),
                    data.getOrDefault("allergies",         ""),
                    data.getOrDefault("conditions",        ""),
                    data.getOrDefault("blood_type",        ""),
                    data.getOrDefault("height",            ""),
                    data.getOrDefault("weight",            ""),
                    data.getOrDefault("medical_remarks",   "")
                );

                // Extended fields (post-constructor setters)
                p.setAddress(data.getOrDefault("address",        ""));
                p.setFatherName(data.getOrDefault("father_name",    ""));
                p.setFatherContact(data.getOrDefault("father_contact", ""));
                p.setMotherName(data.getOrDefault("mother_name",    ""));
                p.setMotherContact(data.getOrDefault("mother_contact", ""));
                p.setPosition(data.getOrDefault("position",       ""));
                p.setSchoolYear(data.getOrDefault("school_year",    ""));

                rows.add(new PatientRow(p, isDuplicate, rowIdx + 1));
            }

        } catch (Exception e) {
            errors.add("Failed to read file: " + e.getMessage());
        }

        return new ImportResult(rows, errors);
    }

    // ── Template download ─────────────────────────────────────────

    /**
     * Creates a blank template .xlsx file that nurses can fill out
     * and then import.  Only the most essential columns are included.
     */
    public static boolean writeTemplate(File dest) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Patients");

            // Create a bold header style
            CellStyle headerStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font font = wb.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] headers = {
                "Student ID",    "Name",           "Age",          "Course",
                "Department",    "Year Level",     "Gender",       "Contact Number",
                "Email",         "Birthdate",      "Type",         "Blood Type",
                "Height",        "Weight",         "Address",      "Father Name",
                "Father Contact","Mother Name",    "Mother Contact","Emergency Name",
                "Relationship",  "Emergency Contact","Allergies",  "Conditions",
                "Medical Remarks","Position",      "School Year"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            // Add one example row so nurses can see the expected format
            Row exRow = sheet.createRow(1);
            String[] example = {
                "2024-00001", "Juan dela Cruz", "18", "BSCS",
                "CCIS",       "1st Year",        "Male","09171234567",
                "juan@email.com","2006-01-15",   "Student","O+",
                "165",         "60",             "Davao City",  "Pedro dela Cruz",
                "09181234567","Maria dela Cruz","09191234567","Pedro dela Cruz",
                "Father",     "09181234567",     "None",        "None",
                "None",        "",               "2024-2025"
            };
            for (int i = 0; i < example.length; i++) {
                exRow.createCell(i).setCellValue(example[i]);
            }

            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(dest)) {
                wb.write(fos);
            }
            return true;
        } catch (Exception e) {
            System.err.println("Template write error: " + e.getMessage());
            return false;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────

    private static String cellToString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    try {
                        LocalDate d = cell.getLocalDateTimeCellValue().toLocalDate();
                        yield d.toString(); // YYYY-MM-DD
                    } catch (Exception e) {
                        yield "";
                    }
                }
                double v = cell.getNumericCellValue();
                // Avoid "123456.0" for ID-type columns
                yield (v == Math.floor(v) && !Double.isInfinite(v))
                        ? String.valueOf((long) v)
                        : String.valueOf(v);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try { yield cell.getStringCellValue().trim(); }
                catch (Exception ex) {
                    double v = cell.getNumericCellValue();
                    yield (v == Math.floor(v)) ? String.valueOf((long) v) : String.valueOf(v);
                }
            }
            default -> "";
        };
    }

    private static boolean isRowBlank(Row row) {
        if (row == null) return true;
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && !cellToString(cell).isEmpty()) return false;
        }
        return true;
    }
}
