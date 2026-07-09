package com.osscheduler.viz;

import com.osscheduler.core.model.GanttSegment;
import com.osscheduler.core.model.ProcessMetrics;
import com.osscheduler.core.model.ScheduleResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Draws a CPU schedule as plain text: a Gantt strip, a metrics table and a
 * short summary. This is what the command line tool prints.
 */
public final class AsciiGanttRenderer {

    private AsciiGanttRenderer() {
    }

    /** The full text block for one algorithm run. */
    public static String render(ScheduleResult result) {
        StringBuilder out = new StringBuilder();
        out.append(result.algorithmName()).append('\n');
        out.append("=".repeat(result.algorithmName().length())).append("\n\n");
        out.append(gantt(result.timeline())).append("\n\n");
        out.append(metricsTable(result)).append('\n');
        out.append(summary(result));
        return out.toString();
    }

    /** Just the Gantt strip with time markers underneath the block boundaries. */
    public static String gantt(List<GanttSegment> timeline) {
        if (timeline.isEmpty()) {
            return "(no activity)";
        }

        StringBuilder bars = new StringBuilder("|");
        List<Integer> boundaryColumns = new ArrayList<>();
        List<String> boundaryTimes = new ArrayList<>();
        boundaryColumns.add(0);
        boundaryTimes.add(Integer.toString(timeline.get(0).start()));

        for (GanttSegment segment : timeline) {
            int inner = Math.max(3, segment.label().length());
            String cell = " " + center(segment.label(), inner) + " ";
            bars.append(cell).append('|');
            boundaryColumns.add(bars.length() - 1);
            boundaryTimes.add(Integer.toString(segment.end()));
        }

        String times = placeLabels(boundaryColumns, boundaryTimes);
        return bars + "\n" + times;
    }

    /** The per process metrics as a bordered table. */
    public static String metricsTable(ScheduleResult result) {
        List<String> headers = List.of(
                "Process", "Arrival", "Burst", "Completion", "Turnaround", "Waiting", "Response");
        List<List<String>> rows = new ArrayList<>();
        for (ProcessMetrics m : result.metrics()) {
            rows.add(List.of(
                    m.id(),
                    Integer.toString(m.arrivalTime()),
                    Integer.toString(m.burstTime()),
                    Integer.toString(m.completionTime()),
                    Integer.toString(m.turnaroundTime()),
                    Integer.toString(m.waitingTime()),
                    Integer.toString(m.responseTime())));
        }
        return AsciiTable.render(headers, rows);
    }

    /** A one paragraph summary of the averages and utilisation. */
    public static String summary(ScheduleResult result) {
        return String.format(Locale.US,
                "Average waiting time    : %.2f%n"
                        + "Average turnaround time : %.2f%n"
                        + "Average response time   : %.2f%n"
                        + "CPU utilisation         : %.2f%%%n"
                        + "Throughput              : %.3f processes per unit time",
                result.averageWaitingTime(),
                result.averageTurnaroundTime(),
                result.averageResponseTime(),
                result.cpuUtilization(),
                result.throughput());
    }

    private static String center(String text, int width) {
        int total = width - text.length();
        int left = total / 2;
        int right = total - left;
        return " ".repeat(left) + text + " ".repeat(right);
    }

    private static String placeLabels(List<Integer> columns, List<String> labels) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            int column = columns.get(i);
            if (line.length() < column) {
                line.append(" ".repeat(column - line.length()));
            } else if (line.length() > column) {
                line.append(' '); // keep at least one gap so numbers never touch
            }
            line.append(labels.get(i));
        }
        return line.toString();
    }
}
