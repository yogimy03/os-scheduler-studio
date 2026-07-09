package com.osscheduler.ui;

import javafx.application.Application;

/**
 * A plain launcher class.
 *
 * <p>Launching a class that extends {@link javafx.application.Application}
 * directly can trip up the JavaFX runtime. The reliable trick is to keep the
 * entry point in a separate class that simply calls {@code Application.launch},
 * which is exactly what this does.</p>
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        Application.launch(SchedulerApp.class, args);
    }
}
