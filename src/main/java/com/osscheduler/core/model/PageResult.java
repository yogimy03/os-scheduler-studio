package com.osscheduler.core.model;

import java.util.List;

/**
 * The outcome of running one page replacement algorithm over a reference string.
 *
 * @param algorithmName a friendly name such as {@code "LRU"}
 * @param frameCount    how many page frames memory had available
 * @param references    the reference string that was replayed
 * @param steps         one snapshot per reference, in order
 * @param pageFaults    how many references were not in memory
 * @param pageHits      how many references were already in memory
 */
public record PageResult(
        String algorithmName,
        int frameCount,
        List<Integer> references,
        List<PageStep> steps,
        int pageFaults,
        int pageHits) {

    public PageResult {
        references = List.copyOf(references);
        steps = List.copyOf(steps);
    }

    /** Fraction of references that caused a page fault, from 0.0 to 1.0. */
    public double faultRate() {
        int total = pageFaults + pageHits;
        return total == 0 ? 0.0 : (double) pageFaults / total;
    }

    /** Fraction of references that were already in memory, from 0.0 to 1.0. */
    public double hitRatio() {
        int total = pageFaults + pageHits;
        return total == 0 ? 0.0 : (double) pageHits / total;
    }
}
