package com.osscheduler.viz;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A small, colour blind friendly palette shared by the SVG charts and the
 * desktop app, so a process keeps the same colour everywhere it appears.
 *
 * <p>Colours are handed out in order of first appearance, which keeps charts
 * stable and easy to compare side by side.</p>
 */
public final class Palette {

    /** Distinct hues chosen to stay readable on the dark chart background. */
    public static final List<String> COLORS = List.of(
            "#4f9dde", // blue
            "#e8703a", // orange
            "#57b26a", // green
            "#c65cd6", // purple
            "#e0c341", // yellow
            "#3ec9c0", // teal
            "#e2688f", // pink
            "#9b8cf0"  // lavender
    );

    /** The colour used for idle stretches of time. */
    public static final String IDLE_COLOR = "#4a5568";

    private Palette() {
    }

    /**
     * Builds a stable label to colour map for the given labels, skipping the
     * idle marker (idle always uses {@link #IDLE_COLOR}).
     */
    public static Map<String, String> assign(List<String> labels) {
        Map<String, String> colors = new LinkedHashMap<>();
        int next = 0;
        for (String label : labels) {
            if (colors.containsKey(label)) {
                continue;
            }
            colors.put(label, COLORS.get(next % COLORS.size()));
            next++;
        }
        return colors;
    }
}
