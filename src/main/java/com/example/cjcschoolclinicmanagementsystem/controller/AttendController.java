package com.example.cjcschoolclinicmanagementsystem.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.example.cjcschoolclinicmanagementsystem.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
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

    // Complaint: editable ComboBox + optional TextArea for "Others"
    @FXML private ComboBox<String> comboComplaint;
    @FXML private TextArea         txtComplaint;

    // Diagnosis: editable ComboBox + optional TextArea for "Others"
    @FXML private ComboBox<String> comboDiagnosis;
    @FXML private TextArea         txtDiagnosis;

    @FXML private TextArea  txtTreatment;
    @FXML private TextArea  txtRemarks;
    @FXML private Label     lblSaveStatus;
    @FXML private TextField txtTimeIn;
    @FXML private TextField txtTimeOut;

    // ── Previous visits table ─────────────────────────────────────
    @FXML private TableView<Map<String, String>> tblPreviousVisits;
    @FXML private TableColumn<Map<String, String>, String> colPrevDate;
    @FXML private TableColumn<Map<String, String>, String> colPrevComplaint;
    @FXML private TableColumn<Map<String, String>, String> colPrevDiagnosis;
    @FXML private TableColumn<Map<String, String>, String> colPrevTreatment;
    @FXML private TableColumn<Map<String, String>, String> colPrevRemarks;
    @FXML private TableColumn<Map<String, String>, String> colPrevVitals;

    // ── Tab navigation ────────────────────────────────────────────
    @FXML private TabPane tabPane;
    @FXML private Button  btnBack, btnNext, btnSave;
    @FXML private Label   lblStep;

    private static final String OTHERS = "Others (specify below)";

    private int    visitId;
    private String currentPatientId = "";

    @FXML
    public void initialize() {
        tabPane.getSelectionModel().selectedIndexProperty().addListener(
                (obs, oldIdx, newIdx) -> updateNavButtons(newIdx.intValue()));
        updateNavButtons(0);

        setupPresetCombos();

        if (tblPreviousVisits != null) {
            colPrevDate.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getOrDefault("visit_date", "")));
            colPrevComplaint.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getOrDefault("chief_complaint", "—")));
            colPrevDiagnosis.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getOrDefault("diagnosis", "—")));
            colPrevTreatment.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getOrDefault("treatment", "—")));
            colPrevRemarks.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getOrDefault("remarks", "—")));
            colPrevVitals.setCellValueFactory(d ->
                    new SimpleStringProperty(buildVitalsString(d.getValue())));
        }
    }

    // ── Preset combo setup ────────────────────────────────────────
    private void setupPresetCombos() {
        // Chief Complaint
        List<String> complaints = new ArrayList<>(DatabaseManager.getPresets("complaint"));
        complaints.add(OTHERS);
        comboComplaint.setItems(FXCollections.observableArrayList(complaints));
        comboComplaint.setPromptText("Select preset or type complaint…");
        comboComplaint.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isOther = OTHERS.equals(newVal);
            if (txtComplaint != null) {
                txtComplaint.setVisible(isOther);
                txtComplaint.setManaged(isOther);
            }
        });

        // Diagnosis
        List<String> diagnoses = new ArrayList<>(DatabaseManager.getPresets("diagnosis"));
        diagnoses.add(OTHERS);
        comboDiagnosis.setItems(FXCollections.observableArrayList(diagnoses));
        comboDiagnosis.setPromptText("Select preset or type diagnosis…");
        comboDiagnosis.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isOther = OTHERS.equals(newVal);
            if (txtDiagnosis != null) {
                txtDiagnosis.setVisible(isOther);
                txtDiagnosis.setManaged(isOther);
            }
        });
    }

    // ── Resolve final value from a combo + textarea pair ─────────
    // Priority: if "Others" is selected → use the textarea text.
    // Otherwise use getValue() first, then getEditor() text as fallback.
    private String resolveValue(ComboBox<String> combo, TextArea area) {
        String val = combo.getValue();
        if (OTHERS.equals(val)) {
            return area != null ? area.getText().trim() : "";
        }
        if (val != null && !val.isBlank()) return val.trim();
        // Editable combo — user may have typed without selecting a list item
        if (combo.getEditor() != null) {
            String editorText = combo.getEditor().getText();
            if (editorText != null && !editorText.isBlank()) return editorText.trim();
        }
        return "";
    }

    // ── Pre-fill a combo from a saved database value ──────────────
    // If the saved value is in the preset list, select it.
    // If not, it means the nurse typed it freely — set it via setValue
    // so getValue() returns it later in resolveValue().
    private void prefillCombo(ComboBox<String> combo, String savedValue) {
        if (savedValue == null || savedValue.isBlank()) return;
        // setValue works on editable ComboBox for both preset and free-typed values.
        // The listener only reacts to OTHERS — any other value just displays safely.
        combo.setValue(savedValue);
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
        btnBack.setVisible(!isFirst);  btnBack.setManaged(!isFirst);
        btnNext.setVisible(!isLast);   btnNext.setManaged(!isLast);
        btnSave.setVisible(isLast);    btnSave.setManaged(isLast);
        lblStep.setText("Step " + (tabIndex + 1) + " of " + (lastTab + 1));
        lblSaveStatus.setText("");
    }

    @FXML
    public void onNext() {
        if (txtTemperature.getText().trim().isEmpty()) {
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
        tabPane.getSelectionModel().select(tabPane.getSelectionModel().getSelectedIndex() + 1);
    }

    @FXML
    public void onBack() {
        int current = tabPane.getSelectionModel().getSelectedIndex();
        if (current > 0) tabPane.getSelectionModel().select(current - 1);
    }

    public void setVisitData(Map<String, String> visitRow) {
        visitId = Integer.parseInt(visitRow.get("visit_id"));
        currentPatientId = visitRow.getOrDefault("patient_id", "");
        lblPatientName.setText(visitRow.get("full_name"));
        lblDate.setText(visitRow.get("visit_date"));

        String timeIn = visitRow.get("time_in");
        if (timeIn != null && !timeIn.isEmpty()) txtTimeIn.setText(timeIn);
        String timeOut = visitRow.get("time_out");
        if (timeOut != null && !timeOut.isEmpty()) txtTimeOut.setText(timeOut);

        String temp = visitRow.get("temperature");
        if (temp != null && !temp.isEmpty()) txtTemperature.setText(temp);

        String bp = visitRow.get("blood_pressure");
        if (bp != null && !bp.isEmpty() && bp.contains("/")) {
            String[] parts = bp.split("/");
            if (parts.length == 2) {
                txtBpSystolic.setText(parts[0].trim());
                txtBpDiastolic.setText(parts[1].trim());
            }
        }

        String hr = visitRow.get("heart_rate");
        if (hr != null && !hr.isEmpty()) txtHeartRate.setText(hr);

        String o2 = visitRow.get("oxygen_sat");
        if (o2 != null && !o2.isEmpty()) txtOxygen.setText(o2);

        // Pre-fill complaint and diagnosis combos with saved values
        prefillCombo(comboComplaint, visitRow.getOrDefault("chief_complaint", ""));
        prefillCombo(comboDiagnosis, visitRow.getOrDefault("diagnosis", ""));

        if (visitRow.get("treatment") != null) txtTreatment.setText(visitRow.get("treatment"));
        if (visitRow.get("remarks")   != null) txtRemarks.setText(visitRow.get("remarks"));

        loadRecentVisits(currentPatientId, visitRow.get("visit_date"));
    }

    @FXML
    private void validateVitals() {
        try {
            double temp = Double.parseDouble(txtTemperature.getText().trim());
            lblTempWarning.setText((temp < 36.1 || temp > 37.2) ? "⚠ Abnormal" : "✔ Normal");
            lblTempWarning.setStyle((temp < 36.1 || temp > 37.2)
                    ? "-fx-text-fill: #e74c3c; -fx-font-size: 11;"
                    : "-fx-text-fill: #27ae60; -fx-font-size: 11;");
        } catch (NumberFormatException e) { lblTempWarning.setText(""); }

        try {
            int sys = Integer.parseInt(txtBpSystolic.getText().trim());
            int dia = Integer.parseInt(txtBpDiastolic.getText().trim());
            boolean ok = sys < 120 && dia < 80;
            lblBpWarning.setText(ok ? "✔ Normal" : "⚠ Abnormal");
            lblBpWarning.setStyle(ok ? "-fx-text-fill: #27ae60; -fx-font-size: 11;"
                    : "-fx-text-fill: #e74c3c; -fx-font-size: 11;");
        } catch (NumberFormatException e) { lblBpWarning.setText(""); }

        try {
            int hr = Integer.parseInt(txtHeartRate.getText().trim());
            boolean ok = hr >= 60 && hr <= 100;
            lblHrWarning.setText(ok ? "✔ Normal" : "⚠ Abnormal");
            lblHrWarning.setStyle(ok ? "-fx-text-fill: #27ae60; -fx-font-size: 11;"
                    : "-fx-text-fill: #e74c3c; -fx-font-size: 11;");
        } catch (NumberFormatException e) { lblHrWarning.setText(""); }

        try {
            double o2 = Double.parseDouble(txtOxygen.getText().trim());
            boolean ok = o2 >= 95;
            lblO2Warning.setText(ok ? "✔ Normal" : "⚠ Abnormal");
            lblO2Warning.setStyle(ok ? "-fx-text-fill: #27ae60; -fx-font-size: 11;"
                    : "-fx-text-fill: #e74c3c; -fx-font-size: 11;");
        } catch (NumberFormatException e) { lblO2Warning.setText(""); }
    }

    @FXML
    private void onSaveRecord() {
        if (txtTimeIn.getText() != null && !txtTimeIn.getText().trim().isEmpty()) {
            DatabaseManager.updateTimeIn(visitId, txtTimeIn.getText().trim());
        }

        String complaint = resolveValue(comboComplaint, txtComplaint);
        String diagnosis = resolveValue(comboDiagnosis, txtDiagnosis);

        if (complaint.isEmpty()) {
            lblSaveStatus.setText("⚠ Chief Complaint is required.");
            lblSaveStatus.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

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

        String bpSys = txtBpSystolic.getText().trim();
        String bpDia = txtBpDiastolic.getText().trim();
        if (bpSys.isEmpty() || bpDia.isEmpty()) {
            lblSaveStatus.setText("⚠ Blood Pressure (systolic / diastolic) is required.");
            lblSaveStatus.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        try { Integer.parseInt(bpSys); Integer.parseInt(bpDia); }
        catch (NumberFormatException e) {
            lblSaveStatus.setText("⚠ Blood Pressure values must be whole numbers.");
            lblSaveStatus.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        boolean success = DatabaseManager.saveVisitRecord(
                visitId, tempStr, bpSys + "/" + bpDia,
                txtHeartRate.getText().trim(), txtOxygen.getText().trim(),
                complaint, diagnosis,
                txtTreatment.getText().trim(), txtRemarks.getText().trim());

        if (success) {
            String timeOut = txtTimeOut.getText() != null ? txtTimeOut.getText().trim() : "";
            if (!timeOut.isEmpty()) DatabaseManager.checkOutPatient(visitId, timeOut);

            lblSaveStatus.setText("✔ Record saved!");
            lblSaveStatus.setStyle("-fx-text-fill: #27ae60;");
            new Thread(() -> {
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                Platform.runLater(() ->
                        ((Stage) lblSaveStatus.getScene().getWindow()).close());
            }).start();
        } else {
            lblSaveStatus.setText("Save failed. Try again.");
            lblSaveStatus.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    // ── Keyboard navigation ───────────────────────────────────────
    @FXML private void onTempKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.TAB && !e.isShiftDown()) { e.consume(); txtBpSystolic.requestFocus(); }
    }
    @FXML private void onComplaintKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.TAB && !e.isShiftDown()) { e.consume(); comboDiagnosis.requestFocus(); }
    }
    @FXML private void onDiagnosisKeyPressed(KeyEvent e) {
        if      (e.getCode() == KeyCode.TAB && !e.isShiftDown()) { e.consume(); txtTreatment.requestFocus(); }
        else if (e.getCode() == KeyCode.TAB &&  e.isShiftDown()) { e.consume(); comboComplaint.requestFocus(); }
    }
    @FXML private void onTreatmentKeyPressed(KeyEvent e) {
        if      (e.getCode() == KeyCode.TAB && !e.isShiftDown()) { e.consume(); txtRemarks.requestFocus(); }
        else if (e.getCode() == KeyCode.TAB &&  e.isShiftDown()) { e.consume(); comboDiagnosis.requestFocus(); }
    }
    @FXML private void onRemarksKeyPressed(KeyEvent e) {
        if      (e.getCode() == KeyCode.ENTER && e.isControlDown()) onSaveRecord();
        else if (e.getCode() == KeyCode.TAB   &&  e.isShiftDown()) { e.consume(); txtTreatment.requestFocus(); }
    }

    // ── Previous visits ───────────────────────────────────────────
    private void loadRecentVisits(String patientId, String currentVisitDate) {
        if (tblPreviousVisits == null || patientId == null || patientId.isEmpty()) return;
        java.util.List<Map<String, String>> visits = DatabaseManager.getVisitsForPatient(patientId);
        visits.removeIf(v ->
                currentVisitDate != null &&
                        currentVisitDate.equals(v.getOrDefault("visit_date", "")) &&
                        v.getOrDefault("chief_complaint", "").isEmpty());
        tblPreviousVisits.setItems(FXCollections.observableArrayList(visits));
    }

    private String buildVitalsString(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        String temp = v.getOrDefault("temperature", "");
        String bp   = v.getOrDefault("blood_pressure", "");
        String hr   = v.getOrDefault("heart_rate", "");
        String o2   = v.getOrDefault("oxygen_sat", "");
        if (!temp.isEmpty()) sb.append("T:").append(temp).append("°C  ");
        if (!bp.isEmpty())   sb.append("BP:").append(bp).append("  ");
        if (!hr.isEmpty())   sb.append("HR:").append(hr).append("  ");
        if (!o2.isEmpty())   sb.append("O2:").append(o2).append("%");
        return sb.toString().trim();
    }

    @FXML private void onCancel() {
        ((Stage) lblSaveStatus.getScene().getWindow()).close();
    }
    @FXML private void setTimeIn() {
        txtTimeIn.setText(java.time.LocalTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
    }
    @FXML private void setTimeOut() {
        txtTimeOut.setText(java.time.LocalTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
    }

    public void setReadOnly(boolean readOnly) {
        txtTemperature.setEditable(!readOnly);
        txtBpSystolic.setEditable(!readOnly);
        txtBpDiastolic.setEditable(!readOnly);
        txtHeartRate.setEditable(!readOnly);
        txtOxygen.setEditable(!readOnly);
        // For combos: make the editor read-only instead of disabling the whole control
        // setDisable(true) was the bug — it greys out the combo and blocks all interaction
        if (comboComplaint.getEditor() != null)  comboComplaint.getEditor().setEditable(!readOnly);
        if (comboDiagnosis.getEditor() != null)   comboDiagnosis.getEditor().setEditable(!readOnly);
        comboComplaint.setMouseTransparent(readOnly);
        comboDiagnosis.setMouseTransparent(readOnly);
        if (txtComplaint != null) txtComplaint.setEditable(!readOnly);
        if (txtDiagnosis != null) txtDiagnosis.setEditable(!readOnly);
        txtTreatment.setEditable(!readOnly);
        txtRemarks.setEditable(!readOnly);
        txtTimeIn.setEditable(!readOnly);
        txtTimeOut.setEditable(!readOnly);
    }
}