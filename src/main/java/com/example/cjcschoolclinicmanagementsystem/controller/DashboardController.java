package com.example.cjcschoolclinicmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
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
        try {
            // Load the login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cjcschoolclinicmanagementsystem/View/login-view.fxml"));
            Parent root = loader.load();

            // Get current stage
            Stage stage = (Stage) mainBorderPane.getScene().getWindow();

            // Set the login scene
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Reset window to normal size (not maximized)
            stage.setMaximized(false);
            stage.setWidth(1024);
            stage.setHeight(700);
            stage.centerOnScreen();

            stage.show();

            System.out.println("[AUDIT LOG] User logged out successfully");
        } catch (IOException e) {
            showError("Logout Error", "Could not return to login screen.");
            System.err.println("Logout Error: " + e.getMessage());
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