package com.example.cjcschoolclinicmanagementsystem.controller;

import com.example.cjcschoolclinicmanagementsystem.database.DatabaseManager;
import com.example.cjcschoolclinicmanagementsystem.model.Patient;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class AddPatientController {

    // ── FXML ─────────────────────────────────────────────────────
    @FXML private Label       lblFormTitle, lblPatientIdDisplay, lblValidationMsg;
    @FXML private TabPane     tabPane;
    @FXML private Button      btnBack, btnNext, btnSave;
    @FXML private Label       lblStep;

    // Personal
    @FXML private TextField   txtPatientId, txtStudentID, txtSchoolYear;
    @FXML private TextField   txtLastName, txtFirstName, txtMiddleInitial;
    @FXML private DatePicker  dateBirth;
    @FXML private TextField   txtAge;
    @FXML private ComboBox<String> comboGender, comboBloodType;
    @FXML private RadioButton rbStudent, rbEmployee, rbCollege, rbBED;
    @FXML private VBox        paneStudentFields, paneEmployeeFields;
    @FXML private ComboBox<String> comboDept, comboYear, comboDeptEmployee;
    @FXML private TextField   txtCourse, txtPosition;
    @FXML private TextField   txtContact, txtEmail, txtAddress;

    // Contact & Emergency
    @FXML private TextField   txtFatherName, txtFatherContact;
    @FXML private TextField   txtMotherName, txtMotherContact;
    @FXML private TextField   txtEmergencyName, txtRelationship, txtEmergencyContact;

    // Medical
    @FXML private TextField   txtHeight, txtWeight;
    @FXML private TextArea    txtAllergies, txtConditions, txtMedicalRemarks;
    @FXML private VBox        paneStatus;
    @FXML private RadioButton rbActive, rbArchived;
    @FXML private ComboBox<String> comboArchiveReason;

    // ── State ─────────────────────────────────────────────────────
    private boolean isEditMode = false;
    private String  editPatientId = null;   // original ID for update

    private static final String[] COLLEGE_DEPTS  = {"CCIS","COE","CABE","CEDAS","CHS","CSP"};
    private static final String[] COLLEGE_YEARS  = {"1st Year","2nd Year","3rd Year","4th Year"};
    private static final String[] BED_YEARS      = {
            "Kinder",
            "Grade 1","Grade 2","Grade 3","Grade 4","Grade 5","Grade 6",
            "Grade 7","Grade 8","Grade 9","Grade 10","Grade 11","Grade 12"
    };

    // ── Init ──────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        setupCombos();
        setupTypeToggle();
        setupBirthDateListener();
        generatePatientId();
        loadSchoolYear();
        // Default: Student + College
        rbStudent.setSelected(true);
        rbCollege.setSelected(true);
        onTypeChanged();
        // Wire tab-change listener so buttons update when user clicks a tab directly
        tabPane.getSelectionModel().selectedIndexProperty().addListener(
                (obs, oldIdx, newIdx) -> updateNavButtons(newIdx.intValue()));
        updateNavButtons(0);
    }

    private void setupCombos() {
        comboGender.setItems(FXCollections.observableArrayList("Male","Female","Other"));
        comboBloodType.setItems(FXCollections.observableArrayList(
                "A+","A-","B+","B-","AB+","AB-","O+","O-","Unknown"));

        comboDept.setItems(FXCollections.observableArrayList(COLLEGE_DEPTS));
        comboDeptEmployee.setItems(FXCollections.observableArrayList(COLLEGE_DEPTS));

        comboArchiveReason.setItems(FXCollections.observableArrayList(
                "Graduated","Transferred","Resigned","Other"));
    }

    private void setupTypeToggle() {
        ToggleGroup typeGroup = new ToggleGroup();
        rbStudent .setToggleGroup(typeGroup);
        rbEmployee.setToggleGroup(typeGroup);

        ToggleGroup subTypeGroup = new ToggleGroup();
        rbCollege.setToggleGroup(subTypeGroup);
        rbBED    .setToggleGroup(subTypeGroup);
    }

    private void setupBirthDateListener() {
        dateBirth.valueProperty().addListener((obs, o, n) -> {
            if (n != null) {
                int age = Period.between(n, LocalDate.now()).getYears();
                txtAge.setText(String.valueOf(age));
            }
        });
    }

    private void generatePatientId() {
        // Format: YYYY-XXXX  where YYYY is current year and XXXX is next sequential number
        String nextId = DatabaseManager.generateNextPatientId();
        txtPatientId.setText(nextId);
        lblPatientIdDisplay.setText("Auto ID: " + nextId);
    }

    private void loadSchoolYear() {
        String sy = DatabaseManager.getSetting("school_year", getCurrentSchoolYear());
        txtSchoolYear.setText(sy);
    }

    private String getCurrentSchoolYear() {
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        // School year starts June
        if (month >= 6) return year + "–" + (year + 1);
        else return (year - 1) + "–" + year;
    }

    // ── Type toggle handlers ──────────────────────────────────────
    @FXML
    public void onTypeChanged() {
        boolean isStudent = rbStudent.isSelected();
        paneStudentFields.setVisible(isStudent);
        paneStudentFields.setManaged(isStudent);
        paneEmployeeFields.setVisible(!isStudent);
        paneEmployeeFields.setManaged(!isStudent);
        if (isStudent) onSubTypeChanged();
    }

    @FXML
    public void onSubTypeChanged() {
        if (!rbStudent.isSelected()) return;
        boolean isBED = rbBED.isSelected();
        comboDept.setItems(FXCollections.observableArrayList(
                isBED ? new String[]{"BED"} : COLLEGE_DEPTS));
        comboYear.setItems(FXCollections.observableArrayList(
                isBED ? BED_YEARS : COLLEGE_YEARS));
        if (isBED) comboDept.setValue("BED");
        else       comboDept.setValue(null);
        comboYear.setValue(null);
    }

    // ── Load patient for editing ──────────────────────────────────
    public void loadPatientForEdit(Patient p) {
        isEditMode   = true;
        editPatientId = p.getId();

        lblFormTitle.setText("Edit Patient Profile");
        txtPatientId.setText(p.getId());
        lblPatientIdDisplay.setText("Patient ID: " + p.getId());

        // Split full name: "Last, First MI" → fields
        String full = p.getName();
        if (full != null && full.contains(",")) {
            String[] parts = full.split(",", 2);
            txtLastName.setText(parts[0].trim());
            String rest = parts[1].trim();
            // try to get MI
            if (rest.contains(" ")) {
                int lastSpace = rest.lastIndexOf(" ");
                String possible = rest.substring(lastSpace + 1);
                if (possible.length() <= 3 && possible.endsWith(".")) {
                    txtFirstName.setText(rest.substring(0, lastSpace).trim());
                    txtMiddleInitial.setText(possible);
                } else {
                    txtFirstName.setText(rest);
                }
            } else {
                txtFirstName.setText(rest);
            }
        } else {
            txtFirstName.setText(full);
        }

        txtStudentID.setText(p.getId());
        comboGender.setValue(p.getGender());
        comboBloodType.setValue(p.getBloodType());

        if (p.getBirthdate() != null && !p.getBirthdate().isBlank()) {
            try { dateBirth.setValue(LocalDate.parse(p.getBirthdate())); }
            catch (Exception ignored) {}
        }
        txtAge.setText(p.getAge());
        txtContact.setText(p.getContact());
        txtEmail.setText(p.getEmail());

        // Type
        if ("Employee".equals(p.getType())) {
            rbEmployee.setSelected(true);
            txtPosition.setText(p.getYear()); // position stored in year for employees
            comboDeptEmployee.setValue(p.getCourse());
        } else {
            rbStudent.setSelected(true);
            String year = p.getYear();
            boolean isBED = year != null && (year.startsWith("Grade") || year.startsWith("Kinder"));
            if (isBED) rbBED.setSelected(true);
            else       rbCollege.setSelected(true);
            comboDept.setValue(p.getCourse());
            comboYear.setValue(year);
            txtCourse.setText(p.getCollege()); // course name stored in college field
        }
        onTypeChanged();

        // Emergency
        txtEmergencyName.setText(p.getEmergencyName());
        txtRelationship.setText(p.getRelationship());
        txtEmergencyContact.setText(p.getEmergencyContact());

        // Medical
        txtAllergies.setText(p.getAllergies());
        txtConditions.setText(p.getConditions());
        txtHeight.setText(p.getHeight());
        txtWeight.setText(p.getWeight());
        txtMedicalRemarks.setText(p.getMedicalRemarks());

        // Show status panel
        paneStatus.setVisible(true);
        paneStatus.setManaged(true);
        ToggleGroup statusGroup = new ToggleGroup();
        rbActive.setToggleGroup(statusGroup);
        rbArchived.setToggleGroup(statusGroup);
        if ("0".equals(p.getIsActive())) rbArchived.setSelected(true);
        else rbActive.setSelected(true);

        loadSchoolYear();
    }

    // ── Tab navigation ────────────────────────────────────────────
    /** Called by TabPane onMouseClicked so buttons stay in sync when user clicks a tab header */
    @FXML
    public void onTabChanged() {
        updateNavButtons(tabPane.getSelectionModel().getSelectedIndex());
    }

    private void updateNavButtons(int tabIndex) {
        int lastTab = tabPane.getTabs().size() - 1;
        boolean isFirst = (tabIndex == 0);
        boolean isLast  = (tabIndex == lastTab);

        // Back: hidden on first tab
        btnBack.setVisible(!isFirst);
        btnBack.setManaged(!isFirst);

        // Next: shown on every tab except the last
        btnNext.setVisible(!isLast);
        btnNext.setManaged(!isLast);

        // Save: shown only on the last tab
        btnSave.setVisible(isLast);
        btnSave.setManaged(isLast);

        // Step indicator
        lblStep.setText("Step " + (tabIndex + 1) + " of " + (lastTab + 1));
        lblValidationMsg.setText("");
    }

    @FXML
    public void handleNext() {
        lblValidationMsg.setText("");
        int current = tabPane.getSelectionModel().getSelectedIndex();

        // Validate Tab 1 before moving to Tab 2
        if (current == 0) {
            if (isBlank(txtLastName) || isBlank(txtFirstName)) {
                showValidationError("Last Name and First Name are required."); return;
            }
            if (dateBirth.getValue() == null) {
                showValidationError("Birthdate is required."); return;
            }
            if (comboGender.getValue() == null) {
                showValidationError("Gender is required."); return;
            }
            if (isBlank(txtContact)) {
                showValidationError("Contact number is required."); return;
            }
            if (rbStudent.isSelected() && comboDept.getValue() == null) {
                showValidationError("College / Dept is required."); return;
            }
            if (rbStudent.isSelected() && comboYear.getValue() == null) {
                showValidationError("Year Level is required."); return;
            }
        }

        tabPane.getSelectionModel().select(current + 1);
    }

    @FXML
    public void handleBack() {
        int current = tabPane.getSelectionModel().getSelectedIndex();
        if (current > 0) tabPane.getSelectionModel().select(current - 1);
    }

    // ── Save ──────────────────────────────────────────────────────
    @FXML
    private void handleSave() {
        lblValidationMsg.setText("");

        // Required field validation
        if (isBlank(txtLastName) || isBlank(txtFirstName)) {
            showValidationError("Last Name and First Name are required."); return;
        }
        if (dateBirth.getValue() == null) {
            showValidationError("Birthdate is required."); return;
        }
        if (comboGender.getValue() == null) {
            showValidationError("Gender is required."); return;
        }
        if (isBlank(txtContact)) {
            showValidationError("Contact number is required."); return;
        }
        if (rbStudent.isSelected() && comboDept.getValue() == null) {
            showValidationError("College / Dept is required."); return;
        }
        if (rbStudent.isSelected() && comboYear.getValue() == null) {
            showValidationError("Year Level is required."); return;
        }
        if (isBlank(txtEmergencyName) || isBlank(txtRelationship) || isBlank(txtEmergencyContact)) {
            showValidationError("Emergency Contact fields (Name, Relationship, Contact #) are required.");
            tabPane.getSelectionModel().select(1); return;
        }
        if (isBlank(txtHeight) || isBlank(txtWeight)) {
            showValidationError("Height and Weight are required.");
            tabPane.getSelectionModel().select(2); return;
        }

        // Build full name
        String lastName  = txtLastName.getText().trim();
        String firstName = txtFirstName.getText().trim();
        String mi        = txtMiddleInitial.getText().trim();
        String fullName  = lastName + ", " + firstName + (mi.isBlank() ? "" : " " + mi);

        String patientId  = txtPatientId.getText().trim();
        String schoolEmpId = txtStudentID.getText().trim();
        String type       = rbEmployee.isSelected() ? "Employee" : "Student";
        String dept       = rbEmployee.isSelected()
                ? (comboDeptEmployee.getValue() != null ? comboDeptEmployee.getValue() : "")
                : (comboDept.getValue() != null ? comboDept.getValue() : "");
        String yearLevel  = rbEmployee.isSelected() ? txtPosition.getText().trim()
                : (comboYear.getValue() != null ? comboYear.getValue() : "");
        String course     = txtCourse.getText().trim();
        String gender     = comboGender.getValue();
        String bloodType  = comboBloodType.getValue() != null ? comboBloodType.getValue() : "";
        String birthdate  = dateBirth.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String age        = txtAge.getText().trim();
        String contact    = txtContact.getText().trim();
        String email      = txtEmail.getText().trim();

        String emergencyName    = txtEmergencyName.getText().trim();
        String relationship     = txtRelationship.getText().trim();
        String emergencyContact = txtEmergencyContact.getText().trim();

        String allergies       = txtAllergies.getText().trim();
        String conditions      = txtConditions.getText().trim();
        String height          = txtHeight.getText().trim();
        String weight          = txtWeight.getText().trim();
        String medicalRemarks  = txtMedicalRemarks.getText().trim();

        boolean success;

        if (isEditMode) {
            // Archive status
            int isActive = (rbArchived != null && rbArchived.isSelected()) ? 0 : 1;
            success = DatabaseManager.updatePatient(
                    editPatientId, fullName, age, dept, yearLevel, gender, course,
                    contact, email, birthdate, type,
                    emergencyName, relationship, emergencyContact,
                    allergies, conditions, bloodType, height, weight, medicalRemarks
            );
            if (success && rbArchived != null) {
                DatabaseManager.setPatientActiveStatus(editPatientId, isActive);
            }
        } else {
            success = DatabaseManager.addPatient(
                    patientId, fullName, age, dept, yearLevel, gender, course,
                    contact, email, birthdate, type,
                    emergencyName, relationship, emergencyContact,
                    allergies, conditions, bloodType, height, weight, medicalRemarks
            );
        }

        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText(isEditMode ? "Patient updated successfully!" : "Patient saved successfully!");
            alert.showAndWait();
            closeForm();
        } else {
            showValidationError("Failed to save. Check console for details.");
        }
    }

    @FXML
    private void handleCancel() { closeForm(); }

    private void closeForm() {
        Stage stage = (Stage) txtLastName.getScene().getWindow();
        stage.close();
    }

    private boolean isBlank(TextField f) {
        return f == null || f.getText() == null || f.getText().isBlank();
    }

    private void showValidationError(String msg) {
        lblValidationMsg.setText("⚠ " + msg);
    }
}