package com.example.cjcschoolclinicmanagementsystem.controller;

import com.example.cjcschoolclinicmanagementsystem.database.DatabaseManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class MedicineController {

    // ── Stat labels ───────────────────────────────────────────────
    @FXML private Label lblLowStock;
    @FXML private Label lblExpiringSoon;
    @FXML private Label lblTotalMedicines;

    // ── Table ─────────────────────────────────────────────────────
    @FXML private TableView<Map<String, String>>           tblMedicines;
    @FXML private TableColumn<Map<String, String>, String> colName;
    @FXML private TableColumn<Map<String, String>, String> colDesc;
    @FXML private TableColumn<Map<String, String>, String> colUnit;
    @FXML private TableColumn<Map<String, String>, String> colQty;
    @FXML private TableColumn<Map<String, String>, String> colLowStock;
    @FXML private TableColumn<Map<String, String>, String> colExpiry;
    @FXML private TableColumn<Map<String, String>, String> colStatus;
    @FXML private TableColumn<Map<String, String>, String> colAction;

    // ── Search & Sort ─────────────────────────────────────────────
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbSort;

    // ── Pagination ────────────────────────────────────────────────
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Label  lblPage;
    private static final int PAGE_SIZE = 30;
    private int currentPage = 0;

    private List<Map<String, String>> allMedicines;

    // ─────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        setupSortCombo();
        setupTable();
        refreshTable();
    }

    private void setupSortCombo() {
        if (cmbSort == null) return;
        cmbSort.getItems().addAll(
                "Name (A–Z)",
                "Name (Z–A)",
                "Stock: Low to High",
                "Stock: High to Low",
                "Expiry: Soonest First"
        );
        cmbSort.setValue("Name (A–Z)");
    }

    // ── Table setup ───────────────────────────────────────────────
    private void setupTable() {
        colName    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOrDefault("name", "")));
        colDesc    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOrDefault("description", "")));
        colUnit    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOrDefault("unit", "")));
        colQty     .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOrDefault("quantity", "0")));
        colLowStock.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOrDefault("low_stock_qty", "10")));
        colExpiry  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOrDefault("expiry_date", "—")));

        // Status cell — color-coded
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(getStatus(d.getValue())));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item) {
                    case "🔴 Low Stock"     -> setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
                    case "🟡 Expiring Soon" -> setStyle("-fx-text-fill: #e65100; -fx-font-weight: bold;");
                    case "❌ Expired"       -> setStyle("-fx-text-fill: #888; -fx-font-weight: bold;");
                    default                 -> setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                }
            }
        });

        // Quantity cell — highlight red when low
        colQty.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                Map<String, String> row = getTableView().getItems().get(getIndex());
                int qty = parseInt(row.get("quantity"));
                int low = parseInt(row.get("low_stock_qty"));
                setStyle(qty <= low
                        ? "-fx-text-fill: #c62828; -fx-font-weight: bold;"
                        : "-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
            }
        });

        // Action column
        colAction.setCellFactory(col -> new TableCell<>() {
            final Button btnRestock = new Button("+ Restock");
            final Button btnEdit    = new Button("✏ Edit");
            final Button btnRemove  = new Button("🗑");
            final HBox   box        = new HBox(4, btnRestock, btnEdit, btnRemove);
            {
                btnRestock.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white;" +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11;");
                btnEdit.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white;" +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11;");
                btnRemove.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;" +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11;");

                btnRestock.setOnAction(e -> {
                    Map<String, String> row = getTableView().getItems().get(getIndex());
                    showRestockDialog(row);
                });
                btnEdit.setOnAction(e -> {
                    Map<String, String> row = getTableView().getItems().get(getIndex());
                    showEditDialog(row);
                });
                btnRemove.setOnAction(e -> {
                    Map<String, String> row = getTableView().getItems().get(getIndex());
                    confirmDeactivate(row);
                });
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        tblMedicines.setPlaceholder(new Label("No medicines in inventory. Click '+ Add Medicine' to begin."));

        // Responsive widths
        javafx.application.Platform.runLater(() -> {
            if (tblMedicines.getScene() != null) bindWidths();
            else tblMedicines.sceneProperty().addListener((obs, ov, nv) -> {
                if (nv != null) bindWidths();
            });
        });
    }

    private void bindWidths() {
        javafx.beans.binding.DoubleBinding w = tblMedicines.widthProperty().subtract(20);
        colName    .prefWidthProperty().bind(w.multiply(0.18));
        colDesc    .prefWidthProperty().bind(w.multiply(0.20));
        colUnit    .prefWidthProperty().bind(w.multiply(0.08));
        colQty     .prefWidthProperty().bind(w.multiply(0.08));
        colLowStock.prefWidthProperty().bind(w.multiply(0.10));
        colExpiry  .prefWidthProperty().bind(w.multiply(0.11));
        colStatus  .prefWidthProperty().bind(w.multiply(0.12));
        colAction  .prefWidthProperty().bind(w.multiply(0.13));
    }

    // ── Data ─────────────────────────────────────────────────────
    @FXML public void refreshTable() {
        allMedicines = DatabaseManager.getAllMedicines();
        currentPage = 0;
        updateStatCards();
        applySearch();
    }

    @FXML private void onSearch() { currentPage = 0; applySearch(); }

    @FXML private void onSortChanged() { currentPage = 0; applySearch(); }

    private void applySearch() {
        String q = txtSearch.getText().trim().toLowerCase();
        java.util.List<Map<String, String>> filtered = allMedicines.stream()
                .filter(r -> q.isEmpty()
                        || r.getOrDefault("name", "").toLowerCase().contains(q)
                        || r.getOrDefault("description", "").toLowerCase().contains(q))
                .collect(java.util.stream.Collectors.toList());

        // Apply sort
        String sort = (cmbSort != null && cmbSort.getValue() != null)
                ? cmbSort.getValue() : "Name (A–Z)";
        switch (sort) {
            case "Name (A–Z)" ->
                    filtered.sort((a, b) -> a.getOrDefault("name","").compareToIgnoreCase(b.getOrDefault("name","")));
            case "Name (Z–A)" ->
                    filtered.sort((a, b) -> b.getOrDefault("name","").compareToIgnoreCase(a.getOrDefault("name","")));
            case "Stock: Low to High" ->
                    filtered.sort((a, b) -> parseInt(a.get("quantity")) - parseInt(b.get("quantity")));
            case "Stock: High to Low" ->
                    filtered.sort((a, b) -> parseInt(b.get("quantity")) - parseInt(a.get("quantity")));
            case "Expiry: Soonest First" ->
                    filtered.sort((a, b) -> {
                        String ea = a.getOrDefault("expiry_date", "9999");
                        String eb = b.getOrDefault("expiry_date", "9999");
                        if (ea.isEmpty()) ea = "9999";
                        if (eb.isEmpty()) eb = "9999";
                        return ea.compareTo(eb);
                    });
        }

        applyPagination(filtered);
    }

    private void applyPagination(List<Map<String, String>> data) {
        int total      = data.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        currentPage    = Math.max(0, Math.min(currentPage, totalPages - 1));

        int from = currentPage * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);

        tblMedicines.setItems(javafx.collections.FXCollections.observableArrayList(
                data.subList(from, to)));

        if (lblPage != null)
            lblPage.setText("Page " + (currentPage + 1) + " of " + totalPages);
        if (btnPrev != null)
            btnPrev.setDisable(currentPage == 0);
        if (btnNext != null)
            btnNext.setDisable(currentPage >= totalPages - 1);
    }

    @FXML private void goToPrevPage() {
        if (currentPage > 0) { currentPage--; applySearch(); }
    }

    @FXML private void goToNextPage() {
        String q = txtSearch.getText().trim().toLowerCase();
        long total = allMedicines.stream().filter(r -> q.isEmpty()
                || r.getOrDefault("name","").toLowerCase().contains(q)
                || r.getOrDefault("description","").toLowerCase().contains(q)).count();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        if (currentPage < totalPages - 1) { currentPage++; applySearch(); }
    }

    private void updateStatCards() {
        int low     = DatabaseManager.getLowStockCount();
        int expiring = DatabaseManager.getExpiringSoonCount(30);
        int total   = allMedicines.size();
        if (lblLowStock      != null) lblLowStock.setText(String.valueOf(low));
        if (lblExpiringSoon  != null) lblExpiringSoon.setText(String.valueOf(expiring));
        if (lblTotalMedicines!= null) lblTotalMedicines.setText(String.valueOf(total));
    }

    // ── Add medicine dialog ───────────────────────────────────────
    @FXML private void showAddDialog() {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Add Medicine");
        dlg.setHeaderText("Add a new medicine to inventory");

        ButtonType btnSave = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        GridPane gp = buildMedicineForm(null);
        dlg.getDialogPane().setContent(gp);
        dlg.getDialogPane().setPrefWidth(480);
        dlg.getDialogPane().setMinWidth(480);

        // Get fields from the GridPane user data
        TextField txtName     = (TextField) gp.getUserData();
        TextArea  txtDesc     = (TextArea)  ((HBox)gp.getChildren().get(3)).getChildren().get(0);
        TextField txtUnit     = (TextField) ((HBox)gp.getChildren().get(5)).getChildren().get(0);
        TextField txtQty      = (TextField) ((HBox)gp.getChildren().get(7)).getChildren().get(0);
        TextField txtLow      = (TextField) ((HBox)gp.getChildren().get(9)).getChildren().get(0);
        DatePicker dpExpiry   = (DatePicker)((HBox)gp.getChildren().get(11)).getChildren().get(0);

        Button saveBtn = (Button) dlg.getDialogPane().lookupButton(btnSave);
        saveBtn.disableProperty().bind(txtName.textProperty().isEmpty());

        dlg.showAndWait().ifPresent(result -> {
            if (result != btnSave) return;
            String name    = txtName.getText().trim();
            String desc    = txtDesc.getText().trim();
            String unit    = txtUnit.getText().trim().isEmpty() ? "tablet" : txtUnit.getText().trim();
            int qty        = parseIntSafe(txtQty.getText());
            int low        = parseIntSafe(txtLow.getText().isEmpty() ? "10" : txtLow.getText());
            String expiry  = dpExpiry.getValue() != null ? dpExpiry.getValue().toString() : "";
            boolean ok = DatabaseManager.addMedicineInventory(name, desc, unit, qty, low, expiry);
            if (ok) refreshTable();
            else showAlert("Error", "Could not add medicine. Please try again.");
        });
    }

    // ── Edit dialog ───────────────────────────────────────────────
    private void showEditDialog(Map<String, String> row) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Edit Medicine");
        dlg.setHeaderText("Edit: " + row.get("name"));

        ButtonType btnSave = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        GridPane gp = buildMedicineForm(row);
        dlg.getDialogPane().setContent(gp);
        dlg.getDialogPane().setPrefWidth(480);

        TextField txtName   = (TextField) gp.getUserData();
        TextArea  txtDesc   = (TextArea)  ((HBox)gp.getChildren().get(3)).getChildren().get(0);
        TextField txtUnit   = (TextField) ((HBox)gp.getChildren().get(5)).getChildren().get(0);
        TextField txtQty    = (TextField) ((HBox)gp.getChildren().get(7)).getChildren().get(0);
        TextField txtLow    = (TextField) ((HBox)gp.getChildren().get(9)).getChildren().get(0);
        DatePicker dpExpiry = (DatePicker)((HBox)gp.getChildren().get(11)).getChildren().get(0);

        Button saveBtn = (Button) dlg.getDialogPane().lookupButton(btnSave);
        saveBtn.disableProperty().bind(txtName.textProperty().isEmpty());

        dlg.showAndWait().ifPresent(result -> {
            if (result != btnSave) return;
            int id       = Integer.parseInt(row.get("medicine_id"));
            String name  = txtName.getText().trim();
            String desc  = txtDesc.getText().trim();
            String unit  = txtUnit.getText().trim().isEmpty() ? "tablet" : txtUnit.getText().trim();
            int qty      = parseIntSafe(txtQty.getText());
            int low      = parseIntSafe(txtLow.getText().isEmpty() ? "10" : txtLow.getText());
            String expiry= dpExpiry.getValue() != null ? dpExpiry.getValue().toString() : "";
            boolean ok   = DatabaseManager.updateMedicineInventory(id, name, desc, unit, qty, low, expiry);
            if (ok) refreshTable();
            else showAlert("Error", "Could not update medicine.");
        });
    }

    // ── Restock dialog ────────────────────────────────────────────
    private void showRestockDialog(Map<String, String> row) {
        TextInputDialog dlg = new TextInputDialog("10");
        dlg.setTitle("Restock");
        dlg.setHeaderText("Restock: " + row.get("name"));
        dlg.setContentText("Quantity to add:");
        dlg.showAndWait().ifPresent(val -> {
            int qty = parseIntSafe(val);
            if (qty <= 0) { showAlert("Invalid", "Please enter a positive number."); return; }
            boolean ok = DatabaseManager.restockMedicine(
                    Integer.parseInt(row.get("medicine_id")), qty);
            if (ok) refreshTable();
            else showAlert("Error", "Could not restock medicine.");
        });
    }

    // ── Deactivate ────────────────────────────────────────────────
    private void confirmDeactivate(Map<String, String> row) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Medicine");
        alert.setHeaderText("Remove \"" + row.get("name") + "\" from inventory?");
        alert.setContentText("It will no longer appear in the dispense list. Dispense history is kept.");
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                DatabaseManager.deactivateMedicine(Integer.parseInt(row.get("medicine_id")));
                refreshTable();
            }
        });
    }

    // ── Export CSV ────────────────────────────────────────────────
    @FXML private void exportCSV() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Inventory CSV");
        chooser.setInitialFileName("medicine_inventory.csv");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        Stage stage = (Stage) tblMedicines.getScene().getWindow();
        File file = chooser.showSaveDialog(stage);
        if (file == null) return;

        String inventoryPath = file.getAbsolutePath();
        String dispensePath  = inventoryPath.replace(".csv", "_dispense_log.csv");

        boolean ok = DatabaseManager.exportMedicineCSV(inventoryPath, dispensePath);
        if (ok) {
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Export Successful");
            info.setHeaderText("CSV files exported.");
            info.setContentText("Inventory: " + inventoryPath +
                    "\nDispense log: " + dispensePath);
            info.showAndWait();
        } else {
            showAlert("Export Failed", "Could not write CSV files. Check file permissions.");
        }
    }

    // ── Helper: shared form builder ───────────────────────────────
    /**
     * Builds a GridPane containing the medicine form fields.
     * If {@code existing} is non-null, fields are pre-filled.
     * The name TextField is stored in gp.setUserData() for easy retrieval.
     */
    private GridPane buildMedicineForm(Map<String, String> existing) {
        GridPane gp = new GridPane();
        gp.setHgap(12);
        gp.setVgap(10);
        gp.setPadding(new Insets(8));

        TextField txtName = new TextField(existing != null ? existing.get("name") : "");
        txtName.setPromptText("e.g. Paracetamol 500mg");
        txtName.setPrefWidth(320);
        gp.setUserData(txtName);   // retrieved by caller

        TextArea txtDesc = new TextArea(existing != null ? existing.getOrDefault("description", "") : "");
        txtDesc.setPromptText("Optional description or dosage notes");
        txtDesc.setPrefRowCount(2);
        txtDesc.setWrapText(true);
        txtDesc.setPrefWidth(320);

        TextField txtUnit = new TextField(existing != null ? existing.getOrDefault("unit", "tablet") : "tablet");
        txtUnit.setPromptText("tablet / capsule / mL / sachet …");
        txtUnit.setPrefWidth(200);

        TextField txtQty = new TextField(existing != null ? existing.getOrDefault("quantity", "0") : "0");
        txtQty.setPrefWidth(120);

        TextField txtLow = new TextField(existing != null ? existing.getOrDefault("low_stock_qty", "10") : "10");
        txtLow.setPrefWidth(120);

        DatePicker dpExpiry = new DatePicker();
        dpExpiry.setPromptText("YYYY-MM-DD (optional)");
        if (existing != null) {
            String exp = existing.getOrDefault("expiry_date", "");
            if (!exp.isEmpty()) {
                try { dpExpiry.setValue(LocalDate.parse(exp)); } catch (Exception ignored) {}
            }
        }

        // Build rows: Label row index is even (0, 2, 4, ...), field row is odd
        addFormRow(gp, "Medicine Name *", wrapInHBox(txtName), 0);
        addFormRow(gp, "Description",     wrapInHBox(txtDesc), 2);
        addFormRow(gp, "Unit",            wrapInHBox(txtUnit), 4);
        addFormRow(gp, "Current Quantity",wrapInHBox(txtQty),  6);
        addFormRow(gp, "Low Stock Alert at", wrapInHBox(txtLow), 8);
        addFormRow(gp, "Expiry Date",     wrapInHBox(dpExpiry), 10);

        return gp;
    }

    /** Adds a label+field pair to a GridPane at the given row (label at rowIndex, field at rowIndex+1). */
    private void addFormRow(GridPane gp, String label, HBox field, int rowIndex) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        GridPane.setRowIndex(lbl, rowIndex);
        GridPane.setRowIndex(field, rowIndex + 1);
        GridPane.setColumnIndex(lbl, 0);
        GridPane.setColumnIndex(field, 0);
        gp.getChildren().addAll(lbl, field);
    }

    private HBox wrapInHBox(javafx.scene.Node node) {
        HBox box = new HBox(node);
        box.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(node, Priority.ALWAYS);
        return box;
    }

    // ── Helpers ───────────────────────────────────────────────────
    private String getStatus(Map<String, String> row) {
        int qty      = parseInt(row.get("quantity"));
        int lowStock = parseInt(row.get("low_stock_qty"));
        String expiry = row.getOrDefault("expiry_date", "");
        if (!expiry.isEmpty()) {
            try {
                LocalDate exp = LocalDate.parse(expiry);
                if (!exp.isAfter(LocalDate.now()))         return "❌ Expired";
                if (!exp.isAfter(LocalDate.now().plusDays(30))) return "🟡 Expiring Soon";
            } catch (Exception ignored) {}
        }
        if (qty <= lowStock) return "🔴 Low Stock";
        return "✅ OK";
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    private int parseIntSafe(String s) {
        try { return Math.max(0, Integer.parseInt(s.trim())); } catch (Exception e) { return 0; }
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}