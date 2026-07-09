package com.osscheduler.core.cpu;

import java.util.Optional;

/**
 * The catalogue of CPU scheduling algorithms the app knows about.
 *
 * <p>Each entry carries a short key (used on the command line), a friendly
 * display name (shown in the app), and a flag saying whether it needs a time
 * quantum. The {@link #create} method builds the matching scheduler. Having one
 * central list means the user interface and the command line stay in sync: add
 * an algorithm here and both pick it up automatically.</p>
 */
public enum CpuAlgorithm {

    FCFS("fcfs", "First Come First Serve", false, false),
    SJF("sjf", "Shortest Job First", false, false),
    SRTF("srtf", "Shortest Remaining Time First", true, false),
    LJF("ljf", "Longest Job First", false, false),
    LRTF("lrtf", "Longest Remaining Time First", true, false),
    HRRN("hrrn", "Highest Response Ratio Next", false, false),
    PRIORITY_NP("priority", "Priority (non-preemptive)", false, true),
    PRIORITY_P("priority-p", "Priority (preemptive)", true, true),
    ROUND_ROBIN("rr", "Round Robin", true, true);

    private final String key;
    private final String displayName;
    private final boolean preemptive;
    private final boolean needsQuantum;

    CpuAlgorithm(String key, String displayName, boolean preemptive, boolean needsQuantum) {
        this.key = key;
        this.displayName = displayName;
        this.preemptive = preemptive;
        this.needsQuantum = needsQuantum;
    }

    public String key() {
        return key;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isPreemptive() {
        return preemptive;
    }

    public boolean needsQuantum() {
        return needsQuantum;
    }

    /** Build a ready to run scheduler for this algorithm using the given options. */
    public CpuScheduler create(EngineOptions options) {
        return switch (this) {
            case FCFS -> new FcfsScheduler();
            case SJF -> new SjfScheduler();
            case SRTF -> new SrtfScheduler();
            case LJF -> new LjfScheduler();
            case LRTF -> new LrtfScheduler();
            case HRRN -> new HrrnScheduler();
            case PRIORITY_NP -> new PriorityScheduler();
            case PRIORITY_P -> new PreemptivePriorityScheduler();
            case ROUND_ROBIN -> new RoundRobinScheduler(options.timeQuantum());
        };
    }

    /** Look up an algorithm by its command line key, ignoring case. */
    public static Optional<CpuAlgorithm> fromKey(String key) {
        for (CpuAlgorithm algorithm : values()) {
            if (algorithm.key.equalsIgnoreCase(key)) {
                return Optional.of(algorithm);
            }
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return displayName;
    }
}
