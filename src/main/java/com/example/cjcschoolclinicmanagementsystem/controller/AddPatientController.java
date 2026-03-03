package com.example.cjcschoolclinicmanagementsystem.controller;

import com.example.cjcschoolclinicmanagementsystem.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddPatientController {
    // Personal Information
    @FXML private TextField txtStudentID, txtFullName, txtAge, txtCollege, txtContact, txtEmail;
    @FXML private ComboBox<String> comboCourse, comboGender, comboYear, comboType;
    @FXML private DatePicker dateBirth;

    // Emergency Contact
    @FXML private TextField txtEmergencyName, txtRelationship, txtEmergencyContact;

    // Medical History
    @FXML private TextField txtAllergies, txtConditions, txtHeight, txtWeight;
    @FXML private ComboBox<String> comboBloodType;
    @FXML private TextArea txtMedicalRemarks;

    @FXML private TabPane tabPane;

    public void initialize() {
        comboCourse.getItems().addAll("CABE", "CCIS", "CHS", "CEDAS", "COE", "CSP", "BED", "Graduate School");
        comboYear.getItems().addAll("1st Year", "2nd Year", "3rd Year", "4th Year", "5th Year", "6th Year");
        comboGender.getItems().addAll("Male", "Female", "Other");
        comboType.getItems().addAll("Student", "Employee");
        comboBloodType.getItems().addAll("O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-");

        setupTabValidation();
    }

    private void setupTabValidation() {
        tabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.intValue() > oldVal.intValue() && !isTabValid(oldVal.intValue())) {
                tabPane.getSelectionModel().select(oldVal.intValue());
                showAlert("Incomplete Section", "Please fill all required fields before proceeding.");
            }
        });
    }

    private boolean isTabValid(int tabIndex) {
        if (tabIndex == 0) { // Personal Information
            return !txtStudentID.getText().isEmpty() && !txtFullName.getText().isEmpty() &&
                   !txtAge.getText().isEmpty() && comboGender.getValue() != null &&
                   dateBirth.getValue() != null && comboCourse.getValue() != null &&
                   comboYear.getValue() != null && comboType.getValue() != null &&
                   !txtCollege.getText().isEmpty() && !txtContact.getText().isEmpty() &&
                   !txtEmail.getText().isEmpty();
        } else if (tabIndex == 1) { // Emergency Contact
            return !txtEmergencyName.getText().isEmpty() && !txtRelationship.getText().isEmpty() &&
                   !txtEmergencyContact.getText().isEmpty();
        } else if (tabIndex == 2) { // Medical History
            return !txtAllergies.getText().isEmpty() && !txtConditions.getText().isEmpty() &&
                   comboBloodType.getValue() != null && !txtHeight.getText().isEmpty() &&
                   !txtWeight.getText().isEmpty();
        }
        return true;
    }

    @FXML
    private void handleSave() {
        if (!isTabValid(2)) {
            showAlert("Incomplete Form", "Please fill all required fields in all sections.");
            return;
        }

        try {
            String studentId = txtStudentID.getText();
            String fullName = txtFullName.getText();
            String age = txtAge.getText();
            String course = comboCourse.getValue();
            String year = comboYear.getValue();
            String gender = comboGender.getValue();
            String type = comboType.getValue();
            String college = txtCollege.getText();
            String contact = txtContact.getText();
            String email = txtEmail.getText();
            String birthdate = (dateBirth.getValue() != null) ? dateBirth.getValue().toString() : "";

            String emergencyName = txtEmergencyName.getText();
            String relationship = txtRelationship.getText();
            String emergencyContact = txtEmergencyContact.getText();

            String allergies = txtAllergies.getText();
            String conditions = txtConditions.getText();
            String bloodType = comboBloodType.getValue();
            String height = txtHeight.getText();
            String weight = txtWeight.getText();
            String medicalRemarks = txtMedicalRemarks.getText();

            if (DatabaseManager.addPatient(studentId, fullName, age, course, year, gender, college,
                    contact, email, birthdate, type, emergencyName, relationship, emergencyContact,
                    allergies, conditions, bloodType, height, weight, medicalRemarks)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Patient Saved Successfully!");
                alert.setContentText("The patient information has been saved to the database.");
                alert.showAndWait();
                closeWindow();
            } else {
                showAlert("Error", "Failed to save patient. Please try again.");
            }
        } catch (Exception e) {
            showAlert("Error", "An error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) txtStudentID.getScene().getWindow()).close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}