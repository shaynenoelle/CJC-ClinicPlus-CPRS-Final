package com.example.cjcschoolclinicmanagementsystem.controller;

import com.example.cjcschoolclinicmanagementsystem.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

    private int visitId;

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