package com.example.cjcschoolclinicmanagementsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("View/login-view.fxml"));

        // Use screen bounds for fully responsive sizing
        javafx.geometry.Rectangle2D screenBounds =
                javafx.stage.Screen.getPrimary().getVisualBounds();

        Scene scene = new Scene(fxmlLoader.load(),
                screenBounds.getWidth(), screenBounds.getHeight());

        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setTitle("CJC-Clinic+");
        stage.setScene(scene);

        // Set the app icon
        try {
            var iconStream = HelloApplication.class.getResourceAsStream("Images/cliniclogov3.png");
            if (iconStream != null) {
                Image icon = new Image(iconStream);
                stage.getIcons().add(icon);
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load app icon: " + e.getMessage());
        }

        // Maximize to fill screen on start
        stage.setMaximized(true);
        stage.show();
    }
}