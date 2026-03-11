package com.example.cjcschoolclinicmanagementsystem.controller;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.example.cjcschoolclinicmanagementsystem.database.DatabaseManager;
import java.io.IOException;
import java.time.LocalDateTime;

public class LoginController {
    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label lblStatus;
    @FXML
    private Label taglineLabel;
    @FXML private TextField txtPasswordVisible;
    @FXML private CheckBox chkRemember;
    @FXML private Label btnTogglePassword;
    private boolean passwordVisible = false;


    @FXML
    public void initialize() {
        // Animate the tagline with sequential phrases
        if (taglineLabel != null) {
            animateTagline();
        }
    }

    private void animateTagline() {
        String[] phrases = {
                "Golden Heart.",
                "Healing Hands.",
                "In the Service for CJC Community."
        };

        // Create an infinite loop of animations
        createSequentialAnimation(phrases);
    }
    // add credits to log in page

    private void tagwithCredits()
    {
        String credits = "CJC School Clinic Management System\n" +
                "Developed by: Bangcailan, Shayne Noelle, Ngojo, Prince Joshua" +
                "\n"  +
                "Version: 1.0\n" +
                "© 2024 CJC School Clinic. All rights reserved.";
        taglineLabel.setText(credits);
    } // end of method
    private void createSequentialAnimation(String[] phrases) {
        SequentialTransition sequence = new SequentialTransition();

        final int[] currentIndex = {0}; // Use array to allow modification in lambda

        for (int i = 0; i < phrases.length; i++) {
            final int index = i;

            // Fade in
            FadeTransition fadeIn = new FadeTransition(Duration.millis(600), taglineLabel);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            // Pause before fade out (text stays visible)
            PauseTransition hold = new PauseTransition(Duration.millis(1500));

            // Fade out
            FadeTransition fadeOut = new FadeTransition(Duration.millis(600), taglineLabel);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            // Update text when fade out completes (before next phrase)
            fadeOut.setOnFinished(e -> {
                currentIndex[0] = (currentIndex[0] + 1) % phrases.length;
                taglineLabel.setText(phrases[currentIndex[0]]);
            });

            sequence.getChildren().addAll(fadeIn, hold, fadeOut);
        }

        // Set to loop indefinitely
        sequence.setCycleCount(javafx.animation.Animation.INDEFINITE);

        // Set initial text
        taglineLabel.setText(phrases[0]);

        sequence.play();
    }

    @FXML
    protected void onLoginClick() {
        String user = txtUsername.getText();
        String pass = passwordVisible ?
                txtPasswordVisible.getText() : txtPassword.getText();

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
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/com/example/cjcschoolclinicmanagementsystem/View/dashboard-view.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) lblStatus.getScene().getWindow();

        // Get full screen bounds so the scene matches before maximizing
        javafx.geometry.Rectangle2D screenBounds =
                javafx.stage.Screen.getPrimary().getVisualBounds();

        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

        // Must set maximized = false first to force a state change, then true
        stage.setMaximized(false);
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);

        // Use Platform.runLater to guarantee maximize happens after scene is attached
        javafx.application.Platform.runLater(() -> {
            stage.setMaximized(true);
        });

        stage.show();
    }

    @FXML
    protected void onTogglePassword() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            txtPasswordVisible.setText(txtPassword.getText());
            txtPasswordVisible.setVisible(true);
            txtPasswordVisible.setManaged(true);
            txtPassword.setVisible(false);
            txtPassword.setManaged(false);
            // RED = password is visible
            btnTogglePassword.setStyle("-fx-cursor: hand; -fx-font-size: 16; -fx-text-fill: #c41e3a;");
        } else {
            txtPassword.setText(txtPasswordVisible.getText());
            txtPassword.setVisible(true);
            txtPassword.setManaged(true);
            txtPasswordVisible.setVisible(false);
            txtPasswordVisible.setManaged(false);
            // GRAY = password is hidden
            btnTogglePassword.setStyle("-fx-cursor: hand; -fx-font-size: 16; -fx-text-fill: #aaaaaa;");
        }
    }
}