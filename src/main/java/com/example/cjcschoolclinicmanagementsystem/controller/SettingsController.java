package com.example.cjcschoolclinicmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.example.cjcschoolclinicmanagementsystem.database.DatabaseManager;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SettingsController {

    // ════════════════════ ACADEMIC SETUP TAB ════════════════════
    @FXML private TextField txtSchoolYear;
    @FXML private Label     lblSchoolYearStatus;
    @FXML private ListView<String> lstDepartments;
    @FXML private TextField txtNewDept;
    @FXML private Label     lblDeptStatus;
    @FXML private ListView<String> lstCourses;
    @FXML private TextField txtNewCourse;
    @FXML private Label     lblCourseStatus;

    // ════════════════════ CLINICAL PRESETS TAB ════════════════════
    @FXML private ListView<String> lstDiagnosisPresets;
    @FXML private TextField txtNewDiagnosis;
    @FXML private Label     lblDiagnosisStatus;
    @FXML private ListView<String> lstComplaintPresets;
    @FXML private TextField txtNewComplaint;
    @FXML private Label     lblComplaintStatus;
    @FXML private ListView<String> lstMedicines;
    @FXML private TextField txtNewMedicine;
    @FXML private Label     lblMedicineStatus;

    // ════════════════════ USER MANAGEMENT TAB ════════════════════
    @FXML private TableView<String> userTable;
    @FXML private TableColumn<String, String> colUsername;
    @FXML private TextField     txtCurrentUsername;
    @FXML private PasswordField txtOldPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label         lblPasswordStrength;

    // ════════════════════ DATA IMPORT TAB ════════════════════
    @FXML private TextField txtImportFilePath;
    @FXML private Label     lblImportStatus;

    // ════════════════════ DATA BACKUP TAB ════════════════════
    @FXML private TextField  txtBackupPath;
    @FXML private TextField  txtAutoBackupPath;
    @FXML private CheckBox   chkAutoBackup;
    @FXML private ComboBox<String>  comboBackupFrequency;
    @FXML private Spinner<Integer>  spinnerHour;
    @FXML private Spinner<Integer>  spinnerMinute;
    @FXML private ComboBox<String>  comboAMPM;
    @FXML private Label lblNextBackup;
    @FXML private Label lblLastBackup;
    @FXML private Label lblLastExport;

    // ════════════════════ APPEARANCE TAB ════════════════════
    @FXML private ComboBox<String> themeComboBox;
    @FXML private Slider fontSizeSlider;
    @FXML private Label  lblFontSize;
    @FXML private CheckBox notificationsCheckBox;

    @FXML private TabPane mainTabPane;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ── Preset category constants ─────────────────────────────────
    private static final String CAT_DIAGNOSIS  = "diagnosis";
    private static final String CAT_COMPLAINT  = "complaint";
    private static final String CAT_MEDICINE   = "medicine";
    private static final String CAT_DEPARTMENT = "department";
    private static final String CAT_COURSE     = "course";

    @FXML
    public void initialize() {
        try {
            initAcademicTab();
            initPresetsTab();
            initUserTab();
            initBackupTab();
            initAppearanceTab();
        } catch (Exception e) {
            System.err.println("SettingsController init error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ════════════════════ ACADEMIC SETUP ════════════════════

    private void initAcademicTab() {
        // School year
        txtSchoolYear.setText(DatabaseManager.getSchoolYear());

        // Departments
        refreshList(lstDepartments, CAT_DEPARTMENT);

        // Courses
        refreshList(lstCourses, CAT_COURSE);
    }

    @FXML
    private void saveSchoolYear() {
        String val = txtSchoolYear.getText().trim();
        if (val.isEmpty()) {
            setStatus(lblSchoolYearStatus, "School year cannot be empty.", false);
            return;
        }
        DatabaseManager.saveSchoolYear(val);
        setStatus(lblSchoolYearStatus, "Saved: " + val, true);
    }

    @FXML private void addDepartment() {
        addPreset(txtNewDept, lstDepartments, CAT_DEPARTMENT, lblDeptStatus);
    }
    @FXML private void removeDepartment() {
        removePreset(lstDepartments, CAT_DEPARTMENT, lblDeptStatus);
    }

    @FXML private void addCourse() {
        addPreset(txtNewCourse, lstCourses, CAT_COURSE, lblCourseStatus);
    }
    @FXML private void removeCourse() {
        removePreset(lstCourses, CAT_COURSE, lblCourseStatus);
    }

    // ════════════════════ CLINICAL PRESETS ════════════════════

    private void initPresetsTab() {
        refreshList(lstDiagnosisPresets, CAT_DIAGNOSIS);
        refreshList(lstComplaintPresets, CAT_COMPLAINT);
        refreshList(lstMedicines,        CAT_MEDICINE);
    }

    @FXML private void addDiagnosisPreset() {
        addPreset(txtNewDiagnosis, lstDiagnosisPresets, CAT_DIAGNOSIS, lblDiagnosisStatus);
    }
    @FXML private void removeDiagnosisPreset() {
        removePreset(lstDiagnosisPresets, CAT_DIAGNOSIS, lblDiagnosisStatus);
    }

    @FXML private void addComplaintPreset() {
        addPreset(txtNewComplaint, lstComplaintPresets, CAT_COMPLAINT, lblComplaintStatus);
    }
    @FXML private void removeComplaintPreset() {
        removePreset(lstComplaintPresets, CAT_COMPLAINT, lblComplaintStatus);
    }

    @FXML private void addMedicine() {
        addPreset(txtNewMedicine, lstMedicines, CAT_MEDICINE, lblMedicineStatus);
    }
    @FXML private void removeMedicine() {
        removePreset(lstMedicines, CAT_MEDICINE, lblMedicineStatus);
    }

    // ── Shared preset helpers ─────────────────────────────────────

    private void addPreset(TextField input, ListView<String> list,
                           String category, Label statusLabel) {
        String val = input.getText().trim();
        if (val.isEmpty()) {
            setStatus(statusLabel, "Please enter a value first.", false);
            return;
        }
        if (DatabaseManager.addPreset(category, val)) {
            input.clear();
            refreshList(list, category);
            setStatus(statusLabel, "Added: " + val, true);
        } else {
            setStatus(statusLabel, "\"" + val + "\" already exists.", false);
        }
    }

    private void removePreset(ListView<String> list, String category, Label statusLabel) {
        String selected = list.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus(statusLabel, "Select an item to remove.", false);
            return;
        }
        if (DatabaseManager.removePreset(category, selected)) {
            refreshList(list, category);
            setStatus(statusLabel, "Removed: " + selected, true);
        } else {
            setStatus(statusLabel, "Could not remove item.", false);
        }
    }

    private void refreshList(ListView<String> list, String category) {
        if (list == null) return;
        List<String> items = DatabaseManager.getPresets(category);
        list.setItems(FXCollections.observableArrayList(items));
    }

    // ════════════════════ USER MANAGEMENT ════════════════════

    private void initUserTab() {
        if (colUsername != null) {
            colUsername.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(d.getValue()));
        }
        refreshUserTable();
    }

    @FXML
    private void handleChangePassword() {
        String username    = txtCurrentUsername.getText().trim();
        String oldPass     = txtOldPassword.getText();
        String newPass     = txtNewPassword.getText();
        String confirmPass = txtConfirmPassword.getText();

        if (username.isEmpty() || oldPass.isEmpty() || newPass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields", "Please fill all password fields.");
            return;
        }
        if (newPass.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Weak Password", "Password must be at least 6 characters.");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            showAlert(Alert.AlertType.WARNING, "Passwords Don't Match", "New password and confirmation do not match.");
            return;
        }
        if (DatabaseManager.changePassword(username, oldPass, newPass)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Password changed successfully!");
            txtCurrentUsername.clear();
            txtOldPassword.clear();
            txtNewPassword.clear();
            txtConfirmPassword.clear();
            if (lblPasswordStrength != null) lblPasswordStrength.setText("");
        } else {
            showAlert(Alert.AlertType.ERROR, "Failed", "Old password is incorrect or user not found.");
        }
    }

    @FXML
    private void handleAddUser() {
        Dialog<java.util.Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Create a new system user");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        TextField   txtU = new TextField();  txtU.setPromptText("Username");
        PasswordField txtP = new PasswordField(); txtP.setPromptText("Password (min 6 chars)");
        grid.add(new Label("Username:"), 0, 0); grid.add(txtU, 1, 0);
        grid.add(new Label("Password:"), 0, 1); grid.add(txtP, 1, 1);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                java.util.Map<String, String> r = new java.util.HashMap<>();
                r.put("username", txtU.getText());
                r.put("password", txtP.getText());
                return r;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(data -> {
            String u = data.get("username").trim();
            String p = data.get("password");
            if (u.isEmpty() || p.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Data", "Username and password are required.");
                return;
            }
            if (p.length() < 6) {
                showAlert(Alert.AlertType.WARNING, "Weak Password", "Password must be at least 6 characters.");
                return;
            }
            if (DatabaseManager.addUser(u, p)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "User '" + u + "' added successfully!");
                refreshUserTable();
            } else {
                showAlert(Alert.AlertType.ERROR, "Failed", "Could not add user. Username may already exist.");
            }
        });
    }

    @FXML
    private void handleResetSelectedPassword() {
        String selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to reset password.");
            return;
        }
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Reset password for: " + selected);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        PasswordField txtNew  = new PasswordField(); txtNew.setPromptText("New password (min 6 chars)");
        PasswordField txtConf = new PasswordField(); txtConf.setPromptText("Confirm password");
        grid.add(new Label("New Password:"), 0, 0); grid.add(txtNew,  1, 0);
        grid.add(new Label("Confirm:"),      0, 1); grid.add(txtConf, 1, 1);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? txtNew.getText() : null);

        dialog.showAndWait().ifPresent(newPass -> {
            if (newPass.isEmpty() || newPass.length() < 6) {
                showAlert(Alert.AlertType.WARNING, "Invalid Password", "Password must be at least 6 characters.");
                return;
            }
            if (!newPass.equals(txtConf.getText())) {
                showAlert(Alert.AlertType.WARNING, "Mismatch", "Passwords do not match.");
                return;
            }
            if (DatabaseManager.resetUserPassword(selected, newPass)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Password for '" + selected + "' reset successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Failed", "Could not reset password.");
            }
        });
    }

    @FXML
    private void handleDeleteUser() {
        String selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete user: " + selected + "?");
        confirm.setContentText("This action cannot be undone.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (DatabaseManager.deleteUser(selected)) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "User '" + selected + "' deleted.");
                    refreshUserTable();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Failed", "Could not delete user.");
                }
            }
        });
    }

    private void refreshUserTable() {
        if (userTable == null) return;
        try {
            List<java.util.Map<String, String>> users = DatabaseManager.getAllUsers();
            ObservableList<String> usernames = FXCollections.observableArrayList();
            for (java.util.Map<String, String> u : users) usernames.add(u.get("username"));
            userTable.setItems(usernames);
        } catch (Exception e) {
            System.err.println("refreshUserTable error: " + e.getMessage());
        }
    }

    // ════════════════════ DATA IMPORT (PLACEHOLDER) ════════════════════

    @FXML
    private void browseImportFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Enrollee File");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Spreadsheet Files", "*.xlsx", "*.csv"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File selected = chooser.showOpenDialog(new Stage());
        if (selected != null) {
            txtImportFilePath.setText(selected.getAbsolutePath());
            setStatus(lblImportStatus, "File selected. Ready to import.", true);
        }
    }

    @FXML
    private void handleImportEnrollees() {
        // ── PLACEHOLDER — import logic will be implemented before final submission ──
        showAlert(Alert.AlertType.INFORMATION,
                "Coming Soon",
                "Enrollee import is currently in development and will be available before the final project submission.\n\n" +
                        "When complete, this will read the registrar's Excel/CSV file and automatically add new patients to the system.");
        setStatus(lblImportStatus, "Feature coming soon.", false);
    }

    // ════════════════════ DATA BACKUP ════════════════════

    private void initBackupTab() {
        if (comboBackupFrequency != null)
            comboBackupFrequency.setItems(FXCollections.observableArrayList("Daily", "Weekly", "Monthly"));

        if (spinnerHour != null)
            spinnerHour.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12, 3));
        if (spinnerMinute != null)
            spinnerMinute.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        if (comboAMPM != null)
            comboAMPM.setItems(FXCollections.observableArrayList("AM", "PM"));

        // Load saved settings
        if (chkAutoBackup != null)
            chkAutoBackup.setSelected(Boolean.parseBoolean(DatabaseManager.getSetting("auto_backup_enabled", "false")));
        if (comboBackupFrequency != null)
            comboBackupFrequency.setValue(DatabaseManager.getSetting("backup_frequency", "Daily"));
        if (spinnerHour != null && spinnerHour.getValueFactory() != null)
            spinnerHour.getValueFactory().setValue(Integer.parseInt(DatabaseManager.getSetting("backup_hour", "3")));
        if (spinnerMinute != null && spinnerMinute.getValueFactory() != null)
            spinnerMinute.getValueFactory().setValue(Integer.parseInt(DatabaseManager.getSetting("backup_minute", "0")));
        if (comboAMPM != null)
            comboAMPM.setValue(DatabaseManager.getSetting("backup_ampm", "PM"));
        if (lblLastBackup != null)
            lblLastBackup.setText("Last backup: " + DatabaseManager.getSetting("last_backup_time", "Never"));
        if (lblLastExport != null)
            lblLastExport.setText("Last export: " + DatabaseManager.getSetting("last_export_time", "Never"));

        updateNextBackupLabel();
    }

    private void updateNextBackupLabel() {
        if (lblNextBackup == null) return;
        if (chkAutoBackup != null && chkAutoBackup.isSelected()) {
            String freq  = comboBackupFrequency != null ? comboBackupFrequency.getValue() : "Daily";
            int    hour  = spinnerHour   != null && spinnerHour.getValue()   != null ? spinnerHour.getValue()   : 3;
            int    min   = spinnerMinute != null && spinnerMinute.getValue() != null ? spinnerMinute.getValue() : 0;
            String ampm  = comboAMPM     != null ? comboAMPM.getValue() : "PM";
            lblNextBackup.setText("Next backup: Every " + (freq != null ? freq.toLowerCase() : "daily")
                    + " at " + String.format("%d:%02d %s", hour, min, ampm));
        } else {
            lblNextBackup.setText("Next backup: -");
        }
    }

    @FXML private void browseBackupFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Backup Destination");
        File selected = chooser.showDialog(new Stage());
        if (selected != null) txtBackupPath.setText(selected.getAbsolutePath());
    }

    @FXML private void browseAutoBackupFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Auto Backup Folder");
        File selected = chooser.showDialog(new Stage());
        if (selected != null) txtAutoBackupPath.setText(selected.getAbsolutePath());
    }

    @FXML private void handleBackupNow() {
        String path = txtBackupPath.getText().trim();
        if (path.isEmpty()) { showAlert(Alert.AlertType.WARNING, "No Destination", "Please specify a backup folder."); return; }
        File dest = new File(path);
        if (!dest.exists()) { showAlert(Alert.AlertType.WARNING, "Invalid Path", "Destination folder does not exist."); return; }

        String backupFile = path + File.separator + "cjclinic_backup_" + System.currentTimeMillis() + ".db";
        if (DatabaseManager.backupDatabase(backupFile)) {
            String now = LocalDateTime.now().format(FORMATTER);
            DatabaseManager.saveSetting("last_backup_time", now);
            if (lblLastBackup != null) lblLastBackup.setText("Last backup: " + now);
            showAlert(Alert.AlertType.INFORMATION, "Backup Successful", "Database backed up to:\n" + backupFile);
        } else {
            showAlert(Alert.AlertType.ERROR, "Backup Failed", "An error occurred during backup.");
        }
    }

    @FXML private void handleExportPatients() {
        String path = txtBackupPath.getText().trim();
        if (path.isEmpty()) { showAlert(Alert.AlertType.WARNING, "No Destination", "Please specify an export folder."); return; }
        String file = path + File.separator + "patients_export_" + System.currentTimeMillis() + ".csv";
        if (DatabaseManager.exportPatientsToCSV(file)) {
            String now = LocalDateTime.now().format(FORMATTER);
            DatabaseManager.saveSetting("last_export_time", now);
            if (lblLastExport != null) lblLastExport.setText("Last export: " + now);
            showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Patient data exported to:\n" + file);
        } else {
            showAlert(Alert.AlertType.ERROR, "Export Failed", "An error occurred during export.");
        }
    }

    @FXML private void handleExportVisits() {
        String path = txtBackupPath.getText().trim();
        if (path.isEmpty()) { showAlert(Alert.AlertType.WARNING, "No Destination", "Please specify an export folder."); return; }
        String file = path + File.separator + "visits_export_" + System.currentTimeMillis() + ".csv";
        if (DatabaseManager.exportVisitsToCSV(file)) {
            String now = LocalDateTime.now().format(FORMATTER);
            DatabaseManager.saveSetting("last_export_time", now);
            if (lblLastExport != null) lblLastExport.setText("Last export: " + now);
            showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Visit data exported to:\n" + file);
        } else {
            showAlert(Alert.AlertType.ERROR, "Export Failed", "An error occurred during export.");
        }
    }

    @FXML private void saveBackupSettings() {
        String autoPath = txtAutoBackupPath.getText().trim();
        if (chkAutoBackup.isSelected() && autoPath.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Folder", "Please specify an auto-backup folder.");
            return;
        }
        DatabaseManager.saveSetting("auto_backup_enabled", String.valueOf(chkAutoBackup.isSelected()));
        DatabaseManager.saveSetting("backup_frequency",    comboBackupFrequency.getValue());
        DatabaseManager.saveSetting("backup_hour",         String.valueOf(spinnerHour.getValue()));
        DatabaseManager.saveSetting("backup_minute",       String.valueOf(spinnerMinute.getValue()));
        DatabaseManager.saveSetting("backup_ampm",         comboAMPM.getValue());
        DatabaseManager.saveSetting("auto_backup_path",    autoPath);
        updateNextBackupLabel();
        showAlert(Alert.AlertType.INFORMATION, "Saved", "Backup settings saved successfully!");
    }

    // ════════════════════ APPEARANCE ════════════════════

    private void initAppearanceTab() {
        if (themeComboBox != null) {
            themeComboBox.setItems(FXCollections.observableArrayList("Light", "Dark", "System"));
            themeComboBox.setValue(DatabaseManager.getSetting("theme", "Light"));
        }
        if (fontSizeSlider != null) {
            fontSizeSlider.setValue(Double.parseDouble(DatabaseManager.getSetting("font_size", "14")));
            fontSizeSlider.valueProperty().addListener((obs, o, n) -> {
                if (lblFontSize != null) lblFontSize.setText(String.format("%.0f pt", n.doubleValue()));
            });
        }
        if (notificationsCheckBox != null)
            notificationsCheckBox.setSelected(Boolean.parseBoolean(DatabaseManager.getSetting("notifications_enabled", "true")));
    }

    @FXML private void saveSettings() {
        if (themeComboBox    != null) DatabaseManager.saveSetting("theme",                  themeComboBox.getValue());
        if (fontSizeSlider   != null) DatabaseManager.saveSetting("font_size",              String.format("%.0f", fontSizeSlider.getValue()));
        if (notificationsCheckBox != null) DatabaseManager.saveSetting("notifications_enabled", String.valueOf(notificationsCheckBox.isSelected()));
        showAlert(Alert.AlertType.INFORMATION, "Saved", "Display settings saved successfully!");
    }

    // ════════════════════ UTILITIES ════════════════════

    private void setStatus(Label lbl, String msg, boolean success) {
        if (lbl == null) return;
        lbl.setText(msg);
        lbl.setStyle(success
                ? "-fx-text-fill: #2e7d32; -fx-font-size: 11; -fx-font-style: italic;"
                : "-fx-text-fill: #c62828; -fx-font-size: 11; -fx-font-style: italic;");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}