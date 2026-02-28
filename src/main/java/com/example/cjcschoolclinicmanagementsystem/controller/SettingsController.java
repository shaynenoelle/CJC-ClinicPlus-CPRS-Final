package com.example.cjcschoolclinicmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class SettingsController {

    @FXML private ComboBox<String> themeComboBox;
    @FXML private Slider fontSizeSlider;
    @FXML private CheckBox notificationsCheckBox;

    @FXML
    public void initialize() {
        // Initialize theme options
        themeComboBox.getItems().addAll("Light", "Dark");
        themeComboBox.setValue("Light");

        // Set default values
        fontSizeSlider.setValue(14);
        notificationsCheckBox.setSelected(true);
    }

    @FXML
    private void saveSettings() {
        String theme = themeComboBox.getValue();
        double fontSize = fontSizeSlider.getValue();
        boolean notifications = notificationsCheckBox.isSelected();

        // TODO: Save to preferences or database
        System.out.println("Theme: " + theme);
        System.out.println("Font Size: " + fontSize);
        System.out.println("Notifications: " + notifications);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Settings saved successfully!");
        alert.showAndWait();

        closeSettings();
    }

    @FXML
    private void closeSettings() {
        Stage stage = (Stage) themeComboBox.getScene().getWindow();
        stage.close();
    }
}
