package com.osscheduler.core.model;

import java.util.List;

/**
 * The complete outcome of running one CPU scheduling algorithm.
 *
 * <p>It carries the timeline (what ran when, used to draw the Gantt chart) and
 * the per process metrics. The average helper methods below are what the tables
 * and summary cards in the app display.</p>
 *
 * @param algorithmName a friendly name such as {@code "Round Robin (q=2)"}
 * @param timeline      the ordered list of Gantt blocks, including idle gaps
 * @param metrics       one metrics row per input process, in input order
 */
public record ScheduleResult(
        String algorithmName,
        List<GanttSegment> timeline,
        List<ProcessMetrics> metrics) {

    public ScheduleResult {
        // Store unmodifiable copies so nobody can change the result after the fact.
        timeline = List.copyOf(timeline);
        metrics = List.copyOf(metrics);
    }

    /** The point in time when the very last process finished. */
    public int totalTime() {
        return timeline.isEmpty() ? 0 : timeline.get(timeline.size() - 1).end();
    }

    /** Total time the CPU spent idle across the whole run. */
    public int idleTime() {
        return timeline.stream().filter(GanttSegment::isIdle).mapToInt(GanttSegment::duration).sum();
    }

    /** Time the CPU spent actually running processes. */
    public int busyTime() {
        return totalTime() - idleTime();
    }

    public double averageWaitingTime() {
        return average(metrics.stream().mapToInt(ProcessMetrics::waitingTime).sum());
    }

    public double averageTurnaroundTime() {
        return average(metrics.stream().mapToInt(ProcessMetrics::turnaroundTime).sum());
    }

    public double averageResponseTime() {
        return average(metrics.stream().mapToInt(ProcessMetrics::responseTime).sum());
    }

    /** Percentage of the total run during which the CPU was busy. */
    public double cpuUtilization() {
        int total = totalTime();
        return total == 0 ? 0.0 : (busyTime() * 100.0) / total;
    }

    /** Processes completed per unit of time. */
    public double throughput() {
        int total = totalTime();
        return total == 0 ? 0.0 : (double) metrics.size() / total;
    }

    private double average(int sum) {
        return metrics.isEmpty() ? 0.0 : (double) sum / metrics.size();
    }
}
