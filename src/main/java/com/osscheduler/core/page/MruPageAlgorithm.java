package com.osscheduler.core.page;

import com.osscheduler.core.model.PageResult;
import com.osscheduler.core.model.PageStep;

import java.util.ArrayList;
import java.util.List;

/**
 * Most Recently Used page replacement.
 *
 * <p>The opposite instinct to LRU: when room is needed, evict the page that was
 * touched most recently. This sounds odd, but it wins for access patterns that
 * cycle through a large set of pages once each, where the page you just used is
 * the one you are least likely to need again soon.</p>
 */
public final class MruPageAlgorithm implements PageReplacementAlgorithm {

    @Override
    public String name() {
        return "MRU";
    }

    @Override
    public PageResult run(List<Integer> references, int frameCount) {
        if (frameCount <= 0) {
            throw new IllegalArgumentException("Frame count must be greater than zero");
        }

        Integer[] frames = new Integer[frameCount];
        // recency.get(last) is the most recently used page.
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
                recency.add(page);
            } else {
                fault = true;
                faults++;
                int slot = PageFrames.firstEmpty(frames);
                if (slot < 0) {
                    evicted = recency.remove(recency.size() - 1); // the most recently used
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
