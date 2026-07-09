package com.osscheduler.ui;

import com.osscheduler.core.InputParser;
import com.osscheduler.core.Samples;
import com.osscheduler.core.model.PageResult;
import com.osscheduler.core.model.PageStep;
import com.osscheduler.core.page.PageAlgorithm;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * The page replacement tab: type a reference string, choose the number of frames
 * and an algorithm, then see the frame by frame table with faults highlighted
 * and the hit and fault totals.
 */
final class PagePane extends BorderPane {

    private final ComboBox<PageAlgorithm> algorithmBox = new ComboBox<>();
    private final TextField referencesField = new TextField(Samples.pageReferencesText());
    private final Spinner<Integer> framesSpinner = new Spinner<>(1, 12, Samples.pageFrames());
    private final GridPane grid = new GridPane();
    private final Label summaryLabel = new Label("Run an algorithm to see the frames.");
    private final Label errorLabel = new Label();

    PagePane() {
        getStyleClass().add("pane");
        setPadding(new Insets(16));
        setLeft(buildInputPanel());
        setCenter(buildResultPanel());
    }

    private VBox buildInputPanel() {
        algorithmBox.setItems(FXCollections.observableArrayList(PageAlgorithm.values()));
        algorithmBox.getSelectionModel().selectFirst();
        algorithmBox.setMaxWidth(Double.MAX_VALUE);

        framesSpinner.setEditable(true);

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
                labelled("Reference string", referencesField),
                labelled("Number of frames", framesSpinner),
                runButtons,
                errorLabel);
        panel.setPadding(new Insets(0, 16, 0, 0));
        panel.setPrefWidth(360);
        panel.setMinWidth(320);
        return panel;
    }

    private VBox buildResultPanel() {
        grid.getStyleClass().add("page-grid");
        ScrollPane scroll = new ScrollPane(grid);
        scroll.getStyleClass().add("chart-scroll");
        scroll.setPrefHeight(420);

        summaryLabel.getStyleClass().add("summary");

        VBox panel = new VBox(10,
                sectionTitle("Frames over time"),
                scroll,
                summaryLabel);
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

    private void loadSample() {
        referencesField.setText(Samples.pageReferencesText());
        framesSpinner.getValueFactory().setValue(Samples.pageFrames());
    }

    private void run() {
        try {
            List<Integer> references = InputParser.parseIntList(referencesField.getText());
            int frames = framesSpinner.getValue();
            PageAlgorithm algorithm = algorithmBox.getValue();
            PageResult result = algorithm.create().run(references, frames);
            buildGrid(result);
            summaryLabel.setText(String.format(
                    "Page faults: %d      Page hits: %d      Hit ratio: %.1f%%      Fault rate: %.1f%%",
                    result.pageFaults(), result.pageHits(),
                    result.hitRatio() * 100.0, result.faultRate() * 100.0));
            errorLabel.setText("");
        } catch (IllegalArgumentException ex) {
            errorLabel.setText(ex.getMessage());
        }
    }

    private void buildGrid(PageResult result) {
        grid.getChildren().clear();
        List<PageStep> steps = result.steps();

        grid.add(cornerCell("ref"), 0, 0);
        for (int slot = 0; slot < result.frameCount(); slot++) {
            grid.add(headerCell("Frame " + (slot + 1)), 0, slot + 1);
        }
        grid.add(headerCell("fault?"), 0, result.frameCount() + 1);

        for (int col = 0; col < steps.size(); col++) {
            PageStep step = steps.get(col);
            grid.add(headerCell(Integer.toString(step.reference())), col + 1, 0);
            for (int slot = 0; slot < result.frameCount(); slot++) {
                Integer value = step.framesAfter().get(slot);
                grid.add(valueCell(value == null ? "" : value.toString(), false), col + 1, slot + 1);
            }
            Label status = valueCell(step.fault() ? "F" : "H", true);
            status.getStyleClass().add(step.fault() ? "fault" : "hit");
            grid.add(status, col + 1, result.frameCount() + 1);
        }
    }

    private Label cornerCell(String text) {
        Label label = new Label(text);
        label.getStyleClass().addAll("page-cell", "corner");
        label.setPrefSize(46, 34);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    private Label headerCell(String text) {
        Label label = new Label(text);
        label.getStyleClass().addAll("page-cell", "header");
        label.setPrefSize(46, 34);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    private Label valueCell(String text, boolean strong) {
        Label label = new Label(text);
        label.getStyleClass().add("page-cell");
        if (strong) {
            label.getStyleClass().add("strong");
        }
        label.setPrefSize(46, 34);
        label.setAlignment(Pos.CENTER);
        return label;
    }
}
