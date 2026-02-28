package com.example.cjcschoolclinicmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import java.io.IOException;
import java.net.URL;

public class DashboardController {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private void showDashboard() {
        mainBorderPane.setCenter(new Label("Welcome to GoldenCare"));
    }

    @FXML
    private void showPatientRegistry() {
        // Updated to pass the specific file name
        System.out.println("Patient Registry button clicked!");
        loadView("patient-registry-view.fxml");
    }

    @FXML
    private void handleLogout() {
        System.exit(0);
    }

    @FXML
    private void openSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cjcschoolclinicmanagementsystem/View/settings-view.fxml"));
            Parent root = loader.load();

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Settings");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            showError("Error", "Failed to open settings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlFile) {
        try {
            // Ensure the folder "View" matches your resources folder casing exactly
            String path = "/com/example/cjcschoolclinicmanagementsystem/View/" + fxmlFile;
            URL resource = getClass().getResource(path);

            if (resource == null) {
                showError("Resource Not Found", "Could not find: " + path);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();
            mainBorderPane.setCenter(view);

        } catch (IOException e) {
            showError("Loading Error", "Could not load " + fxmlFile);
            // Replaced printStackTrace with a simple console log for better practice
            System.err.println("FXML Load Log: " + e.getMessage());
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}