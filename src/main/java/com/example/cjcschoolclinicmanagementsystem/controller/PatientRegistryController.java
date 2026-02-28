package com.example.cjcschoolclinicmanagementsystem.controller;

import com.example.cjcschoolclinicmanagementsystem.database.DatabaseManager;
import com.example.cjcschoolclinicmanagementsystem.model.Patient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

        refreshTable();
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