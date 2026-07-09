package com.osscheduler.viz;

import com.osscheduler.core.model.PageResult;
import com.osscheduler.core.model.PageStep;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Renders a page replacement run as plain text for the command line: a column
 * per reference, one row per frame, and an F or H marking each fault or hit.
 */
public final class AsciiPageRenderer {

    private AsciiPageRenderer() {
    }

    public static String render(PageResult result) {
        List<String> headers = new ArrayList<>();
        headers.add("");
        for (int reference : result.references()) {
            headers.add(Integer.toString(reference));
        }

        List<List<String>> rows = new ArrayList<>();
        for (int slot = 0; slot < result.frameCount(); slot++) {
            List<String> row = new ArrayList<>();
            row.add("Frame " + (slot + 1));
            for (PageStep step : result.steps()) {
                Integer value = step.framesAfter().get(slot);
                row.add(value == null ? "." : value.toString());
            }
            rows.add(row);
        }

        List<String> statusRow = new ArrayList<>();
        statusRow.add("Fault?");
        for (PageStep step : result.steps()) {
            statusRow.add(step.fault() ? "F" : "H");
        }
        rows.add(statusRow);

        String table = AsciiTable.render(headers, rows);
        String summary = String.format(Locale.US,
                "Page faults : %d%n"
                        + "Page hits   : %d%n"
                        + "Hit ratio   : %.2f%%%n"
                        + "Fault rate  : %.2f%%",
                result.pageFaults(),
                result.pageHits(),
                result.hitRatio() * 100.0,
                result.faultRate() * 100.0);

        return result.algorithmName() + "\n"
                + "=".repeat(result.algorithmName().length()) + "\n\n"
                + table + "\n\n" + summary;
    }
}
