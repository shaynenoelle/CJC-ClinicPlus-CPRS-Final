package com.example.cjcschoolclinicmanagementsystem.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.example.cjcschoolclinicmanagementsystem.database.DatabaseManager;
import com.example.cjcschoolclinicmanagementsystem.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    @FXML private Label lblRrWarning;
    @FXML private TextField txtRespiratoryRate;

    // ── Cues (merged complaint + diagnosis) ───────────────────────
    @FXML private ComboBox<String> comboCues;
    @FXML private FlowPane         cuesFlowPane;
    @FXML private Button           btnAddCue;
    @FXML private Button           btnScheduleRecheck;

    @FXML private TextArea  txtTreatment;
    @FXML private TextArea  txtRemarks;
    @FXML private Label     lblSaveStatus;
    @FXML private Label     lblAttendedBy;
    @FXML private TextField txtTimeIn;
    @FXML private TextField txtTimeOut;

    // ── Previous visits table ─────────────────────────────────────
    @FXML private TableView<Map<String, String>>           tblPreviousVisits;
    @FXML private TableColumn<Map<String, String>, String> colPrevDate;
    @FXML private TableColumn<Map<String, String>, String> colPrevCues;
    @FXML private TableColumn<Map<String, String>, String> colPrevTreatment;
    @FXML private TableColumn<Map<String, String>, String> colPrevRemarks;
    @FXML private TableColumn<Map<String, String>, String> colPrevVitals;

    // ── Follow-up tab ─────────────────────────────────────────────
    @FXML private Tab                                      tabFollowUps;
    @FXML private TableView<Map<String, String>>           tblFollowUps;
    @FXML private TableColumn<Map<String, String>, String> colFuScheduled;
    @FXML private TableColumn<Map<String, String>, String> colFuStatus;
    @FXML private TableColumn<Map<String, String>, String> colFuNotes;
    @FXML private TableColumn<Map<String, String>, String> colFuActual;
    @FXML private TableColumn<Map<String, String>, String> colFuVitals;
    @FXML private TableColumn<Map<String, String>, String> colFuRemarks;  // new
    @FXML private TableColumn<Map<String, String>, String> colFuAction;
    @FXML private Label                                    lblFollowUpCount;

    // ── Tab navigation ────────────────────────────────────────────
    @FXML private TabPane tabPane;
    @FXML private Button  btnBack, btnNext, btnSave;
    @FXML private Label   lblStep;

    private final List<String> selectedCues = new ArrayList<>();
    private boolean isReadOnly       = false;
    private int     visitId;
    private String  currentPatientId = "";

    // ── Medicine dispensing ───────────────────────────────────────
    @FXML private ComboBox<String>  comboMedicine;
    @FXML private TextField         txtMedicineQty;
    @FXML private FlowPane          medicineFlowPane;

    // Internal list: each entry = "id|name|unit|qty"
    private final List<String> selectedMedicines = new ArrayList<>();
    // Tracks entries that were loaded from dispense_log (already saved) — skip these on save
    private final java.util.Set<String> preExistingMedicines = new java.util.HashSet<>();

    // ─────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        tabPane.getSelectionModel().selectedIndexProperty().addListener(
                (obs, oldIdx, newIdx) -> updateNavButtons(newIdx.intValue()));
        updateNavButtons(0);

        setupCuesCombo();
        setupMedicineCombo();
        setupFollowUpsTable();

        if (tblPreviousVisits != null) {
            colPrevDate.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getOrDefault("visit_date", "")));
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
        List<String> presets = new ArrayList<>(DatabaseManager.getPresets("cue"));
        presets.sort(String.CASE_INSENSITIVE_ORDER);
        comboCues.setItems(FXCollections.observableArrayList(presets));
        comboCues.setPromptText("Select preset or type a cue…");
        comboCues.getEditor().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) { e.consume(); onAddCue(); }
        });
    }

    // ── Medicine combo setup ─────────────────────────────────────
    private void setupMedicineCombo() {
        if (comboMedicine == null) return;
        refreshMedicineCombo();
        // Attach Enter key handler to the medicine combo's editor so Enter adds a medicine,
        // NOT a cue. This prevents the event from bubbling up to the comboCues handler.
        comboMedicine.getEditor().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) { e.consume(); onAddMedicine(); }
        });
    }

    @FXML
    private void onMedicineQtyKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) { e.consume(); onAddMedicine(); }
    }

    private void refreshMedicineCombo() {
        if (comboMedicine == null) return;
        List<Map<String, String>> medicines = DatabaseManager.getAllMedicines();
        // Format for display: "Name (unit) — qty left"
        comboMedicine.getItems().clear();
        for (Map<String, String> m : medicines) {
            comboMedicine.getItems().add(
                    m.get("name") + " (" + m.get("unit") + ") — stock: " + m.get("quantity"));
        }
        comboMedicine.setPromptText("Select medicine…");
        // Store full medicine data as user data on the ComboBox for lookup
        comboMedicine.setUserData(medicines);
    }

    @FXML
    private void onAddMedicine() {
        if (comboMedicine == null) return;

        // 1. Get the exact text currently typed or selected
        String text = comboMedicine.getEditor().getText();
        if (text == null || text.trim().isEmpty()) {
            text = comboMedicine.getValue();
        }
        if (text == null || text.trim().isEmpty()) return;
        text = text.trim();

        @SuppressWarnings("unchecked")
        List<Map<String, String>> medicines = (List<Map<String, String>>) comboMedicine.getUserData();
        if (medicines == null) return;

        int selectedIndex = comboMedicine.getSelectionModel().getSelectedIndex();
        Map<String, String> med = null;

        // 2. Identify medicine whether they clicked the dropdown OR typed it manually
        if (selectedIndex >= 0 && selectedIndex < medicines.size()) {
            med = medicines.get(selectedIndex);
        } else {
            for (Map<String, String> m : medicines) {
                String displayText = m.get("name") + " (" + m.get("unit") + ") — stock: " + m.get("quantity");
                if (text.equalsIgnoreCase(m.get("name")) || text.equalsIgnoreCase(displayText)) {
                    med = m;
                    break;
                }
            }
        }

        if (med == null) {
            showStatus("⚠ Please select a valid medicine from the list.", false);
            return;
        }

        // 3. Stock & Quantity Checks
        int stock = Integer.parseInt(med.getOrDefault("quantity", "0"));
        if (stock <= 0) {
            showStatus("⚠ " + med.get("name") + " is out of stock.", false);
            return;
        }

        int qty = 1;
        if (txtMedicineQty != null && !txtMedicineQty.getText().trim().isEmpty()) {
            try { qty = Math.max(1, Integer.parseInt(txtMedicineQty.getText().trim())); }
            catch (NumberFormatException ignored) {}
        }
        if (qty > stock) {
            showStatus("⚠ Only " + stock + " " + med.get("unit") + "(s) in stock.", false);
            return;
        }

        // 4. Duplicate Check
        String medId = med.get("medicine_id");
        boolean dup = selectedMedicines.stream().anyMatch(s -> s.startsWith(medId + "|"));
        if (dup) {
            showStatus("⚠ " + med.get("name") + " already added. Remove it first to change qty.", false);
            return;
        }

        // 5. Add the medicine chip
        String entry = medId + "|" + med.get("name") + "|" + med.get("unit") + "|" + qty;
        selectedMedicines.add(entry);
        addMedicineChip(med.get("name"), med.get("unit"), qty, entry);

        // 6. CRITICAL FIX: Fully wipe the memory so the dropdown choices work again!
        comboMedicine.getSelectionModel().clearSelection();
        comboMedicine.setValue(null);
        comboMedicine.getEditor().clear();

        if (txtMedicineQty != null) txtMedicineQty.setText("1");
    }

    private void addMedicineChip(String name, String unit, int qty, String entryKey) {
        if (medicineFlowPane == null) return;
        Label lbl = new Label("💊 " + name + " ×" + qty + " " + unit);
        lbl.setStyle("-fx-font-size: 12; -fx-text-fill: white;");
        Button removeBtn = new Button("×");
        removeBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.85);" +
                        "-fx-font-size: 13; -fx-font-weight: bold; -fx-cursor: hand;" +
                        "-fx-padding: 0 2 0 4;");
        removeBtn.setOnAction(e -> removeMedicineChip(entryKey));
        HBox chip = new HBox(4, lbl, removeBtn);
        chip.setStyle(
                "-fx-background-color: #2e7d32; -fx-background-radius: 14;" +
                        "-fx-padding: 4 10 4 10; -fx-alignment: CENTER_LEFT;");
        chip.setUserData(entryKey);
        if (isReadOnly) removeBtn.setVisible(false);
        medicineFlowPane.getChildren().add(chip);
    }

    private void removeMedicineChip(String entryKey) {
        selectedMedicines.remove(entryKey);
        if (medicineFlowPane != null)
            medicineFlowPane.getChildren().removeIf(node -> entryKey.equals(node.getUserData()));
    }

    // ── Follow-ups table setup ────────────────────────────────────
    private void setupFollowUpsTable() {
        if (tblFollowUps == null) return;

        colFuScheduled.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOrDefault("scheduled_time", "")));
        colFuStatus.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOrDefault("status", "pending")));
        colFuNotes.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOrDefault("nurse_notes", "—")));
        colFuActual.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOrDefault("actual_time", "—")));
        colFuVitals.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOrDefault("vitals_snapshot", "—")));
        if (colFuRemarks != null)
            colFuRemarks.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getOrDefault("recheck_remarks", "—")));

        // Colour-code the Status cell
        colFuStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle("done".equalsIgnoreCase(item)
                        ? "-fx-text-fill: #27ae60; -fx-font-weight: bold;"
                        : "-fx-text-fill: #8B0000; -fx-font-weight: bold;");
            }
        });

        // Action column:
        //   pending → dark-red "✚ Re-check" button  +  red "✕ Remove"
        //   done    → green "✔ Done" label (no action needed)  +  red "✕ Remove"
        colFuAction.setCellFactory(col -> new TableCell<>() {
            final Button btnRecheck = new Button("✚ Re-check");
            final Button btnDelete  = new Button("✕ Remove");
            final Label  lblDone    = new Label("✔ Done");
            final HBox   boxPending = new HBox(6, btnRecheck, btnDelete);
            final HBox   boxDone    = new HBox(6, lblDone, btnDelete);
            {
                btnRecheck.setStyle(
                        "-fx-background-color: #8B0000; -fx-text-fill: white;" +
                                "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 10;" +
                                "-fx-font-weight: bold;");
                btnDelete.setStyle(
                        "-fx-background-color: #e74c3c; -fx-text-fill: white;" +
                                "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 10;");
                lblDone.setStyle(
                        "-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 11;");

                btnRecheck.setOnAction(e -> {
                    Map<String, String> row = getTableView().getItems().get(getIndex());
                    openRecheckVitalsDialog(row);
                });
                btnDelete.setOnAction(e -> {
                    Map<String, String> row = getTableView().getItems().get(getIndex());
                    confirmDeleteFollowUp(row);
                });
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Map<String, String> row = getTableView().getItems().get(getIndex());
                boolean done = "done".equalsIgnoreCase(row.getOrDefault("status", "pending"));
                btnRecheck.setDisable(isReadOnly);
                btnDelete.setDisable(isReadOnly);
                setGraphic(done ? boxDone : boxPending);
            }
        });
    }

    // ── Add a cue chip ────────────────────────────────────────────
    @FXML
    private void onAddCue() {
        if (comboCues == null) return;

        // 1. Get the exact text currently typed or selected
        String value = comboCues.getEditor().getText();
        if (value == null || value.trim().isEmpty()) {
            value = comboCues.getValue(); // Fallback
        }
        if (value == null || value.trim().isEmpty()) return;

        value = value.trim();

        // 2. Prevent duplicates and add chip
        final String finalValue = value;
        boolean duplicate = selectedCues.stream().anyMatch(c -> c.equalsIgnoreCase(finalValue));
        if (!duplicate) {
            addCueChip(value);
        }

        // 3. CRITICAL FIX: Fully wipe the memory so the dropdown choices work again!
        comboCues.getSelectionModel().clearSelection();
        comboCues.setValue(null);
        comboCues.getEditor().clear();
    }

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
        chip.setStyle("-fx-background-color: #8B0000; -fx-background-radius: 14;" +
                "-fx-padding: 4 10 4 10; -fx-alignment: CENTER_LEFT;");
        chip.setUserData(cue);
        if (isReadOnly) removeBtn.setVisible(false);
        cuesFlowPane.getChildren().add(chip);
    }

    private void removeCue(String cue) {
        selectedCues.remove(cue);
        cuesFlowPane.getChildren().removeIf(node -> cue.equals(node.getUserData()));
    }

    private String getCuesString() {
        return selectedCues.stream().filter(s -> !s.isBlank()).collect(Collectors.joining(", "));
    }

    // ── Tab navigation ────────────────────────────────────────────
    @FXML private void onTabChanged() {}

    private void updateNavButtons(int tabIndex) {
        int total = tabPane.getTabs().size();
        if (btnBack != null) { btnBack.setVisible(tabIndex > 0); btnBack.setManaged(tabIndex > 0); }
        boolean isLast = (tabIndex == total - 1);
        if (btnNext != null) { btnNext.setVisible(!isLast); btnNext.setManaged(!isLast); }
        if (btnSave != null) { btnSave.setVisible(isLast);  btnSave.setManaged(isLast); }
        if (lblStep != null) lblStep.setText("Step " + (tabIndex + 1) + " of " + total);
    }

    @FXML private void onBack() {
        int idx = tabPane.getSelectionModel().getSelectedIndex();
        if (idx > 0) tabPane.getSelectionModel().select(idx - 1);
    }
    @FXML private void onNext() {
        int idx = tabPane.getSelectionModel().getSelectedIndex();
        if (idx < tabPane.getTabs().size() - 1) tabPane.getSelectionModel().select(idx + 1);
    }

    // ── Populate form from a visit map ────────────────────────────
    public void setVisitData(Map<String, String> row) {
        visitId          = Integer.parseInt(row.getOrDefault("visit_id", "0"));
        currentPatientId = row.getOrDefault("patient_id", "");

        if (lblPatientName != null) lblPatientName.setText(row.getOrDefault("full_name", ""));
        if (lblDate        != null) lblDate.setText(row.getOrDefault("visit_date",
                java.time.LocalDate.now().toString()));

        String savedBy = row.getOrDefault("attended_by", "").trim();
        if (lblAttendedBy != null)
            lblAttendedBy.setText(savedBy.isEmpty() ? SessionManager.getCurrentUser() : savedBy);

        if (txtTimeIn  != null) txtTimeIn.setText(row.getOrDefault("time_in", ""));
        if (txtTimeOut != null) txtTimeOut.setText(row.getOrDefault("time_out", ""));

        if (txtTemperature != null) txtTemperature.setText(row.getOrDefault("temperature", ""));
        String bp = row.getOrDefault("blood_pressure", "");
        if (bp.contains("/")) {
            String[] parts = bp.split("/");
            if (txtBpSystolic  != null) txtBpSystolic.setText(parts[0].trim());
            if (txtBpDiastolic != null && parts.length > 1) txtBpDiastolic.setText(parts[1].trim());
        }
        if (txtHeartRate       != null) txtHeartRate.setText(row.getOrDefault("heart_rate", ""));
        if (txtOxygen          != null) txtOxygen.setText(row.getOrDefault("oxygen_sat", ""));
        if (txtRespiratoryRate != null) txtRespiratoryRate.setText(row.getOrDefault("respiratory_rate", ""));

        String cuesRaw = row.getOrDefault("chief_complaint", "");
        if (!cuesRaw.isEmpty())
            Arrays.stream(cuesRaw.split(",")).map(String::trim)
                    .filter(s -> !s.isEmpty()).forEach(this::addCueChip);

        if (txtTreatment != null) txtTreatment.setText(row.getOrDefault("treatment", ""));
        if (txtRemarks   != null) txtRemarks.setText(row.getOrDefault("remarks", ""));

        // Load existing dispense records for this visit (read-only view)
        if (medicineFlowPane != null && visitId != 0) {
            List<Map<String, String>> dispensed = DatabaseManager.getDispenseLogForVisit(visitId);
            for (Map<String, String> d : dispensed) {
                String key = d.get("medicine_id") + "|" + d.get("medicine_name") +
                        "|" + d.get("unit") + "|" + d.get("quantity_used");
                selectedMedicines.add(key);
                preExistingMedicines.add(key);  // ← mark as already dispensed
                addMedicineChip(d.get("medicine_name"), d.get("unit"),
                        Integer.parseInt(d.getOrDefault("quantity_used", "1")), key);
            }
        }

        loadRecentVisits(currentPatientId, row.getOrDefault("visit_date", ""));
        loadFollowUps();
    }

    // ── Load / refresh follow-ups tab ─────────────────────────────
    private void loadFollowUps() {
        if (tblFollowUps == null || visitId == 0) return;
        List<Map<String, String>> list = DatabaseManager.getFollowUpsForVisit(visitId);
        tblFollowUps.setItems(FXCollections.observableArrayList(list));
        long pending = list.stream()
                .filter(r -> "pending".equalsIgnoreCase(r.getOrDefault("status", "")))
                .count();
        if (lblFollowUpCount != null)
            lblFollowUpCount.setText(list.size() + " re-check(s)   "
                    + (pending > 0 ? pending + " pending" : "all done"));
        if (tabFollowUps != null)
            tabFollowUps.setText("Re-checks" + (pending > 0 ? " 🔴 " + pending : ""));
    }

    // ── Schedule Re-check dialog ──────────────────────────────────
    @FXML
    private void onScheduleRecheck() {
        if (visitId == 0) {
            showStatus("⚠ Please save the visit record first before scheduling a re-check.", false);
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Schedule Re-check");
        dialog.setHeaderText("When should this patient be re-checked?");
        dialog.setResizable(true);

        ButtonType btnOk = new ButtonType("Schedule", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

        TextField txtTime = new TextField(
                addMinutes(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), 30));
        txtTime.setPromptText("HH:mm");
        txtTime.setPrefWidth(120);
        GridPane.setHgrow(txtTime, Priority.ALWAYS);

        Button btn15 = quickMinBtn("+15 min", 15, txtTime);
        Button btn30 = quickMinBtn("+30 min", 30, txtTime);
        Button btn45 = quickMinBtn("+45 min", 45, txtTime);
        Button btn60 = quickMinBtn("+1 hr",   60, txtTime);

        TextArea txtNote = new TextArea();
        txtNote.setPromptText("Optional note (e.g. \"temp still elevated, check again\")");
        txtNote.setPrefRowCount(3); txtNote.setPrefWidth(300); txtNote.setWrapText(true);
        GridPane.setHgrow(txtNote, Priority.ALWAYS);

        GridPane gp = new GridPane();
        gp.setHgap(12); gp.setVgap(12); gp.setPadding(new Insets(10));
        ColumnConstraints c0 = new ColumnConstraints(); c0.setMinWidth(100); c0.setHgrow(Priority.NEVER);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setHgrow(Priority.ALWAYS); c1.setFillWidth(true);
        gp.getColumnConstraints().addAll(c0, c1);

        HBox quickRow = new HBox(8, btn15, btn30, btn45, btn60);
        quickRow.setAlignment(Pos.CENTER_LEFT);
        gp.add(new Label("Quick select:"),  0, 0); gp.add(quickRow, 1, 0);
        gp.add(new Label("Re-check time:"), 0, 1); gp.add(txtTime,  1, 1);
        gp.add(new Label("Note:"),          0, 2); gp.add(txtNote,  1, 2);

        Label hint = new Label("The re-check time will appear as a badge in the Logbook row.");
        hint.setStyle("-fx-font-size: 10; -fx-text-fill: #888; -fx-font-style: italic;");
        hint.setWrapText(true);
        gp.add(hint, 0, 3, 2, 1);

        dialog.getDialogPane().setContent(gp);
        dialog.getDialogPane().setMinWidth(480);
        dialog.getDialogPane().setMinHeight(280);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(btnOk);
        okBtn.disableProperty().bind(txtTime.textProperty().isEmpty());

        dialog.showAndWait().ifPresent(result -> {
            if (result != btnOk) return;
            String time  = txtTime.getText().trim();
            String notes = txtNote.getText().trim();
            if (time.isEmpty()) return;
            boolean ok = DatabaseManager.scheduleFollowUp(visitId, time, notes);
            if (ok) { showStatus("✔ Re-check scheduled for " + time + ".", true); loadFollowUps(); }
            else    { showStatus("Failed to schedule re-check. Try again.", false); }
        });
    }

    // ── Re-check Vitals Dialog ────────────────────────────────────
    /**
     * Opened when the nurse clicks "✚ Re-check" on a pending row.
     *
     * Shows the same proper vitals fields (Temp, BP, HR, SpO2, RR) with
     * real-time warning labels — exactly like the main Vital Signs tab.
     * Cues and Treatment are intentionally omitted (they belong to the
     * initial visit). A Remarks field is shown after the vitals.
     */
    private void openRecheckVitalsDialog(Map<String, String> fuRow) {
        int fuId = Integer.parseInt(fuRow.getOrDefault("followup_id", "0"));
        if (fuId == 0) return;

        String scheduledTime = fuRow.getOrDefault("scheduled_time", "");
        String nurseNote     = fuRow.getOrDefault("nurse_notes", "");

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Re-check — " + scheduledTime);
        dialog.setResizable(true);

        // Header
        VBox header = new VBox(4);
        header.setPadding(new Insets(14, 16, 10, 16));
        Label lblTitle = new Label("Record Re-check Vitals");
        lblTitle.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #8B0000;");
        Label lblNote = new Label(nurseNote.isBlank()
                ? "Scheduled: " + scheduledTime
                : "Scheduled: " + scheduledTime + "   |   Note: " + nurseNote);
        lblNote.setStyle("-fx-font-size: 11; -fx-text-fill: #555;");
        lblNote.setWrapText(true);
        header.getChildren().addAll(lblTitle, lblNote);
        dialog.getDialogPane().setHeader(header);

        ButtonType btnSaveRecheck = new ButtonType("Save Re-check", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSaveRecheck, ButtonType.CANCEL);

        // ── Actual time row ───────────────────────────────────────
        TextField txtActualTime = new TextField(
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        txtActualTime.setPrefWidth(90);
        Button btnNow = new Button("⏱ Now");
        btnNow.setStyle("-fx-background-color: #e8e8e8; -fx-text-fill: #333;" +
                "-fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 11;");
        btnNow.setOnAction(e -> txtActualTime.setText(
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))));
        HBox timeRow = new HBox(8, txtActualTime, btnNow);
        timeRow.setAlignment(Pos.CENTER_LEFT);

        // ── Vitals fields (same style as main form) ───────────────
        // Temperature
        TextField fuTemp = vitalsField("e.g. 36.5");
        Label fuTempWarn = warnLabel();
        fuTemp.setOnKeyReleased(e -> {
            try {
                double v = Double.parseDouble(fuTemp.getText().trim()); boolean ok = v >= 36.1 && v <= 37.2;
                fuTempWarn.setText(ok ? "✔ Normal" : (v > 37.2 ? "⚠ Fever" : "⚠ Low"));
                fuTempWarn.setStyle(ok ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e74c3c;");
            } catch (NumberFormatException ex) { fuTempWarn.setText(""); }
        });

        // Blood Pressure
        TextField fuBpSys = new TextField(); fuBpSys.setPromptText("120");
        fuBpSys.setPrefWidth(72); fuBpSys.setPrefHeight(36);
        TextField fuBpDia = new TextField(); fuBpDia.setPromptText("80");
        fuBpDia.setPrefWidth(72); fuBpDia.setPrefHeight(36);
        Label fuBpWarn = warnLabel();
        Runnable bpCheck = () -> {
            try {
                int s = Integer.parseInt(fuBpSys.getText().trim());
                int d = Integer.parseInt(fuBpDia.getText().trim());
                boolean ok = s >= 90 && s <= 120 && d >= 60 && d <= 80;
                fuBpWarn.setText(ok ? "✔ Normal" : "⚠ Abnormal");
                fuBpWarn.setStyle(ok ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e74c3c;");
            } catch (NumberFormatException ex) { fuBpWarn.setText(""); }
        };
        fuBpSys.setOnKeyReleased(e -> bpCheck.run());
        fuBpDia.setOnKeyReleased(e -> bpCheck.run());
        Label slash = new Label("/");
        slash.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        HBox bpBox = new HBox(6, fuBpSys, slash, fuBpDia);
        bpBox.setAlignment(Pos.CENTER_LEFT);

        // Heart Rate
        TextField fuHr = vitalsField("e.g. 75");
        Label fuHrWarn = warnLabel();
        fuHr.setOnKeyReleased(e -> {
            try {
                int v = Integer.parseInt(fuHr.getText().trim()); boolean ok = v >= 60 && v <= 100;
                fuHrWarn.setText(ok ? "✔ Normal" : "⚠ Abnormal");
                fuHrWarn.setStyle(ok ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e74c3c;");
            } catch (NumberFormatException ex) { fuHrWarn.setText(""); }
        });

        // SpO2
        TextField fuO2 = vitalsField("e.g. 98");
        Label fuO2Warn = warnLabel();
        fuO2.setOnKeyReleased(e -> {
            try {
                int v = Integer.parseInt(fuO2.getText().trim()); boolean ok = v >= 95;
                fuO2Warn.setText(ok ? "✔ Normal" : "⚠ Low SpO2");
                fuO2Warn.setStyle(ok ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e74c3c;");
            } catch (NumberFormatException ex) { fuO2Warn.setText(""); }
        });

        // Respiratory Rate
        TextField fuRr = vitalsField("e.g. 16");
        Label fuRrWarn = warnLabel();
        fuRr.setOnKeyReleased(e -> {
            try {
                int v = Integer.parseInt(fuRr.getText().trim()); boolean ok = v >= 12 && v <= 20;
                fuRrWarn.setText(ok ? "✔ Normal" : "⚠ Abnormal");
                fuRrWarn.setStyle(ok ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e74c3c;");
            } catch (NumberFormatException ex) { fuRrWarn.setText(""); }
        });

        // ── Remarks ───────────────────────────────────────────────
        TextArea fuRemarks = new TextArea();
        fuRemarks.setPromptText("Optional nurse remarks for this re-check…");
        fuRemarks.setPrefRowCount(3);
        fuRemarks.setWrapText(true);

        // ── Layout ────────────────────────────────────────────────
        GridPane gp = new GridPane();
        gp.setHgap(14); gp.setVgap(12); gp.setPadding(new Insets(12));

        ColumnConstraints lCol = new ColumnConstraints(); lCol.setMinWidth(170); lCol.setHgrow(Priority.NEVER);
        ColumnConstraints fCol = new ColumnConstraints(); fCol.setHgrow(Priority.ALWAYS); fCol.setFillWidth(true);
        ColumnConstraints wCol = new ColumnConstraints(); wCol.setMinWidth(95); wCol.setHgrow(Priority.NEVER);
        gp.getColumnConstraints().addAll(lCol, fCol, wCol);

        Label hintLbl = new Label("All vitals are optional — fill in only what was measured.");
        hintLbl.setStyle("-fx-font-size: 11; -fx-text-fill: #2196F3; -fx-font-style: italic;");
        gp.add(hintLbl, 0, 0, 3, 1);

        gp.add(boldLabel("Re-check Time"),             0, 1); gp.add(timeRow, 1, 1);

        Label divVitals = sectionDivider("Vitals");
        gp.add(divVitals, 0, 2, 3, 1);

        gp.add(boldLabel("Temperature (°C)"),          0, 3); gp.add(fuTemp,  1, 3); gp.add(fuTempWarn, 2, 3);
        gp.add(boldLabel("Blood Pressure (mmHg)"),     0, 4); gp.add(bpBox,   1, 4); gp.add(fuBpWarn,   2, 4);
        gp.add(boldLabel("Heart Rate (BPM)"),          0, 5); gp.add(fuHr,    1, 5); gp.add(fuHrWarn,   2, 5);
        gp.add(boldLabel("Oxygen Saturation (%)"),     0, 6); gp.add(fuO2,    1, 6); gp.add(fuO2Warn,   2, 6);
        gp.add(boldLabel("Respiratory Rate (/min)"),   0, 7); gp.add(fuRr,    1, 7); gp.add(fuRrWarn,   2, 7);

        gp.add(sectionDivider("Remarks"), 0, 8, 3, 1);
        gp.add(boldLabel("Nurse Remarks"), 0, 9);
        GridPane.setHgrow(fuRemarks, Priority.ALWAYS);
        gp.add(fuRemarks, 1, 9, 2, 1);

        dialog.getDialogPane().setContent(gp);
        dialog.getDialogPane().setMinWidth(580);
        dialog.getDialogPane().setPrefHeight(540);

        dialog.showAndWait().ifPresent(result -> {
            if (result != btnSaveRecheck) return;

            String temp   = fuTemp.getText().trim();
            String bpSys  = fuBpSys.getText().trim();
            String bpDia  = fuBpDia.getText().trim();
            String hr     = fuHr.getText().trim();
            String o2     = fuO2.getText().trim();
            String rr     = fuRr.getText().trim();
            String rem    = fuRemarks.getText().trim();
            String actual = txtActualTime.getText().trim();

            // Build a structured snapshot for the "Vitals" column in the re-checks table
            StringBuilder snap = new StringBuilder();
            if (!temp.isEmpty())                        snap.append("T:").append(temp).append("°C  ");
            if (!bpSys.isEmpty() && !bpDia.isEmpty())   snap.append("BP:").append(bpSys).append("/").append(bpDia).append("  ");
            if (!hr.isEmpty())                          snap.append("HR:").append(hr).append("  ");
            if (!o2.isEmpty())                          snap.append("O2:").append(o2).append("%  ");
            if (!rr.isEmpty())                          snap.append("RR:").append(rr);
            String snapshot = snap.toString().trim();
            if (snapshot.isEmpty()) snapshot = "—";

            boolean ok = DatabaseManager.completeFollowUp(
                    fuId, actual, snapshot, rem, SessionManager.getCurrentUser());

            if (ok) { showStatus("✔ Re-check saved at " + actual + ".", true); loadFollowUps(); }
            else    { showStatus("Failed to save re-check. Try again.", false); }
        });
    }

    // ── Dialog field helpers ──────────────────────────────────────
    private TextField vitalsField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setPrefWidth(180); f.setPrefHeight(36);
        f.setStyle("-fx-background-radius: 5; -fx-border-color: #ddd;" +
                "-fx-border-radius: 5; -fx-padding: 8; -fx-font-size: 13;");
        return f;
    }
    private Label warnLabel() {
        Label l = new Label(""); l.setStyle("-fx-font-size: 11;"); return l;
    }
    private Label boldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #444;");
        return l;
    }
    private Label sectionDivider(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #444;" +
                "-fx-border-color: transparent transparent #ddd transparent; -fx-padding: 8 0 4 0;");
        return l;
    }

    /** Creates a quick "+N min" button that fills the time field. */
    private Button quickMinBtn(String label, int minutes, TextField target) {
        Button btn = new Button(label);
        btn.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white;" +
                "-fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 11;");
        btn.setOnAction(e -> target.setText(
                addMinutes(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), minutes)));
        return btn;
    }

    private String addMinutes(String baseHHmm, int minutes) {
        try {
            return LocalTime.parse(baseHHmm, DateTimeFormatter.ofPattern("HH:mm"))
                    .plusMinutes(minutes).format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception ex) {
            return LocalTime.now().plusMinutes(minutes).format(DateTimeFormatter.ofPattern("HH:mm"));
        }
    }

    // ── Delete follow-up confirmation ─────────────────────────────
    private void confirmDeleteFollowUp(Map<String, String> row) {
        int fuId = Integer.parseInt(row.getOrDefault("followup_id", "0"));
        if (fuId == 0) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Re-check");
        alert.setHeaderText("Remove this re-check entry?");
        alert.setContentText("Scheduled: " + row.getOrDefault("scheduled_time", "")
                + "\nNote: " + row.getOrDefault("nurse_notes", "—"));
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) { DatabaseManager.deleteFollowUp(fuId); loadFollowUps(); }
        });
    }

    // ── Vitals validation (main form) ─────────────────────────────
    @FXML
    private void validateVitals() {
        try {
            double temp = Double.parseDouble(txtTemperature.getText().trim());
            boolean ok  = temp >= 36.1 && temp <= 37.2;
            lblTempWarning.setText(ok ? "✔ Normal" : (temp > 37.2 ? "⚠ Fever" : "⚠ Low"));
            lblTempWarning.setStyle(ok ? "-fx-text-fill: #27ae60; -fx-font-size: 11;"
                    : "-fx-text-fill: #e74c3c; -fx-font-size: 11;");
        } catch (NumberFormatException e) { lblTempWarning.setText(""); }

        try {
            int sys = Integer.parseInt(txtBpSystolic.getText().trim());
            int dia = Integer.parseInt(txtBpDiastolic.getText().trim());
            boolean ok = sys >= 90 && sys <= 120 && dia >= 60 && dia <= 80;
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
            int o2 = Integer.parseInt(txtOxygen.getText().trim());
            boolean ok = o2 >= 95;
            lblO2Warning.setText(ok ? "✔ Normal" : "⚠ Low SpO2");
            lblO2Warning.setStyle(ok ? "-fx-text-fill: #27ae60; -fx-font-size: 11;"
                    : "-fx-text-fill: #e74c3c; -fx-font-size: 11;");
        } catch (NumberFormatException e) { lblO2Warning.setText(""); }

        if (txtRespiratoryRate != null) {
            try {
                int rr = Integer.parseInt(txtRespiratoryRate.getText().trim());
                boolean ok = rr >= 12 && rr <= 20;
                if (lblRrWarning != null) {
                    lblRrWarning.setText(ok ? "✔ Normal" : "⚠ Abnormal");
                    lblRrWarning.setStyle(ok ? "-fx-text-fill: #27ae60; -fx-font-size: 11;"
                            : "-fx-text-fill: #e74c3c; -fx-font-size: 11;");
                }
            } catch (NumberFormatException e) { if (lblRrWarning != null) lblRrWarning.setText(""); }
        }
    }

    // ── Save main record ──────────────────────────────────────────
    @FXML
    private void onSaveRecord() {
        if (txtTimeIn.getText() != null && !txtTimeIn.getText().trim().isEmpty())
            DatabaseManager.updateTimeIn(visitId, txtTimeIn.getText().trim());

        String cues = getCuesString();
        if (cues.isEmpty()) { showStatus("⚠ At least one Cue is required.", false); return; }

        String tempStr = txtTemperature.getText().trim();
        if (!tempStr.isEmpty()) {
            try { Double.parseDouble(tempStr); }
            catch (NumberFormatException e) {
                showStatus("⚠ Temperature must be a valid number (e.g. 36.5).", false); return;
            }
        }

        String bpSys = txtBpSystolic.getText().trim();
        String bpDia = txtBpDiastolic.getText().trim();
        if (!bpSys.isEmpty() || !bpDia.isEmpty()) {
            if (bpSys.isEmpty() || bpDia.isEmpty()) {
                showStatus("⚠ Both systolic and diastolic are needed for Blood Pressure.", false); return;
            }
            try { Integer.parseInt(bpSys); Integer.parseInt(bpDia); }
            catch (NumberFormatException e) {
                showStatus("⚠ Blood Pressure values must be whole numbers.", false); return;
            }
        }

        String attendedBy = (lblAttendedBy != null && !lblAttendedBy.getText().isBlank())
                ? lblAttendedBy.getText() : SessionManager.getCurrentUser();
        String rrStr = (txtRespiratoryRate != null) ? txtRespiratoryRate.getText().trim() : "";

        boolean success = DatabaseManager.saveVisitRecord(
                visitId, tempStr,
                (bpSys.isEmpty() && bpDia.isEmpty()) ? "" : bpSys + "/" + bpDia,
                txtHeartRate.getText().trim(), txtOxygen.getText().trim(), rrStr,
                cues, cues, txtTreatment.getText().trim(), txtRemarks.getText().trim(), attendedBy);

        if (success) {
            String timeOut = txtTimeOut.getText() != null ? txtTimeOut.getText().trim() : "";
            if (!timeOut.isEmpty()) DatabaseManager.checkOutPatient(visitId, timeOut);

            // Dispense newly added medicines BEFORE closing the window.
            // Entries in preExistingMedicines were already saved on a previous save — skip them.
            String attendedBy2 = (lblAttendedBy != null && !lblAttendedBy.getText().isBlank())
                    ? lblAttendedBy.getText() : SessionManager.getCurrentUser();
            for (String entry : selectedMedicines) {
                if (preExistingMedicines.contains(entry)) continue; // already dispensed
                String[] parts = entry.split("\\|", 4);
                if (parts.length < 4) continue;
                int medId      = Integer.parseInt(parts[0]);
                String medName = parts[1];
                String unit    = parts[2];
                int qty        = Integer.parseInt(parts[3]);
                boolean dispensed = DatabaseManager.dispenseMedicine(visitId, medId, medName, unit, qty, attendedBy2);
                if (dispensed) preExistingMedicines.add(entry); // mark so double-save is safe too
            }

            showStatus("✔ Record saved!", true);
            new Thread(() -> {
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> ((Stage) lblSaveStatus.getScene().getWindow()).close());
            }).start();
        } else {
            showStatus("Save failed. Try again.", false);
        }
    }

    // ── Keyboard navigation ───────────────────────────────────────
    @FXML private void onTempKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.TAB && !e.isShiftDown()) { e.consume(); txtBpSystolic.requestFocus(); }
    }
    @FXML private void onCuesKeyPressed(KeyEvent e) {
        if      (e.getCode() == KeyCode.ENTER)                   { e.consume(); onAddCue(); }
        else if (e.getCode() == KeyCode.TAB && !e.isShiftDown()) { e.consume(); txtTreatment.requestFocus(); }
    }
    @FXML private void onTreatmentKeyPressed(KeyEvent e) {
        if      (e.getCode() == KeyCode.TAB && !e.isShiftDown()) { e.consume(); txtRemarks.requestFocus(); }
        else if (e.getCode() == KeyCode.TAB &&  e.isShiftDown()) { e.consume(); comboCues.requestFocus(); }
    }
    @FXML private void onRemarksKeyPressed(KeyEvent e) {
        if      (e.getCode() == KeyCode.ENTER && e.isControlDown()) onSaveRecord();
        else if (e.getCode() == KeyCode.TAB   &&  e.isShiftDown())  { e.consume(); txtTreatment.requestFocus(); }
    }

    // ── Previous visits ───────────────────────────────────────────
    private void loadRecentVisits(String patientId, String currentVisitDate) {
        if (tblPreviousVisits == null || patientId == null || patientId.isEmpty()) return;
        List<Map<String, String>> visits = DatabaseManager.getVisitsForPatient(patientId);
        visits.removeIf(v -> currentVisitDate != null
                && currentVisitDate.equals(v.getOrDefault("visit_date", ""))
                && v.getOrDefault("chief_complaint", "").isEmpty());
        tblPreviousVisits.setItems(FXCollections.observableArrayList(visits));
    }

    private String buildVitalsString(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        String temp = v.getOrDefault("temperature", "");
        String bp   = v.getOrDefault("blood_pressure", "");
        String hr   = v.getOrDefault("heart_rate", "");
        String o2   = v.getOrDefault("oxygen_sat", "");
        String rr   = v.getOrDefault("respiratory_rate", "");
        if (!temp.isEmpty()) sb.append("T:").append(temp).append("°C  ");
        if (!bp.isEmpty())   sb.append("BP:").append(bp).append("  ");
        if (!hr.isEmpty())   sb.append("HR:").append(hr).append("  ");
        if (!o2.isEmpty())   sb.append("O2:").append(o2).append("%  ");
        if (!rr.isEmpty())   sb.append("RR:").append(rr);
        return sb.toString().trim();
    }

    // ── Read-only mode ────────────────────────────────────────────
    public void setReadOnly(boolean readOnly) {
        isReadOnly = readOnly;
        txtTemperature.setEditable(!readOnly);
        txtBpSystolic.setEditable(!readOnly);
        txtBpDiastolic.setEditable(!readOnly);
        txtHeartRate.setEditable(!readOnly);
        txtOxygen.setEditable(!readOnly);
        if (txtRespiratoryRate != null) txtRespiratoryRate.setEditable(!readOnly);
        comboCues.setDisable(readOnly);
        if (btnAddCue          != null) btnAddCue.setDisable(readOnly);
        if (btnScheduleRecheck != null) btnScheduleRecheck.setDisable(readOnly);
        cuesFlowPane.getChildren().forEach(node -> {
            if (node instanceof HBox chip)
                chip.getChildren().stream().filter(n -> n instanceof Button)
                        .forEach(n -> n.setVisible(false));
        });
        txtTreatment.setEditable(!readOnly);
        txtRemarks.setEditable(!readOnly);
        txtTimeIn.setEditable(!readOnly);
        txtTimeOut.setEditable(!readOnly);
    }

    // ── Helpers ───────────────────────────────────────────────────
    private void showStatus(String msg, boolean success) {
        if (lblSaveStatus == null) return;
        lblSaveStatus.setText(msg);
        lblSaveStatus.setStyle(success ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e74c3c;");
    }

    @FXML private void onCancel() {
        ((Stage) lblSaveStatus.getScene().getWindow()).close();
    }
    @FXML private void setTimeIn() {
        txtTimeIn.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }
    @FXML private void setTimeOut() {
        txtTimeOut.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }
}