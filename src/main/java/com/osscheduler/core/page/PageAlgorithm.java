package com.osscheduler.core.page;

import java.util.Optional;

/**
 * The catalogue of page replacement algorithms the app knows about.
 */
public enum PageAlgorithm {

    FIFO("fifo", "FIFO"),
    LRU("lru", "LRU"),
    OPTIMAL("optimal", "Optimal"),
    LFU("lfu", "LFU"),
    CLOCK("clock", "Clock (Second Chance)"),
    MRU("mru", "MRU");

    private final String key;
    private final String displayName;

    PageAlgorithm(String key, String displayName) {
        this.key = key;
        this.displayName = displayName;
    }

    public String key() {
        return key;
    }

    public String displayName() {
        return displayName;
    }

    public PageReplacementAlgorithm create() {
        return switch (this) {
            case FIFO -> new FifoPageAlgorithm();
            case LRU -> new LruPageAlgorithm();
            case OPTIMAL -> new OptimalPageAlgorithm();
            case LFU -> new LfuPageAlgorithm();
            case CLOCK -> new ClockPageAlgorithm();
            case MRU -> new MruPageAlgorithm();
        };
    }

    public static Optional<PageAlgorithm> fromKey(String key) {
        for (PageAlgorithm algorithm : values()) {
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
