package com.example.cjcschoolclinicmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.example.cjcschoolclinicmanagementsystem.database.DatabaseManager;
import java.io.IOException;
import java.time.LocalDateTime;

public class LoginController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblStatus;

    @FXML
    protected void onLoginClick() {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        if (DatabaseManager.validateLogin(user, pass)) {
            System.out.println("[AUDIT LOG] Access Granted: " + user + " at " + LocalDateTime.now());
            try {
                switchToDashboard();
            } catch (IOException e) {
                lblStatus.setText("System Error: Dashboard View missing.");
            }
        } else {
            lblStatus.setText("Access Denied: Invalid Credentials.");
        }
    }

    private void switchToDashboard() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cjcschoolclinicmanagementsystem/View/dashboard-view.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) lblStatus.getScene().getWindow();
        Scene scene = new Scene(root);

        stage.setScene(scene);

        // Force Maximize to fill the Dell Latitude screen
        stage.setMaximized(true);
        stage.show();
    }
}