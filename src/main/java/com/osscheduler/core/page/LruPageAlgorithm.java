package com.osscheduler.core.page;

import com.osscheduler.core.model.PageResult;
import com.osscheduler.core.model.PageStep;

import java.util.ArrayList;
import java.util.List;

/**
 * Least Recently Used page replacement.
 *
 * <p>Throw out the page that has gone the longest without being touched. The
 * idea is that a page used recently is likely to be used again soon, so the one
 * gathering dust is the safest to evict. It usually beats FIFO and never suffers
 * from Belady's anomaly.</p>
 */
public final class LruPageAlgorithm implements PageReplacementAlgorithm {

    @Override
    public String name() {
        return "LRU";
    }

    @Override
    public PageResult run(List<Integer> references, int frameCount) {
        if (frameCount <= 0) {
            throw new IllegalArgumentException("Frame count must be greater than zero");
        }

        Integer[] frames = new Integer[frameCount];
        // recency.get(0) is the least recently used, the last entry the most.
        List<Integer> recency = new ArrayList<>();
        List<PageStep> steps = new ArrayList<>();
        int faults = 0;
        int hits = 0;

        for (int page : references) {
            boolean fault;
            Integer evicted = null;

            if (PageFrames.contains(frames, page)) {
                fault = false;
                hits++;
                recency.remove((Integer) page);
                recency.add(page); // touched, so now the most recently used
            } else {
                fault = true;
                faults++;
                int slot = PageFrames.firstEmpty(frames);
                if (slot < 0) {
                    evicted = recency.remove(0); // the least recently used
                    slot = PageFrames.indexOf(frames, evicted);
                }
                frames[slot] = page;
                recency.add(page);
            }

            steps.add(new PageStep(page, fault, PageFrames.snapshot(frames), evicted));
        }

        return new PageResult(name(), frameCount, references, steps, faults, hits);
    }
}
