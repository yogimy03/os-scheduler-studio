package com.osscheduler.viz;

import com.osscheduler.core.model.GanttSegment;
import com.osscheduler.core.model.ScheduleResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Turns a CPU schedule into a standalone SVG image.
 *
 * <p>The output is a single self contained file with a dark card, coloured
 * blocks for each process, a time axis and a footer of averages. It is used both
 * for the pictures embedded in the README and for the "export image" feature.</p>
 */
public final class SvgGanttRenderer {

    private static final int LEFT = 30;
    private static final int RIGHT = 30;
    private static final int TOP = 64;
    private static final int BAR_HEIGHT = 60;
    private static final int AXIS_GAP = 26;
    private static final int FOOTER = 54;

    private SvgGanttRenderer() {
    }

    public static String render(ScheduleResult result) {
        List<GanttSegment> timeline = result.timeline();
        int totalTime = Math.max(1, result.totalTime());
        int startTime = timeline.isEmpty() ? 0 : timeline.get(0).start();

        double pxPerUnit = SvgSupport.clamp(760.0 / totalTime, 14.0, 52.0);
        int chartWidth = (int) Math.round(totalTime * pxPerUnit);
        int width = LEFT + chartWidth + RIGHT;
        int barTop = TOP;
        int axisY = barTop + BAR_HEIGHT + AXIS_GAP;
        int height = axisY + FOOTER;

        List<String> labels = new ArrayList<>();
        for (GanttSegment segment : timeline) {
            if (!segment.isIdle()) {
                labels.add(segment.label());
            }
        }
        Map<String, String> colors = Palette.assign(labels);

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

        int x0 = LEFT;
        // Blocks.
        for (GanttSegment segment : timeline) {
            int x = (int) Math.round(x0 + (segment.start() - startTime) * pxPerUnit);
            int w = Math.max(1, (int) Math.round(segment.duration() * pxPerUnit));
            String fill = segment.isIdle() ? Palette.IDLE_COLOR : colors.get(segment.label());
            svg.append(String.format(Locale.US,
                    "  <rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" rx=\"6\" fill=\"%s\" "
                            + "stroke=\"#0b1220\" stroke-width=\"2\"/>%n",
                    x, barTop, w, BAR_HEIGHT, fill));
            if (w >= 22) {
                svg.append(String.format(Locale.US,
                        "  <text x=\"%d\" y=\"%d\" fill=\"#f9fafb\" font-size=\"15\" font-weight=\"600\" "
                                + "text-anchor=\"middle\">%s</text>%n",
                        x + w / 2, barTop + BAR_HEIGHT / 2 + 5, SvgSupport.escape(segment.label())));
            }
        }

        // Time axis: a tick at each block boundary.
        svg.append(String.format(Locale.US,
                "  <line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"#374151\" stroke-width=\"1.5\"/>%n",
                LEFT, barTop + BAR_HEIGHT + 6, LEFT + chartWidth, barTop + BAR_HEIGHT + 6));
        List<Integer> boundaries = new ArrayList<>();
        boundaries.add(startTime);
        for (GanttSegment segment : timeline) {
            boundaries.add(segment.end());
        }
        for (int boundary : boundaries) {
            int x = (int) Math.round(x0 + (boundary - startTime) * pxPerUnit);
            svg.append(String.format(Locale.US,
                    "  <line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"#374151\" stroke-width=\"1.5\"/>%n",
                    x, barTop + BAR_HEIGHT + 2, x, barTop + BAR_HEIGHT + 12));
            svg.append(String.format(Locale.US,
                    "  <text x=\"%d\" y=\"%d\" fill=\"#9ca3af\" font-size=\"13\" text-anchor=\"middle\">%d</text>%n",
                    x, axisY, boundary));
        }

        // Footer summary.
        String footer = String.format(Locale.US,
                "avg waiting %.2f    avg turnaround %.2f    avg response %.2f    CPU %.0f%%",
                result.averageWaitingTime(),
                result.averageTurnaroundTime(),
                result.averageResponseTime(),
                result.cpuUtilization());
        svg.append(String.format(Locale.US,
                "  <text x=\"%d\" y=\"%d\" fill=\"#9ca3af\" font-size=\"14\">%s</text>%n",
                LEFT, height - 18, SvgSupport.escape(footer)));

        svg.append("</svg>\n");
        return svg.toString();
    }
}
