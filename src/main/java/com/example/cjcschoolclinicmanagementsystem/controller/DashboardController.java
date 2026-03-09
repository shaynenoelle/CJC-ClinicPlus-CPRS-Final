package com.example.cjcschoolclinicmanagementsystem.controller;

import com.example.cjcschoolclinicmanagementsystem.database.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class DashboardController {

    @FXML private BorderPane mainBorderPane;
    @FXML private MenuButton btnExport;
    private javafx.scene.Node dashboardCenter;

    // ── Stat-card labels ──────────────────────────────────────────
    @FXML private Label lblToday;
    @FXML private Label lblWeekly;
    @FXML private Label lblMonthly;
    @FXML private Label lblTotalPatients;
    @FXML private Label lblUnattended;       // NEW — checked-in but not yet attended

    // ── Charts ────────────────────────────────────────────────────
    @FXML private VBox    vboxCollegeBreakdown;
    @FXML private BarChart<String, Number>   chartIllnesses;  // Category on X, Number on Y
    @FXML private LineChart<String, Number>  chartMonthly;    // illness trend over 8 weeks
    @FXML private BarChart<String, Number>   chartCollege;    // visits by college, colored bars
    @FXML private PieChart                   chartPurpose;

    // ── Filters ───────────────────────────────────────────────────

    @FXML private ComboBox<String> cmbIllnessFilter;

    // ── College color palette ─────────────────────────────────────
    // CCIS=blue, COE=orange, CHS=pink, CABE=yellow, CEDAS=maroon, CSP=green, BED=red, Employees=purple
    private static final String[] COLLEGE_ORDER  =
            {"CCIS", "COE", "CHS", "CABE", "CEDAS", "CSP", "BED", "Employees"};
    private static final String[] COLLEGE_COLORS =
            {"#1976D2", "#FF9800", "#E91E63", "#FDD835", "#8B0000", "#388E3C", "#D32F2F", "#7B1FA2"};

    @FXML
    public void initialize() {
        setupFilters();
        loadDashboard();
        dashboardCenter = mainBorderPane.getCenter();

        // Force white text/arrow on MenuButton after render
        javafx.application.Platform.runLater(() -> {
            if (btnExport != null) {
                btnExport.lookupAll(".label").forEach(n ->
                        n.setStyle("-fx-text-fill: white; -fx-font-size: 12;"));
                btnExport.lookupAll(".arrow-button").forEach(n ->
                        n.setStyle("-fx-background-color: transparent;"));
                btnExport.lookupAll(".arrow").forEach(n ->
                        n.setStyle("-fx-background-color: white;"));
            }
        });
    }

    private void setupFilters() {
        cmbIllnessFilter.setItems(FXCollections.observableArrayList("Daily", "Weekly", "Monthly"));
        cmbIllnessFilter.setValue("Weekly");

    }

    // ── Main load ─────────────────────────────────────────────────
    private void loadDashboard() {
        loadCountCards();
        loadTotalPatients();
        loadIllnessChart("weekly");
        loadIllnessTrendChart();
        loadCollegeBarChart();
        loadPurposeChart();
    }

    // ── ROW 1: Stat cards ─────────────────────────────────────────
    private void loadCountCards() {
        lblToday  .setText(String.valueOf(DatabaseManager.getVisitCount("today")));
        lblWeekly .setText(String.valueOf(DatabaseManager.getVisitCount("weekly")));
        lblMonthly.setText(String.valueOf(DatabaseManager.getVisitCount("monthly")));

        // Unattended = checked in today but chief_complaint is still empty
        int unattended = DatabaseManager.getUnattendedToday();
        lblUnattended.setText(String.valueOf(unattended));
    }

    private void loadTotalPatients() {
        int total = DatabaseManager.getTotalPatients();
        lblTotalPatients.setText(String.valueOf(total));

        vboxCollegeBreakdown.getChildren().clear();
        Map<String, Integer> byCollege = DatabaseManager.getPatientsByCollege();
        byCollege.forEach((college, count) -> {
            Label lbl = new Label(college + ": " + count);
            lbl.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");
            vboxCollegeBreakdown.getChildren().add(lbl);
        });
    }

    // ── ROW 2: Hero illness charts ────────────────────────────────

    /**
     * Horizontal bar chart — categories on Y axis, count on X axis.
     * Sorted by count descending so the longest bar is at the top.
     */
    private void loadIllnessChart(String period) {
        chartIllnesses.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Diagnoses");

        Map<String, Integer> data = DatabaseManager.getTopIllnesses(period);
        if (data.isEmpty()) {
            series.getData().add(new XYChart.Data<>("No data yet", 0));
        } else {
            // Sort descending, limit to top 5
            data.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(5)
                    .forEach(e -> series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));
        }

        chartIllnesses.getData().add(series);

        // Style bars maroon after render
        javafx.application.Platform.runLater(() ->
                chartIllnesses.lookupAll(".bar").forEach(node ->
                        node.setStyle("-fx-background-color: #8B0000; -fx-background-radius: 3;")));
    }

    /**
     * Illness trend — line chart over the last 8 weeks.
     * Each series is a top diagnosis plotted week-by-week.
     */
    private void loadIllnessTrendChart() {
        chartMonthly.getData().clear();

        // Get weekly visit counts (existing method) as a simple total trend
        Map<String, Integer> weeklyData = DatabaseManager.getVisitsByMonth();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Total Visits");

        if (weeklyData.isEmpty()) {
            series.getData().add(new XYChart.Data<>("No data", 0));
        } else {
            weeklyData.forEach((month, count) ->
                    series.getData().add(new XYChart.Data<>(month, count)));
        }
        chartMonthly.getData().add(series);

        // Style line red
        javafx.application.Platform.runLater(() ->
                chartMonthly.lookupAll(".chart-series-line").forEach(node ->
                        node.setStyle("-fx-stroke: #8B0000; -fx-stroke-width: 2;")));
    }

    // ── ROW 3: College bar chart ──────────────────────────────────

    /**
     * Colored bar chart — one series per college so each bar gets its own color.
     * Order: CCIS, COE, CHS, CABE, CEDAS, CSP, BED, Employees
     */
    private void loadCollegeBarChart() {
        chartCollege.getData().clear();

        Map<String, Integer> data = DatabaseManager.getPatientsByCollege();

        for (int i = 0; i < COLLEGE_ORDER.length; i++) {
            String college = COLLEGE_ORDER[i];
            String color   = COLLEGE_COLORS[i];
            int    count   = data.getOrDefault(college, 0);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(college);
            series.getData().add(new XYChart.Data<>(college, count));
            chartCollege.getData().add(series);

            // Apply color after nodes are created
            final String c = color;
            final int idx  = i;
            javafx.application.Platform.runLater(() -> {
                chartCollege.getData().get(idx).getData().forEach(d -> {
                    if (d.getNode() != null)
                        d.getNode().setStyle("-fx-background-color: " + c +
                                "; -fx-background-radius: 3;");
                });
            });
        }
    }

    // ── Purpose pie ───────────────────────────────────────────────
    private void loadPurposeChart() {
        chartPurpose.getData().clear();
        Map<String, Integer> data = DatabaseManager.getVisitPurposeCount();
        if (data.isEmpty()) {
            chartPurpose.getData().add(new PieChart.Data("No data yet", 1));
            return;
        }
        data.forEach((purpose, count) ->
                chartPurpose.getData().add(
                        new PieChart.Data(purpose + " (" + count + ")", count)));
    }

    // ── FXML event handlers ───────────────────────────────────────
    @FXML private void refreshDashboard()       { loadDashboard(); }

    @FXML
    private void onIllnessFilterChanged() {
        String selected = cmbIllnessFilter.getValue();
        if (selected != null) loadIllnessChart(selected.toLowerCase());
    }

    // ── Nav ───────────────────────────────────────────────────────
    @FXML private void showDashboard() {
        mainBorderPane.setCenter(dashboardCenter);
        loadDashboard();
    }
    @FXML private void showPatientRegistry() { loadView("patient-registry-view.fxml"); }
    @FXML private void showLogbook()         { loadView("logbook-view.fxml"); }
    @FXML private void showVisitHistory()    { loadView("visithistory-view.fxml"); }
    @FXML private void handleLogout()        { System.exit(0); }

    @FXML
    private void openSettings() {
        try {
            String fxmlPath = "/com/example/cjcschoolclinicmanagementsystem/View/settings-view.fxml";
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                resource = getClass().getClassLoader().getResource(
                        "com/example/cjcschoolclinicmanagementsystem/View/settings-view.fxml");
            }
            if (resource == null) {
                showError("File Not Found", "Could not locate settings-view.fxml");
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Settings");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setWidth(900);
            stage.setHeight(700);
            stage.showAndWait();
        } catch (Exception e) {
            showError("Error", "Failed to open settings:\n" + e.getMessage());
        }
    }

    private void loadView(String fxmlFile) {
        try {
            String path = "/com/example/cjcschoolclinicmanagementsystem/View/" + fxmlFile;
            URL resource = getClass().getResource(path);
            if (resource == null) { showError("Not Found", "Could not find: " + path); return; }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();
            mainBorderPane.setCenter(view);
        } catch (IOException e) {
            showError("Loading Error", "Could not load " + fxmlFile);
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}