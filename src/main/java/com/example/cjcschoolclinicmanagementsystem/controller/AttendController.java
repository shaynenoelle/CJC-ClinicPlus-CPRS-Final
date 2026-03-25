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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    // ── Cues (merged complaint + diagnosis) ───────────────────────
    @FXML private ComboBox<String> comboCues;   // preset picker / free-type
    @FXML private FlowPane         cuesFlowPane; // chip display area
    @FXML private Button           btnAddCue;

    @FXML private TextArea  txtTreatment;
    @FXML private TextArea  txtRemarks;
    @FXML private Label     lblSaveStatus;
    @FXML private TextField txtTimeIn;
    @FXML private TextField txtTimeOut;

    // ── Previous visits table ─────────────────────────────────────
    @FXML private TableView<Map<String, String>>           tblPreviousVisits;
    @FXML private TableColumn<Map<String, String>, String> colPrevDate;
    @FXML private TableColumn<Map<String, String>, String> colPrevCues;       // renamed from colPrevComplaint
    @FXML private TableColumn<Map<String, String>, String> colPrevTreatment;
    @FXML private TableColumn<Map<String, String>, String> colPrevRemarks;
    @FXML private TableColumn<Map<String, String>, String> colPrevVitals;

    // ── Tab navigation ────────────────────────────────────────────
    @FXML private TabPane tabPane;
    @FXML private Button  btnBack, btnNext, btnSave;
    @FXML private Label   lblStep;

    // Internal list of cues currently selected for this visit
    private final List<String> selectedCues = new ArrayList<>();

    private boolean isReadOnly      = false;
    private int     visitId;
    private String  currentPatientId = "";

    // ─────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        tabPane.getSelectionModel().selectedIndexProperty().addListener(
                (obs, oldIdx, newIdx) -> updateNavButtons(newIdx.intValue()));
        updateNavButtons(0);

        setupCuesCombo();

        if (tblPreviousVisits != null) {
            colPrevDate.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getOrDefault("visit_date", "")));
            // Show cues — stored in chief_complaint
            colPrevCues.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getOrDefault("chief_complaint", "—")));
            colPrevTreatment.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getOrDefault("treatment", "—")));
            colPrevRemarks.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getOrDefault("remarks", "—")));
            colPrevVitals.setCellValueFactory(d ->
                    new SimpleStringProperty(buildVitalsString(d.getValue())));
        }
    }

    // ── Cues combo setup ──────────────────────────────────────────
    private void setupCuesCombo() {
        // Load presets from both complaint and diagnosis buckets so the nurse
        // has the full list available in one dropdown.
        List<String> presets = new ArrayList<>(DatabaseManager.getPresets("complaint"));
        List<String> diagPresets = DatabaseManager.getPresets("diagnosis");
        for (String d : diagPresets) {
            if (!presets.contains(d)) presets.add(d);
        }
        presets.sort(String.CASE_INSENSITIVE_ORDER);
        comboCues.setItems(FXCollections.observableArrayList(presets));
        comboCues.setPromptText("Select preset or type a cue…");

        // Allow pressing Enter inside the combo editor to add the cue
        comboCues.getEditor().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                e.consume();
                onAddCue();
            }
        });
    }

    // ── Add a cue chip ────────────────────────────────────────────
    @FXML
    private void onAddCue() {
        // getValue() catches a selected preset; getEditor().getText() catches free-typed text
        String value = comboCues.getValue() != null
                ? comboCues.getValue().trim()
                : comboCues.getEditor().getText().trim();

        if (value.isEmpty()) return;

        // Avoid exact duplicates (case-insensitive)
        boolean duplicate = selectedCues.stream()
                .anyMatch(c -> c.equalsIgnoreCase(value));
        if (duplicate) {
            comboCues.getEditor().clear();
            comboCues.setValue(null);
            return;
        }

        addCueChip(value);
        comboCues.getEditor().clear();
        comboCues.setValue(null);
    }

    /**
     * Creates a visual chip for the given cue text and appends it to the
     * FlowPane. Also registers the cue in selectedCues.
     */
    private void addCueChip(String cue) {
        if (cue == null || cue.isBlank()) return;
        selectedCues.add(cue);

        Label lbl = new Label(cue);
        lbl.setStyle("-fx-font-size: 12; -fx-text-fill: white;");

        Button removeBtn = new Button("×");
        removeBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.85);" +
                        "-fx-font-size: 13; -fx-font-weight: bold; -fx-cursor: hand;" +
                        "-fx-padding: 0 2 0 4;");
        removeBtn.setOnAction(e -> removeCue(cue));

        HBox chip = new HBox(4, lbl, removeBtn);
        chip.setStyle(
                "-fx-background-color: #8B0000; -fx-background-radius: 14;" +
                        "-fx-padding: 4 10 4 10; -fx-alignment: CENTER_LEFT;");
        chip.setUserData(cue); // used for removal lookup

        if (isReadOnly) removeBtn.setVisible(false);

        cuesFlowPane.getChildren().add(chip);
    }

    /** Removes a cue from both the selectedCues list and the FlowPane. */
    private void removeCue(String cue) {
        selectedCues.remove(cue);
        cuesFlowPane.getChildren().removeIf(node ->
                cue.equals(node.getUserData()));
    }

    /** Returns the comma-separated cues string for DB storage. */
    private String getCuesString() {
        return selectedCues.stream()
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));
    }

    /** Clears all chips and the internal list, then re-populates from a saved string. */
    private void loadCuesFromString(String saved) {
        selectedCues.clear();
        cuesFlowPane.getChildren().clear();
        if (saved == null || saved.isBlank()) return;
        Arrays.stream(saved.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(this::addCueChip);
    }

    // ── Tab navigation ────────────────────────────────────────────
    @FXML
    public void onTabChanged() {
        updateNavButtons(tabPane.getSelectionModel().getSelectedIndex());
    }

    private void updateNavButtons(int tabIndex) {
        int lastTab  = tabPane.getTabs().size() - 1;
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

    // ── Load visit data into form ─────────────────────────────────
    public void setVisitData(Map<String, String> visitRow) {
        visitId          = Integer.parseInt(visitRow.get("visit_id"));
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

        // Load saved cues — stored in chief_complaint as comma-separated string
        loadCuesFromString(visitRow.getOrDefault("chief_complaint", ""));

        if (visitRow.get("treatment") != null) txtTreatment.setText(visitRow.get("treatment"));
        if (visitRow.get("remarks")   != null) txtRemarks.setText(visitRow.get("remarks"));

        loadRecentVisits(currentPatientId, visitRow.get("visit_date"));
    }

    // ── Vitals validation ─────────────────────────────────────────
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

    // ── Save record ───────────────────────────────────────────────
    @FXML
    private void onSaveRecord() {
        if (txtTimeIn.getText() != null && !txtTimeIn.getText().trim().isEmpty()) {
            DatabaseManager.updateTimeIn(visitId, txtTimeIn.getText().trim());
        }

        // Cues string → stored in chief_complaint; also mirrored to diagnosis for reports
        String cues = getCuesString();
        if (cues.isEmpty()) {
            lblSaveStatus.setText("⚠ At least one Cue is required.");
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

        // diagnosis is auto-populated from cues (kept for dashboard/report queries)
        boolean success = DatabaseManager.saveVisitRecord(
                visitId, tempStr, bpSys + "/" + bpDia,
                txtHeartRate.getText().trim(), txtOxygen.getText().trim(),
                cues,  /* chief_complaint */
                cues,  /* diagnosis — mirrors cues; report layer reads this */
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
    @FXML private void onCuesKeyPressed(KeyEvent e) {
        if      (e.getCode() == KeyCode.ENTER)                              { e.consume(); onAddCue(); }
        else if (e.getCode() == KeyCode.TAB && !e.isShiftDown()) { e.consume(); txtTreatment.requestFocus(); }
    }
    @FXML private void onTreatmentKeyPressed(KeyEvent e) {
        if      (e.getCode() == KeyCode.TAB && !e.isShiftDown()) { e.consume(); txtRemarks.requestFocus(); }
        else if (e.getCode() == KeyCode.TAB &&  e.isShiftDown()) { e.consume(); comboCues.requestFocus(); }
    }
    @FXML private void onRemarksKeyPressed(KeyEvent e) {
        if      (e.getCode() == KeyCode.ENTER && e.isControlDown()) onSaveRecord();
        else if (e.getCode() == KeyCode.TAB   &&  e.isShiftDown()) { e.consume(); txtTreatment.requestFocus(); }
    }

    // ── Previous visits ───────────────────────────────────────────
    private void loadRecentVisits(String patientId, String currentVisitDate) {
        if (tblPreviousVisits == null || patientId == null || patientId.isEmpty()) return;
        List<Map<String, String>> visits = DatabaseManager.getVisitsForPatient(patientId);
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

    // ── Read-only mode (View from Visit History) ──────────────────
    public void setReadOnly(boolean readOnly) {
        isReadOnly = readOnly;

        txtTemperature.setEditable(!readOnly);
        txtBpSystolic.setEditable(!readOnly);
        txtBpDiastolic.setEditable(!readOnly);
        txtHeartRate.setEditable(!readOnly);
        txtOxygen.setEditable(!readOnly);

        // Disable cues input
        comboCues.setDisable(readOnly);
        if (btnAddCue != null) btnAddCue.setDisable(readOnly);

        // Hide × buttons on existing chips when read-only
        cuesFlowPane.getChildren().forEach(node -> {
            if (node instanceof HBox chip) {
                chip.getChildren().stream()
                        .filter(n -> n instanceof Button)
                        .forEach(n -> n.setVisible(false));
            }
        });

        txtTreatment.setEditable(!readOnly);
        txtRemarks.setEditable(!readOnly);
        txtTimeIn.setEditable(!readOnly);
        txtTimeOut.setEditable(!readOnly);
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
}