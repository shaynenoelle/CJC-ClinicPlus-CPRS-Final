package com.example.cjcschoolclinicmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import com.example.cjcschoolclinicmanagementsystem.database.DatabaseManager;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SettingsController {

    // ════════════════════ DATA BACKUP TAB ════════════════════
    @FXML private TextField txtBackupPath;
    @FXML private TextField txtAutoBackupPath;
    @FXML private CheckBox chkAutoBackup;
    @FXML private ComboBox<String> comboBackupFrequency;
    @FXML private Spinner<Integer> spinnerHour;
    @FXML private Spinner<Integer> spinnerMinute;
    @FXML private ComboBox<String> comboAMPM;
    @FXML private Label lblNextBackup;
    @FXML private Label lblLastBackup;
    @FXML private Label lblLastExport;

    // ════════════════════ ACCOUNTS TAB ════════════════════
    @FXML private TextField txtCurrentUsername;
    @FXML private PasswordField txtOldPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label lblPasswordStrength;
    @FXML private TableView<String> userTable;
    @FXML private TableColumn<String, String> colUsername;

    // ════════════════════ APPEARANCE TAB ════════════════════
    @FXML private ComboBox<String> themeComboBox;
    @FXML private Slider fontSizeSlider;
    @FXML private Label lblFontSize;
    @FXML private CheckBox notificationsCheckBox;
    @FXML private TabPane mainTabPane;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        try {
            // Initialize Backup Tab
            initializeBackupTab();

            // Initialize Accounts Tab
            initializeAccountsTab();

            // Initialize Appearance Tab
            initializeAppearanceTab();

            // Load all settings
            loadSettings();
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR in SettingsController initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeBackupTab() {
        try {
            // Backup frequency options
            if (comboBackupFrequency != null) {
                comboBackupFrequency.setItems(FXCollections.observableArrayList(
                    "Daily", "Weekly", "Monthly"
                ));
                comboBackupFrequency.setValue("Daily");
            }

            // Hour spinner (1-12)
            if (spinnerHour != null) {
                SpinnerValueFactory<Integer> hourFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12, 3);
                spinnerHour.setValueFactory(hourFactory);
            }

            // Minute spinner (0-59)
            if (spinnerMinute != null) {
                SpinnerValueFactory<Integer> minuteFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
                spinnerMinute.setValueFactory(minuteFactory);
            }

            // AM/PM combo
            if (comboAMPM != null) {
                comboAMPM.setItems(FXCollections.observableArrayList("AM", "PM"));
                comboAMPM.setValue("PM");
            }

            // Load backup settings from database
            String autoBackupEnabled = DatabaseManager.getSetting("auto_backup_enabled", "false");
            if (chkAutoBackup != null) {
                chkAutoBackup.setSelected(Boolean.parseBoolean(autoBackupEnabled));
            }

            String backupFreq = DatabaseManager.getSetting("backup_frequency", "Daily");
            if (comboBackupFrequency != null) {
                comboBackupFrequency.setValue(backupFreq);
            }

            String backupHour = DatabaseManager.getSetting("backup_hour", "3");
            if (spinnerHour != null && spinnerHour.getValueFactory() != null) {
                spinnerHour.getValueFactory().setValue(Integer.parseInt(backupHour));
            }

            String backupMin = DatabaseManager.getSetting("backup_minute", "0");
            if (spinnerMinute != null && spinnerMinute.getValueFactory() != null) {
                spinnerMinute.getValueFactory().setValue(Integer.parseInt(backupMin));
            }

            String backupAMPM = DatabaseManager.getSetting("backup_ampm", "PM");
            if (comboAMPM != null) {
                comboAMPM.setValue(backupAMPM);
            }

            String lastBackupTime = DatabaseManager.getSetting("last_backup_time", "Never");
            if (lblLastBackup != null) {
                lblLastBackup.setText("Last backup: " + lastBackupTime);
            }

            String lastExportTime = DatabaseManager.getSetting("last_export_time", "Never");
            if (lblLastExport != null) {
                lblLastExport.setText("Last export: " + lastExportTime);
            }

            updateNextBackupLabel();
        } catch (Exception e) {
            System.err.println("Error initializing backup tab: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeAccountsTab() {
        try {
            // Set up username column
            if (colUsername != null) {
                colUsername.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue()));
            }

            // Load users into table
            refreshUserTable();
        } catch (Exception e) {
            System.err.println("Error initializing accounts tab: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeAppearanceTab() {
        try {
            // Theme options
            if (themeComboBox != null) {
                themeComboBox.setItems(FXCollections.observableArrayList("Light", "Dark", "System"));
                themeComboBox.setValue("Light");
            }

            // Font size slider listener
            if (fontSizeSlider != null) {
                fontSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (lblFontSize != null) {
                        lblFontSize.setText(String.format("%.0f pt", newVal.doubleValue()));
                    }
                });
            }

            // Load appearance settings
            String theme = DatabaseManager.getSetting("theme", "Light");
            if (themeComboBox != null) {
                themeComboBox.setValue(theme);
            }

            String fontSize = DatabaseManager.getSetting("font_size", "14");
            if (fontSizeSlider != null) {
                fontSizeSlider.setValue(Double.parseDouble(fontSize));
            }

            String notificationsEnabled = DatabaseManager.getSetting("notifications_enabled", "true");
            if (notificationsCheckBox != null) {
                notificationsCheckBox.setSelected(Boolean.parseBoolean(notificationsEnabled));
            }
        } catch (Exception e) {
            System.err.println("Error initializing appearance tab: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSettings() {
        // All settings are loaded in their respective initialize methods
    }

    private void updateNextBackupLabel() {
        try {
            if (chkAutoBackup != null && chkAutoBackup.isSelected()) {
                String freq = comboBackupFrequency != null ? comboBackupFrequency.getValue() : "Daily";
                int hour = spinnerHour != null && spinnerHour.getValue() != null ? spinnerHour.getValue() : 3;
                int min = spinnerMinute != null && spinnerMinute.getValue() != null ? spinnerMinute.getValue() : 0;
                String ampm = comboAMPM != null ? comboAMPM.getValue() : "PM";

                String timeStr = String.format("%d:%02d %s", hour, min, ampm);
                String freqStr = freq != null ? freq.toLowerCase() : "daily";

                if (lblNextBackup != null) {
                    lblNextBackup.setText("Next backup: Every " + freqStr + " at " + timeStr);
                }
            } else {
                if (lblNextBackup != null) {
                    lblNextBackup.setText("Next backup: —");
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating next backup label: " + e.getMessage());
        }
    }

    // ════════════════════ DATA BACKUP HANDLERS ════════════════════

    @FXML
    private void browseBackupFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Backup Destination");
        File selected = chooser.showDialog(new Stage());
        if (selected != null) {
            txtBackupPath.setText(selected.getAbsolutePath());
        }
    }

    @FXML
    private void browseAutoBackupFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Auto Backup Folder");
        File selected = chooser.showDialog(new Stage());
        if (selected != null) {
            txtAutoBackupPath.setText(selected.getAbsolutePath());
        }
    }

    @FXML
    private void handleBackupNow() {
        String path = txtBackupPath.getText().trim();
        if (path.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Destination",
                      "Please specify a backup destination folder.");
            return;
        }

        File dest = new File(path);
        if (!dest.exists()) {
            showAlert(Alert.AlertType.WARNING, "Invalid Path",
                      "Destination folder does not exist.");
            return;
        }

        String backupFile = path + File.separator + "cjclinic_backup_" +
                           System.currentTimeMillis() + ".db";

        if (DatabaseManager.backupDatabase(backupFile)) {
            String now = LocalDateTime.now().format(formatter);
            DatabaseManager.saveSetting("last_backup_time", now);
            lblLastBackup.setText("Last backup: " + now);
            showAlert(Alert.AlertType.INFORMATION, "Backup Successful",
                      "Database backed up to:\n" + backupFile);
        } else {
            showAlert(Alert.AlertType.ERROR, "Backup Failed",
                      "An error occurred during backup.");
        }
    }

    @FXML
    private void handleExportPatients() {
        String path = txtBackupPath.getText().trim();
        if (path.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Destination",
                      "Please specify an export destination folder.");
            return;
        }

        File dest = new File(path);
        if (!dest.exists()) {
            showAlert(Alert.AlertType.WARNING, "Invalid Path",
                      "Destination folder does not exist.");
            return;
        }

        String exportFile = path + File.separator + "patients_export_" +
                           System.currentTimeMillis() + ".csv";

        if (DatabaseManager.exportPatientsToCSV(exportFile)) {
            String now = LocalDateTime.now().format(formatter);
            DatabaseManager.saveSetting("last_export_time", now);
            lblLastExport.setText("Last export: " + now);
            showAlert(Alert.AlertType.INFORMATION, "Export Successful",
                      "Patient data exported to:\n" + exportFile);
        } else {
            showAlert(Alert.AlertType.ERROR, "Export Failed",
                      "An error occurred during export.");
        }
    }

    @FXML
    private void handleExportVisits() {
        String path = txtBackupPath.getText().trim();
        if (path.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Destination",
                      "Please specify an export destination folder.");
            return;
        }

        File dest = new File(path);
        if (!dest.exists()) {
            showAlert(Alert.AlertType.WARNING, "Invalid Path",
                      "Destination folder does not exist.");
            return;
        }

        String exportFile = path + File.separator + "visits_export_" +
                           System.currentTimeMillis() + ".csv";

        if (DatabaseManager.exportVisitsToCSV(exportFile)) {
            String now = LocalDateTime.now().format(formatter);
            DatabaseManager.saveSetting("last_export_time", now);
            lblLastExport.setText("Last export: " + now);
            showAlert(Alert.AlertType.INFORMATION, "Export Successful",
                      "Visit data exported to:\n" + exportFile);
        } else {
            showAlert(Alert.AlertType.ERROR, "Export Failed",
                      "An error occurred during export.");
        }
    }

    @FXML
    private void saveBackupSettings() {
        String autoBackupPath = txtAutoBackupPath.getText().trim();
        if (chkAutoBackup.isSelected() && autoBackupPath.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Folder",
                      "Please specify an auto-backup folder.");
            return;
        }

        DatabaseManager.saveSetting("auto_backup_enabled", String.valueOf(chkAutoBackup.isSelected()));
        DatabaseManager.saveSetting("backup_frequency", comboBackupFrequency.getValue());
        DatabaseManager.saveSetting("backup_hour", String.valueOf(spinnerHour.getValue()));
        DatabaseManager.saveSetting("backup_minute", String.valueOf(spinnerMinute.getValue()));
        DatabaseManager.saveSetting("backup_ampm", comboAMPM.getValue());
        DatabaseManager.saveSetting("auto_backup_path", autoBackupPath);

        updateNextBackupLabel();
        showAlert(Alert.AlertType.INFORMATION, "Saved",
                  "Backup settings saved successfully!");
    }

    // ════════════════════ ACCOUNT HANDLERS ════════════════════

    @FXML
    private void handleChangePassword() {
        String username = txtCurrentUsername.getText().trim();
        String oldPass = txtOldPassword.getText();
        String newPass = txtNewPassword.getText();
        String confirmPass = txtConfirmPassword.getText();

        if (username.isEmpty() || oldPass.isEmpty() || newPass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields",
                      "Please fill all password fields.");
            return;
        }

        if (newPass.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Weak Password",
                      "Password must be at least 6 characters.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            showAlert(Alert.AlertType.WARNING, "Passwords Don't Match",
                      "New password and confirmation do not match.");
            return;
        }

        if (DatabaseManager.changePassword(username, oldPass, newPass)) {
            showAlert(Alert.AlertType.INFORMATION, "Success",
                      "Password changed successfully!");
            txtCurrentUsername.clear();
            txtOldPassword.clear();
            txtNewPassword.clear();
            txtConfirmPassword.clear();
            lblPasswordStrength.setText("");
        } else {
            showAlert(Alert.AlertType.ERROR, "Failed",
                      "Old password is incorrect or user not found.");
        }
    }

    @FXML
    private void handleAddUser() {
        // Create dialog for new user
        Dialog<java.util.Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Create a new system user");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField txtUsername = new TextField();
        txtUsername.setPromptText("Username");
        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Password (min 6 chars)");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(txtUsername, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(txtPassword, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                java.util.Map<String, String> result = new java.util.HashMap<>();
                result.put("username", txtUsername.getText());
                result.put("password", txtPassword.getText());
                return result;
            }
            return null;
        });

        java.util.Optional<java.util.Map<String, String>> result = dialog.showAndWait();
        result.ifPresent(data -> {
            String username = data.get("username").trim();
            String password = data.get("password");

            if (username.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Data",
                          "Username and password are required.");
                return;
            }

            if (password.length() < 6) {
                showAlert(Alert.AlertType.WARNING, "Weak Password",
                          "Password must be at least 6 characters.");
                return;
            }

            if (DatabaseManager.addUser(username, password)) {
                showAlert(Alert.AlertType.INFORMATION, "Success",
                          "User '" + username + "' added successfully!");
                refreshUserTable();
            } else {
                showAlert(Alert.AlertType.ERROR, "Failed",
                          "Could not add user. Username may already exist.");
            }
        });
    }

    @FXML
    private void handleResetSelectedPassword() {
        String selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                      "Please select a user to reset password.");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Reset password for user: " + selected);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        PasswordField txtNewPassword = new PasswordField();
        txtNewPassword.setPromptText("New password (min 6 chars)");
        PasswordField txtConfirm = new PasswordField();
        txtConfirm.setPromptText("Confirm password");

        grid.add(new Label("New Password:"), 0, 0);
        grid.add(txtNewPassword, 1, 0);
        grid.add(new Label("Confirm:"), 0, 1);
        grid.add(txtConfirm, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return txtNewPassword.getText();
            }
            return null;
        });

        java.util.Optional<String> result = dialog.showAndWait();
        result.ifPresent(newPass -> {
            if (newPass.isEmpty() || newPass.length() < 6) {
                showAlert(Alert.AlertType.WARNING, "Invalid Password",
                          "Password must be at least 6 characters.");
                return;
            }

            if (!newPass.equals(txtConfirm.getText())) {
                showAlert(Alert.AlertType.WARNING, "Mismatch",
                          "Passwords do not match.");
                return;
            }

            if (DatabaseManager.resetUserPassword(selected, newPass)) {
                showAlert(Alert.AlertType.INFORMATION, "Success",
                          "Password for '" + selected + "' reset successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Failed",
                          "Could not reset password.");
            }
        });
    }

    @FXML
    private void handleDeleteUser() {
        String selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                      "Please select a user to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete user: " + selected + "?");
        confirm.setContentText("This action cannot be undone.");

        java.util.Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (DatabaseManager.deleteUser(selected)) {
                showAlert(Alert.AlertType.INFORMATION, "Success",
                          "User '" + selected + "' deleted successfully!");
                refreshUserTable();
            } else {
                showAlert(Alert.AlertType.ERROR, "Failed",
                          "Could not delete user.");
            }
        }
    }

    private void refreshUserTable() {
        try {
            if (userTable != null) {
                java.util.List<java.util.Map<String, String>> users = DatabaseManager.getAllUsers();
                ObservableList<String> usernames = FXCollections.observableArrayList();
                for (java.util.Map<String, String> user : users) {
                    usernames.add(user.get("username"));
                }
                userTable.setItems(usernames);
            }
        } catch (Exception e) {
            System.err.println("Error refreshing user table: " + e.getMessage());
        }
    }

    // ════════════════════ APPEARANCE HANDLERS ════════════════════

    @FXML
    private void saveSettings() {
        try {
            if (themeComboBox != null && themeComboBox.getValue() != null) {
                DatabaseManager.saveSetting("theme", themeComboBox.getValue());
            }
            if (fontSizeSlider != null) {
                DatabaseManager.saveSetting("font_size", String.format("%.0f", fontSizeSlider.getValue()));
            }
            if (notificationsCheckBox != null) {
                DatabaseManager.saveSetting("notifications_enabled", String.valueOf(notificationsCheckBox.isSelected()));
            }

            showAlert(Alert.AlertType.INFORMATION, "Success",
                      "Display settings saved successfully!");
        } catch (Exception e) {
            System.err.println("Error saving settings: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save settings: " + e.getMessage());
        }
    }

    // ════════════════════ UTILITY METHODS ════════════════════

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
