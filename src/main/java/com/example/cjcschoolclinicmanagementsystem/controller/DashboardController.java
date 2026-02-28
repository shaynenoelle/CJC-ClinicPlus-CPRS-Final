package com.example.cjcschoolclinicmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class DashboardController {

    @FXML private TableView<?> patientTable;
    @FXML private TextField txtSearch;

    @FXML
    private void handleLogout() throws IOException {
        Stage stage = (Stage) patientTable.getScene().getWindow();

        // Return to Login View
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/cjcschoolclinicmanagementsystem/View/login-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);

        // Reset window for login
        stage.setMaximized(false);
        stage.centerOnScreen();
    }
}