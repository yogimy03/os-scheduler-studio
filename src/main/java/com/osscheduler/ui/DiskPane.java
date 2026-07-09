package com.osscheduler.ui;

import com.osscheduler.core.InputParser;
import com.osscheduler.core.Samples;
import com.osscheduler.core.disk.DiskAlgorithm;
import com.osscheduler.core.disk.DiskInputs;
import com.osscheduler.core.model.DiskDirection;
import com.osscheduler.core.model.DiskResult;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The disk scheduling tab: choose an algorithm, set the head position, disk size
 * and request queue, then see the seek graph and total head movement.
 */
final class DiskPane extends BorderPane {

    private final ComboBox<DiskAlgorithm> algorithmBox = new ComboBox<>();
    private final ComboBox<DiskDirection> directionBox = new ComboBox<>();
    private final TextField headField = new TextField(Integer.toString(Samples.diskStartHead()));
    private final TextField sizeField = new TextField(Integer.toString(Samples.diskSize()));
    private final TextField requestsField = new TextField(Samples.diskRequestsText());
    private final Canvas seekCanvas = new Canvas(760, 300);
    private final Label movementLabel = new Label("Run an algorithm to see the seek graph.");
    private final Label orderLabel = new Label();
    private final Label errorLabel = new Label();

    DiskPane() {
        getStyleClass().add("pane");
        setPadding(new Insets(16));
        setLeft(buildInputPanel());
        setCenter(buildResultPanel());
    }

    private VBox buildInputPanel() {
        algorithmBox.setItems(FXCollections.observableArrayList(DiskAlgorithm.values()));
        algorithmBox.getSelectionModel().selectFirst();
        algorithmBox.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, value) -> updateDirectionState());
        algorithmBox.setMaxWidth(Double.MAX_VALUE);

        directionBox.setItems(FXCollections.observableArrayList(DiskDirection.values()));
        directionBox.getSelectionModel().select(DiskDirection.UP);
        directionBox.setMaxWidth(Double.MAX_VALUE);

        Button runButton = new Button("Run");
        runButton.getStyleClass().add("primary");
        runButton.setOnAction(e -> run());
        Button sampleButton = new Button("Load sample");
        sampleButton.setOnAction(e -> loadSample());
        HBox runButtons = new HBox(8, runButton, sampleButton);

        errorLabel.getStyleClass().add("error");
        errorLabel.setWrapText(true);

        VBox panel = new VBox(10,
                sectionTitle("Algorithm"),
                algorithmBox,
                labelled("Start head", headField),
                labelled("Disk size (cylinders)", sizeField),
                labelled("Requests", requestsField),
                labelled("Initial direction", directionBox),
                runButtons,
                errorLabel);
        panel.setPadding(new Insets(0, 16, 0, 0));
        panel.setPrefWidth(360);
        panel.setMinWidth(320);
        updateDirectionState();
        return panel;
    }

    private VBox buildResultPanel() {
        ScrollPane scroll = new ScrollPane(seekCanvas);
        scroll.getStyleClass().add("chart-scroll");
        scroll.setPrefHeight(460);

        movementLabel.getStyleClass().add("summary");
        orderLabel.getStyleClass().add("summary");
        orderLabel.setWrapText(true);

        VBox panel = new VBox(10,
                sectionTitle("Seek graph"),
                scroll,
                movementLabel,
                orderLabel);
        return panel;
    }

    private VBox labelled(String title, javafx.scene.Node field) {
        Label label = new Label(title);
        label.getStyleClass().add("field-label");
        return new VBox(4, label, field);
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-title");
        return label;
    }

    private void updateDirectionState() {
        DiskAlgorithm selected = algorithmBox.getValue();
        directionBox.setDisable(selected == null || !selected.usesDirection());
    }

    private void loadSample() {
        headField.setText(Integer.toString(Samples.diskStartHead()));
        sizeField.setText(Integer.toString(Samples.diskSize()));
        requestsField.setText(Samples.diskRequestsText());
        directionBox.getSelectionModel().select(DiskDirection.UP);
    }

    private void run() {
        try {
            int head = parseInt(headField.getText(), "start head");
            int size = parseInt(sizeField.getText(), "disk size");
            List<Integer> requests = InputParser.parseIntList(requestsField.getText());
            DiskInputs.validate(head, requests, size);
            DiskDirection direction = directionBox.getValue();
            DiskAlgorithm algorithm = algorithmBox.getValue();

            DiskResult result = algorithm.create().schedule(head, requests, size, direction);
            Charts.drawSeek(seekCanvas, result, size - 1);
            movementLabel.setText("Total head movement: " + result.totalHeadMovement());
            orderLabel.setText("Order: " + result.path().stream()
                    .map(String::valueOf).collect(Collectors.joining(" -> ")));
            errorLabel.setText("");
        } catch (IllegalArgumentException ex) {
            errorLabel.setText(ex.getMessage());
        }
    }

    private static int parseInt(String text, String what) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Could not read " + what + " from '" + text.trim() + "'");
        }
    }
}
