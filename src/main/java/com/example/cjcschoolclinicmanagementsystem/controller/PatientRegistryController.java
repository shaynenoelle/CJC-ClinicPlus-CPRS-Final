package com.example.cjcschoolclinicmanagementsystem.controller;

import com.example.cjcschoolclinicmanagementsystem.database.DatabaseManager;
import com.example.cjcschoolclinicmanagementsystem.model.Patient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class PatientRegistryController {

    @FXML private TableView<Patient> patientTable;
    @FXML private TableColumn<Patient, String> colID, colName, colType, colDept;

    private ObservableList<Patient> patientList = FXCollections.observableArrayList();

    public void initialize() {
        // Link UI columns to the Patient model properties
        colID.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        colName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        colType.setCellValueFactory(cellData -> cellData.getValue().yearProperty());
        colDept.setCellValueFactory(cellData -> cellData.getValue().courseProperty());

        // Add double-click listener to the table for Name column
        patientTable.setRowFactory(tv -> {
            TableRow<Patient> row = new TableRow<Patient>() {
                @Override
                protected void updateItem(Patient patient, boolean empty) {
                    super.updateItem(patient, empty);
                }
            };
            row.setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Patient patient = row.getItem();
                    showPatientDetails(patient);
                }
            });
            return row;
        });

        refreshTable();
    }

    private void showPatientDetails(Patient patient) {
        System.out.println("Viewing details for: " + patient.getName());

        Stage stage = new Stage();
        stage.setTitle("Patient Details - " + patient.getName());
        stage.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #FFFFFF;");

        // Title
        Label titleLabel = new Label("Patient Information");
        titleLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #8B0000;");

        // Create a grid for patient info
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.setStyle("-fx-border-color: #E0E0E0; -fx-border-radius: 5; -fx-padding: 15;");

        // Create text fields for editing
        TextField nameField = new TextField(patient.getName());
        TextField yearField = new TextField(patient.getYear());
        TextField courseField = new TextField(patient.getCourse());
        TextField genderField = new TextField(patient.getGender());
        TextField contactField = new TextField(patient.getContact());
        TextField birthdateField = new TextField(patient.getBirthdate());
        TextArea historyArea = new TextArea(patient.getMedicalHistory());
        historyArea.setWrapText(true);
        historyArea.setPrefHeight(150);

        // Style for view mode
        styleReadOnlyField(nameField, true);
        styleReadOnlyField(yearField, true);
        styleReadOnlyField(courseField, true);
        styleReadOnlyField(genderField, true);
        styleReadOnlyField(contactField, true);
        styleReadOnlyField(birthdateField, true);
        historyArea.setEditable(false);
        historyArea.setStyle("-fx-control-inner-background: #F5F5F5;");

        // Add detail rows with text fields
        addEditableDetailRow(grid, 0, "Student ID:", patient.getId(), null);
        addEditableDetailRow(grid, 1, "Full Name:", null, nameField);
        addEditableDetailRow(grid, 2, "Year Level:", null, yearField);
        addEditableDetailRow(grid, 3, "Course/Department:", null, courseField);
        addEditableDetailRow(grid, 4, "Gender:", null, genderField);
        addEditableDetailRow(grid, 5, "Contact Number:", null, contactField);
        addEditableDetailRow(grid, 6, "Birthdate:", null, birthdateField);

        // Medical History label and area
        Label historyLabel = new Label("Medical History:");
        historyLabel.setStyle("-fx-font-weight: bold;");
        grid.add(historyLabel, 0, 7);
        grid.add(historyArea, 0, 8, 2, 1);

        root.getChildren().addAll(titleLabel, grid);

        // Button container
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button editBtn = new Button("Edit");
        Button saveBtn = new Button("Save");
        Button cancelBtn = new Button("Cancel");
        Button closeBtn = new Button("Close");

        editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8px 20px;");
        saveBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8px 20px;");
        cancelBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8px 20px;");
        closeBtn.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8px 20px;");

        // Initially show Edit and Close buttons
        buttonBox.getChildren().addAll(editBtn, closeBtn);

        // Edit button action
        editBtn.setOnAction(e -> {
            enableEditMode(nameField, yearField, courseField, genderField, contactField, birthdateField, historyArea);
            buttonBox.getChildren().clear();
            buttonBox.getChildren().addAll(saveBtn, cancelBtn);
        });

        // Save button action
        saveBtn.setOnAction(e -> {
            boolean success = DatabaseManager.updatePatient(
                    patient.getId(),
                    nameField.getText(),
                    courseField.getText(),
                    yearField.getText(),
                    genderField.getText(),
                    contactField.getText(),
                    birthdateField.getText(),
                    historyArea.getText()
            );

            if (success) {
                refreshTable();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Patient updated successfully!");
                disableEditMode(nameField, yearField, courseField, genderField, contactField, birthdateField, historyArea);
                buttonBox.getChildren().clear();
                buttonBox.getChildren().addAll(editBtn, closeBtn);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update patient!");
            }
        });

        // Cancel button action
        cancelBtn.setOnAction(e -> {
            nameField.setText(patient.getName());
            yearField.setText(patient.getYear());
            courseField.setText(patient.getCourse());
            genderField.setText(patient.getGender());
            contactField.setText(patient.getContact());
            birthdateField.setText(patient.getBirthdate());
            historyArea.setText(patient.getMedicalHistory());
            disableEditMode(nameField, yearField, courseField, genderField, contactField, birthdateField, historyArea);
            buttonBox.getChildren().clear();
            buttonBox.getChildren().addAll(editBtn, closeBtn);
        });

        // Close button action
        closeBtn.setOnAction(e -> stage.close());

        root.getChildren().add(buttonBox);

        Scene scene = new Scene(root, 700, 800);
        stage.setScene(scene);
        stage.show();
    }

    private void enableEditMode(TextField nameField, TextField yearField, TextField courseField,
                                TextField genderField, TextField contactField, TextField birthdateField,
                                TextArea historyArea) {
        nameField.setEditable(true);
        yearField.setEditable(true);
        courseField.setEditable(true);
        genderField.setEditable(true);
        contactField.setEditable(true);
        birthdateField.setEditable(true);
        historyArea.setEditable(true);

        nameField.setStyle("-fx-border-color: #8B0000; -fx-border-width: 1; -fx-padding: 5; -fx-control-inner-background: #FFFFFF;");
        yearField.setStyle("-fx-border-color: #8B0000; -fx-border-width: 1; -fx-padding: 5; -fx-control-inner-background: #FFFFFF;");
        courseField.setStyle("-fx-border-color: #8B0000; -fx-border-width: 1; -fx-padding: 5; -fx-control-inner-background: #FFFFFF;");
        genderField.setStyle("-fx-border-color: #8B0000; -fx-border-width: 1; -fx-padding: 5; -fx-control-inner-background: #FFFFFF;");
        contactField.setStyle("-fx-border-color: #8B0000; -fx-border-width: 1; -fx-padding: 5; -fx-control-inner-background: #FFFFFF;");
        birthdateField.setStyle("-fx-border-color: #8B0000; -fx-border-width: 1; -fx-padding: 5; -fx-control-inner-background: #FFFFFF;");
        historyArea.setStyle("-fx-control-inner-background: #FFFFFF;");
    }

    private void disableEditMode(TextField nameField, TextField yearField, TextField courseField,
                                 TextField genderField, TextField contactField, TextField birthdateField,
                                 TextArea historyArea) {
        nameField.setEditable(false);
        yearField.setEditable(false);
        courseField.setEditable(false);
        genderField.setEditable(false);
        contactField.setEditable(false);
        birthdateField.setEditable(false);
        historyArea.setEditable(false);

        styleReadOnlyField(nameField, true);
        styleReadOnlyField(yearField, true);
        styleReadOnlyField(courseField, true);
        styleReadOnlyField(genderField, true);
        styleReadOnlyField(contactField, true);
        styleReadOnlyField(birthdateField, true);
        historyArea.setStyle("-fx-control-inner-background: #F5F5F5;");
    }

    private void styleReadOnlyField(TextField field, boolean readOnly) {
        if (readOnly) {
            field.setStyle("-fx-text-fill: #555; -fx-control-inner-background: #F5F5F5; -fx-padding: 5;");
        }
    }

    private void addEditableDetailRow(GridPane grid, int row, String label, String staticValue, TextField field) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        if (staticValue != null) {
            // For Student ID - just a label
            Label valueNode = new Label(staticValue);
            valueNode.setStyle("-fx-text-fill: #555;");
            grid.add(labelNode, 0, row);
            grid.add(valueNode, 1, row);
        } else if (field != null) {
            // For editable fields
            field.setPrefWidth(300);
            grid.add(labelNode, 0, row);
            grid.add(field, 1, row);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void editPatient(Patient patient) {
        System.out.println("Editing patient: " + patient.getName());
        showPatientDetails(patient);
    }

    public void refreshTable() {
        patientList.setAll(DatabaseManager.getAllPatients());
        patientTable.setItems(patientList);
    }

    @FXML
    private void openAddPatientForm() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cjcschoolclinicmanagementsystem/View/add-patient-view.fxml"));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(loader.load()));
        stage.showAndWait(); // Wait for user to save
        refreshTable(); // REFRESH THE LIST IMMEDIATELY
    }
}