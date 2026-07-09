package com.osscheduler.ui;

import com.osscheduler.core.model.CpuProcess;

/**
 * A plain, editable row backing the process table in the CPU tab.
 *
 * <p>The table edits these mutable fields directly, and {@link #toProcess()}
 * converts a row into an immutable {@link CpuProcess} when it is time to run the
 * algorithm.</p>
 */
public final class ProcessRow {

    private String id;
    private int arrival;
    private int burst;
    private int priority;

    public ProcessRow(String id, int arrival, int burst, int priority) {
        this.id = id;
        this.arrival = arrival;
        this.burst = burst;
        this.priority = priority;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getArrival() {
        return arrival;
    }

    public void setArrival(int arrival) {
        this.arrival = arrival;
    }

    public int getBurst() {
        return burst;
    }

    public void setBurst(int burst) {
        this.burst = burst;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public CpuProcess toProcess() {
        return new CpuProcess(id, arrival, burst, priority);
    }
}
