package com.example.cjcschoolclinicmanagementsystem.controller;

import com.example.cjcschoolclinicmanagementsystem.database.DatabaseManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VisitHistoryController {

    @FXML private TextField txtSearch;
    @FXML private TableView<Map<String, String>> tblVisits;
    @FXML private TableColumn<Map<String, String>, String> colDate;
    @FXML private TableColumn<Map<String, String>, String> colPatientId;
    @FXML private TableColumn<Map<String, String>, String> colName;
    @FXML private TableColumn<Map<String, String>, String> colTimeIn;
    @FXML private TableColumn<Map<String, String>, String> colTimeOut;
    @FXML private TableColumn<Map<String, String>, String> colPurpose;
    @FXML private TableColumn<Map<String, String>, String> colDiagnosis;
    @FXML private TableColumn<Map<String, String>, String> colAction;
    @FXML private Label lblCount;
    @FXML private Button btnDaily;
    @FXML private Button btnWeekly;
    @FXML private Button btnMonthly;
    @FXML private Button btnAll;
    @FXML private DatePicker dpFrom;
    @FXML private DatePicker dpTo;

    @FXML private Button btnSort;
    private boolean sortNewestFirst = true;

    // ── Pagination ────────────────────────────────────────────────
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Label  lblPage;

    private static final int    PAGE_SIZE     = 35;
    private int currentPage = 0;

    private List<Map<String, String>> allData;
    private String currentFilter = "daily";

    @FXML
    public void initialize() {
        setupTable();
        refreshTable();

        // Bind column widths as % of table — responsive
        javafx.application.Platform.runLater(() -> {
            if (tblVisits.getScene() != null) bindColumnWidths();
            else tblVisits.sceneProperty().addListener((obs, ov, nv) -> {
                if (nv != null) bindColumnWidths();
            });
        });
    }

    private void bindColumnWidths() {
        javafx.beans.binding.DoubleBinding w = tblVisits.widthProperty().subtract(20);
        colDate      .prefWidthProperty().bind(w.multiply(0.10));
        colPatientId .prefWidthProperty().bind(w.multiply(0.11));
        colName      .prefWidthProperty().bind(w.multiply(0.18));
        colTimeIn    .prefWidthProperty().bind(w.multiply(0.08));
        colTimeOut   .prefWidthProperty().bind(w.multiply(0.08));
        colPurpose   .prefWidthProperty().bind(w.multiply(0.11));
        colDiagnosis .prefWidthProperty().bind(w.multiply(0.18));
        colAction    .prefWidthProperty().bind(w.multiply(0.16));
    }

    private void setupTable() {
        colDate.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOrDefault("visit_date", "")));
        colPatientId.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOrDefault("patient_id", "")));
        colName.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOrDefault("full_name", "")));
        colTimeIn.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOrDefault("time_in", "")));
        colTimeOut.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOrDefault("time_out", "—")));
        colPurpose.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOrDefault("purpose", "")));
        colDiagnosis.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOrDefault("diagnosis", "—")));

        // Action column: View + Edit + Delete
        colAction.setCellFactory(col -> new TableCell<>() {
            final Button btnView   = new Button("View");
            final Button btnEdit   = new Button("Edit");
            final Button btnDelete = new Button("Delete");
            final HBox box = new HBox(4, btnView, btnEdit, btnDelete);
            {
                btnView.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;" +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 10;");
                btnEdit.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white;" +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 10;");
                btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;" +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 10;");

                btnView.setOnAction(e -> {
                    Map<String, String> row = getTableView().getItems().get(getIndex());
                    openAttendForm(row, true);
                });
                btnEdit.setOnAction(e -> {
                    Map<String, String> row = getTableView().getItems().get(getIndex());
                    openAttendForm(row, false);
                });
                btnDelete.setOnAction(e -> {
                    Map<String, String> row = getTableView().getItems().get(getIndex());
                    confirmDelete(row);
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void openAttendForm(Map<String, String> row, boolean readOnly) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/cjcschoolclinicmanagementsystem/View/attend-view.fxml"));
            Parent root = loader.load();
            AttendController controller = loader.getController();
            controller.setVisitData(row);
            if (readOnly) controller.setReadOnly(true);

            Stage stage = new Stage();
            stage.setTitle((readOnly ? "View" : "Edit") + " Visit — " + row.get("full_name"));
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            refreshTable();
        } catch (Exception e) {
            e.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error Opening Form");
            alert.setHeaderText("Could not open the attend form.");
            alert.setContentText(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
            alert.showAndWait();
        }
    }

    private void confirmDelete(Map<String, String> row) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Visit Record");
        alert.setHeaderText("Are you sure you want to delete this record?");
        alert.setContentText("Patient: " + row.get("full_name") +
                "\nDate: " + row.get("visit_date"));
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                DatabaseManager.deleteVisit(
                        Integer.parseInt(row.get("visit_id")));
                refreshTable();
            }
        });
    }

    @FXML public void refreshTable() {
        allData = DatabaseManager.getAllVisits(currentFilter);
        currentPage = 0;
        applySearch();
    }

    @FXML private void onSearch() { currentPage = 0; applySearch(); }

    private void applySearch() {
        String query = txtSearch.getText().trim().toLowerCase();
        List<Map<String, String>> filtered = allData.stream()
                .filter(row ->
                        query.isEmpty() ||
                                row.getOrDefault("full_name", "").toLowerCase().contains(query) ||
                                row.getOrDefault("diagnosis", "").toLowerCase().contains(query) ||
                                row.getOrDefault("course_dept", "").toLowerCase().contains(query) ||
                                row.getOrDefault("patient_id", "").toLowerCase().contains(query))
                .collect(Collectors.toList());

        applyPagination(filtered);
    }

    private void applyPagination(List<Map<String, String>> data) {
        int total      = data.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        currentPage    = Math.max(0, Math.min(currentPage, totalPages - 1));

        int from = currentPage * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);

        tblVisits.setItems(FXCollections.observableArrayList(data.subList(from, to)));

        if (lblPage != null)
            lblPage.setText("Page " + (currentPage + 1) + " of " + totalPages);
        if (btnPrev != null)
            btnPrev.setDisable(currentPage == 0);
        if (btnNext != null)
            btnNext.setDisable(currentPage >= totalPages - 1);

        lblCount.setText(total + " record" + (total != 1 ? "s" : ""));
    }

    @FXML private void goToPrevPage() {
        if (currentPage > 0) { currentPage--; applySearch(); }
    }

    @FXML private void goToNextPage() {
        // re-derive total to check bounds
        String query = txtSearch.getText().trim().toLowerCase();
        long total = allData.stream().filter(row ->
                query.isEmpty() ||
                        row.getOrDefault("full_name","").toLowerCase().contains(query) ||
                        row.getOrDefault("diagnosis","").toLowerCase().contains(query) ||
                        row.getOrDefault("course_dept","").toLowerCase().contains(query) ||
                        row.getOrDefault("patient_id","").toLowerCase().contains(query)).count();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        if (currentPage < totalPages - 1) { currentPage++; applySearch(); }
    }

    @FXML private void clearDateFilter() {
        if (dpFrom != null) dpFrom.setValue(null);
        if (dpTo   != null) dpTo.setValue(null);
        filterDaily();
    }

    @FXML private void filterDaily() {
        currentFilter = "daily"; currentPage = 0; updateFilterButtons(btnDaily); refreshTable(); }
    @FXML private void filterWeekly() {
        currentFilter = "weekly"; currentPage = 0; updateFilterButtons(btnWeekly); refreshTable(); }
    @FXML private void filterMonthly() {
        currentFilter = "monthly"; currentPage = 0; updateFilterButtons(btnMonthly); refreshTable(); }
    @FXML private void filterAll() {
        currentFilter = "all"; currentPage = 0; updateFilterButtons(btnAll); refreshTable(); }

    @FXML private void filterCustomRange() {
        if (dpFrom.getValue() == null || dpTo.getValue() == null) return;
        allData = DatabaseManager.getVisitsByDateRange(
                dpFrom.getValue().toString(), dpTo.getValue().toString());
        currentPage = 0;
        applySearch();
        updateFilterButtons(null);
    }

    private void updateFilterButtons(Button active) {
        String on  = "-fx-background-color: #8B0000; -fx-text-fill: white;" +
                "-fx-background-radius: 20; -fx-cursor: hand; -fx-font-size: 12;";
        String off = "-fx-background-color: #e0e0e0; -fx-text-fill: #555;" +
                "-fx-background-radius: 20; -fx-cursor: hand; -fx-font-size: 12;";
        btnDaily.setStyle(off); btnWeekly.setStyle(off);
        btnMonthly.setStyle(off); btnAll.setStyle(off);
        if (active != null) active.setStyle(on);
    }

    @FXML
    private void toggleSort() {
        sortNewestFirst = !sortNewestFirst;
        btnSort.setText(sortNewestFirst ? "↓ Newest First" : "↑ Oldest First");

        if (allData != null) {
            allData.sort((a, b) -> {
                String dateA = a.getOrDefault("visit_date", "") +
                        a.getOrDefault("time_in", "");
                String dateB = b.getOrDefault("visit_date", "") +
                        b.getOrDefault("time_in", "");
                return sortNewestFirst
                        ? dateB.compareTo(dateA)
                        : dateA.compareTo(dateB);
            });
            applySearch();
        }
    }
}