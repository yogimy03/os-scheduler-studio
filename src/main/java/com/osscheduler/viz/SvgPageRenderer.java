package com.osscheduler.viz;

import com.osscheduler.core.model.PageResult;
import com.osscheduler.core.model.PageStep;

import java.util.List;
import java.util.Locale;

/**
 * Turns a page replacement run into a standalone SVG grid: one column per
 * reference, one row per frame, and a bottom row that marks each fault or hit.
 */
public final class SvgPageRenderer {

    private static final int LABEL_WIDTH = 74;
    private static final int CELL_WIDTH = 34;
    private static final int CELL_HEIGHT = 30;
    private static final int TOP = 52;
    private static final int PADDING = 20;

    private SvgPageRenderer() {
    }

    public static String render(PageResult result) {
        List<PageStep> steps = result.steps();
        int columns = steps.size();
        int rows = result.frameCount() + 2; // header row + frames + fault row

        int gridLeft = PADDING + LABEL_WIDTH;
        int width = gridLeft + columns * CELL_WIDTH + PADDING;
        int height = TOP + rows * CELL_HEIGHT + PADDING + 24;

        StringBuilder svg = new StringBuilder();
        svg.append(String.format(Locale.US,
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" "
                        + "viewBox=\"0 0 %d %d\" font-family=\"'Segoe UI',Roboto,Helvetica,Arial,sans-serif\">%n",
                width, height, width, height));
        svg.append(String.format(Locale.US,
                "  <rect x=\"0\" y=\"0\" width=\"%d\" height=\"%d\" rx=\"14\" fill=\"#111827\"/>%n",
                width, height));
        svg.append(String.format(Locale.US,
                "  <text x=\"%d\" y=\"32\" fill=\"#f9fafb\" font-size=\"18\" font-weight=\"700\">%s</text>%n",
                PADDING, SvgSupport.escape(result.algorithmName() + " page replacement")));

        int headerY = TOP;
        // Row labels down the left side.
        text(svg, PADDING, headerY + CELL_HEIGHT - 10, "#9ca3af", 12, "ref");
        for (int slot = 0; slot < result.frameCount(); slot++) {
            int y = headerY + (slot + 1) * CELL_HEIGHT + CELL_HEIGHT - 10;
            text(svg, PADDING, y, "#9ca3af", 12, "frame " + (slot + 1));
        }
        text(svg, PADDING, headerY + (rows - 1) * CELL_HEIGHT + CELL_HEIGHT - 10, "#9ca3af", 12, "fault?");

        for (int col = 0; col < columns; col++) {
            PageStep step = steps.get(col);
            int x = gridLeft + col * CELL_WIDTH;

            // Reference number header.
            cell(svg, x, headerY, "#16202f");
            text(svg, x + CELL_WIDTH / 2, headerY + CELL_HEIGHT - 10, "#e5e7eb", 13,
                    Integer.toString(step.reference()), true);

            // Frame contents.
            for (int slot = 0; slot < result.frameCount(); slot++) {
                int y = headerY + (slot + 1) * CELL_HEIGHT;
                cell(svg, x, y, "#1f2937");
                Integer value = step.framesAfter().get(slot);
                if (value != null) {
                    text(svg, x + CELL_WIDTH / 2, y + CELL_HEIGHT - 10, "#e5e7eb", 13,
                            value.toString(), true);
                }
            }

            // Fault or hit marker.
            int faultY = headerY + (rows - 1) * CELL_HEIGHT;
            cell(svg, x, faultY, step.fault() ? "#7f1d1d" : "#14532d");
            text(svg, x + CELL_WIDTH / 2, faultY + CELL_HEIGHT - 10,
                    step.fault() ? "#fee2e2" : "#dcfce7", 13, step.fault() ? "F" : "H", true);
        }

        String summary = String.format(Locale.US,
                "page faults %d    page hits %d    hit ratio %.0f%%",
                result.pageFaults(), result.pageHits(), result.hitRatio() * 100.0);
        text(svg, PADDING, height - 14, "#9ca3af", 13, SvgSupport.escape(summary));

        svg.append("</svg>\n");
        return svg.toString();
    }

    private static void cell(StringBuilder svg, int x, int y, String fill) {
        svg.append(String.format(Locale.US,
                "  <rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"%s\" "
                        + "stroke=\"#0b1220\" stroke-width=\"1\"/>%n",
                x, y, CELL_WIDTH, CELL_HEIGHT, fill));
    }

    private static void text(StringBuilder svg, int x, int y, String fill, int size, String value) {
        text(svg, x, y, fill, size, value, false);
    }

    private static void text(StringBuilder svg, int x, int y, String fill, int size,
                             String value, boolean center) {
        String anchor = center ? " text-anchor=\"middle\"" : "";
        svg.append(String.format(Locale.US,
                "  <text x=\"%d\" y=\"%d\" fill=\"%s\" font-size=\"%d\"%s>%s</text>%n",
                x, y, fill, size, anchor, value));
    }
}
