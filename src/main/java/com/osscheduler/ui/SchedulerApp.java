package com.osscheduler.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;

/**
 * The desktop application shell.
 *
 * <p>It builds a window with a title bar and three tabs, one for each family of
 * algorithm. Each tab is a self contained panel, so this class stays small and
 * only wires the pieces together.</p>
 */
public final class SchedulerApp extends Application {

    @Override
    public void start(Stage stage) {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().add(tab("CPU Scheduling", new CpuPane()));
        tabs.getTabs().add(tab("Disk Scheduling", new DiskPane()));
        tabs.getTabs().add(tab("Page Replacement", new PagePane()));

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setTop(buildHeader());
        root.setCenter(tabs);

        Scene scene = new Scene(root, 1180, 780);
        URL css = getClass().getResource("/theme.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setTitle("OS Scheduler Studio");
        stage.setScene(scene);
        stage.setMinWidth(960);
        stage.setMinHeight(640);
        stage.show();
    }

    private VBox buildHeader() {
        Label title = new Label("OS Scheduler Studio");
        title.getStyleClass().add("app-title");
        Label subtitle = new Label(
                "Simulate and visualise CPU, disk and page replacement scheduling algorithms.");
        subtitle.getStyleClass().add("app-subtitle");

        VBox header = new VBox(2, title, subtitle);
        header.getStyleClass().add("app-header");
        header.setPadding(new Insets(16, 20, 16, 20));
        return header;
    }

    private Tab tab(String title, javafx.scene.Node content) {
        Tab tab = new Tab(title, content);
        return tab;
    }
}
