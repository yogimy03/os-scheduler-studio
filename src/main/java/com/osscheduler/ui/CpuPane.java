package com.osscheduler.ui;

import com.osscheduler.core.Samples;
import com.osscheduler.core.cpu.CpuAlgorithm;
import com.osscheduler.core.cpu.EngineOptions;
import com.osscheduler.core.model.CpuProcess;
import com.osscheduler.core.model.ProcessMetrics;
import com.osscheduler.core.model.ScheduleResult;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * The CPU scheduling tab: edit a table of processes, pick an algorithm, run it,
 * and see the Gantt chart, per process metrics and averages. A compare button
 * runs every algorithm on the same processes and charts their waiting times.
 */
final class CpuPane extends BorderPane {

    private final ObservableList<ProcessRow> rows = FXCollections.observableArrayList();
    private final ObservableList<ProcessMetrics> metricsRows = FXCollections.observableArrayList();
    private final ComboBox<CpuAlgorithm> algorithmBox = new ComboBox<>();
    private final Spinner<Integer> quantumSpinner = new Spinner<>(1, 50, 2);
    private final Canvas ganttCanvas = new Canvas(760, 160);
    private final Label summaryLabel = new Label("Run an algorithm to see the results.");
    private final Label errorLabel = new Label();

    CpuPane() {
        getStyleClass().add("pane");
        setPadding(new Insets(16));
        setLeft(buildInputPanel());
        setCenter(buildResultPanel());
        loadSample();
    }

    private VBox buildInputPanel() {
        TableView<ProcessRow> table = buildProcessTable();

        Button addButton = new Button("Add row");
        addButton.setOnAction(e -> rows.add(new ProcessRow("P" + (rows.size() + 1), 0, 1, 0)));
        Button removeButton = new Button("Remove");
        removeButton.setOnAction(e -> {
            int index = table.getSelectionModel().getSelectedIndex();
            if (index >= 0) {
                rows.remove(index);
            } else if (!rows.isEmpty()) {
                rows.remove(rows.size() - 1);
            }
        });
        Button sampleButton = new Button("Load sample");
        sampleButton.setOnAction(e -> loadSample());
        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> rows.clear());

        HBox rowButtons = new HBox(8, addButton, removeButton, sampleButton, clearButton);

        algorithmBox.setItems(FXCollections.observableArrayList(CpuAlgorithm.values()));
        algorithmBox.getSelectionModel().selectFirst();
        algorithmBox.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, value) -> updateQuantumState());
        algorithmBox.setMaxWidth(Double.MAX_VALUE);

        quantumSpinner.setEditable(true);
        HBox quantumBox = new HBox(8, new Label("Time quantum"), quantumSpinner);
        quantumBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Button runButton = new Button("Run");
        runButton.getStyleClass().add("primary");
        runButton.setOnAction(e -> run());
        Button compareButton = new Button("Compare all");
        compareButton.setOnAction(e -> compareAll());
        HBox runButtons = new HBox(8, runButton, compareButton);

        errorLabel.getStyleClass().add("error");
        errorLabel.setWrapText(true);

        VBox panel = new VBox(10,
                sectionTitle("Processes"),
                table,
                rowButtons,
                new Separator(),
                sectionTitle("Algorithm"),
                algorithmBox,
                quantumBox,
                runButtons,
                errorLabel);
        panel.setPadding(new Insets(0, 16, 0, 0));
        panel.setPrefWidth(400);
        panel.setMinWidth(360);
        updateQuantumState();
        return panel;
    }

    private TableView<ProcessRow> buildProcessTable() {
        TableView<ProcessRow> table = new TableView<>(rows);
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(240);

        TableColumn<ProcessRow, String> idColumn = new TableColumn<>("Process");
        idColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        idColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        idColumn.setOnEditCommit(e -> e.getRowValue().setId(e.getNewValue()));

        TableColumn<ProcessRow, Integer> arrivalColumn = intColumn(
                "Arrival", ProcessRow::getArrival, ProcessRow::setArrival);
        TableColumn<ProcessRow, Integer> burstColumn = intColumn(
                "Burst", ProcessRow::getBurst, ProcessRow::setBurst);
        TableColumn<ProcessRow, Integer> priorityColumn = intColumn(
                "Priority", ProcessRow::getPriority, ProcessRow::setPriority);

        table.getColumns().add(idColumn);
        table.getColumns().add(arrivalColumn);
        table.getColumns().add(burstColumn);
        table.getColumns().add(priorityColumn);
        return table;
    }

    private interface IntGetter {
        int get(ProcessRow row);
    }

    private interface IntSetter {
        void set(ProcessRow row, int value);
    }

    private TableColumn<ProcessRow, Integer> intColumn(String title, IntGetter getter, IntSetter setter) {
        TableColumn<ProcessRow, Integer> column = new TableColumn<>(title);
        column.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(getter.get(c.getValue())));
        column.setCellFactory(TextFieldTableCell.forTableColumn(new SafeIntegerStringConverter()));
        column.setOnEditCommit(e -> {
            if (e.getNewValue() != null) {
                setter.set(e.getRowValue(), e.getNewValue());
            } else {
                e.getTableView().refresh(); // bad input rejected, restore the old value
            }
        });
        return column;
    }

    private VBox buildResultPanel() {
        ScrollPane ganttScroll = new ScrollPane(ganttCanvas);
        ganttScroll.setFitToHeight(true);
        ganttScroll.getStyleClass().add("chart-scroll");
        ganttScroll.setPrefHeight(200);

        TableView<ProcessMetrics> metricsTable = buildMetricsTable();

        summaryLabel.getStyleClass().add("summary");
        summaryLabel.setWrapText(true);

        VBox panel = new VBox(10,
                sectionTitle("Gantt chart"),
                ganttScroll,
                sectionTitle("Metrics"),
                metricsTable,
                summaryLabel);
        VBox.setVgrow(metricsTable, Priority.ALWAYS);
        return panel;
    }

    private TableView<ProcessMetrics> buildMetricsTable() {
        TableView<ProcessMetrics> table = new TableView<>(metricsRows);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.getColumns().add(metricColumn("Process", m -> m.id()));
        table.getColumns().add(metricColumn("Arrival", m -> Integer.toString(m.arrivalTime())));
        table.getColumns().add(metricColumn("Burst", m -> Integer.toString(m.burstTime())));
        table.getColumns().add(metricColumn("Completion", m -> Integer.toString(m.completionTime())));
        table.getColumns().add(metricColumn("Turnaround", m -> Integer.toString(m.turnaroundTime())));
        table.getColumns().add(metricColumn("Waiting", m -> Integer.toString(m.waitingTime())));
        table.getColumns().add(metricColumn("Response", m -> Integer.toString(m.responseTime())));
        return table;
    }

    private interface MetricText {
        String of(ProcessMetrics metrics);
    }

    private TableColumn<ProcessMetrics, String> metricColumn(String title, MetricText text) {
        TableColumn<ProcessMetrics, String> column = new TableColumn<>(title);
        column.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(text.of(c.getValue())));
        return column;
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-title");
        return label;
    }

    private void updateQuantumState() {
        CpuAlgorithm selected = algorithmBox.getValue();
        quantumSpinner.setDisable(selected == null || !selected.needsQuantum());
    }

    private void loadSample() {
        rows.clear();
        for (CpuProcess process : Samples.processes()) {
            rows.add(new ProcessRow(process.id(), process.arrivalTime(),
                    process.burstTime(), process.priority()));
        }
    }

    private List<CpuProcess> currentProcesses() {
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Add at least one process first.");
        }
        return rows.stream().map(ProcessRow::toProcess).toList();
    }

    private void run() {
        try {
            List<CpuProcess> processes = currentProcesses();
            CpuAlgorithm algorithm = algorithmBox.getValue();
            ScheduleResult result = algorithm.create(new EngineOptions(quantumSpinner.getValue()))
                    .schedule(processes);
            Charts.drawGantt(ganttCanvas, result);
            metricsRows.setAll(result.metrics());
            summaryLabel.setText(String.format(
                    "Average waiting time: %.2f      Average turnaround: %.2f%n"
                            + "Average response: %.2f      CPU utilisation: %.1f%%      Throughput: %.3f",
                    result.averageWaitingTime(),
                    result.averageTurnaroundTime(),
                    result.averageResponseTime(),
                    result.cpuUtilization(),
                    result.throughput()));
            errorLabel.setText("");
        } catch (IllegalArgumentException ex) {
            errorLabel.setText(ex.getMessage());
        }
    }

    private void compareAll() {
        try {
            List<CpuProcess> processes = currentProcesses();
            int quantum = quantumSpinner.getValue();
            java.util.List<String> names = new java.util.ArrayList<>();
            java.util.List<Double> waiting = new java.util.ArrayList<>();
            for (CpuAlgorithm algorithm : CpuAlgorithm.values()) {
                ScheduleResult result = algorithm.create(new EngineOptions(quantum)).schedule(processes);
                names.add(result.algorithmName());
                waiting.add(result.averageWaitingTime());
            }
            Canvas canvas = new Canvas();
            Charts.drawBarChart(canvas, "Average waiting time (lower is better)", names, waiting);
            showChartWindow("Compare CPU algorithms", canvas);
            errorLabel.setText("");
        } catch (IllegalArgumentException ex) {
            errorLabel.setText(ex.getMessage());
        }
    }

    private void showChartWindow(String title, Canvas canvas) {
        ScrollPane scroll = new ScrollPane(canvas);
        Scene scene = new Scene(scroll);
        if (getScene() != null) {
            scene.getStylesheets().addAll(getScene().getStylesheets());
        }
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }
}
