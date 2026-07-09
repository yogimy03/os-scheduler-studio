package com.osscheduler.core.disk;

import java.util.Optional;

/**
 * The catalogue of disk scheduling algorithms the app knows about.
 *
 * <p>Each entry has a command line key, a friendly name, and a flag saying
 * whether it relies on the sweep direction. The sweep algorithms (SCAN, C-SCAN,
 * LOOK, C-LOOK) do; FCFS and SSTF do not.</p>
 */
public enum DiskAlgorithm {

    FCFS("fcfs", "First Come First Serve", false),
    SSTF("sstf", "Shortest Seek Time First", false),
    SCAN("scan", "SCAN (elevator)", true),
    CSCAN("cscan", "C-SCAN", true),
    LOOK("look", "LOOK", true),
    CLOOK("clook", "C-LOOK", true);

    private final String key;
    private final String displayName;
    private final boolean usesDirection;

    DiskAlgorithm(String key, String displayName, boolean usesDirection) {
        this.key = key;
        this.displayName = displayName;
        this.usesDirection = usesDirection;
    }

    public String key() {
        return key;
    }

    public String displayName() {
        return displayName;
    }

    public boolean usesDirection() {
        return usesDirection;
    }

    public DiskScheduler create() {
        return switch (this) {
            case FCFS -> new FcfsDiskScheduler();
            case SSTF -> new SstfDiskScheduler();
            case SCAN -> new ScanDiskScheduler();
            case CSCAN -> new CscanDiskScheduler();
            case LOOK -> new LookDiskScheduler();
            case CLOOK -> new ClookDiskScheduler();
        };
    }

    public static Optional<DiskAlgorithm> fromKey(String key) {
        for (DiskAlgorithm algorithm : values()) {
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
