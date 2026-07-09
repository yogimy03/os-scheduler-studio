package com.osscheduler.core.page;

import com.osscheduler.core.model.PageResult;
import com.osscheduler.core.model.PageStep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Least Frequently Used page replacement.
 *
 * <p>Count how many times each page has been referenced while it has been in
 * memory. When room is needed, evict the page with the smallest count. If
 * several pages tie on count, this implementation evicts the one that has been
 * in memory the longest, so the result is always deterministic.</p>
 */
public final class LfuPageAlgorithm implements PageReplacementAlgorithm {

    @Override
    public String name() {
        return "LFU";
    }

    @Override
    public PageResult run(List<Integer> references, int frameCount) {
        if (frameCount <= 0) {
            throw new IllegalArgumentException("Frame count must be greater than zero");
        }

        Integer[] frames = new Integer[frameCount];
        Map<Integer, Integer> frequency = new HashMap<>();
        Map<Integer, Integer> loadedAt = new HashMap<>(); // tie breaker: smaller is older
        List<PageStep> steps = new ArrayList<>();
        int faults = 0;
        int hits = 0;
        int clock = 0;

        for (int page : references) {
            boolean fault;
            Integer evicted = null;

            if (PageFrames.contains(frames, page)) {
                fault = false;
                hits++;
                frequency.merge(page, 1, Integer::sum);
            } else {
                fault = true;
                faults++;
                int slot = PageFrames.firstEmpty(frames);
                if (slot < 0) {
                    evicted = leastFrequent(frames, frequency, loadedAt);
                    slot = PageFrames.indexOf(frames, evicted);
                    frequency.remove(evicted);
                    loadedAt.remove(evicted);
                }
                frames[slot] = page;
                frequency.put(page, 1);
                loadedAt.put(page, clock++);
            }

            steps.add(new PageStep(page, fault, PageFrames.snapshot(frames), evicted));
        }

        return new PageResult(name(), frameCount, references, steps, faults, hits);
    }

    private static int leastFrequent(Integer[] frames, Map<Integer, Integer> frequency,
                                     Map<Integer, Integer> loadedAt) {
        int victim = frames[0];
        for (Integer candidate : frames) {
            if (candidate == null) {
                continue;
            }
            int candidateFreq = frequency.get(candidate);
            int victimFreq = frequency.get(victim);
            if (candidateFreq < victimFreq
                    || (candidateFreq == victimFreq && loadedAt.get(candidate) < loadedAt.get(victim))) {
                victim = candidate;
            }
        }
        return victim;
    }
}
