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
import java.util.ArrayList;
import java.util.List;

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
    @FXML private ComboBox<String> comboCourse;   // was txtCourse — now editable ComboBox
    @FXML private TextField   txtPosition;
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
    private boolean isEditMode    = false;
    private String  editPatientId = null;

    private static final String[] DEFAULT_COLLEGE_DEPTS = {"CCIS","COE","CABE","CEDAS","CHS","CSP"};
    private static final String[] COLLEGE_YEARS = {"1st Year","2nd Year","3rd Year","4th Year", "5th year"};
    private static final String[] BED_YEARS     = {
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
        rbStudent.setSelected(true);
        rbCollege.setSelected(true);
        onTypeChanged();
        tabPane.getSelectionModel().selectedIndexProperty().addListener(
                (obs, oldIdx, newIdx) -> updateNavButtons(newIdx.intValue()));
        updateNavButtons(0);
    }

    // ── Load departments from DB, seed defaults if empty ─────────
    private List<String> loadCollegeDepts() {
        List<String> depts = DatabaseManager.getPresets("department");
        if (depts == null || depts.isEmpty()) {
            for (String d : DEFAULT_COLLEGE_DEPTS) DatabaseManager.addPreset("department", d);
            depts = new ArrayList<>(List.of(DEFAULT_COLLEGE_DEPTS));
        }
        return depts;
    }

    // ── Load courses from DB ──────────────────────────────────────
    private List<String> loadCourses() {
        return DatabaseManager.getPresets("course");
    }

    private void setupCombos() {
        comboGender.setItems(FXCollections.observableArrayList("Male","Female","Other"));
        comboBloodType.setItems(FXCollections.observableArrayList(
                "A+","A-","B+","B-","AB+","AB-","O+","O-","Unknown"));

        List<String> depts = loadCollegeDepts();
        comboDept.setItems(FXCollections.observableArrayList(depts));
        comboDeptEmployee.setItems(FXCollections.observableArrayList(depts));

        // Course: editable ComboBox — nurse can type freely OR pick from preset list
        comboCourse.setEditable(true);
        comboCourse.setItems(FXCollections.observableArrayList(loadCourses()));

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
            if (n != null) txtAge.setText(String.valueOf(Period.between(n, LocalDate.now()).getYears()));
        });
    }

    private void generatePatientId() {
        String nextId = DatabaseManager.generateNextPatientId();
        txtPatientId.setText(nextId);
        lblPatientIdDisplay.setText("Auto ID: " + nextId);
    }

    private void loadSchoolYear() {
        txtSchoolYear.setText(DatabaseManager.getSetting("school_year", getCurrentSchoolYear()));
    }

    private String getCurrentSchoolYear() {
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        return month >= 6 ? year + "–" + (year + 1) : (year - 1) + "–" + year;
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
        if (isBED) {
            comboDept.setItems(FXCollections.observableArrayList("BED"));
            comboDept.setValue("BED");
        } else {
            comboDept.setItems(FXCollections.observableArrayList(loadCollegeDepts()));
            comboDept.setValue(null);
        }
        comboYear.setItems(FXCollections.observableArrayList(isBED ? BED_YEARS : COLLEGE_YEARS));
        comboYear.setValue(null);
    }

    // ── Load patient for editing ──────────────────────────────────
    public void loadPatientForEdit(Patient p) {
        isEditMode    = true;
        editPatientId = p.getId();

        lblFormTitle.setText("Edit Patient Profile");
        txtPatientId.setText(p.getId());
        lblPatientIdDisplay.setText("Patient ID: " + p.getId());

        String full = p.getName();
        if (full != null && full.contains(",")) {
            String[] parts = full.split(",", 2);
            txtLastName.setText(parts[0].trim());
            String rest = parts[1].trim();
            if (rest.contains(" ")) {
                int lastSpace   = rest.lastIndexOf(" ");
                String possible = rest.substring(lastSpace + 1);
                if (possible.length() <= 3 && possible.endsWith(".")) {
                    txtFirstName.setText(rest.substring(0, lastSpace).trim());
                    txtMiddleInitial.setText(possible);
                } else { txtFirstName.setText(rest); }
            } else { txtFirstName.setText(rest); }
        } else { txtFirstName.setText(full); }

        txtStudentID.setText(p.getId());
        comboGender.setValue(p.getGender());
        comboBloodType.setValue(p.getBloodType());

        if (p.getBirthdate() != null && !p.getBirthdate().isBlank()) {
            try { dateBirth.setValue(LocalDate.parse(p.getBirthdate())); } catch (Exception ignored) {}
        }
        txtAge.setText(p.getAge());
        txtContact.setText(p.getContact());
        txtEmail.setText(p.getEmail());

        if ("Employee".equals(p.getType())) {
            rbEmployee.setSelected(true);
            onTypeChanged();
            txtPosition.setText(p.getYear());
            comboDeptEmployee.setValue(p.getCourse());
        } else {
            rbStudent.setSelected(true);
            String year = p.getYear();
            boolean isBED = year != null && (year.startsWith("Grade") || year.startsWith("Kinder"));
            if (isBED) rbBED.setSelected(true); else rbCollege.setSelected(true);
            onTypeChanged();
            comboDept.setValue(p.getCourse());
            comboYear.setValue(year);
            // Restore course name into the editable combo
            String savedCourse = p.getCollege();
            if (savedCourse != null && !savedCourse.isBlank()) {
                comboCourse.setValue(savedCourse);
            }
        }

        txtEmergencyName.setText(p.getEmergencyName());
        txtRelationship.setText(p.getRelationship());
        txtEmergencyContact.setText(p.getEmergencyContact());
        txtAllergies.setText(p.getAllergies());
        txtConditions.setText(p.getConditions());
        txtHeight.setText(p.getHeight());
        txtWeight.setText(p.getWeight());
        txtMedicalRemarks.setText(p.getMedicalRemarks());

        if (txtAddress       != null) txtAddress.setText(p.getAddress());
        if (txtFatherName    != null) txtFatherName.setText(p.getFatherName());
        if (txtFatherContact != null) txtFatherContact.setText(p.getFatherContact());
        if (txtMotherName    != null) txtMotherName.setText(p.getMotherName());
        if (txtMotherContact != null) txtMotherContact.setText(p.getMotherContact());

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
    @FXML
    public void onTabChanged() {
        updateNavButtons(tabPane.getSelectionModel().getSelectedIndex());
    }

    private void updateNavButtons(int tabIndex) {
        int lastTab  = tabPane.getTabs().size() - 1;
        boolean isFirst = (tabIndex == 0);
        boolean isLast  = (tabIndex == lastTab);
        btnBack.setVisible(!isFirst);  btnBack.setManaged(!isFirst);
        btnNext.setVisible(!isLast);   btnNext.setManaged(!isLast);
        btnSave.setVisible(isLast);    btnSave.setManaged(isLast);
        lblStep.setText("Step " + (tabIndex + 1) + " of " + (lastTab + 1));
        lblValidationMsg.setText("");
    }

    @FXML
    public void handleNext() {
        lblValidationMsg.setText("");
        if (tabPane.getSelectionModel().getSelectedIndex() == 0 && !validateTab1()) return;
        tabPane.getSelectionModel().select(tabPane.getSelectionModel().getSelectedIndex() + 1);
    }

    @FXML
    public void handleBack() {
        int current = tabPane.getSelectionModel().getSelectedIndex();
        if (current > 0) tabPane.getSelectionModel().select(current - 1);
    }

    private boolean validateTab1() {
        if (isBlank(txtLastName) || isBlank(txtFirstName)) {
            showValidationError("Last Name and First Name are required."); return false;
        }
        if (dateBirth.getValue() == null) {
            showValidationError("Birthdate is required."); return false;
        }
        if (comboGender.getValue() == null) {
            showValidationError("Gender is required."); return false;
        }
        if (isBlank(txtContact)) {
            showValidationError("Contact number is required."); return false;
        }
        if (rbStudent.isSelected() && comboDept.getValue() == null) {
            showValidationError("College / Dept is required."); return false;
        }
        if (rbStudent.isSelected() && comboYear.getValue() == null) {
            showValidationError("Year Level is required."); return false;
        }
        return true;
    }

    // ── Helper: get value from an editable ComboBox ───────────────
    private String getComboValue(ComboBox<String> combo) {
        if (combo == null) return "";
        String val = combo.getValue();
        if (val != null) return val.trim();
        // editable combo — check the editor text
        if (combo.getEditor() != null) return combo.getEditor().getText().trim();
        return "";
    }

    // ── Save ──────────────────────────────────────────────────────
    @FXML
    private void handleSave() {
        lblValidationMsg.setText("");
        if (!validateTab1()) return;
        if (isBlank(txtEmergencyName) || isBlank(txtRelationship) || isBlank(txtEmergencyContact)) {
            showValidationError("Emergency Contact fields (Name, Relationship, Contact #) are required.");
            tabPane.getSelectionModel().select(1); return;
        }
        if (isBlank(txtHeight) || isBlank(txtWeight)) {
            showValidationError("Height and Weight are required.");
            tabPane.getSelectionModel().select(2); return;
        }

        String lastName  = txtLastName.getText().trim();
        String firstName = txtFirstName.getText().trim();
        String mi        = txtMiddleInitial.getText().trim();
        String fullName  = lastName + ", " + firstName + (mi.isBlank() ? "" : " " + mi);

        String patientId = txtPatientId.getText().trim();
        String type      = rbEmployee.isSelected() ? "Employee" : "Student";
        String dept      = rbEmployee.isSelected()
                ? getComboValue(comboDeptEmployee)
                : getComboValue(comboDept);
        String yearLevel = rbEmployee.isSelected()
                ? (txtPosition != null ? txtPosition.getText().trim() : "")
                : getComboValue(comboYear);
        String course    = getComboValue(comboCourse);  // editable combo — typed or selected
        String gender    = comboGender.getValue();
        String bloodType = comboBloodType.getValue() != null ? comboBloodType.getValue() : "";
        String birthdate = dateBirth.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String age       = txtAge.getText().trim();
        String contact   = txtContact.getText().trim();
        String email     = txtEmail.getText().trim();

        String emergencyName    = txtEmergencyName.getText().trim();
        String relationship     = txtRelationship.getText().trim();
        String emergencyContact = txtEmergencyContact.getText().trim();
        String allergies        = txtAllergies.getText().trim();
        String conditions       = txtConditions.getText().trim();
        String height           = txtHeight.getText().trim();
        String weight           = txtWeight.getText().trim();
        String medicalRemarks   = txtMedicalRemarks.getText().trim();

        String address       = txtAddress       != null ? txtAddress.getText().trim()       : "";
        String fatherName    = txtFatherName    != null ? txtFatherName.getText().trim()    : "";
        String fatherContact = txtFatherContact != null ? txtFatherContact.getText().trim() : "";
        String motherName    = txtMotherName    != null ? txtMotherName.getText().trim()    : "";
        String motherContact = txtMotherContact != null ? txtMotherContact.getText().trim() : "";
        String position      = (rbEmployee.isSelected() && txtPosition != null)
                ? txtPosition.getText().trim() : "";
        String schoolYearVal = txtSchoolYear != null ? txtSchoolYear.getText().trim() : "";

        boolean success;
        if (isEditMode) {
            int isActive = (rbArchived != null && rbArchived.isSelected()) ? 0 : 1;
            success = DatabaseManager.updatePatientFull(
                    editPatientId, fullName, age, dept, yearLevel, gender, course,
                    contact, email, birthdate, type,
                    emergencyName, relationship, emergencyContact,
                    allergies, conditions, bloodType, height, weight, medicalRemarks,
                    address, fatherName, fatherContact, motherName, motherContact,
                    position, schoolYearVal);
            if (success && rbArchived != null) {
                DatabaseManager.setPatientActiveStatus(editPatientId, isActive);
            }
        } else {
            success = DatabaseManager.addPatientFull(
                    patientId, fullName, age, dept, yearLevel, gender, course,
                    contact, email, birthdate, type,
                    emergencyName, relationship, emergencyContact,
                    allergies, conditions, bloodType, height, weight, medicalRemarks,
                    address, fatherName, fatherContact, motherName, motherContact,
                    position, schoolYearVal);
        }

        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success"); alert.setHeaderText(null);
            alert.setContentText(isEditMode ? "Patient updated successfully!" : "Patient saved successfully!");
            alert.showAndWait();
            closeForm();
        } else {
            showValidationError("Failed to save. Check console for details.");
        }
    }

    @FXML private void handleCancel() { closeForm(); }

    private void closeForm() {
        ((Stage) txtLastName.getScene().getWindow()).close();
    }

    private boolean isBlank(TextField f) {
        return f == null || f.getText() == null || f.getText().isBlank();
    }

    private void showValidationError(String msg) {
        lblValidationMsg.setText("⚠ " + msg);
    }
}