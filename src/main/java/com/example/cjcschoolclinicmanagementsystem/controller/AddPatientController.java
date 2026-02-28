package com.example.cjcschoolclinicmanagementsystem.controller;

import com.example.cjcschoolclinicmanagementsystem.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddPatientController {
    @FXML private TextField txtStudentID, txtFullName, txtContact;
    @FXML private ComboBox<String> comboCourse, comboGender, comboYear;
    @FXML private DatePicker dateBirth;
    @FXML private TextArea txtMedicalHistory;

    public void initialize() {
        comboCourse.getItems().addAll("CCIS", "CBE", "CHS", "COE", "CAS");
        comboYear.getItems().addAll("1st Year", "2nd Year", "3rd Year", "4th Year");
        comboGender.getItems().addAll("Male", "Female", "Other");
    }

    @FXML
    private void handleSave() {
        String id = txtStudentID.getText();
        String name = txtFullName.getText();
        String course = comboCourse.getValue();
        String year = comboYear.getValue();
        String gender = comboGender.getValue();
        String contact = txtContact.getText();
        String bday = (dateBirth.getValue() != null) ? dateBirth.getValue().toString() : "";
        String history = txtMedicalHistory.getText();

        if (id.isEmpty() || name.isEmpty()) {
            showAlert("Required Fields", "Please enter Student ID and Full Name.");
            return;
        }

        if (DatabaseManager.addPatient(id, name, course, year, gender, contact, bday, history)) {
            closeWindow();
        }
    }

    @FXML private void handleCancel() { closeWindow(); }

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