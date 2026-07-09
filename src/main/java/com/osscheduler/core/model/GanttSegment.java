package com.osscheduler.core.model;

/**
 * One coloured block on a Gantt chart: a stretch of time during which the CPU
 * was doing one thing.
 *
 * <p>If {@link #label} equals {@link #IDLE} the CPU was sitting idle because no
 * process had arrived yet. Otherwise the label is a process id such as
 * {@code "P2"}.</p>
 *
 * @param label the process id running in this block, or {@code "idle"}
 * @param start the time the block begins (inclusive)
 * @param end   the time the block ends (exclusive)
 */
public record GanttSegment(String label, int start, int end) {

    /** The label used for stretches of time where the CPU had nothing to run. */
    public static final String IDLE = "idle";

    public GanttSegment {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("Gantt segment must have a label");
        }
        if (end < start) {
            throw new IllegalArgumentException("Gantt segment end cannot be before its start");
        }
    }

    /** How many time units this block covers. */
    public int duration() {
        return end - start;
    }

    /** True when this block represents idle CPU time. */
    public boolean isIdle() {
        return IDLE.equals(label);
    }
}
