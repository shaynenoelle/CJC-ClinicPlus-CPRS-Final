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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

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

    private Patient selectedPatient = null;
    private String currentFilter = "daily";

    @FXML
    public void initialize() {
        setupSearchTable();
        setupLogbookTable();
        setCurrentTime();
        refreshLogbook();
    }

    private void setupSearchTable() {
        colSearchName.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getFullName()));
        colSearchId.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getPatientId()));

        // Select button column
        colSearchAction.setCellFactory(col -> new TableCell<>() {
            final Button btn = new Button("Select");
            {
                btn.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white;" +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11;");
                btn.setOnAction(e -> {
                    Patient p = getTableView().getItems().get(getIndex());
                    selectPatient(p);
                });
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

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
                new SimpleStringProperty(
                        d.getValue().getOrDefault("time_out", "—")));

        // Attend button column — color changes if already attended
        colAction.setCellFactory(col -> new TableCell<>() {
            final Button btn = new Button("Attend");
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
                boolean attended = row.get("diagnosis") != null &&
                        !row.get("diagnosis").isEmpty();
                btn.setStyle(attended
                        ? "-fx-background-color: #4CAF50; -fx-text-fill: white;" +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11;"
                        : "-fx-background-color: #8B0000; -fx-text-fill: white;" +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11;");
                btn.setText(attended ? "✔ Done" : "Attend");
                setGraphic(btn);
            }
        });
    }

    @FXML
    private void onSearchKeyReleased() {
        String query = txtSearch.getText().trim();
        if (query.length() >= 2) {
            onSearch();
        } else if (query.isEmpty()) {
            searchResultsPane.setVisible(false);
            searchResultsPane.setManaged(false);
        }
    }

    @FXML
    private void onSearch() {
        String query = txtSearch.getText().trim();
        if (query.isEmpty()) return;

        List<Patient> results = DatabaseManager.searchPatients(query);
        if (results.isEmpty()) {
            lblCheckInStatus.setText("No patients found.");
            return;
        }
        tblSearchResults.setItems(FXCollections.observableArrayList(results));
        searchResultsPane.setVisible(true);
        searchResultsPane.setManaged(true);
        lblCheckInStatus.setText("");
    }

    private void selectPatient(Patient p) {
        selectedPatient = p;
        lblSelectedName.setText(p.getFullName());
        lblSelectedId.setText("ID: " + p.getPatientId());

        // Hide search, show selected panel
        searchResultsPane.setVisible(false);
        searchResultsPane.setManaged(false);
        selectedPatientPane.setVisible(true);
        selectedPatientPane.setManaged(true);
        txtSearch.clear();
        lblCheckInStatus.setText("");
    }

    @FXML
    private void setCurrentTime() {
        String now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        txtTimeIn.setText(now);
    }

    @FXML
    private void onConfirmCheckIn() {
        if (selectedPatient == null) {
            lblCheckInStatus.setText("Please select a patient first.");
            return;
        }
        String timeIn = txtTimeIn.getText().trim();
        if (timeIn.isEmpty()) {
            lblCheckInStatus.setText("Please enter time in.");
            return;
        }
        String purpose = rbMedCert.isSelected() ? "Med Cert" : "Health-Related";

        boolean success = DatabaseManager.checkInPatient(
                selectedPatient.getPatientId(), timeIn, purpose);

        if (success) {
            lblCheckInStatus.setText("✔ " + selectedPatient.getFullName() + " checked in!");
            lblCheckInStatus.setStyle("-fx-text-fill: green; -fx-font-size: 11;");
            // Reset
            selectedPatient = null;
            selectedPatientPane.setVisible(false);
            selectedPatientPane.setManaged(false);
            refreshLogbook();
        } else {
            lblCheckInStatus.setText("Check-in failed. Try again.");
            lblCheckInStatus.setStyle("-fx-text-fill: #8B0000; -fx-font-size: 11;");
        }
    }

    private void openAttendForm(Map<String, String> visitRow) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/cjcschoolclinicmanagementsystem/View/attend-view.fxml"));
            Parent root = loader.load();

            AttendController controller = loader.getController();
            controller.setVisitData(visitRow);

            Stage stage = new Stage();
            stage.setTitle("Attend Patient — " + visitRow.get("full_name"));
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            refreshLogbook(); // refresh after attending
        } catch (Exception e) {
            System.err.println("Attend form error: " + e.getMessage());
        }
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

    private void updateFilterButtons(Button active) {
        String activeStyle = "-fx-background-color: #8B0000; -fx-text-fill: white;" +
                "-fx-background-radius: 20; -fx-cursor: hand; -fx-font-size: 12;";
        String inactiveStyle = "-fx-background-color: #e0e0e0; -fx-text-fill: #555;" +
                "-fx-background-radius: 20; -fx-cursor: hand; -fx-font-size: 12;";
        btnToday.setStyle(inactiveStyle);
        btnWeekly.setStyle(inactiveStyle);
        btnMonthly.setStyle(inactiveStyle);
        active.setStyle(activeStyle);
    }

    @FXML
    public void refreshLogbook() {
        List<Map<String, String>> visits = DatabaseManager.getAllVisits(currentFilter);
        ObservableList<Map<String, String>> data = FXCollections.observableArrayList(visits);
        tblLogbook.setItems(data);
        lblVisitorCount.setText(visits.size() + " patient" + (visits.size() != 1 ? "s" : ""));
    }
}