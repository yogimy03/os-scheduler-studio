package com.osscheduler.viz;

import java.util.List;
import java.util.Locale;

/**
 * A compact horizontal bar chart in SVG, used to compare one metric across
 * several algorithms (for example average waiting time for every CPU
 * algorithm on the same input).
 */
public final class SvgBarChartRenderer {

    private static final int LEFT = 210;   // room for the algorithm names
    private static final int RIGHT = 70;   // room for the value labels
    private static final int TOP = 58;
    private static final int ROW_HEIGHT = 34;
    private static final int BAR_HEIGHT = 22;
    private static final int BOTTOM = 24;

    private SvgBarChartRenderer() {
    }

    public static String render(String title, List<String> labels, List<Double> values) {
        if (labels.size() != values.size()) {
            throw new IllegalArgumentException("labels and values must be the same length");
        }
        int rows = labels.size();
        double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        if (max <= 0) {
            max = 1.0;
        }

        int plotWidth = 420;
        int width = LEFT + plotWidth + RIGHT;
        int height = TOP + rows * ROW_HEIGHT + BOTTOM;

        StringBuilder svg = new StringBuilder();
        svg.append(String.format(Locale.US,
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" "
                        + "viewBox=\"0 0 %d %d\" font-family=\"'Segoe UI',Roboto,Helvetica,Arial,sans-serif\">%n",
                width, height, width, height));
        svg.append(String.format(Locale.US,
                "  <rect x=\"0\" y=\"0\" width=\"%d\" height=\"%d\" rx=\"14\" fill=\"#111827\"/>%n",
                width, height));
        svg.append(String.format(Locale.US,
                "  <text x=\"20\" y=\"34\" fill=\"#f9fafb\" font-size=\"18\" font-weight=\"700\">%s</text>%n",
                SvgSupport.escape(title)));

        for (int i = 0; i < rows; i++) {
            int y = TOP + i * ROW_HEIGHT;
            double value = values.get(i);
            int barWidth = (int) Math.round((value / max) * plotWidth);
            barWidth = Math.max(barWidth, value > 0 ? 2 : 0);
            String color = Palette.COLORS.get(i % Palette.COLORS.size());

            svg.append(String.format(Locale.US,
                    "  <text x=\"%d\" y=\"%d\" fill=\"#e5e7eb\" font-size=\"14\" text-anchor=\"end\">%s</text>%n",
                    LEFT - 12, y + BAR_HEIGHT - 5, SvgSupport.escape(labels.get(i))));
            svg.append(String.format(Locale.US,
                    "  <rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" rx=\"5\" fill=\"%s\"/>%n",
                    LEFT, y, barWidth, BAR_HEIGHT, color));
            svg.append(String.format(Locale.US,
                    "  <text x=\"%d\" y=\"%d\" fill=\"#9ca3af\" font-size=\"13\">%.2f</text>%n",
                    LEFT + barWidth + 8, y + BAR_HEIGHT - 5, value));
        }

        svg.append("</svg>\n");
        return svg.toString();
    }
}
