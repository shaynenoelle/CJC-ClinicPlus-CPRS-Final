package com.example.cjcschoolclinicmanagementsystem.controller;

import com.example.cjcschoolclinicmanagementsystem.database.DatabaseManager;
import com.example.cjcschoolclinicmanagementsystem.util.PreferencesManager;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.print.PrinterJob;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

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
    @FXML private StackPane loadingOverlay;
    @FXML private ProgressIndicator loadingSpinner;
    private javafx.scene.Node dashboardCenter;

    // ── Nav buttons (all 5 need fx:id for highlight) ──────────────
    @FXML private Button btnDashboard;
    @FXML private Button btnPatientRecords;
    @FXML private Button btnLogbook;
    @FXML private Button btnVisitHistory;
    @FXML private Button btnSettings;

    // ── Stat-card labels ──────────────────────────────────────────
    @FXML private Label lblTotalVisits;       // period-driven (replaces Today/Weekly/Monthly)
    @FXML private Label lblVisitsCardTitle;   // e.g. "VISITS THIS WEEK"
    @FXML private Label lblVisitsCardSub;     // e.g. "this week"
    @FXML private Label lblTotalPatients;
    @FXML private Label lblUnattended;

    // ── Live queue panel ──────────────────────────────────────────
    @FXML private VBox  vboxQueueRows;
    @FXML private Label lblQueueEmpty;

    // ── Pending re-checks panel (injected below queue) ────────────
    @FXML private VBox  vboxRecheckRows;
    @FXML private Label lblRecheckEmpty;
    @FXML private Label lblRecheckHeader;
    @FXML private Label lblPendingRechecks;  // stat card — count of pending re-checks today

    // ── Charts ────────────────────────────────────────────────────
    @FXML private VBox                       vboxCollegeBreakdown;
    @FXML private BarChart<String, Number>   chartIllnesses;
    @FXML private LineChart<String, Number>  chartMonthly;
    @FXML private BarChart<String, Number>   chartCollege;
    @FXML private PieChart                   chartPurpose;

    // ── UNIFIED period filter (controls Rows 2 & 3) ───────────────
    @FXML private ComboBox<String> cmbPeriod;

    // ── Chart subtitle labels (update when period changes) ────────
    @FXML private Label lblIllnessSubtitle;
    @FXML private Label lblTrendSubtitle;
    @FXML private Label lblCollegeSubtitle;
    @FXML private Label lblPurposeSubtitle;

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
                    "-fx-font-size: 13; -fx-font-weight: normal;" +
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
        // Ensure loading overlay is visible at start
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(true);
            loadingOverlay.setManaged(true);
        }

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

            // Hide loading overlay with fade-out animation after a brief delay
            if (loadingOverlay != null && loadingOverlay.isVisible()) {
                // Wait 300ms to ensure loading screen is visible, then fade out
                PauseTransition pause = new PauseTransition(Duration.millis(300));
                pause.setOnFinished(e -> {
                    FadeTransition fadeOut = new FadeTransition(
                            Duration.millis(300), loadingOverlay);
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    fadeOut.setCycleCount(1);
                    fadeOut.setAutoReverse(false);
                    fadeOut.setOnFinished(finish -> {
                        loadingOverlay.setVisible(false);
                        loadingOverlay.setManaged(false);
                    });
                    fadeOut.play();
                });
                pause.play();
            }
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
        cmbPeriod.setItems(FXCollections.observableArrayList("Daily", "Weekly", "Monthly"));
        cmbPeriod.setValue("Weekly");
    }

    /** Returns lowercase period string from the unified dropdown. */
    private String currentPeriod() {
        String v = cmbPeriod.getValue();
        return v != null ? v.toLowerCase() : "weekly";
    }

    /** Human-readable label for chart subtitles. */
    private String periodLabel() {
        return switch (currentPeriod()) {
            case "daily"   -> "Today";
            case "monthly" -> "This Month";
            default        -> "This Week";
        };
    }

    // ── Master load ───────────────────────────────────────────────
    private void loadDashboard() {
        loadStatCards();   // Row 1 + queue panel
        loadCharts();      // Rows 2 & 3 — driven by cmbPeriod
    }

    // ── Rows 2 & 3: all four charts — driven by unified dropdown ──
    private void loadCharts() {
        String period = currentPeriod();
        String label  = periodLabel();
        loadIllnessChart(period, label);
        loadIllnessTrendChart(period, label);
        loadCollegeBarChart(period, label);
        loadPurposeChart(period, label);
    }

    // ── ROW 1: Stat cards — Visits follows period; others fixed ───
    private void loadStatCards() {
        // Period-driven visit count
        String period = currentPeriod();
        int visits = DatabaseManager.getVisitCount(period);
        lblTotalVisits.setText(String.valueOf(visits));

        // Update card title and subtitle to match period
        String title = switch (period) {
            case "daily"   -> "VISITS TODAY";
            case "monthly" -> "VISITS THIS MONTH";
            default        -> "VISITS THIS WEEK";
        };
        String sub = switch (period) {
            case "daily"   -> "check-ins today";
            case "monthly" -> "this month";
            default        -> "this week";
        };
        lblVisitsCardTitle.setText(title);
        lblVisitsCardSub.setText(sub);

        // Fixed cards
        lblTotalPatients.setText(String.valueOf(DatabaseManager.getTotalPatients()));
        lblUnattended.setText(String.valueOf(DatabaseManager.getUnattendedToday()));

        // Live queue panel
        loadQueuePanel();
        // Pending re-checks panel (below queue)
        loadRecheckPanel();
    }

    // ── Live queue panel ──────────────────────────────────────────
    private void loadQueuePanel() {
        vboxQueueRows.getChildren().clear();
        java.util.List<java.util.Map<String, String>> queue =
                DatabaseManager.getQueueToday();

        if (queue.isEmpty()) {
            lblQueueEmpty.setVisible(true);
            lblQueueEmpty.setManaged(true);
        } else {
            lblQueueEmpty.setVisible(false);
            lblQueueEmpty.setManaged(false);

            for (java.util.Map<String, String> row : queue) {
                String name      = row.getOrDefault("full_name",  "Unknown");
                String timeIn    = row.getOrDefault("time_in",    "—");
                String waiting   = row.getOrDefault("waiting",    "");
                String allergies = row.getOrDefault("allergies",  "");
                String conditions= row.getOrDefault("conditions", "");

                // Build one queue row
                javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(10);
                hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                hbox.setStyle("-fx-background-color: #fafafa; -fx-border-color: #eeeeee; " +
                        "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 12;");

                Label lblName = new Label("👤 " + name);
                lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #333;");
                lblName.setMinWidth(200);

                Label lblTime = new Label("In: " + timeIn);
                lblTime.setStyle("-fx-font-size: 11; -fx-text-fill: #777;");
                lblTime.setMinWidth(80);

                Label lblWait = new Label(waiting.isEmpty() ? "" : "⏱ " + waiting);
                lblWait.setStyle("-fx-font-size: 11; -fx-text-fill: #e65100; -fx-font-weight: bold;");
                lblWait.setMinWidth(90);

                hbox.getChildren().addAll(lblName, lblTime, lblWait);

                // Allergy/condition flags — shown in red if present
                boolean hasFlag = (!allergies.isBlank() && !allergies.equals("None") && !allergies.equals("—"))
                        || (!conditions.isBlank() && !conditions.equals("None") && !conditions.equals("—"));
                if (hasFlag) {
                    StringBuilder flagText = new StringBuilder("⚠ ");
                    if (!allergies.isBlank() && !allergies.equals("None") && !allergies.equals("—"))
                        flagText.append("Allergies: ").append(allergies).append("  ");
                    if (!conditions.isBlank() && !conditions.equals("None") && !conditions.equals("—"))
                        flagText.append("Conditions: ").append(conditions);
                    Label lblFlag = new Label(flagText.toString().trim());
                    lblFlag.setStyle("-fx-font-size: 10; -fx-text-fill: #c62828; " +
                            "-fx-font-weight: bold; -fx-wrap-text: true;");
                    lblFlag.setMaxWidth(280);
                    hbox.getChildren().add(lblFlag);
                }

                // ── → Attend button (pushed to the far right) ─────────────
                if (!row.getOrDefault("visit_id", "").isEmpty()) {
                    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                    javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                    Button btnAttend = new Button("→ Attend");
                    btnAttend.setStyle(
                            "-fx-background-color: #8B0000; -fx-text-fill: white;" +
                                    "-fx-background-radius: 4; -fx-cursor: hand;" +
                                    "-fx-font-size: 11; -fx-font-weight: bold;");
                    final java.util.Map<String, String> rowRef = row;
                    btnAttend.setOnAction(ev -> openAttendFormFromDashboard(rowRef));
                    hbox.getChildren().addAll(spacer, btnAttend);
                }

                vboxQueueRows.getChildren().add(hbox);
            }
        }
    }


    // ── ROW 2: Illness charts ─────────────────────────────────────
    /**
     * Shows patients who have a pending re-check today.
     * Uses the same DB method (getPendingFollowUpsToday) that LogbookController
     * uses for its badge cache — no new query needed.
     *
     * Rows are color-coded:
     *   Red   — scheduled time has already passed (overdue)
     *   Orange — due within 10 minutes
     *   Gray  — scheduled later today
     *
     * This panel is intentionally separate from the waiting queue because
     * re-check patients are already attended — they are being monitored,
     * not waiting in line.
     */
    private void loadRecheckPanel() {
        // Guard: FXML nodes are optional — dashboard-view.fxml must declare them
        // before this method does anything visible. If they are absent (e.g. during
        // a staged rollout), the method exits silently without crashing.
        if (vboxRecheckRows == null) return;

        vboxRecheckRows.getChildren().clear();

        java.util.List<java.util.Map<String, String>> followUps =
                DatabaseManager.getPendingFollowUpsToday();

        // ── Update the stat card immediately — even if the panel VBox is absent ──
        if (lblPendingRechecks != null)
            lblPendingRechecks.setText(String.valueOf(followUps.size()));

        boolean hasRows = !followUps.isEmpty();

        if (lblRecheckEmpty != null) {
            lblRecheckEmpty.setVisible(!hasRows);
            lblRecheckEmpty.setManaged(!hasRows);
        }

        // Update header count badge
        if (lblRecheckHeader != null && hasRows) {
            lblRecheckHeader.setText("🔄  Pending Re-checks  (" + followUps.size() + ")");
        }

        if (!hasRows) return;

        java.time.LocalTime now = java.time.LocalTime.now();
        java.time.format.DateTimeFormatter fmt =
                java.time.format.DateTimeFormatter.ofPattern("HH:mm");

        for (java.util.Map<String, String> fu : followUps) {
            String name      = fu.getOrDefault("full_name",      "Unknown");
            String sched     = fu.getOrDefault("scheduled_time", "—");
            String notes     = fu.getOrDefault("nurse_notes",    "");

            // Calculate how many minutes remain (negative = overdue)
            int minsUntil = Integer.MAX_VALUE;
            try {
                java.time.LocalTime target = java.time.LocalTime.parse(sched, fmt);
                minsUntil = (int) java.time.Duration.between(now, target).toMinutes();
            } catch (Exception ignored) {}

            // Choose row color and status text based on urgency
            String rowBg;
            String statusText;
            String statusStyle;

            if (minsUntil <= 0) {
                rowBg       = "#fff0f0";
                statusText  = "🔴 OVERDUE  " + sched;
                statusStyle = "-fx-font-size: 11; -fx-text-fill: #c0392b; -fx-font-weight: bold;";
            } else if (minsUntil <= 10) {
                rowBg       = "#fff8f0";
                statusText  = "🔄 Due in " + minsUntil + " min  (" + sched + ")";
                statusStyle = "-fx-font-size: 11; -fx-text-fill: #e67e22; -fx-font-weight: bold;";
            } else {
                rowBg       = "#fafafa";
                statusText  = "🕐 Scheduled: " + sched;
                statusStyle = "-fx-font-size: 11; -fx-text-fill: #555;";
            }

            javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(10);
            hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            hbox.setStyle(
                    "-fx-background-color: " + rowBg + "; " +
                            "-fx-border-color: #eeeeee; " +
                            "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 12;");

            Label lblName = new Label("👤 " + name);
            lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #333;");
            lblName.setMinWidth(200);

            Label lblStatus = new Label(statusText);
            lblStatus.setStyle(statusStyle);
            lblStatus.setMinWidth(180);

            hbox.getChildren().addAll(lblName, lblStatus);

            // Nurse note (if any) — shown in muted text
            if (!notes.isBlank()) {
                Label lblNote = new Label("📝 " + notes);
                lblNote.setStyle("-fx-font-size: 10; -fx-text-fill: #888; -fx-wrap-text: true;");
                lblNote.setMaxWidth(280);
                hbox.getChildren().add(lblNote);
            }

            vboxRecheckRows.getChildren().add(hbox);
        }
    }
    // ── Change 1: open attend form directly from the dashboard queue ─
    private void openAttendFormFromDashboard(java.util.Map<String, String> visitRow) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/cjcschoolclinicmanagementsystem/View/attend-view.fxml"));
            Parent root = loader.load();
            AttendController controller = loader.getController();
            controller.setVisitData(visitRow);

            Stage stage = new Stage();
            stage.setTitle("Attend — " + visitRow.getOrDefault("full_name", ""));
            javafx.geometry.Rectangle2D screen =
                    javafx.stage.Screen.getPrimary().getVisualBounds();
            double w = Math.min(820, screen.getWidth()  * 0.75);
            double h = Math.min(700, screen.getHeight() * 0.88);
            stage.setScene(new Scene(root, w, h));
            stage.setMinWidth(640);
            stage.setMinHeight(540);
            stage.setResizable(true);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadDashboard();   // refresh queue + stat cards after attending
        } catch (Exception e) {
            showError("Attend Form Error",
                    "Could not open attend form: " +
                            (e.getMessage() != null ? e.getMessage() : e.getClass().getName()));
        }
    }

    // ── Change 2: print all four charts — uses JavaFX PrinterJob ─
    // No external library needed. On Windows, choosing "Microsoft Print to PDF"
    // in the print dialog saves directly as a PDF file.
    @FXML
    private void exportChartsPDF() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            showError("Print / PDF Export",
                    "No printer found.\n" +
                            "On Windows, install 'Microsoft Print to PDF' via Settings → Printers.");
            return;
        }

        Window owner = mainBorderPane != null && mainBorderPane.getScene() != null
                ? mainBorderPane.getScene().getWindow() : null;

        // Show the OS print dialog (user selects "Microsoft Print to PDF" to get a PDF)
        boolean proceed = job.showPrintDialog(owner);
        if (!proceed) {
            job.cancelJob();
            return;
        }

        // Use the printer's default page layout — printPage(Node) with no
        // PageLayout argument applies the printer's own default automatically.
        javafx.scene.Node[] charts = {
                chartIllnesses, chartMonthly, chartCollege, chartPurpose
        };
        String[] titles = {
                "Top Diagnoses", "Illness Trend",
                "Visits by College / Dept", "Purpose of Visit"
        };

        boolean allOk = true;
        for (int i = 0; i < charts.length; i++) {
            if (!job.printPage(charts[i])) {
                allOk = false;
                showError("Print Error", "Failed to print chart: " + titles[i]);
                break;
            }
        }

        if (allOk) job.endJob();
        else       job.cancelJob();
    }

    private void loadIllnessChart(String period, String label) {
        chartIllnesses.getData().clear();
        lblIllnessSubtitle.setText("Most frequent diagnoses — " + label);

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

    private void loadIllnessTrendChart(String period, String label) {
        chartMonthly.getData().clear();

        String subtitleDetail = switch (period) {
            case "daily"   -> "Hourly visits today";
            case "monthly" -> "Monthly visits this year";
            default        -> "Weekly visits over the past 8 weeks";
        };
        lblTrendSubtitle.setText(subtitleDetail);
        ((CategoryAxis) chartMonthly.getXAxis()).setLabel(
                switch (period) { case "daily" -> "Hour"; case "monthly" -> "Month"; default -> "Week"; });

        Map<String, Integer> data = DatabaseManager.getVisitTrendByPeriod(period);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Visits — " + label);

        if (data.isEmpty()) {
            series.getData().add(new XYChart.Data<>("No data", 0));
        } else {
            data.forEach((k, v) -> series.getData().add(new XYChart.Data<>(k, v)));
        }
        chartMonthly.getData().add(series);

        javafx.application.Platform.runLater(() ->
                chartMonthly.lookupAll(".chart-series-line").forEach(node ->
                        node.setStyle("-fx-stroke: #8B0000; -fx-stroke-width: 2;")));
    }

    // ── ROW 3: College bar chart ──────────────────────────────────
    private void loadCollegeBarChart(String period, String label) {
        chartCollege.getData().clear();
        vboxCollegeBreakdown.getChildren().clear();
        lblCollegeSubtitle.setText("Visits by department — " + label);

        Map<String, Integer> data = DatabaseManager.getVisitsByCollegePeriod(period);

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
                if (idx < chartCollege.getData().size()) {
                    chartCollege.getData().get(idx).getData().forEach(d -> {
                        if (d.getNode() != null)
                            d.getNode().setStyle("-fx-background-color: " + c +
                                    "; -fx-background-radius: 3;");
                    });
                }
            });

            // Breakdown text below chart (only depts with visits)
            if (count > 0) {
                Label lbl = new Label(college + ": " + count);
                lbl.setStyle("-fx-font-size: 10; -fx-text-fill: #666;");
                vboxCollegeBreakdown.getChildren().add(lbl);
            }
        }
    }

    // ── Purpose pie ───────────────────────────────────────────────
    private void loadPurposeChart(String period, String label) {
        chartPurpose.getData().clear();
        lblPurposeSubtitle.setText("Health-Related vs Med Cert — " + label);
        Map<String, Integer> data = DatabaseManager.getVisitPurposeCount(period);
        if (data.isEmpty()) {
            chartPurpose.getData().add(new PieChart.Data("No data yet", 1));
            return;
        }
        data.forEach((purpose, count) ->
                chartPurpose.getData().add(
                        new PieChart.Data(purpose + " (" + count + ")", count)));
    }

    // ── FXML event handlers ───────────────────────────────────────
    @FXML private void refreshDashboard() { loadDashboard(); loadRecheckPanel(); }

    /** Called when the unified period dropdown changes — reloads stat cards AND charts. */
    @FXML
    private void onPeriodChanged() {
        loadStatCards();  // visits card title/number follows period
        loadCharts();
    }

    @FXML private void exportDaily()   { exportDashboardData("daily"); }
    @FXML private void exportWeekly()  { exportDashboardData("weekly"); }
    @FXML private void exportMonthly() { exportDashboardData("monthly"); }
    private void exportDashboardData(String period) {
        // Sync the dropdown and charts to match the export period
        String label = switch (period) {
            case "daily"   -> "Daily";
            case "monthly" -> "Monthly";
            default        -> "Weekly";
        };
        cmbPeriod.setValue(label);
        loadCharts();  // refresh charts so what's on screen = what's in the export

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

    @FXML private void handleLogout() {
        // Confirm before signing out
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Sign Out");
        confirm.setHeaderText("Are you sure you want to sign out?");
        confirm.setContentText("Any unsaved changes will be lost.");
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        java.util.Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.YES) return;

        // Clear saved credentials on logout
        PreferencesManager.clearCredentials();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/cjcschoolclinicmanagementsystem/View/login-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) mainBorderPane.getScene().getWindow();

            // Return to login as a clean fixed-size centered window — NOT maximized.
            // Mirrors HelloApplication.start() so the window is always consistent.
            Scene scene = new Scene(root, 960, 600);

            stage.setResizable(false);
            stage.setMaximized(false);
            stage.setScene(scene);
            stage.setMinWidth(0);
            stage.setMinHeight(0);
            stage.setTitle("CJC School Clinic - Login");

            // Center on screen — works on any monitor size
            javafx.geometry.Rectangle2D screenBounds =
                    javafx.stage.Screen.getPrimary().getVisualBounds();
            stage.setX((screenBounds.getWidth()  - 960) / 2);
            stage.setY((screenBounds.getHeight() - 600) / 2);

            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading login view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void openSettings() {
        setActiveNav(btnSettings);
        loadView("settings-view.fxml");
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