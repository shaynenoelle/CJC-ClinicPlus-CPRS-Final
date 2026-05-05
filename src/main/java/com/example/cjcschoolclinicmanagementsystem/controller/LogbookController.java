package com.example.cjcschoolclinicmanagementsystem.controller;

import com.example.cjcschoolclinicmanagementsystem.database.DatabaseManager;
import com.example.cjcschoolclinicmanagementsystem.model.Patient;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LogbookController {

    // Search panel
    @FXML private TextField txtSearch;
    @FXML private VBox searchResultsPane;
    @FXML private TableView<Patient> tblSearchResults;
    @FXML private TableColumn<Patient, String> colSearchName;
    @FXML private TableColumn<Patient, String> colSearchId;
    @FXML private TableColumn<Patient, String> colSearchAction;

    // Selected patient panel
    @FXML private VBox selectedPatientPane;
    @FXML private Label lblSelectedName;
    @FXML private Label lblSelectedId;
    @FXML private TextField txtTimeIn;
    @FXML private RadioButton rbHealthRelated;
    @FXML private RadioButton rbMedCert;
    @FXML private Label lblCheckInStatus;

    // Logbook table
    @FXML private TableView<Map<String, String>> tblLogbook;
    @FXML private TableColumn<Map<String, String>, String> colId;
    @FXML private TableColumn<Map<String, String>, String> colName;
    @FXML private TableColumn<Map<String, String>, String> colTimeIn;
    @FXML private TableColumn<Map<String, String>, String> colPurpose;
    @FXML private TableColumn<Map<String, String>, String> colTimeOut;
    @FXML private TableColumn<Map<String, String>, String> colAttendedBy;
    @FXML private TableColumn<Map<String, String>, String> colAction;
    @FXML private Label lblVisitorCount;
    @FXML private Label lblLogbookTitle;

    // Filter buttons
    @FXML private Button btnToday;
    @FXML private Button btnWeekly;
    @FXML private Button btnMonthly;
    @FXML private Button btnAll;

    // Custom date range
    @FXML private DatePicker dpFrom;
    @FXML private DatePicker dpTo;

    // Pagination
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Label  lblPage;

    private static final int PAGE_SIZE = 25;

    private int currentPage = 0;
    private List<Map<String, String>> allLogbookData = new ArrayList<>();

    /**
     * Cache of today's pending follow-ups, keyed by visit_id.
     * Refreshed every time refreshLogbook() is called.
     * Structure: List<Map> where each map has: followup_id, visit_id,
     *            scheduled_time, nurse_notes, full_name, patient_id
     */
    private List<Map<String, String>> pendingFollowUps = new ArrayList<>();

    private Patient selectedPatient = null;
    private String  currentFilter   = "daily";

    @FXML
    public void initialize() {
        setupSearchTable();
        setupLogbookTable();
        setCurrentTime();
        refreshLogbook();
    }

    // ── Search table ──────────────────────────────────────────────
    private void setupSearchTable() {
        colSearchName.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getFullName()));
        colSearchId.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getPatientId()));

        colSearchAction.setCellFactory(col -> new TableCell<>() {
            final Button btn = new Button("Select");
            {
                btn.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white;" +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11;");
                btn.setOnAction(e -> selectPatient(
                        getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    // ── Logbook table ─────────────────────────────────────────────
    private void setupLogbookTable() {
        colId.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOrDefault("patient_id", "")));
        colName.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOrDefault("full_name", "")));
        colName.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) { setGraphic(null); setText(null); return; }
                Map<String, String> row = getTableView().getItems().get(getIndex());
                String type = row.getOrDefault("type", "Student");
                boolean isEmployee = "Employee".equalsIgnoreCase(type);
                Label pill = new Label(isEmployee ? "EMP" : "STU");
                pill.setStyle(
                        "-fx-background-color: " + (isEmployee ? "#6a1b9a" : "#1565c0") + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 8;" +
                                "-fx-font-size: 9;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 1 5 1 5;");
                Label nameLbl = new Label(name);
                nameLbl.setStyle("-fx-font-size: 12;");
                HBox box = new HBox(5, pill, nameLbl);
                box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                setGraphic(box);
                setText(null);
            }
        });
        colTimeIn.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOrDefault("time_in", "")));
        colPurpose.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOrDefault("purpose", "")));
        colTimeOut.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOrDefault("time_out", "—")));
        colAttendedBy.setCellValueFactory(d -> {
            String val = d.getValue().getOrDefault("attended_by", "");
            return new SimpleStringProperty(val == null || val.isBlank() ? "—" : val);
        });

        // ── Context-aware Action button (4 states) ────────────────
        // Priority order (checked top-to-bottom):
        //   1. OVERDUE   — attended + a pending re-check is past its scheduled time
        //   2. RE-CHECK  — attended + a pending re-check is coming up today
        //   3. Attend    — not yet attended (no chief_complaint and is_done != 1)
        //   4. Done ✓    — attended, all re-checks done (or none scheduled)
        //
        // Clicking any state opens the Attend form so the nurse can act.
        // Re-check count badge appended to Done label (e.g. "✔ Done (2✓)").
        colAction.setCellFactory(col -> new TableCell<>() {
            final Button btn = new Button();
            {
                btn.setOnAction(e -> {
                    Map<String, String> row = getTableView().getItems().get(getIndex());
                    openAttendForm(row);
                });
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }

                Map<String, String> row    = getTableView().getItems().get(getIndex());
                String visitIdStr          = row.getOrDefault("visit_id", "0");
                int    rowVisitId;
                try   { rowVisitId = Integer.parseInt(visitIdStr); }
                catch (NumberFormatException ex) { rowVisitId = 0; }

                final int fid = rowVisitId;

                // All pending follow-ups for this visit (already cached in pendingFollowUps)
                List<Map<String, String>> pending = pendingFollowUps.stream()
                        .filter(fu -> fid != 0 &&
                                fu.getOrDefault("visit_id", "0").equals(String.valueOf(fid)))
                        .collect(java.util.stream.Collectors.toList());

                // Earliest pending re-check for this visit
                Map<String, String> earliest = pending.stream()
                        .min((a, b) -> a.getOrDefault("scheduled_time", "")
                                .compareTo(b.getOrDefault("scheduled_time", "")))
                        .orElse(null);

                if (earliest != null) {
                    // ── States 1 & 2: pending re-check exists ────
                    String sched        = earliest.getOrDefault("scheduled_time", "");
                    int    minsUntil    = minutesUntil(sched);
                    long   pendingCount = pending.size();

                    String countLabel = pendingCount > 1
                            ? " (" + pendingCount + ")" : "";

                    if (minsUntil <= 0) {
                        // OVERDUE — at or past scheduled time
                        // Show exactly when it was due so the nurse knows at a glance
                        btn.setText("🔴 OVERDUE " + sched + countLabel);
                        btn.setStyle(
                                "-fx-background-color: #c0392b; -fx-text-fill: white;" +
                                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11;" +
                                        "-fx-font-weight: bold;");
                        btn.setTooltip(new Tooltip(
                                "Re-check was due at " + sched + " — click to attend now"));
                    } else {
                        // DUE / UPCOMING
                        String timeLabel = minsUntil <= 10
                                ? "Due in " + minsUntil + " min"
                                : "Re-check " + sched;
                        btn.setText("🔄 " + timeLabel + countLabel);
                        btn.setStyle(
                                "-fx-background-color: #e67e22; -fx-text-fill: white;" +
                                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11;" +
                                        "-fx-font-weight: bold;");
                        btn.setTooltip(new Tooltip(
                                "Re-check scheduled at " + sched + " — click to open"));
                    }

                } else if (!isDone(row)) {
                    // ── State 3: not yet attended ─────────────────
                    btn.setText("Attend");
                    btn.setStyle(
                            "-fx-background-color: #8B0000; -fx-text-fill: white;" +
                                    "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11;");
                    btn.setTooltip(new Tooltip("Click to record this patient's visit"));

                } else {
                    // ── State 4: attended, all re-checks done ─────
                    // Count completed re-checks from the visit row if available,
                    // or leave blank when none were ever scheduled.
                    String doneExtra = row.containsKey("done_recheck_count") &&
                            !row.getOrDefault("done_recheck_count","0").equals("0")
                            ? " (" + row.get("done_recheck_count") + "✓)" : "";
                    btn.setText("✔ Done" + doneExtra);
                    btn.setStyle(
                            "-fx-background-color: #27ae60; -fx-text-fill: white;" +
                                    "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11;" +
                                    "-fx-font-weight: bold;");
                    btn.setTooltip(new Tooltip("Attended — click to view or edit record"));
                }

                setGraphic(btn);
            }
        });

        // Empty state
        tblLogbook.setPlaceholder(new Label("No check-ins for this period."));

        // Double-click to open Attend form
        tblLogbook.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Map<String, String> row = tblLogbook.getSelectionModel().getSelectedItem();
                if (row != null) openAttendForm(row);
            }
        });
    }

    /**
     * Returns how many minutes from now until the given "HH:mm" time today.
     * Negative means the time has already passed.
     */
    private int minutesUntil(String hhMm) {
        try {
            LocalTime target = LocalTime.parse(hhMm, DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime now    = LocalTime.now();
            return (int) java.time.Duration.between(now, target).toMinutes();
        } catch (Exception ex) {
            return Integer.MAX_VALUE;
        }
    }

    private boolean isDone(Map<String, String> row) {
        String complaint = row.getOrDefault("chief_complaint", "");
        String isDone    = row.getOrDefault("is_done", "0");
        return (complaint != null && !complaint.isEmpty()) || "1".equals(isDone);
    }

    // ── Search ────────────────────────────────────────────────────
    @FXML
    private void onSearchKeyReleased() {
        String query = txtSearch.getText().trim();
        if (query.length() >= 2) onSearch();
        else if (query.isEmpty()) {
            searchResultsPane.setVisible(false);
            searchResultsPane.setManaged(false);
        }
    }

    @FXML
    private void onSearch() {
        String query = txtSearch.getText().trim();
        if (query.isEmpty()) return;
        List<Patient> results = DatabaseManager.searchPatients(query);
        if (results.isEmpty()) { lblCheckInStatus.setText("No patients found."); return; }
        tblSearchResults.setItems(FXCollections.observableArrayList(results));
        searchResultsPane.setVisible(true);
        searchResultsPane.setManaged(true);
        lblCheckInStatus.setText("");
    }

    private void selectPatient(Patient p) {
        selectedPatient = p;
        lblSelectedName.setText(p.getFullName());
        lblSelectedId.setText("ID: " + p.getPatientId());
        searchResultsPane.setVisible(false);
        searchResultsPane.setManaged(false);
        selectedPatientPane.setVisible(true);
        selectedPatientPane.setManaged(true);
        txtSearch.clear();
        lblCheckInStatus.setText("");
    }

    @FXML
    private void setCurrentTime() {
        txtTimeIn.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    @FXML
    private void onConfirmCheckIn() {
        if (selectedPatient == null) {
            lblCheckInStatus.setText("Please select a patient first."); return;
        }
        String timeIn = txtTimeIn.getText().trim();
        if (timeIn.isEmpty()) {
            lblCheckInStatus.setText("Please enter time in."); return;
        }
        String purpose = rbMedCert.isSelected() ? "Med Cert" : "Health-Related";
        boolean success = DatabaseManager.checkInPatient(
                selectedPatient.getPatientId(), timeIn, purpose);
        if (success) {
            lblCheckInStatus.setText("✔ " + selectedPatient.getFullName() + " checked in!");
            lblCheckInStatus.setStyle("-fx-text-fill: green; -fx-font-size: 11;");
            selectedPatient = null;
            selectedPatientPane.setVisible(false);
            selectedPatientPane.setManaged(false);
            refreshLogbook();
        } else {
            lblCheckInStatus.setText("Check-in failed. Try again.");
            lblCheckInStatus.setStyle("-fx-text-fill: #8B0000; -fx-font-size: 11;");
        }
    }

    // ── Attend form ───────────────────────────────────────────────
    private void openAttendForm(Map<String, String> visitRow) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/cjcschoolclinicmanagementsystem/View/attend-view.fxml"));
            Parent root = loader.load();
            AttendController controller = loader.getController();
            controller.setVisitData(visitRow);

            Stage stage = new Stage();
            stage.setTitle("Attend — " + visitRow.get("full_name"));
            javafx.geometry.Rectangle2D screen =
                    javafx.stage.Screen.getPrimary().getVisualBounds();
            double w = screen.getWidth()  * 0.80;
            double h = screen.getHeight() * 0.88;
            stage.setScene(new Scene(root, w, h));
            stage.setMinWidth(640);
            stage.setMinHeight(540);
            stage.setResizable(true);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // After closing the form, refresh so the badge + button update immediately
            refreshLogbook();
        } catch (Exception e) {
            System.err.println("Attend form error: " + e.getMessage());
        }
    }

    // ── Filters ───────────────────────────────────────────────────
    @FXML private void clearDateFilter() {
        if (dpFrom != null) dpFrom.setValue(null);
        if (dpTo   != null) dpTo.setValue(null);
        filterToday();
    }

    @FXML private void filterToday() {
        currentFilter = "daily";
        updateFilterButtons(btnToday);
        lblLogbookTitle.setText("Today's Visitors");
        refreshLogbook();
    }
    @FXML private void filterWeekly() {
        currentFilter = "weekly";
        updateFilterButtons(btnWeekly);
        lblLogbookTitle.setText("This Week's Visitors");
        refreshLogbook();
    }
    @FXML private void filterMonthly() {
        currentFilter = "monthly";
        updateFilterButtons(btnMonthly);
        lblLogbookTitle.setText("This Month's Visitors");
        refreshLogbook();
    }
    @FXML private void filterAll() {
        currentFilter = "all";
        updateFilterButtons(btnAll);
        lblLogbookTitle.setText("All Visitors");
        refreshLogbook();
    }
    @FXML private void filterCustomRange() {
        if (dpFrom == null || dpTo == null) return;
        LocalDate from = dpFrom.getValue();
        LocalDate to   = dpTo.getValue();
        if (from == null || to == null) return;
        allLogbookData = DatabaseManager.getVisitsByDateRange(from.toString(), to.toString());
        lblLogbookTitle.setText("Visitors: " + from + " → " + to);
        updateFilterButtons(null);
        currentPage = 0;
        updatePagination();
    }

    private void updateFilterButtons(Button active) {
        String on  = "-fx-background-color: #8B0000; -fx-text-fill: white;" +
                "-fx-background-radius: 20; -fx-cursor: hand; -fx-font-size: 12;";
        String off = "-fx-background-color: #e0e0e0; -fx-text-fill: #555;" +
                "-fx-background-radius: 20; -fx-cursor: hand; -fx-font-size: 12;";
        if (btnToday   != null) btnToday.setStyle(off);
        if (btnWeekly  != null) btnWeekly.setStyle(off);
        if (btnMonthly != null) btnMonthly.setStyle(off);
        if (btnAll     != null) btnAll.setStyle(off);
        if (active     != null) active.setStyle(on);
    }

    // ── Refresh + Pagination ──────────────────────────────────────
    @FXML
    public void refreshLogbook() {
        allLogbookData  = DatabaseManager.getAllVisits(currentFilter);

        // Refresh the pending follow-ups cache so badges are up-to-date
        // (only today's visits are in scope — no performance concern)
        pendingFollowUps = DatabaseManager.getPendingFollowUpsToday();

        currentPage = 0;
        updatePagination();
    }

    private void updatePagination() {
        int total      = allLogbookData.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        currentPage    = Math.max(0, Math.min(currentPage, totalPages - 1));

        int from = currentPage * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);

        ObservableList<Map<String, String>> page =
                FXCollections.observableArrayList(allLogbookData.subList(from, to));
        tblLogbook.setItems(page);
        tblLogbook.refresh();   // force cell re-render so badge colours update

        if (lblPage != null)
            lblPage.setText("Page " + (currentPage + 1) + " of " + totalPages);
        if (btnPrev != null) btnPrev.setDisable(currentPage == 0);
        if (btnNext != null) btnNext.setDisable(currentPage >= totalPages - 1);

        lblVisitorCount.setText(total + " patient" + (total != 1 ? "s" : ""));
    }

    @FXML
    private void markAllDone() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Set All Time-Out");
        confirm.setHeaderText("Set time-out for all remaining patients?");
        confirm.setContentText(
                "This will record the current time as Time Out for every patient\n" +
                        "today who has no Time Out yet. This cannot be undone.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                int count = DatabaseManager.bulkTimeOutToday();
                Alert done = new Alert(Alert.AlertType.INFORMATION);
                done.setTitle("Done");
                done.setHeaderText(null);
                done.setContentText(count + " patient" + (count != 1 ? "s" : "") +
                        " marked as done.");
                done.showAndWait();
                refreshLogbook();
            }
        });
    }

    @FXML private void goToPrevPage() {
        if (currentPage > 0) { currentPage--; updatePagination(); }
    }
    @FXML private void goToNextPage() {
        int totalPages = Math.max(1, (int) Math.ceil((double) allLogbookData.size() / PAGE_SIZE));
        if (currentPage < totalPages - 1) { currentPage++; updatePagination(); }
    }
}