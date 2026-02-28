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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class PatientRegistryController {

    @FXML private TableView<Patient> patientTable;
    @FXML private TableColumn<Patient, String> colID, colName, colType, colDept;
    @FXML private TableColumn<Patient, Void> colAction;

    private ObservableList<Patient> patientList = FXCollections.observableArrayList();

    public void initialize() {
        // Link UI columns to the Patient model properties
        colID.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        colName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        colType.setCellValueFactory(cellData -> cellData.getValue().yearProperty());
        colDept.setCellValueFactory(cellData -> cellData.getValue().courseProperty());

        // Add action buttons to the Action column
        addActionButtons();

        refreshTable();
    }

    private void addActionButtons() {
        colAction.setCellFactory(col -> new TableCell<Patient, Void>() {
            private final Button viewMoreBtn = new Button("View More");
            private final Button editBtn = new Button("Edit");
            private final HBox container = new HBox(8);

            {
                viewMoreBtn.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 4px 12px; -fx-font-size: 12;");
                editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 4px 12px; -fx-font-size: 12;");

                viewMoreBtn.setOnAction(e -> showPatientDetails(getTableView().getItems().get(getIndex())));
                editBtn.setOnAction(e -> editPatient(getTableView().getItems().get(getIndex())));

                container.getChildren().addAll(viewMoreBtn, editBtn);
                container.setStyle("-fx-alignment: CENTER;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
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

        addDetailRow(grid, 0, "Student ID:", patient.getId());
        addDetailRow(grid, 1, "Full Name:", patient.getName());
        addDetailRow(grid, 2, "Year Level:", patient.getYear());
        addDetailRow(grid, 3, "Course/Department:", patient.getCourse());
        addDetailRow(grid, 4, "Gender:", patient.getGender());
        addDetailRow(grid, 5, "Contact Number:", patient.getContact());
        addDetailRow(grid, 6, "Birthdate:", patient.getBirthdate());

        // Medical History (larger text area)
        Label historyLabel = new Label("Medical History:");
        historyLabel.setStyle("-fx-font-weight: bold;");
        TextArea historyArea = new TextArea(patient.getMedicalHistory());
        historyArea.setWrapText(true);
        historyArea.setPrefHeight(150);
        historyArea.setEditable(false);
        historyArea.setStyle("-fx-control-inner-background: #F5F5F5;");

        root.getChildren().addAll(titleLabel, grid, historyLabel, historyArea);

        // Close button
        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8px 20px;");
        closeBtn.setOnAction(e -> stage.close());

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(closeBtn);

        root.getChildren().add(buttonBox);

        Scene scene = new Scene(root, 600, 700);
        stage.setScene(scene);
        stage.show();
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        Label valueNode = new Label(value != null ? value : "N/A");
        valueNode.setStyle("-fx-text-fill: #555;");
        valueNode.setWrapText(true);

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private void editPatient(Patient patient) {
        System.out.println("Editing patient: " + patient.getName());
        // TODO: Implement edit functionality later
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Edit Patient");
        alert.setHeaderText("Edit Patient");
        alert.setContentText("Edit functionality for " + patient.getName() + " coming soon!");
        alert.showAndWait();
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