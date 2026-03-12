package com.example.cjcschoolclinicmanagementsystem.controller;

import com.example.cjcschoolclinicmanagementsystem.database.DatabaseManager;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class DashboardController {

    @FXML private BorderPane mainBorderPane;
    @FXML private VBox       sidebar;
    @FXML private ImageView  logoImg;
    @FXML private Label      lblClinicName;
    @FXML private Label      lblSubtitle;
    @FXML private Label      lblDashboardTitle;
    @FXML private MenuButton btnExport;
    private javafx.scene.Node dashboardCenter;

    // ── Nav buttons (all 5 need fx:id for highlight) ──────────────
    @FXML private Button btnDashboard;
    @FXML private Button btnPatientRecords;
    @FXML private Button btnLogbook;
    @FXML private Button btnVisitHistory;
    @FXML private Button btnSettings;

    // ── Stat-card labels ──────────────────────────────────────────
    @FXML private Label lblToday;
    @FXML private Label lblWeekly;
    @FXML private Label lblMonthly;
    @FXML private Label lblTotalPatients;
    @FXML private Label lblUnattended;

    // ── Charts ────────────────────────────────────────────────────
    @FXML private VBox                       vboxCollegeBreakdown;
    @FXML private BarChart<String, Number>   chartIllnesses;
    @FXML private LineChart<String, Number>  chartMonthly;
    @FXML private BarChart<String, Number>   chartCollege;
    @FXML private PieChart                   chartPurpose;

    // ── Filters ───────────────────────────────────────────────────
    @FXML private ComboBox<String> cmbIllnessFilter;

    // ── College color palette ─────────────────────────────────────
    private static final String[] COLLEGE_ORDER  =
            {"CCIS", "COE", "CHS", "CABE", "CEDAS", "CSP", "BED", "Employees"};
    private static final String[] COLLEGE_COLORS =
            {"#1976D2", "#FF9800", "#E91E63", "#FDD835", "#8B0000", "#388E3C", "#D32F2F", "#7B1FA2"};

    // ── Nav style constants ───────────────────────────────────────
    // Default: transparent bg, white text
    private static final String NAV_DEFAULT =
            "-fx-background-color: transparent; -fx-text-fill: white;" +
                    "-fx-alignment: BASELINE_LEFT; -fx-cursor: hand;" +
                    "-fx-font-size: 13; -fx-font-weight: bold;" +
                    "-fx-background-radius: 8; -fx-padding: 10 12 10 12;";

    // Active: white/light-gray bg, dark-red text
    private static final String NAV_ACTIVE =
            "-fx-background-color: #f0f0f0; -fx-text-fill: #8B0000;" +
                    "-fx-alignment: BASELINE_LEFT; -fx-cursor: hand;" +
                    "-fx-font-size: 13; -fx-font-weight: bold;" +
                    "-fx-background-radius: 8; -fx-padding: 10 12 10 12;";

    // ─────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        setupFilters();
        loadDashboard();
        dashboardCenter = mainBorderPane.getCenter();

        // Dashboard is the default active screen on load
        setActiveNav(btnDashboard);

        javafx.application.Platform.runLater(() -> {
            if (btnExport != null) {
                btnExport.lookupAll(".label").forEach(n ->
                        n.setStyle("-fx-text-fill: white; -fx-font-size: 12;"));
                btnExport.lookupAll(".arrow-button").forEach(n ->
                        n.setStyle("-fx-background-color: transparent;"));
                btnExport.lookupAll(".arrow").forEach(n ->
                        n.setStyle("-fx-background-color: white;"));
            }
            setupResponsiveBindings();
        });
    }

    // ── Active nav highlight ──────────────────────────────────────
    /**
     * Resets all nav buttons to default style, then applies the active
     * style to whichever button was just clicked.
     */
    private void setActiveNav(Button active) {
        Button[] navButtons = {
                btnDashboard, btnPatientRecords, btnLogbook, btnVisitHistory, btnSettings
        };
        for (Button btn : navButtons) {
            if (btn != null) btn.setStyle(NAV_DEFAULT);
        }
        if (active != null) active.setStyle(NAV_ACTIVE);
    }

    // ── Responsive bindings ───────────────────────────────────────
    private void setupResponsiveBindings() {
        if (mainBorderPane.getScene() == null) return;

        javafx.scene.Scene scene = mainBorderPane.getScene();

        sidebar.prefWidthProperty().bind(
                Bindings.createDoubleBinding(
                        () -> Math.max(80, Math.min(260, scene.getWidth() * 0.18)),
                        scene.widthProperty()));

        lblClinicName.visibleProperty().bind(
                Bindings.createBooleanBinding(
                        () -> scene.getWidth() >= 960, scene.widthProperty()));
        lblClinicName.managedProperty().bind(lblClinicName.visibleProperty());

        // Subtitle hides when sidebar is narrow (same threshold as name)
        lblSubtitle.visibleProperty().bind(lblClinicName.visibleProperty());
        lblSubtitle.managedProperty().bind(lblSubtitle.visibleProperty());

        logoImg.fitWidthProperty().bind(
                Bindings.createDoubleBinding(
                        () -> scene.getWidth() < 960 ? 32.0 : 48.0,
                        scene.widthProperty()));
        logoImg.fitHeightProperty().bind(logoImg.fitWidthProperty());

        lblDashboardTitle.styleProperty().bind(
                Bindings.createStringBinding(
                        () -> {
                            double fs = Math.max(16, Math.min(22, scene.getWidth() * 0.018));
                            return "-fx-font-size: " + fs + "; -fx-font-weight: bold; -fx-text-fill: #8B0000;";
                        },
                        scene.widthProperty()));
    }

    // ── Filters ───────────────────────────────────────────────────
    private void setupFilters() {
        cmbIllnessFilter.setItems(FXCollections.observableArrayList("Daily", "Weekly", "Monthly"));
        cmbIllnessFilter.setValue("Weekly");
    }

    // ── Master load ───────────────────────────────────────────────
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

    // ── ROW 2: Illness charts ─────────────────────────────────────
    private void loadIllnessChart(String period) {
        chartIllnesses.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Diagnoses");

        Map<String, Integer> data = DatabaseManager.getTopIllnesses(period);
        if (data.isEmpty()) {
            series.getData().add(new XYChart.Data<>("No data yet", 0));
        } else {
            data.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(5)
                    .forEach(e -> series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));
        }

        chartIllnesses.getData().add(series);

        javafx.application.Platform.runLater(() ->
                chartIllnesses.lookupAll(".bar").forEach(node ->
                        node.setStyle("-fx-background-color: #8B0000; -fx-background-radius: 3;")));
    }

    private void loadIllnessTrendChart() {
        chartMonthly.getData().clear();

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

        javafx.application.Platform.runLater(() ->
                chartMonthly.lookupAll(".chart-series-line").forEach(node ->
                        node.setStyle("-fx-stroke: #8B0000; -fx-stroke-width: 2;")));
    }

    // ── ROW 3: College bar chart ──────────────────────────────────
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

            final String c   = color;
            final int    idx = i;
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
    @FXML private void refreshDashboard() { loadDashboard(); }

    @FXML
    private void onIllnessFilterChanged() {
        String selected = cmbIllnessFilter.getValue();
        if (selected != null) loadIllnessChart(selected.toLowerCase());
    }

    @FXML private void exportDaily()   { exportDashboardData("daily"); }
    @FXML private void exportWeekly()  { exportDashboardData("weekly"); }
    @FXML private void exportMonthly() { exportDashboardData("monthly"); }

    private void exportDashboardData(String period) {
        Window owner = mainBorderPane != null && mainBorderPane.getScene() != null
                ? mainBorderPane.getScene().getWindow() : null;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Dashboard Data");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        chooser.setInitialFileName("dashboard_" + period + "_" + stamp + ".csv");

        java.io.File selected = chooser.showSaveDialog(owner);
        if (selected == null) return;

        boolean ok = DatabaseManager.exportDashboardToCSV(selected.getAbsolutePath(), period);
        if (ok) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Export Successful");
            a.setHeaderText(null);
            a.setContentText("Dashboard data exported to:\n" + selected.getAbsolutePath());
            a.showAndWait();
        } else {
            showError("Export Failed", "Could not export dashboard data.");
        }
    }

    // ── Nav ───────────────────────────────────────────────────────
    @FXML private void showDashboard() {
        setActiveNav(btnDashboard);
        mainBorderPane.setCenter(dashboardCenter);
        loadDashboard();
    }

    @FXML private void showPatientRegistry() {
        setActiveNav(btnPatientRecords);
        loadView("patient-registry-view.fxml");
    }

    @FXML private void showLogbook() {
        setActiveNav(btnLogbook);
        loadView("logbook-view.fxml");
    }

    @FXML private void showVisitHistory() {
        setActiveNav(btnVisitHistory);
        loadView("visithistory-view.fxml");
    }

    @FXML private void handleLogout() { System.exit(0); }

    @FXML
    private void openSettings() {
        setActiveNav(btnSettings);
        try {
            String fxmlPath = "/com/example/cjcschoolclinicmanagementsystem/View/settings-view.fxml";
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                resource = getClass().getClassLoader().getResource(
                        "com/example/cjcschoolclinicmanagementsystem/View/settings-view.fxml");
            }
            if (resource == null) {
                showError("File Not Found", "Could not locate settings-view.fxml");
                setActiveNav(null); // clear highlight if settings failed to open
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Settings");
            javafx.geometry.Rectangle2D screen =
                    javafx.stage.Screen.getPrimary().getVisualBounds();
            double w = Math.min(960, screen.getWidth()  * 0.82);
            double h = Math.min(740, screen.getHeight() * 0.88);
            stage.setScene(new javafx.scene.Scene(root, w, h));
            stage.setMinWidth(760);
            stage.setMinHeight(580);
            stage.setResizable(true);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
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