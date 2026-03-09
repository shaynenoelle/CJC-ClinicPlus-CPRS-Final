package com.example.cjcschoolclinicmanagementsystem.controller;

import com.example.cjcschoolclinicmanagementsystem.database.DatabaseManager;
import com.example.cjcschoolclinicmanagementsystem.model.Patient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class PatientRegistryController {

    @FXML private TableView<Patient> patientTable;
    @FXML private TableColumn<Patient, String> colID, colName, colType, colDept;
    @FXML private TextField txtSearch;

    final private ObservableList<Patient> patientList = FXCollections.observableArrayList();
    private FilteredList<Patient> filteredList;

    public void initialize() {
        // Link UI columns to the Patient model properties
        colID.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        colName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        colType.setCellValueFactory(cellData -> cellData.getValue().yearProperty());
        colDept.setCellValueFactory(cellData -> cellData.getValue().courseProperty());

        // Set up filtered list
        filteredList = new FilteredList<>(patientList, p -> true);
        patientTable.setItems(filteredList);

        // Live search as user types
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilter(newVal));
        }

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

    @FXML
    private void handleSearch() {
        applyFilter(txtSearch != null ? txtSearch.getText() : "");
    }

    private void applyFilter(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            filteredList.setPredicate(p -> true);
        } else {
            String lower = keyword.toLowerCase().trim();
            filteredList.setPredicate(p ->
                p.getName().toLowerCase().contains(lower) ||
                p.getId().toLowerCase().contains(lower)
            );
        }
    }

    private void showPatientDetails(Patient patient) {
        System.out.println("Viewing details for: " + patient.getName());

        Stage stage = new Stage();
        stage.setTitle("Patient Details - " + patient.getName());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setMaximized(true);

        VBox root = new VBox(0);
        root.setPadding(new Insets(0));
        root.setStyle("-fx-background-color: #F9F9F9;");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-control-inner-background: #F9F9F9; -fx-padding: 0;");
        scrollPane.setFitToWidth(true);
        VBox contentVBox = new VBox(15);
        contentVBox.setPadding(new Insets(20));
        contentVBox.setStyle("-fx-control-inner-background: #F9F9F9;");

        // Create field references for all sections so we can enable/disable editing
        TextField idField = new TextField(patient.getId());
        TextField nameField = new TextField(patient.getName());
        TextField ageField = new TextField(patient.getAge());
        TextField genderField = new TextField(patient.getGender());
        TextField birthdateField = new TextField(patient.getBirthdate());
        TextField courseField = new TextField(patient.getCourse());
        TextField yearField = new TextField(patient.getYear());
        TextField collegeField = new TextField(patient.getCollege());
        TextField contactField = new TextField(patient.getContact());
        TextField emailField = new TextField(patient.getEmail());

        TextField emergencyNameField = new TextField(patient.getEmergencyName());
        TextField relationshipField = new TextField(patient.getRelationship());
        TextField emergencyContactField = new TextField(patient.getEmergencyContact());

        TextField allergiesField = new TextField(patient.getAllergies());
        TextField conditionsField = new TextField(patient.getConditions());
        TextField bloodTypeField = new TextField(patient.getBloodType());
        TextField heightField = new TextField(patient.getHeight());
        TextField weightField = new TextField(patient.getWeight());

        TextArea medicalRemarksArea = new TextArea(patient.getMedicalRemarks());
        medicalRemarksArea.setWrapText(true);
        medicalRemarksArea.setPrefHeight(100);
        medicalRemarksArea.setEditable(false);
        medicalRemarksArea.setStyle("-fx-control-inner-background: #F5F5F5; -fx-text-fill: #555;");

        // PERSONAL INFORMATION SECTION
        VBox personalSection = createSection("PERSONAL INFORMATION", "#8B0000");
        VBox personalContent = new VBox(8);
        personalContent.setPadding(new Insets(15));

        personalContent.getChildren().addAll(
            createFieldRow("Student ID:", idField),
            createFieldRow("Full Name:", nameField),
            createFieldRow("Age:", ageField),
            createFieldRow("Gender:", genderField),
            createFieldRow("Birthdate:", birthdateField),
            createFieldRow("Course/Department:", courseField),
            createFieldRow("Year Level:", yearField),
            createFieldRow("College/Department:", collegeField),
            createFieldRow("Contact #:", contactField),
            createFieldRow("Email:", emailField)
        );
        personalSection.getChildren().add(personalContent);

        // EMERGENCY CONTACT SECTION
        VBox emergencySection = createSection("EMERGENCY CONTACT", "#8B0000");
        VBox emergencyContent = new VBox(8);
        emergencyContent.setPadding(new Insets(15));

        emergencyContent.getChildren().addAll(
            createFieldRow("Name:", emergencyNameField),
            createFieldRow("Relationship:", relationshipField),
            createFieldRow("Contact #:", emergencyContactField)
        );
        emergencySection.getChildren().add(emergencyContent);

        // MEDICAL HISTORY SECTION
        VBox medicalSection = createSection("MEDICAL HISTORY", "#8B0000");
        VBox medicalContent = new VBox(8);
        medicalContent.setPadding(new Insets(15));

        medicalContent.getChildren().addAll(
            createFieldRow("Allergies:", allergiesField),
            createFieldRow("Conditions:", conditionsField),
            createFieldRow("Blood Type:", bloodTypeField),
            createFieldRow("Height (cm):", heightField),
            createFieldRow("Weight (kg):", weightField),
            createFieldRowWithArea("Medical Remarks:", medicalRemarksArea)
        );
        medicalSection.getChildren().add(medicalContent);

        contentVBox.getChildren().addAll(personalSection, emergencySection, medicalSection);
        scrollPane.setContent(contentVBox);

        // Button container - reduced padding
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 20, 10, 20));
        buttonBox.setStyle("-fx-background-color: #F9F9F9; -fx-border-color: #E0E0E0; -fx-border-width: 1 0 0 0;");

        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");
        Button closeBtn = new Button("Close");

        editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8px 25px; -fx-font-size: 12; -fx-font-weight: bold;");
        closeBtn.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8px 25px; -fx-font-size: 12; -fx-font-weight: bold;");

        buttonBox.getChildren().addAll(editBtn, closeBtn);

        // Edit button action
        editBtn.setOnAction(e -> {
            enableEditMode(idField, nameField, ageField, genderField, birthdateField, courseField,
                          yearField, collegeField, contactField, emailField, emergencyNameField,
                          relationshipField, emergencyContactField, allergiesField, conditionsField,
                          bloodTypeField, heightField, weightField, medicalRemarksArea);

            buttonBox.getChildren().clear();
            Button saveBtn = new Button("Save");
            Button cancelBtn = new Button("Cancel");
            saveBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8px 25px; -fx-font-size: 12; -fx-font-weight: bold;");
            cancelBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8px 25px; -fx-font-size: 12; -fx-font-weight: bold;");

            saveBtn.setOnAction(save -> {
                boolean success = DatabaseManager.updatePatient(
                        patient.getId(),
                        nameField.getText(),
                        ageField.getText(),
                        courseField.getText(),
                        yearField.getText(),
                        genderField.getText(),
                        collegeField.getText(),
                        contactField.getText(),
                        emailField.getText(),
                        birthdateField.getText(),
                        patient.getType(),
                        emergencyNameField.getText(),
                        relationshipField.getText(),
                        emergencyContactField.getText(),
                        allergiesField.getText(),
                        conditionsField.getText(),
                        bloodTypeField.getText(),
                        heightField.getText(),
                        weightField.getText(),
                        medicalRemarksArea.getText()
                );

                if (success) {
                    refreshTable();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Patient updated successfully!");
                    disableEditMode(idField, nameField, ageField, genderField, birthdateField, courseField,
                                   yearField, collegeField, contactField, emailField, emergencyNameField,
                                   relationshipField, emergencyContactField, allergiesField, conditionsField,
                                   bloodTypeField, heightField, weightField, medicalRemarksArea);
                    buttonBox.getChildren().clear();
                    buttonBox.getChildren().addAll(editBtn, closeBtn);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update patient!");
                }
            });

            cancelBtn.setOnAction(cancel -> {
                nameField.setText(patient.getName());
                ageField.setText(patient.getAge());
                genderField.setText(patient.getGender());
                birthdateField.setText(patient.getBirthdate());
                courseField.setText(patient.getCourse());
                yearField.setText(patient.getYear());
                collegeField.setText(patient.getCollege());
                contactField.setText(patient.getContact());
                emailField.setText(patient.getEmail());
                emergencyNameField.setText(patient.getEmergencyName());
                relationshipField.setText(patient.getRelationship());
                emergencyContactField.setText(patient.getEmergencyContact());
                allergiesField.setText(patient.getAllergies());
                conditionsField.setText(patient.getConditions());
                bloodTypeField.setText(patient.getBloodType());
                heightField.setText(patient.getHeight());
                weightField.setText(patient.getWeight());
                medicalRemarksArea.setText(patient.getMedicalRemarks());

                disableEditMode(idField, nameField, ageField, genderField, birthdateField, courseField,
                               yearField, collegeField, contactField, emailField, emergencyNameField,
                               relationshipField, emergencyContactField, allergiesField, conditionsField,
                               bloodTypeField, heightField, weightField, medicalRemarksArea);
                buttonBox.getChildren().clear();
                buttonBox.getChildren().addAll(editBtn, closeBtn);
            });

            buttonBox.getChildren().addAll(saveBtn, cancelBtn);
        });

        closeBtn.setOnAction(e -> stage.close());

        root.getChildren().addAll(scrollPane, buttonBox);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private void enableEditMode(TextField... fields) {
        for (TextField field : fields) {
            field.setEditable(true);
            field.setStyle("-fx-border-color: #8B0000; -fx-border-width: 2; -fx-padding: 8; -fx-control-inner-background: #FFFFFF;");
        }
    }

    private void enableEditMode(Object... fields) {
        for (Object field : fields) {
            if (field instanceof TextField) {
                ((TextField) field).setEditable(true);
                ((TextField) field).setStyle("-fx-border-color: #8B0000; -fx-border-width: 2; -fx-padding: 8; -fx-control-inner-background: #FFFFFF;");
            } else if (field instanceof TextArea) {
                ((TextArea) field).setEditable(true);
                ((TextArea) field).setStyle("-fx-border-color: #8B0000; -fx-border-width: 2; -fx-padding: 8; -fx-control-inner-background: #FFFFFF;");
            }
        }
    }

    private void disableEditMode(Object... fields) {
        for (Object field : fields) {
            if (field instanceof TextField) {
                ((TextField) field).setEditable(false);
                ((TextField) field).setStyle("-fx-text-fill: #555; -fx-control-inner-background: #F5F5F5; -fx-padding: 8;");
            } else if (field instanceof TextArea) {
                ((TextArea) field).setEditable(false);
                ((TextArea) field).setStyle("-fx-control-inner-background: #F5F5F5; -fx-text-fill: #555;");
            }
        }
    }

    private HBox createFieldRow(String label, TextField field) {
        HBox row = new HBox(10);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: bold; -fx-text-fill: #333; -fx-min-width: 150;");

        field.setEditable(false);
        field.setStyle("-fx-text-fill: #555; -fx-control-inner-background: #F5F5F5; -fx-padding: 8;");
        field.setPrefWidth(600);

        row.getChildren().addAll(labelNode, field);
        return row;
    }

    private HBox createFieldRowWithArea(String label, TextArea area) {
        HBox row = new HBox(10);
        row.setAlignment(javafx.geometry.Pos.TOP_LEFT);

        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: bold; -fx-text-fill: #333; -fx-min-width: 150;");

        area.setPrefWidth(600);

        row.getChildren().addAll(labelNode, area);
        return row;
    }

    private VBox createSection(String title, String titleColor) {
        VBox section = new VBox(10);
        section.setStyle("-fx-border-color: #CCCCCC; -fx-border-radius: 5; -fx-border-width: 1; -fx-padding: 10;");
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + titleColor + ";");
        Separator separator = new Separator();
        section.getChildren().addAll(titleLabel, separator);
        return section;
    }

    private void setFieldProperties(TextField field, boolean editable) {
        field.setEditable(editable);
        field.setPrefWidth(300);
        if (!editable) {
            field.setStyle("-fx-text-fill: #555; -fx-control-inner-background: #F5F5F5; -fx-padding: 5;");
        }
    }

    private void addDetailRow(GridPane grid, int row, String label, TextField field) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        grid.add(labelNode, 0, row);
        grid.add(field, 1, row);
    }

    private void enableEditAllFields(Object... fields) {
        for (Object field : fields) {
            if (field instanceof TextField) {
                ((TextField) field).setEditable(true);
                ((TextField) field).setStyle("-fx-border-color: #8B0000; -fx-border-width: 1; -fx-padding: 5; -fx-control-inner-background: #FFFFFF;");
            } else if (field instanceof TextArea) {
                ((TextArea) field).setEditable(true);
                ((TextArea) field).setStyle("-fx-border-color: #8B0000; -fx-border-width: 1; -fx-padding: 5; -fx-control-inner-background: #FFFFFF;");
            }
        }
    }

    private void disableEditAllFields(Object... fields) {
        for (Object field : fields) {
            if (field instanceof TextField) {
                ((TextField) field).setEditable(false);
                setFieldProperties((TextField) field, false);
            } else if (field instanceof TextArea) {
                ((TextArea) field).setEditable(false);
                ((TextArea) field).setStyle("-fx-control-inner-background: #F5F5F5; -fx-text-fill: #555;");
            }
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
        if (filteredList == null) {
            patientTable.setItems(patientList);
        }
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





