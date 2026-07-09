package com.osscheduler.core.cpu;

/**
 * Extra settings some CPU algorithms need.
 *
 * <p>Right now the only tunable knob is the Round Robin time quantum. Keeping it
 * in its own small record means the registry can build any algorithm from a
 * single, consistent set of options.</p>
 *
 * @param timeQuantum the slice size used by Round Robin
 */
public record EngineOptions(int timeQuantum) {

    public EngineOptions {
        if (timeQuantum <= 0) {
            throw new IllegalArgumentException("Time quantum must be greater than zero");
        }
    }

    /** Sensible defaults for a quick demo run. */
    public static EngineOptions defaults() {
        return new EngineOptions(2);
    }
}
