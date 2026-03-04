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
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class DashboardController {

    @FXML private BorderPane mainBorderPane;
    @FXML private MenuButton btnExport;
    private javafx.scene.Node dashboardCenter;

    // Dashboard widgets
    @FXML private Label lblToday;
    @FXML private Label lblWeekly;
    @FXML private Label lblMonthly;
    @FXML private Label lblTotalPatients;
    @FXML private VBox vboxCollegeBreakdown;
    @FXML private PieChart chartCollege;
    @FXML private BarChart<String, Number> chartIllnesses;
    @FXML private LineChart<String, Number> chartMonthly;
    @FXML private PieChart chartPurpose;
    @FXML private ComboBox<String> cmbCheckInFilter;
    @FXML private ComboBox<String> cmbIllnessFilter;

    @FXML
    public void initialize() {
        setupFilters();
        loadDashboard();
        dashboardCenter = mainBorderPane.getCenter();
        // Force white text on MenuButton label (JavaFX requires post-render lookup)
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
        cmbIllnessFilter.setItems(FXCollections.observableArrayList(
                "Daily", "Weekly", "Monthly"));
        cmbIllnessFilter.setValue("Weekly");

        cmbCheckInFilter.setItems(FXCollections.observableArrayList(
                "Today", "Weekly", "Monthly"));
        cmbCheckInFilter.setValue("Today");
    }

    private void loadDashboard() {
        loadCountCards();
        loadTotalPatients();
        loadCollegeChart();
        loadIllnessChart("weekly");
        loadMonthlyChart();
        loadPurposeChart();
    }

    private void loadCountCards() {
        lblToday.setText(String.valueOf(
                DatabaseManager.getVisitCount("today")));
        lblWeekly.setText(String.valueOf(
                DatabaseManager.getVisitCount("weekly")));
        lblMonthly.setText(String.valueOf(
                DatabaseManager.getVisitCount("monthly")));
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

    private void loadCollegeChart() {
        chartCollege.getData().clear();
        Map<String, Integer> data = DatabaseManager.getPatientsByCollege();
        if (data.isEmpty()) {
            chartCollege.getData().add(
                    new PieChart.Data("No data yet", 1));
            return;
        }
        data.forEach((college, count) ->
                chartCollege.getData().add(
                        new PieChart.Data(college + " (" + count + ")", count)));
    }

    private void loadIllnessChart(String period) {
        chartIllnesses.getData().clear();
        Map<String, Integer> data = DatabaseManager.getTopIllnesses(period);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Illnesses");

        if (data.isEmpty()) {
            series.getData().add(
                    new XYChart.Data<>("No data yet", 0));
        } else {
            data.forEach((illness, count) ->
                    series.getData().add(
                            new XYChart.Data<>(illness, count)));
        }
        chartIllnesses.getData().add(series);
    }

    private void loadMonthlyChart() {
        chartMonthly.getData().clear();
        Map<String, Integer> data = DatabaseManager.getVisitsByMonth();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Visits");

        if (data.isEmpty()) {
            series.getData().add(new XYChart.Data<>("No data", 0));
        } else {
            data.forEach((month, count) ->
                    series.getData().add(new XYChart.Data<>(month, count)));
        }
        chartMonthly.getData().add(series);
    }

    private void loadPurposeChart() {
        chartPurpose.getData().clear();
        Map<String, Integer> data = DatabaseManager.getVisitPurposeCount();
        if (data.isEmpty()) {
            chartPurpose.getData().add(
                    new PieChart.Data("No data yet", 1));
            return;
        }
        data.forEach((purpose, count) ->
                chartPurpose.getData().add(
                        new PieChart.Data(purpose + " (" + count + ")", count)));
    }

    @FXML
    private void refreshDashboard() {
        loadDashboard();
    }

    @FXML
    private void onCheckInFilterChanged() {
        // Count cards always show all three periods
        // This filter can be used for future drill-down
        loadCountCards();
    }

    @FXML
    private void onIllnessFilterChanged() {
        String selected = cmbIllnessFilter.getValue();
        if (selected == null) return;
        loadIllnessChart(selected.toLowerCase());
    }

    @FXML
    private void showDashboard() {
        // Restore the original dashboard center (keeps @FXML bindings intact)
        mainBorderPane.setCenter(dashboardCenter);
        loadDashboard();
    }

    @FXML
    private void showPatientRegistry() {
        loadView("patient-registry-view.fxml");
    }

    @FXML
    private void showLogbook() {
        loadView("logbook-view.fxml");
    }

    @FXML
    private void showVisitHistory() {
        loadView("visithistory-view.fxml");
    }

    @FXML
    private void handleLogout() {
        System.exit(0);
    }

    @FXML
    private void openSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/cjcschoolclinicmanagementsystem/View/settings-view.fxml"));
            Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Settings");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            showError("Error", "Failed to open settings: " + e.getMessage());
        }
    }

    private void loadView(String fxmlFile) {
        try {
            String path = "/com/example/cjcschoolclinicmanagementsystem/View/" + fxmlFile;
            URL resource = getClass().getResource(path);
            if (resource == null) {
                showError("Not Found", "Could not find: " + path);
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();
            mainBorderPane.setCenter(view);
        } catch (IOException e) {
            showError("Loading Error", "Could not load " + fxmlFile);
            System.err.println("FXML Load Error: " + e.getMessage());
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