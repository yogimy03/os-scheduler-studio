package com.osscheduler.core.page;

import com.osscheduler.core.model.PageResult;
import com.osscheduler.core.model.PageStep;

import java.util.ArrayList;
import java.util.List;

/**
 * Optimal (also called OPT or Belady's) page replacement.
 *
 * <p>When memory is full, throw out the page that will not be needed for the
 * longest time in the future. This produces the fewest possible faults, which is
 * why it is the yardstick every other algorithm is measured against. It cannot
 * be used for real because it needs to see the future, but as a simulation over
 * a known reference string it is perfectly computable.</p>
 */
public final class OptimalPageAlgorithm implements PageReplacementAlgorithm {

    @Override
    public String name() {
        return "Optimal";
    }

    @Override
    public PageResult run(List<Integer> references, int frameCount) {
        if (frameCount <= 0) {
            throw new IllegalArgumentException("Frame count must be greater than zero");
        }

        Integer[] frames = new Integer[frameCount];
        List<PageStep> steps = new ArrayList<>();
        int faults = 0;
        int hits = 0;

        for (int i = 0; i < references.size(); i++) {
            int page = references.get(i);
            boolean fault;
            Integer evicted = null;

            if (PageFrames.contains(frames, page)) {
                fault = false;
                hits++;
            } else {
                fault = true;
                faults++;
                int slot = PageFrames.firstEmpty(frames);
                if (slot < 0) {
                    slot = victimSlot(frames, references, i + 1);
                    evicted = frames[slot];
                }
                frames[slot] = page;
            }

            steps.add(new PageStep(page, fault, PageFrames.snapshot(frames), evicted));
        }

        return new PageResult(name(), frameCount, references, steps, faults, hits);
    }

    /**
     * Finds the slot holding the page whose next use is farthest away. A page
     * never referenced again is treated as infinitely far and wins immediately.
     */
    private static int victimSlot(Integer[] frames, List<Integer> references, int from) {
        int victim = 0;
        int farthest = -1;
        for (int slot = 0; slot < frames.length; slot++) {
            int nextUse = nextUseIndex(frames[slot], references, from);
            if (nextUse > farthest) {
                farthest = nextUse;
                victim = slot;
            }
        }
        return victim;
    }

    /** The next index at or after {@code from} where the page appears, or a huge value if never. */
    private static int nextUseIndex(int page, List<Integer> references, int from) {
        for (int i = from; i < references.size(); i++) {
            if (references.get(i) == page) {
                return i;
            }
        }
        return Integer.MAX_VALUE;
    }
}
