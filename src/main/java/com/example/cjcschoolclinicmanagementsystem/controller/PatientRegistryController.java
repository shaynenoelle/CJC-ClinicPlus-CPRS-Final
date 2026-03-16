package com.example.cjcschoolclinicmanagementsystem.controller;

import com.example.cjcschoolclinicmanagementsystem.database.DatabaseManager;
import com.example.cjcschoolclinicmanagementsystem.model.Patient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PatientRegistryController {

    // ── FXML bindings ────────────────────────────────────────────
    @FXML private TableView<Patient> patientTable;

    // New column set — matches clinic Google Sheet layout
    @FXML private TableColumn<Patient,String> colID, colLastName, colFirstName, colMI;
    @FXML private TableColumn<Patient,String> colGender, colBirthdate;
    @FXML private TableColumn<Patient,String> colYear, colCourse, colDept, colType;
    @FXML private TableColumn<Patient,String> colContact, colMedHistory;
    @FXML private TableColumn<Patient,String> colStatus, colAction;

    @FXML private TextField        txtSearch;
    @FXML private ComboBox<String> cmbCollege, cmbType, cmbYear, cmbSort;
    @FXML private Label            lblTotal, lblPage;
    @FXML private Button           btnPrev, btnNext, btnShowArchived;

    // ── State ─────────────────────────────────────────────────────
    private ObservableList<Patient> allPatients = FXCollections.observableArrayList();
    private ObservableList<Patient> pageItems   = FXCollections.observableArrayList();
    private boolean showArchived = false;
    private static final int PAGE_SIZE = 50;
    private int currentPage = 1;
    private int totalPages  = 1;

    // ── Init ──────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        setupColumns();
        setupFilters();
        refreshTable();

        // Bind column widths as percentages of table width — responsive layout
        javafx.application.Platform.runLater(() -> {
            if (patientTable.getScene() != null) {
                bindColumnWidths();
            } else {
                patientTable.sceneProperty().addListener((obs, ov, nv) -> {
                    if (nv != null) bindColumnWidths();
                });
            }
        });
    }

    private void bindColumnWidths() {
        javafx.beans.binding.DoubleBinding w = patientTable.widthProperty()
                .subtract(20); // subtract scrollbar width
        colID         .prefWidthProperty().bind(w.multiply(0.085));
        colLastName   .prefWidthProperty().bind(w.multiply(0.085));
        colFirstName  .prefWidthProperty().bind(w.multiply(0.080));
        colMI         .prefWidthProperty().bind(w.multiply(0.033));
        colGender     .prefWidthProperty().bind(w.multiply(0.050));
        colBirthdate  .prefWidthProperty().bind(w.multiply(0.068));
        colYear       .prefWidthProperty().bind(w.multiply(0.052));
        colCourse     .prefWidthProperty().bind(w.multiply(0.075));
        colDept       .prefWidthProperty().bind(w.multiply(0.068));
        colType       .prefWidthProperty().bind(w.multiply(0.055));
        colContact    .prefWidthProperty().bind(w.multiply(0.082));
        colMedHistory .prefWidthProperty().bind(w.multiply(0.125));
        colStatus     .prefWidthProperty().bind(w.multiply(0.055));
        colAction     .prefWidthProperty().bind(w.multiply(0.187));
    }

    // ── Column setup ──────────────────────────────────────────────
    private void setupColumns() {
        colID.setCellValueFactory(d -> d.getValue().idProperty());

        // Split "Last, First MI" stored in name field → separate columns
        colLastName.setCellValueFactory(d -> {
            String full = d.getValue().getName();
            if (full != null && full.contains(","))
                return new javafx.beans.property.SimpleStringProperty(full.split(",")[0].trim());
            return new javafx.beans.property.SimpleStringProperty(full == null ? "" : full);
        });

        colFirstName.setCellValueFactory(d -> {
            String full = d.getValue().getName();
            if (full != null && full.contains(",")) {
                String rest = full.split(",", 2)[1].trim();
                // Strip trailing MI (≤3 chars ending with ".")
                if (rest.contains(" ")) {
                    String possible = rest.substring(rest.lastIndexOf(" ") + 1);
                    if (possible.length() <= 3 && (possible.endsWith(".") || possible.matches("[A-Z]")))
                        return new javafx.beans.property.SimpleStringProperty(
                                rest.substring(0, rest.lastIndexOf(" ")).trim());
                }
                return new javafx.beans.property.SimpleStringProperty(rest);
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        colMI.setCellValueFactory(d -> {
            String full = d.getValue().getName();
            if (full != null && full.contains(",")) {
                String rest = full.split(",", 2)[1].trim();
                if (rest.contains(" ")) {
                    String possible = rest.substring(rest.lastIndexOf(" ") + 1);
                    if (possible.length() <= 3 && (possible.endsWith(".") || possible.matches("[A-Z]")))
                        return new javafx.beans.property.SimpleStringProperty(possible);
                }
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        colGender   .setCellValueFactory(d -> d.getValue().genderProperty());
        colBirthdate.setCellValueFactory(d -> d.getValue().birthdateProperty());
        colYear     .setCellValueFactory(d -> d.getValue().yearProperty());
        // college field = course name (BSCS, BSN, Grade 10)
        colCourse   .setCellValueFactory(d -> d.getValue().collegeProperty());
        // course field = college/dept abbreviation (CCIS, CHS, BED)
        colDept     .setCellValueFactory(d -> d.getValue().courseProperty());
        colType     .setCellValueFactory(d -> d.getValue().typeProperty());
        colContact  .setCellValueFactory(d -> d.getValue().contactProperty());

        // Medical History = Allergies + Pre-existing Conditions combined
        colMedHistory.setCellValueFactory(d -> {
            String a = d.getValue().getAllergies();
            String c = d.getValue().getConditions();
            String combined = "";
            if (a != null && !a.isBlank()) combined += a;
            if (c != null && !c.isBlank()) combined += (combined.isBlank() ? "" : " | ") + c;
            return new javafx.beans.property.SimpleStringProperty(combined.isBlank() ? "—" : combined);
        });
        // Wrap text in the medical history cell
        colMedHistory.setCellFactory(col -> new TableCell<>() {
            private final Label lbl = new Label();
            { lbl.setWrapText(true); lbl.setStyle("-fx-font-size: 11;"); setGraphic(lbl); }
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                lbl.setText(empty || s == null ? "" : s);
            }
        });

        // Status — colored text
        colStatus.setCellValueFactory(d -> {
            String active = d.getValue().getIsActive();
            return new javafx.beans.property.SimpleStringProperty(
                    "0".equals(active) ? "Archived" : "Active");
        });
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); setStyle(""); return; }
                setText(s);
                setStyle("Archived".equals(s)
                        ? "-fx-text-fill: #888; -fx-font-style: italic;"
                        : "-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
            }
        });

        // Action column — text buttons (View, Edit, Check-In) + delete
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnView    = new Button("View");
            private final Button btnEdit    = new Button("Edit");
            private final Button btnCheckin = new Button("Check-In");
            private final Button btnDelete  = new Button("🗑 Delete");
            private final HBox   box        = new HBox(4, btnView, btnEdit, btnCheckin, btnDelete);
            {
                String base = "-fx-cursor: hand; -fx-background-radius: 4; -fx-padding: 4 10 4 10; -fx-font-size: 11;";
                btnView   .setStyle(base + "-fx-background-color: #1976D2; -fx-text-fill: white;");
                btnEdit   .setStyle(base + "-fx-background-color: #388E3C; -fx-text-fill: white;");
                btnCheckin.setStyle(base + "-fx-background-color: #F57C00; -fx-text-fill: white;");
                btnDelete .setStyle("-fx-cursor: hand; -fx-background-radius: 4; -fx-padding: 4 8 4 8; -fx-font-size: 12; -fx-background-color: #C62828; -fx-text-fill: white;");
                box.setAlignment(Pos.CENTER);

                btnView   .setOnAction(e -> openViewProfile(getTableView().getItems().get(getIndex())));
                btnEdit   .setOnAction(e -> openEditForm(getTableView().getItems().get(getIndex())));
                btnCheckin.setOnAction(e -> openCheckInAndAttend(getTableView().getItems().get(getIndex())));
                btnDelete .setOnAction(e -> confirmDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setGraphic(empty ? null : box);
            }
        });

        patientTable.setItems(pageItems);
    }

    private void setupFilters() {
        cmbCollege.setItems(FXCollections.observableArrayList(
                "All Colleges", "CCIS", "COE", "CABE", "CEDAS", "CHS", "CSP", "BED"));
        cmbCollege.setValue("All Colleges");

        cmbType.setItems(FXCollections.observableArrayList("All Types", "Student", "Employee"));
        cmbType.setValue("All Types");

        cmbYear.setItems(FXCollections.observableArrayList(
                "All Years",
                "1st Year", "2nd Year", "3rd Year", "4th Year", "5th Year",
                "Kinder", "Grade 1", "Grade 2", "Grade 3", "Grade 4", "Grade 5", "Grade 6",
                "Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12", "N/A"));
        cmbYear.setValue("All Years");

        cmbSort.setItems(FXCollections.observableArrayList(
                "Name A–Z", "Name Z–A", "Patient ID ↑", "Patient ID ↓"));
        cmbSort.setValue("Name A–Z");
    }

    // ── Refresh / filter / paginate ───────────────────────────────
    @FXML
    public void refreshTable() {
        allPatients.setAll(DatabaseManager.getAllPatientsWithArchived());
        applyFiltersAndPaginate();
    }

    @FXML private void handleSearch() { currentPage = 1; applyFiltersAndPaginate(); }

    @FXML
    private void clearFilters() {
        txtSearch.clear();
        cmbCollege.setValue("All Colleges");
        cmbType.setValue("All Types");
        cmbYear.setValue("All Years");
        cmbSort.setValue("Name A–Z");
        currentPage = 1;
        applyFiltersAndPaginate();
    }

    @FXML
    private void toggleArchived() {
        showArchived = !showArchived;
        btnShowArchived.setText(showArchived ? "🗂 Hide Archived" : "📦 Show Archived");
        btnShowArchived.setStyle(showArchived
                ? "-fx-background-color: #8B0000; -fx-text-fill: white; -fx-font-size: 12; -fx-cursor: hand; -fx-background-radius: 5;"
                : "-fx-background-color: #888; -fx-text-fill: white; -fx-font-size: 12; -fx-cursor: hand; -fx-background-radius: 5;");
        currentPage = 1;
        applyFiltersAndPaginate();
    }

    private void applyFiltersAndPaginate() {
        String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        String college = cmbCollege.getValue();
        String type    = cmbType.getValue();
        String year    = cmbYear.getValue();
        String sort    = cmbSort.getValue();

        List<Patient> filtered = allPatients.stream()
                .filter(p -> showArchived || !"0".equals(p.getIsActive()))
                .filter(p -> keyword.isEmpty()
                        || p.getName().toLowerCase().contains(keyword)
                        || p.getId().toLowerCase().contains(keyword))
                .filter(p -> "All Colleges".equals(college) || college.equals(p.getCourse()))
                .filter(p -> "All Types".equals(type)       || type.equals(p.getType()))
                .filter(p -> "All Years".equals(year)       || year.equals(p.getYear()))
                .collect(Collectors.toList());

        Comparator<Patient> cmp = switch (sort == null ? "Name A–Z" : sort) {
            case "Name Z–A"     -> Comparator.comparing(Patient::getName, String.CASE_INSENSITIVE_ORDER).reversed();
            case "Patient ID ↑" -> Comparator.comparing(Patient::getId);
            case "Patient ID ↓" -> Comparator.comparing(Patient::getId).reversed();
            default              -> Comparator.comparing(Patient::getName, String.CASE_INSENSITIVE_ORDER);
        };
        filtered.sort(cmp);

        int total = filtered.size();
        totalPages  = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        currentPage = Math.min(currentPage, totalPages);

        int from = (currentPage - 1) * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);

        pageItems.setAll(filtered.subList(from, to));
        lblTotal.setText("Total Patients: " + total + (showArchived ? " (incl. archived)" : ""));
        lblPage.setText("Page " + currentPage + " of " + totalPages);
        btnPrev.setDisable(currentPage <= 1);
        btnNext.setDisable(currentPage >= totalPages);
    }

    @FXML private void prevPage() { if (currentPage > 1) { currentPage--; applyFiltersAndPaginate(); } }
    @FXML private void nextPage() { if (currentPage < totalPages) { currentPage++; applyFiltersAndPaginate(); } }

    // ── Add Patient ───────────────────────────────────────────────
    @FXML
    private void openAddPatientForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/cjcschoolclinicmanagementsystem/View/add-patient-view.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Add New Patient");
            stage.initModality(Modality.APPLICATION_MODAL);
            javafx.geometry.Rectangle2D screen = javafx.stage.Screen.getPrimary().getVisualBounds();
            double w = Math.min(860, screen.getWidth() * 0.80);
            double h = Math.min(720, screen.getHeight() * 0.88);
            stage.setScene(new Scene(loader.load(), w, h));
            stage.setMinWidth(620);
            stage.setMinHeight(560);
            stage.setResizable(true);
            stage.showAndWait();
            refreshTable();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot open Add Patient form:\n" + e.getMessage());
        }
    }

    // ── Edit Patient ──────────────────────────────────────────────
    private void openEditForm(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/cjcschoolclinicmanagementsystem/View/add-patient-view.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Edit Patient — " + patient.getName());
            stage.initModality(Modality.APPLICATION_MODAL);
            javafx.geometry.Rectangle2D screen = javafx.stage.Screen.getPrimary().getVisualBounds();
            double w = Math.min(860, screen.getWidth() * 0.80);
            double h = Math.min(720, screen.getHeight() * 0.88);
            stage.setScene(new Scene(loader.load(), w, h));
            stage.setMinWidth(620);
            stage.setMinHeight(560);
            stage.setResizable(true);
            AddPatientController ctrl = loader.getController();
            ctrl.loadPatientForEdit(patient);
            stage.showAndWait();
            refreshTable();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot open Edit form:\n" + e.getMessage());
        }
    }

    // ── Check-In → immediately opens Attend form ──────────────────
    // Flow: Nurse clicks Check-In → enters time + purpose → visit record created →
    //       Attend form opens right away so nurse can record vitals without going to Logbook
    private void openCheckInAndAttend(Patient patient) {
        Stage dlg = new Stage();
        dlg.setTitle("Check-In: " + patient.getName());
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setResizable(false);

        VBox root = new VBox(12);
        root.setStyle("-fx-padding: 20; -fx-background-color: white;");
        root.setPrefWidth(340);

        Label title = new Label("Check-In Patient");
        title.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #8B0000;");
        Label nameL = new Label(patient.getName() + "  (" + patient.getId() + ")");
        nameL.setStyle("-fx-font-size: 12; -fx-text-fill: #333;");

        Label timeLabel = new Label("Time In:");
        timeLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");
        TextField timeField = new TextField(
                java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        timeField.setStyle("-fx-border-color: #ccc; -fx-border-radius: 4; -fx-padding: 6;");

        Label purpLabel = new Label("Purpose:");
        purpLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");
        ComboBox<String> purposeBox = new ComboBox<>(
                FXCollections.observableArrayList("Health-Related", "Med Cert"));
        purposeBox.setValue("Health-Related");
        purposeBox.setPrefWidth(240);

        Label statusLbl = new Label("");
        statusLbl.setStyle("-fx-text-fill: #c00; -fx-font-size: 11;");

        HBox btns = new HBox(10);
        btns.setAlignment(Pos.CENTER_RIGHT);
        Button btnConfirm = new Button("Check-In & Attend →");
        Button btnCancel  = new Button("Cancel");
        btnConfirm.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4; -fx-padding: 7 16 7 16; -fx-font-weight: bold;");
        btnCancel .setStyle("-fx-background-color: #888; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4; -fx-padding: 7 14 7 14;");
        btns.getChildren().addAll(btnCancel, btnConfirm);

        btnConfirm.setOnAction(e -> {
            String timeIn = timeField.getText().trim();
            if (timeIn.isEmpty()) { statusLbl.setText("⚠ Please enter time in."); return; }

            // 1. Create the check-in (inserts a Visits row)
            boolean ok = DatabaseManager.checkInPatient(
                    patient.getId(), timeIn, purposeBox.getValue());
            if (!ok) { statusLbl.setText("⚠ Check-in failed. Try again."); return; }
            dlg.close();

            // 2. Find the newly created visit row for this patient from today's visits
            List<Map<String,String>> today = DatabaseManager.getTodayVisits();
            Map<String,String> visitRow = today.stream()
                    .filter(v -> patient.getId().equals(v.get("patient_id")))
                    .findFirst().orElse(null);

            if (visitRow != null) {
                openAttendForm(visitRow, patient.getName());
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Checked In",
                        patient.getName() + " checked in at " + timeIn
                                + ".\nUse Logbook → Attend to record vitals.");
            }
            refreshTable();
        });

        btnCancel.setOnAction(e -> dlg.close());

        root.getChildren().addAll(title, nameL, new Separator(),
                timeLabel, timeField, purpLabel, purposeBox, statusLbl, btns);
        dlg.setScene(new Scene(root));
        dlg.show();
    }

    // Opens attend-view.fxml (reuses AttendController)
    private void openAttendForm(Map<String,String> visitRow, String patientName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/cjcschoolclinicmanagementsystem/View/attend-view.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Attend — " + patientName);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(loader.load()));
            AttendController ctrl = loader.getController();
            ctrl.setVisitData(visitRow);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot open Attend form:\n" + e.getMessage());
        }
    }

    private void openAttendFormReadOnly(Map<String,String> visitRow, String patientName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/cjcschoolclinicmanagementsystem/View/attend-view.fxml"));
            Stage stage = new Stage();
            stage.setTitle("View Visit — " + patientName);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(loader.load()));
            AttendController ctrl = loader.getController();
            ctrl.setVisitData(visitRow);
            ctrl.setReadOnly(true);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot open visit:\n" + e.getMessage());
        }
    }

    // ── View Profile ──────────────────────────────────────────────
    private void openViewProfile(Patient patient) {
        Stage stage = new Stage();
        stage.setTitle("Patient Profile — " + patient.getName());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setMaximized(true);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #f5f5f5;");

        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #8B0000; -fx-padding: 12 20 12 20;");
        Button btnBackBtn = new Button("← Back");
        btnBackBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 13;");
        btnBackBtn.setOnAction(e -> stage.close());
        Label lblTitle = new Label(patient.getId() + "  —  " + patient.getName());
        lblTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnEditBtn = new Button("Edit");
        btnEditBtn.setStyle("-fx-background-color: white; -fx-text-fill: #8B0000; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4; -fx-padding: 6 16 6 16;");
        btnEditBtn.setOnAction(e -> { stage.close(); openEditForm(patient); });
        topBar.getChildren().addAll(btnBackBtn, lblTitle, spacer, btnEditBtn);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #f5f5f5;");
        VBox content = new VBox(16);
        content.setStyle("-fx-padding: 20;");

        // Parse name
        String lastName = "", firstName = "", mi = "";
        String full = patient.getName();
        if (full != null && full.contains(",")) {
            lastName = full.split(",")[0].trim();
            String rest = full.split(",", 2)[1].trim();
            if (rest.contains(" ")) {
                String possible = rest.substring(rest.lastIndexOf(" ") + 1);
                if (possible.length() <= 3 && (possible.endsWith(".") || possible.matches("[A-Z]"))) {
                    firstName = rest.substring(0, rest.lastIndexOf(" ")).trim();
                    mi = possible;
                } else { firstName = rest; }
            } else { firstName = rest; }
        } else { firstName = full == null ? "" : full; }

        content.getChildren().addAll(
                buildProfileSection("👤 Personal Information", new String[][]{
                        {"Patient ID",     patient.getId()},
                        {"Last Name",      lastName},
                        {"First Name",     firstName},
                        {"Middle Initial", mi},
                        {"Gender",         patient.getGender()},
                        {"Birthdate",      patient.getBirthdate()},
                        {"Age",            patient.getAge()},
                        {"Blood Type",     patient.getBloodType()},
                        {"Type",           patient.getType()},
                        {"College/Dept",   patient.getCourse()},
                        {"Course",         patient.getCollege()},
                        {"Year Level",     patient.getYear()},
                        {"Contact #",      patient.getContact()},
                        {"Email",          patient.getEmail()},
                        {"Address",        patient.getAddress()},
                }),
                buildProfileSection("👨‍👩‍👧 Family & Emergency Contact", new String[][]{
                        {"Father's Name",     patient.getFatherName()},
                        {"Father's Contact",  patient.getFatherContact()},
                        {"Mother's Name",     patient.getMotherName()},
                        {"Mother's Contact",  patient.getMotherContact()},
                        {"Emergency Name",    patient.getEmergencyName()},
                        {"Relationship",      patient.getRelationship()},
                        {"Emergency Contact", patient.getEmergencyContact()},
                }),
                buildProfileSection("🏥 Medical History", new String[][]{
                        {"Allergies",       patient.getAllergies()},
                        {"Conditions",      patient.getConditions()},
                        {"Blood Type",      patient.getBloodType()},
                        {"Height (cm)",     patient.getHeight()},
                        {"Weight (kg)",     patient.getWeight()},
                        {"Medical Remarks", patient.getMedicalRemarks()},
                }),
                buildVisitHistorySection(patient)
        );

        scroll.setContent(content);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.getChildren().addAll(topBar, scroll);
        stage.setScene(new Scene(root));
        stage.show();
    }

    private VBox buildProfileSection(String title, String[][] rows) {
        VBox box = new VBox(8);
        box.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 6; -fx-padding: 16; -fx-background-radius: 6;");
        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #8B0000; -fx-font-size: 14;");
        box.getChildren().addAll(lbl, new Separator());
        for (String[] r : rows) {
            HBox row = new HBox(8);
            Label key = new Label(r[0] + ":"); key.setStyle("-fx-font-weight: bold; -fx-text-fill: #555; -fx-min-width: 160;");
            Label val = new Label(r[1] == null || r[1].isBlank() ? "—" : r[1]); val.setStyle("-fx-text-fill: #222;");
            row.getChildren().addAll(key, val);
            box.getChildren().add(row);
        }
        return box;
    }

    private VBox buildVisitHistorySection(Patient patient) {
        VBox box = new VBox(8);
        box.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 6; -fx-padding: 16; -fx-background-radius: 6;");
        Label lbl = new Label("📋 Visit History");
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #8B0000; -fx-font-size: 14;");
        box.getChildren().addAll(lbl, new Separator());

        TableView<Map<String,String>> table = new TableView<>();
        table.setPrefHeight(220);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // ── Data columns ──────────────────────────────────────────
        String[] cols   = {"visit_date","time_in","time_out","purpose","chief_complaint","diagnosis","treatment","remarks"};
        String[] hdrs   = {"Date","Time In","Time Out","Purpose","Complaint","Diagnosis","Treatment","Remarks"};
        double[] widths = {0.09, 0.07, 0.07, 0.09, 0.13, 0.13, 0.13, 0.11};
        for (int i = 0; i < cols.length; i++) {
            final String key = cols[i];
            TableColumn<Map<String,String>, String> tc = new TableColumn<>(hdrs[i]);
            tc.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                    d.getValue().getOrDefault(key, "")));
            table.getColumns().add(tc);
        }

        // ── Vitals column ─────────────────────────────────────────
        TableColumn<Map<String,String>, String> colVitals = new TableColumn<>("Vitals");
        colVitals.setCellValueFactory(d -> {
            Map<String,String> v = d.getValue();
            StringBuilder sb = new StringBuilder();
            String temp = v.getOrDefault("temperature", "");
            String bp   = v.getOrDefault("blood_pressure", "");
            String hr   = v.getOrDefault("heart_rate", "");
            String o2   = v.getOrDefault("oxygen_sat", "");
            if (!temp.isEmpty()) sb.append("T:").append(temp).append("°  ");
            if (!bp.isEmpty())   sb.append("BP:").append(bp).append("  ");
            if (!hr.isEmpty())   sb.append("HR:").append(hr).append("  ");
            if (!o2.isEmpty())   sb.append("O2:").append(o2).append("%");
            return new javafx.beans.property.SimpleStringProperty(sb.toString().trim());
        });
        table.getColumns().add(colVitals);

        // ── Action column: View / Edit / Delete ───────────────────
        TableColumn<Map<String,String>, String> colAction = new TableColumn<>("Action");
        colAction.setCellFactory(col -> new TableCell<>() {
            final Button btnView   = new Button("View");
            final Button btnEdit   = new Button("Edit");
            final Button btnDelete = new Button("Delete");
            final HBox   actionBox = new HBox(4, btnView, btnEdit, btnDelete);
            {
                btnView.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;" +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 10;");
                btnEdit.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white;" +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 10;");
                btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;" +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 10;");

                btnView.setOnAction(e -> {
                    Map<String,String> row = getTableView().getItems().get(getIndex());
                    openAttendFormReadOnly(row, patient.getName());
                });
                btnEdit.setOnAction(e -> {
                    Map<String,String> row = getTableView().getItems().get(getIndex());
                    openAttendForm(row, patient.getName());
                    table.setItems(FXCollections.observableArrayList(
                            DatabaseManager.getVisitsForPatient(patient.getId())));
                });
                btnDelete.setOnAction(e -> {
                    Map<String,String> row = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Delete Visit");
                    alert.setHeaderText("Delete this visit record?");
                    alert.setContentText("Date: " + row.getOrDefault("visit_date", "") +
                            "\nComplaint: " + row.getOrDefault("chief_complaint", "—"));
                    alert.showAndWait().ifPresent(resp -> {
                        if (resp == ButtonType.OK && row.containsKey("visit_id")) {
                            DatabaseManager.deleteVisit(Integer.parseInt(row.get("visit_id")));
                            table.setItems(FXCollections.observableArrayList(
                                    DatabaseManager.getVisitsForPatient(patient.getId())));
                        }
                    });
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });
        table.getColumns().add(colAction);

        table.setItems(FXCollections.observableArrayList(
                DatabaseManager.getVisitsForPatient(patient.getId())));
        table.setPlaceholder(new Label("No visits recorded yet."));
        box.getChildren().add(table);
        return box;
    }

    // ── Delete with "yes" confirmation ────────────────────────────
    private void confirmDelete(Patient patient) {
        Stage dlgStage = new Stage();
        dlgStage.setTitle("Confirm Delete");
        dlgStage.initModality(Modality.APPLICATION_MODAL);
        dlgStage.setResizable(false);

        VBox root = new VBox(14);
        root.setStyle("-fx-padding: 24; -fx-background-color: white;");
        root.setPrefWidth(380);

        Label icon  = new Label("🗑"); icon.setStyle("-fx-font-size: 36;");
        Label title = new Label("Delete Patient Record?");
        title.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #C62828;");
        Label msg = new Label("This will permanently remove:\n" + patient.getName()
                + " (" + patient.getId() + ")\n\nThis action cannot be undone.");
        msg.setStyle("-fx-font-size: 12; -fx-text-fill: #555;");
        msg.setWrapText(true);

        Label instruct = new Label("Type 'yes'  below to confirm:");
        instruct.setStyle("-fx-font-size: 12; -fx-text-fill: #333; -fx-font-weight: bold;");
        TextField confirmField = new TextField();
        confirmField.setPromptText("type \"yes\" to confirm");
        confirmField.setStyle("-fx-border-color: #ccc; -fx-border-radius: 4; -fx-padding: 7; -fx-font-size: 13;");

        HBox btns = new HBox(10); btns.setAlignment(Pos.CENTER_RIGHT);
        Button btnDelete = new Button("Delete");
        Button btnCancel = new Button("Cancel");
        btnDelete.setDisable(true);
        btnDelete.setStyle("-fx-background-color: #C62828; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4; -fx-padding: 7 18 7 18; -fx-font-weight: bold;");
        btnCancel.setStyle("-fx-background-color: #888; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4; -fx-padding: 7 14 7 14;");
        confirmField.textProperty().addListener((obs, o, n) ->
                btnDelete.setDisable(!"yes".equals(n.trim().toLowerCase())));
        btnDelete.setOnAction(e -> {
            boolean ok = DatabaseManager.deletePatient(patient.getId());
            dlgStage.close();
            if (ok) {
                showAlert(Alert.AlertType.INFORMATION, "Deleted", patient.getName() + " has been deleted.");
                refreshTable();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete patient.");
            }
        });
        btnCancel.setOnAction(e -> dlgStage.close());
        btns.getChildren().addAll(btnCancel, btnDelete);

        root.getChildren().addAll(icon, title, msg, new Separator(), instruct, confirmField, btns);
        root.setAlignment(Pos.TOP_LEFT);
        dlgStage.setScene(new Scene(root));
        dlgStage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(content);
        a.showAndWait();
    }
}