package com.transactionanomaly.ui;

import com.transactionanomaly.db.TransactionDAO;
import com.transactionanomaly.ml.AnomalyDetector;
import com.transactionanomaly.model.Transaction;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The whole dashboard screen, built with plain JavaFX (no FXML) so the
 * layout code and the event-handling code sit next to each other and are
 * easy to follow top to bottom.
 */
public class DashboardView extends BorderPane {

    private final TransactionDAO dao = new TransactionDAO();
    private final AnomalyDetector detector = new AnomalyDetector();

    private final ObservableList<Transaction> transactionData = FXCollections.observableArrayList();
    private final TableView<Transaction> table = new TableView<>(transactionData);

    private final Label statusLabel = new Label("Ready.");
    private final ScatterChart<Number, Number> chart = buildChart();

    // --- Add-transaction form fields ---
    private final TextField userIdField = new TextField();
    private final TextField amountField = new TextField();
    private final ComboBox<String> typeBox = new ComboBox<>(
            FXCollections.observableArrayList("PURCHASE", "TRANSFER", "WITHDRAWAL", "DEPOSIT"));
    private final TextField locationField = new TextField();

    public DashboardView() {
        setPadding(new Insets(15));

        setTop(buildTopBar());
        setLeft(buildFormPanel());
        setCenter(buildTablePanel());
        setRight(buildChartPanel());
        setBottom(statusLabel);

        BorderPane.setMargin(statusLabel, new Insets(10, 0, 0, 0));

        // Load whatever is already in the database as soon as the app opens.
        loadTransactions();
    }

    // ---------------------------------------------------------------
    // Top bar: title + action buttons
    // ---------------------------------------------------------------
    private HBox buildTopBar() {
        Label title = new Label("AI Transaction Anomaly Dashboard");
        title.getStyleClass().add("title-label");

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> loadTransactions());

        Button detectBtn = new Button("Run Anomaly Detection");
        detectBtn.getStyleClass().add("primary-button");
        detectBtn.setOnAction(e -> runAnomalyDetection());

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(15, title, spacer, refreshBtn, detectBtn);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 0, 15, 0));
        return bar;
    }

    // ---------------------------------------------------------------
    // Left panel: form for adding a new transaction
    // ---------------------------------------------------------------
    private VBox buildFormPanel() {
        Label heading = new Label("Add Transaction");
        heading.getStyleClass().add("section-label");

        typeBox.setValue("PURCHASE");
        userIdField.setPromptText("User ID (e.g. U102)");
        amountField.setPromptText("Amount");
        locationField.setPromptText("Location");

        Button addBtn = new Button("Add Transaction");
        addBtn.getStyleClass().add("primary-button");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> addTransaction());

        VBox form = new VBox(10,
                heading,
                new Label("User ID"), userIdField,
                new Label("Amount"), amountField,
                new Label("Type"), typeBox,
                new Label("Location"), locationField,
                addBtn);
        form.setPadding(new Insets(0, 20, 0, 0));
        form.setPrefWidth(220);
        return form;
    }

    private void addTransaction() {
        try {
            String userId = userIdField.getText().trim();
            String location = locationField.getText().trim();
            double amount = Double.parseDouble(amountField.getText().trim());

            if (userId.isEmpty() || location.isEmpty()) {
                statusLabel.setText("Please fill in User ID and Location.");
                return;
            }

            Transaction t = new Transaction(userId, amount, LocalDateTime.now(),
                    typeBox.getValue(), location);
            dao.insertTransaction(t);

            userIdField.clear();
            amountField.clear();
            locationField.clear();

            statusLabel.setText("Transaction added.");
            loadTransactions();

        } catch (NumberFormatException ex) {
            statusLabel.setText("Amount must be a number.");
        } catch (Exception ex) {
            statusLabel.setText("Error adding transaction: " + ex.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Center panel: table of transactions
    // ---------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private VBox buildTablePanel() {
        Label heading = new Label("Transactions");
        heading.getStyleClass().add("section-label");

        TableColumn<Transaction, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Transaction, String> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(new PropertyValueFactory<>("userId"));

        TableColumn<Transaction, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<Transaction, LocalDateTime> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("transactionTime"));
        timeCol.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, HH:mm");
            @Override
            protected void updateItem(LocalDateTime value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : value.format(fmt));
            }
        });

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Transaction, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));

        TableColumn<Transaction, Double> scoreCol = new TableColumn<>("Anomaly Score");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("anomalyScore"));

        TableColumn<Transaction, Boolean> flagCol = new TableColumn<>("Suspicious?");
        flagCol.setCellValueFactory(new PropertyValueFactory<>("anomaly"));
        flagCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : (value ? "YES" : "no"));
            }
        });

        table.getColumns().setAll(List.of(idCol, userCol, amountCol, timeCol, typeCol,
                locationCol, scoreCol, flagCol));

        // Highlight anomalous rows in red so they jump out visually.
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Transaction t, boolean empty) {
                super.updateItem(t, empty);
                getStyleClass().remove("anomaly-row");
                if (!empty && t != null && t.isAnomaly()) {
                    getStyleClass().add("anomaly-row");
                }
            }
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        VBox box = new VBox(10, heading, table);
        BorderPane.setMargin(box, new Insets(0, 20, 0, 20));
        return box;
    }

    // ---------------------------------------------------------------
    // Right panel: chart showing normal vs. anomalous transactions
    // ---------------------------------------------------------------
    private ScatterChart<Number, Number> buildChart() {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Hour of Day");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount");

        ScatterChart<Number, Number> c = new ScatterChart<>(xAxis, yAxis);
        c.setTitle("Transactions: Normal vs Suspicious");
        c.setPrefWidth(380);
        return c;
    }

    private VBox buildChartPanel() {
        Label heading = new Label("Visualization");
        heading.getStyleClass().add("section-label");
        VBox box = new VBox(10, heading, chart);
        box.setPrefWidth(380);
        return box;
    }

    private void refreshChart() {
        XYChart.Series<Number, Number> normalSeries = new XYChart.Series<>();
        normalSeries.setName("Normal");
        XYChart.Series<Number, Number> anomalySeries = new XYChart.Series<>();
        anomalySeries.setName("Suspicious");

        for (Transaction t : transactionData) {
            XYChart.Data<Number, Number> point =
                    new XYChart.Data<>(t.getTransactionTime().getHour(), t.getAmount());
            if (t.isAnomaly()) {
                anomalySeries.getData().add(point);
            } else {
                normalSeries.getData().add(point);
            }
        }

        chart.getData().setAll(normalSeries, anomalySeries);
    }

    // ---------------------------------------------------------------
    // Data loading / anomaly detection actions
    // ---------------------------------------------------------------
    private void loadTransactions() {
        try {
            List<Transaction> all = dao.getAllTransactions();
            transactionData.setAll(all);
            refreshChart();
            statusLabel.setText("Loaded " + all.size() + " transactions.");
        } catch (Exception ex) {
            statusLabel.setText("Could not load transactions: " + ex.getMessage());
        }
    }

    private void runAnomalyDetection() {
        try {
            List<Transaction> updated = detector.detectAnomalies(transactionData);

            long anomalyCount = 0;
            for (Transaction t : updated) {
                dao.updateAnomalyResult(t.getId(), t.getAnomalyScore(), t.isAnomaly());
                if (t.isAnomaly()) anomalyCount++;
            }

            table.refresh();
            refreshChart();
            statusLabel.setText("Detection complete: " + anomalyCount + " suspicious transaction(s) found.");

        } catch (Exception ex) {
            statusLabel.setText("Detection failed: " + ex.getMessage());
        }
    }
}
