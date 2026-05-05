package com.example.cjcschoolclinicmanagementsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;

public class HelloApplication extends Application {
    public static void main(String[] args) {
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("View/login-view.fxml"));

        // Login opens as a clean fixed-size centered window — NOT maximized.
        // This ensures the dashboard's setMaximized(true) always fires correctly
        // because it will be a genuine normal → maximized state change.
        Scene scene = new Scene(fxmlLoader.load(), 960, 600);

        stage.setResizable(false);  // login doesn't need resizing
        stage.setTitle("CJC School Clinic - Login");
        stage.setScene(scene);

        // Center the login window on whatever screen the user has
        javafx.geometry.Rectangle2D screenBounds =
                javafx.stage.Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth()  - 960) / 2);
        stage.setY((screenBounds.getHeight() - 600) / 2);

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

        stage.show();
    }
}