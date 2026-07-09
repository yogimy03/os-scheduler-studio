package com.osscheduler.viz;

import com.osscheduler.core.model.DiskResult;

import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

/**
 * Turns a disk schedule into a standalone SVG seek graph.
 *
 * <p>Cylinder numbers run along the top, and the head's journey is drawn as a
 * connected line stepping downward, one row per stop. This is the classic
 * picture you see in operating systems textbooks.</p>
 */
public final class SvgDiskRenderer {

    private static final int LEFT = 50;
    private static final int RIGHT = 50;
    private static final int TOP_AXIS = 70;
    private static final int ROW_HEIGHT = 34;
    private static final int BOTTOM = 46;

    private SvgDiskRenderer() {
    }

    public static String render(DiskResult result, int axisMax) {
        List<Integer> path = result.path();
        int stops = path.size();
        int maxValue = Math.max(axisMax, path.stream().mapToInt(Integer::intValue).max().orElse(1));
        maxValue = Math.max(1, maxValue);

        int plotWidth = 720;
        int width = LEFT + plotWidth + RIGHT;
        int height = TOP_AXIS + stops * ROW_HEIGHT + BOTTOM;

        StringBuilder svg = new StringBuilder();
        svg.append(String.format(Locale.US,
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" "
                        + "viewBox=\"0 0 %d %d\" font-family=\"'Segoe UI',Roboto,Helvetica,Arial,sans-serif\">%n",
                width, height, width, height));
        svg.append(String.format(Locale.US,
                "  <rect x=\"0\" y=\"0\" width=\"%d\" height=\"%d\" rx=\"14\" fill=\"#111827\"/>%n",
                width, height));
        svg.append(String.format(Locale.US,
                "  <text x=\"%d\" y=\"34\" fill=\"#f9fafb\" font-size=\"20\" font-weight=\"700\">%s</text>%n",
                LEFT, SvgSupport.escape(result.algorithmName())));
        svg.append(String.format(Locale.US,
                "  <text x=\"%d\" y=\"34\" fill=\"#9ca3af\" font-size=\"14\" text-anchor=\"end\">"
                        + "total head movement: %d</text>%n",
                width - RIGHT, result.totalHeadMovement()));

        // Top cylinder axis with a tick and label for every distinct position visited.
        svg.append(String.format(Locale.US,
                "  <line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"#374151\" stroke-width=\"1.5\"/>%n",
                LEFT, TOP_AXIS, LEFT + plotWidth, TOP_AXIS));
        for (int position : new TreeSet<>(path)) {
            int x = xFor(position, maxValue, plotWidth);
            svg.append(String.format(Locale.US,
                    "  <line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"#374151\" stroke-width=\"1\"/>%n",
                    x, TOP_AXIS - 6, x, TOP_AXIS));
            svg.append(String.format(Locale.US,
                    "  <text x=\"%d\" y=\"%d\" fill=\"#9ca3af\" font-size=\"12\" text-anchor=\"middle\">%d</text>%n",
                    x, TOP_AXIS - 10, position));
        }

        // Faint vertical guide lines so the eye can follow each cylinder down.
        for (int position : new TreeSet<>(path)) {
            int x = xFor(position, maxValue, plotWidth);
            svg.append(String.format(Locale.US,
                    "  <line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"#1f2937\" stroke-width=\"1\"/>%n",
                    x, TOP_AXIS, x, height - BOTTOM));
        }

        // The head's journey as a connected line.
        StringBuilder points = new StringBuilder();
        for (int i = 0; i < stops; i++) {
            int x = xFor(path.get(i), maxValue, plotWidth);
            int y = TOP_AXIS + 18 + i * ROW_HEIGHT;
            points.append(x).append(',').append(y).append(' ');
        }
        svg.append(String.format(Locale.US,
                "  <polyline points=\"%s\" fill=\"none\" stroke=\"#4f9dde\" stroke-width=\"2.5\"/>%n",
                points.toString().trim()));

        for (int i = 0; i < stops; i++) {
            int x = xFor(path.get(i), maxValue, plotWidth);
            int y = TOP_AXIS + 18 + i * ROW_HEIGHT;
            String fill = i == 0 ? "#e0c341" : "#4f9dde";
            svg.append(String.format(Locale.US,
                    "  <circle cx=\"%d\" cy=\"%d\" r=\"5\" fill=\"%s\"/>%n", x, y, fill));
            svg.append(String.format(Locale.US,
                    "  <text x=\"%d\" y=\"%d\" fill=\"#e5e7eb\" font-size=\"12\">%d</text>%n",
                    x + 10, y + 4, path.get(i)));
        }

        svg.append("</svg>\n");
        return svg.toString();
    }

    private static int xFor(int position, int maxValue, int plotWidth) {
        return (int) Math.round(LEFT + (position / (double) maxValue) * plotWidth);
    }
}
