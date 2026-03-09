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

    private static final int    PAGE_SIZE   = 25;

    private int currentPage = 0;
    private List<Map<String, String>> allLogbookData = new ArrayList<>();

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
        colTimeIn.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOrDefault("time_in", "")));
        colPurpose.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOrDefault("purpose", "")));
        colTimeOut.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOrDefault("time_out", "—")));

        // Single Attend / ✔ Done button — always opens the attend form
        // Red = not yet attended, Green = record saved (still editable)
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
                Map<String, String> row = getTableView().getItems().get(getIndex());
                boolean done = isDone(row);
                if (done) {
                    btn.setText("✔ Done");
                    btn.setStyle(
                            "-fx-background-color: #27ae60; -fx-text-fill: white;" +
                                    "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11;" +
                                    "-fx-font-weight: bold;");
                    btn.setTooltip(new Tooltip("Attended — click to view or edit record"));
                } else {
                    btn.setText("Attend");
                    btn.setStyle(
                            "-fx-background-color: #8B0000; -fx-text-fill: white;" +
                                    "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11;");
                    btn.setTooltip(new Tooltip("Click to record this patient's visit"));
                }
                setGraphic(btn);
            }
        });
    }

    /**
     * A visit is "done" when the nurse has saved a full attend record
     * (diagnosis/complaint filled) OR manually flagged it via is_done.
     */
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
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // After closing the form, refresh so the button colour updates immediately
            refreshLogbook();
        } catch (Exception e) {
            System.err.println("Attend form error: " + e.getMessage());
        }
    }

    // ── Filters ───────────────────────────────────────────────────
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
        lblLogbookTitle.setText("Visitors: " + from + " \u2192 " + to);
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
        allLogbookData = DatabaseManager.getAllVisits(currentFilter);
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
        tblLogbook.refresh();

        if (lblPage != null)
            lblPage.setText("Page " + (currentPage + 1) + " of " + totalPages);
        if (btnPrev != null) btnPrev.setDisable(currentPage == 0);
        if (btnNext != null) btnNext.setDisable(currentPage >= totalPages - 1);

        lblVisitorCount.setText(total + " patient" + (total != 1 ? "s" : ""));
    }

    @FXML private void goToPrevPage() {
        if (currentPage > 0) { currentPage--; updatePagination(); }
    }
    @FXML private void goToNextPage() {
        int totalPages = Math.max(1, (int) Math.ceil((double) allLogbookData.size() / PAGE_SIZE));
        if (currentPage < totalPages - 1) { currentPage++; updatePagination(); }
    }
}