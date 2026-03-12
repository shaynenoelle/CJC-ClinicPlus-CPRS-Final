package com.example.cjcschoolclinicmanagementsystem.controller;

import com.example.cjcschoolclinicmanagementsystem.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Map;

public class AttendController {

    @FXML private Label lblPatientName;
    @FXML private Label lblDate;
    @FXML private TextField txtTemperature;
    @FXML private TextField txtBpSystolic;
    @FXML private TextField txtBpDiastolic;
    @FXML private TextField txtHeartRate;
    @FXML private TextField txtOxygen;
    @FXML private Label lblTempWarning;
    @FXML private Label lblBpWarning;
    @FXML private Label lblHrWarning;
    @FXML private Label lblO2Warning;
    @FXML private TextArea txtComplaint;
    @FXML private TextArea txtDiagnosis;
    @FXML private TextArea txtTreatment;
    @FXML private TextArea txtRemarks;
    @FXML private Label lblSaveStatus;
    @FXML private TextField txtTimeIn;
    @FXML private TextField txtTimeOut;

    // ── Tab navigation controls ───────────────────────────────────
    @FXML private TabPane tabPane;
    @FXML private Button  btnBack, btnNext, btnSave;
    @FXML private Label   lblStep;

    private int visitId;

    @FXML
    public void initialize() {
        // Wire tab-change listener
        tabPane.getSelectionModel().selectedIndexProperty().addListener(
                (obs, oldIdx, newIdx) -> updateNavButtons(newIdx.intValue()));
        updateNavButtons(0);
    }

    // ── Tab navigation ────────────────────────────────────────────
    @FXML
    public void onTabChanged() {
        updateNavButtons(tabPane.getSelectionModel().getSelectedIndex());
    }

    private void updateNavButtons(int tabIndex) {
        int lastTab = tabPane.getTabs().size() - 1;
        boolean isFirst = (tabIndex == 0);
        boolean isLast  = (tabIndex == lastTab);

        btnBack.setVisible(!isFirst);
        btnBack.setManaged(!isFirst);
        btnNext.setVisible(!isLast);
        btnNext.setManaged(!isLast);
        btnSave.setVisible(isLast);
        btnSave.setManaged(isLast);

        lblStep.setText("Step " + (tabIndex + 1) + " of " + (lastTab + 1));
        lblSaveStatus.setText("");
    }

    @FXML
    public void onNext() {
        // Validate vitals before leaving Tab 1
        String tempStr = txtTemperature.getText().trim();
        if (tempStr.isEmpty()) {
            lblSaveStatus.setText("⚠ Temperature is required before proceeding.");
            lblSaveStatus.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (txtBpSystolic.getText().trim().isEmpty() || txtBpDiastolic.getText().trim().isEmpty()) {
            lblSaveStatus.setText("⚠ Blood Pressure is required before proceeding.");
            lblSaveStatus.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        lblSaveStatus.setText("");
        int current = tabPane.getSelectionModel().getSelectedIndex();
        tabPane.getSelectionModel().select(current + 1);
    }

    @FXML
    public void onBack() {
        int current = tabPane.getSelectionModel().getSelectedIndex();
        if (current > 0) tabPane.getSelectionModel().select(current - 1);
    }

    public void setVisitData(Map<String, String> visitRow) {
        visitId = Integer.parseInt(visitRow.get("visit_id"));
        lblPatientName.setText(visitRow.get("full_name"));
        lblDate.setText(visitRow.get("visit_date"));

        // Pre-fill if already attended
        if (visitRow.get("time_in") != null &&
                !visitRow.get("time_in").isEmpty())
            txtTimeIn.setText(visitRow.get("time_in"));
        if (visitRow.get("time_out") != null &&
                !visitRow.get("time_out").isEmpty())
            txtTimeOut.setText(visitRow.get("time_out"));
        String temp = visitRow.get("temperature");
        if (temp != null && !temp.isEmpty())
            txtTemperature.setText(temp);

        String bp = visitRow.get("blood_pressure");
        if (bp != null && !bp.isEmpty() && bp.contains("/")) {
            String[] parts = bp.split("/");
            if (parts.length == 2) {
                txtBpSystolic.setText(parts[0].trim());
                txtBpDiastolic.setText(parts[1].trim());
            }
        }

        String hr = visitRow.get("heart_rate");
        if (hr != null && !hr.isEmpty())
            txtHeartRate.setText(hr);

        String o2 = visitRow.get("oxygen_sat");
        if (o2 != null && !o2.isEmpty())
            txtOxygen.setText(o2);
        if (visitRow.get("chief_complaint") != null)
            txtComplaint.setText(visitRow.get("chief_complaint"));
        if (visitRow.get("diagnosis") != null)
            txtDiagnosis.setText(visitRow.get("diagnosis"));
        if (visitRow.get("treatment") != null)
            txtTreatment.setText(visitRow.get("treatment"));
        if (visitRow.get("remarks") != null)
            txtRemarks.setText(visitRow.get("remarks"));
    }

    @FXML
    private void validateVitals() {
        // Temperature: normal 36.1–37.2
        try {
            double temp = Double.parseDouble(txtTemperature.getText().trim());
            lblTempWarning.setText(
                    (temp < 36.1 || temp > 37.2) ? "⚠ Abnormal" : "✔ Normal");
            lblTempWarning.setStyle(
                    (temp < 36.1 || temp > 37.2)
                            ? "-fx-text-fill: #e74c3c; -fx-font-size: 11;"
                            : "-fx-text-fill: #27ae60; -fx-font-size: 11;");
        } catch (NumberFormatException e) {
            lblTempWarning.setText("");
        }

        // Blood Pressure: normal <120/<80
        try {
            int sys = Integer.parseInt(txtBpSystolic.getText().trim());
            int dia = Integer.parseInt(txtBpDiastolic.getText().trim());
            boolean bpOk = sys < 120 && dia < 80;
            lblBpWarning.setText(bpOk ? "✔ Normal" : "⚠ Abnormal");
            lblBpWarning.setStyle(bpOk
                    ? "-fx-text-fill: #27ae60; -fx-font-size: 11;"
                    : "-fx-text-fill: #e74c3c; -fx-font-size: 11;");
        } catch (NumberFormatException e) {
            lblBpWarning.setText("");
        }

        // Heart Rate: normal 60–100
        try {
            int hr = Integer.parseInt(txtHeartRate.getText().trim());
            boolean hrOk = hr >= 60 && hr <= 100;
            lblHrWarning.setText(hrOk ? "✔ Normal" : "⚠ Abnormal");
            lblHrWarning.setStyle(hrOk
                    ? "-fx-text-fill: #27ae60; -fx-font-size: 11;"
                    : "-fx-text-fill: #e74c3c; -fx-font-size: 11;");
        } catch (NumberFormatException e) {
            lblHrWarning.setText("");
        }

        // O2 Sat: normal ≥ 95
        try {
            double o2 = Double.parseDouble(txtOxygen.getText().trim());
            boolean o2Ok = o2 >= 95;
            lblO2Warning.setText(o2Ok ? "✔ Normal" : "⚠ Abnormal");
            lblO2Warning.setStyle(o2Ok
                    ? "-fx-text-fill: #27ae60; -fx-font-size: 11;"
                    : "-fx-text-fill: #e74c3c; -fx-font-size: 11;");
        } catch (NumberFormatException e) {
            lblO2Warning.setText("");
        }
    }

    @FXML
    private void onSaveRecord() {

        // Update time in if changed
        if (txtTimeIn.getText() != null && !txtTimeIn.getText().trim().isEmpty()) {
            DatabaseManager.updateTimeIn(visitId, txtTimeIn.getText().trim());
        }

        if (txtComplaint.getText().trim().isEmpty()) {
            lblSaveStatus.setText("⚠ Chief Complaint is required.");
            lblSaveStatus.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        // Validate temperature
        String tempStr = txtTemperature.getText().trim();
        if (tempStr.isEmpty()) {
            lblSaveStatus.setText("⚠ Temperature is required.");
            lblSaveStatus.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        try { Double.parseDouble(tempStr); }
        catch (NumberFormatException e) {
            lblSaveStatus.setText("⚠ Temperature must be a valid number (e.g. 36.5).");
            lblSaveStatus.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        // Validate blood pressure
        String bpSys = txtBpSystolic.getText().trim();
        String bpDia = txtBpDiastolic.getText().trim();
        if (bpSys.isEmpty() || bpDia.isEmpty()) {
            lblSaveStatus.setText("⚠ Blood Pressure (systolic / diastolic) is required.");
            lblSaveStatus.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        try { Integer.parseInt(bpSys); Integer.parseInt(bpDia); }
        catch (NumberFormatException e) {
            lblSaveStatus.setText("⚠ Blood Pressure values must be whole numbers (e.g. 120 / 80).");
            lblSaveStatus.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        String bp = txtBpSystolic.getText().trim() + "/" + txtBpDiastolic.getText().trim();

        boolean success = DatabaseManager.saveVisitRecord(
                visitId,
                txtTemperature.getText().trim(),
                bp,
                txtHeartRate.getText().trim(),
                txtOxygen.getText().trim(),
                txtComplaint.getText().trim(),
                txtDiagnosis.getText().trim(),
                txtTreatment.getText().trim(),
                txtRemarks.getText().trim()
        );

        if (success) {
            // Only save time out if nurse actually filled it in
            String timeOut = txtTimeOut.getText() != null ?
                    txtTimeOut.getText().trim() : "";

            if (!timeOut.isEmpty()) {
                DatabaseManager.checkOutPatient(visitId, timeOut);
            }

            DatabaseManager.checkOutPatient(visitId, timeOut);

            lblSaveStatus.setText("✔ Record saved!");
            lblSaveStatus.setStyle("-fx-text-fill: #27ae60;");

            // Close after 1 second
            new Thread(() -> {
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(() ->
                        ((Stage) lblSaveStatus.getScene().getWindow()).close());
            }).start();
        } else {
            lblSaveStatus.setText("Save failed. Try again.");
            lblSaveStatus.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    // ── Tab/Enter navigation for Vital Signs ─────────────────────
    @FXML
    private void onTempKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.TAB && !e.isShiftDown()) {
            e.consume();
            txtBpSystolic.requestFocus();
        }
    }

    // ── Tab/Enter navigation for Visit Details TextAreas ─────────
    // Tab moves focus to next field; Enter on Remarks triggers Save
    @FXML
    private void onComplaintKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.TAB && !e.isShiftDown()) {
            e.consume();
            txtDiagnosis.requestFocus();
        }
    }

    @FXML
    private void onDiagnosisKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.TAB && !e.isShiftDown()) {
            e.consume();
            txtTreatment.requestFocus();
        } else if (e.getCode() == KeyCode.TAB && e.isShiftDown()) {
            e.consume();
            txtComplaint.requestFocus();
        }
    }

    @FXML
    private void onTreatmentKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.TAB && !e.isShiftDown()) {
            e.consume();
            txtRemarks.requestFocus();
        } else if (e.getCode() == KeyCode.TAB && e.isShiftDown()) {
            e.consume();
            txtDiagnosis.requestFocus();
        }
    }

    @FXML
    private void onRemarksKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER && e.isControlDown()) {
            // Ctrl+Enter triggers Save from anywhere
            onSaveRecord();
        } else if (e.getCode() == KeyCode.TAB && e.isShiftDown()) {
            e.consume();
            txtTreatment.requestFocus();
        }
    }

    @FXML
    private void onCancel() {
        ((Stage) lblSaveStatus.getScene().getWindow()).close();
    }

    @FXML
    private void setTimeIn() {
        String now = java.time.LocalTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        txtTimeIn.setText(now);
    }

    @FXML
    private void setTimeOut() {
        String now = java.time.LocalTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        txtTimeOut.setText(now);
    }

    public void setReadOnly(boolean readOnly) {
        txtTemperature.setEditable(!readOnly);
        txtBpSystolic.setEditable(!readOnly);
        txtBpDiastolic.setEditable(!readOnly);
        txtHeartRate.setEditable(!readOnly);
        txtOxygen.setEditable(!readOnly);
        txtComplaint.setEditable(!readOnly);
        txtDiagnosis.setEditable(!readOnly);
        txtTreatment.setEditable(!readOnly);
        txtRemarks.setEditable(!readOnly);
        txtTimeIn.setEditable(!readOnly);
        txtTimeOut.setEditable(!readOnly);
    }
}