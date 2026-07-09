package com.osscheduler.core.model;

/**
 * A single process (or job) that the CPU needs to run.
 *
 * <p>This is a Java {@code record}, so it is immutable: once you create a
 * process you cannot change its numbers. That keeps the simulation honest,
 * because an algorithm can never accidentally rewrite its own input.</p>
 *
 * @param id          a short label shown in charts, for example {@code "P1"}
 * @param arrivalTime the moment the process shows up in the ready queue
 * @param burstTime   how many time units of CPU work the process needs
 * @param priority    priority value where a smaller number means more important
 */
public record CpuProcess(String id, int arrivalTime, int burstTime, int priority) {

    public CpuProcess {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Process id must not be empty");
        }
        if (arrivalTime < 0) {
            throw new IllegalArgumentException("Arrival time cannot be negative for " + id);
        }
        if (burstTime <= 0) {
            throw new IllegalArgumentException("Burst time must be greater than zero for " + id);
        }
    }

    /** Convenience constructor for algorithms that do not use priority. */
    public CpuProcess(String id, int arrivalTime, int burstTime) {
        this(id, arrivalTime, burstTime, 0);
    }
}
