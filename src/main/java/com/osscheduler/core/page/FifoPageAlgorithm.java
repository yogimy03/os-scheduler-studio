package com.osscheduler.core.page;

import com.osscheduler.core.model.PageResult;
import com.osscheduler.core.model.PageStep;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * First In First Out page replacement.
 *
 * <p>When a new page needs a frame and memory is full, throw out whichever page
 * has been in memory the longest, regardless of how often it is used. Simple,
 * but it can suffer from Belady's anomaly, where adding more frames actually
 * causes more faults.</p>
 */
public final class FifoPageAlgorithm implements PageReplacementAlgorithm {

    @Override
    public String name() {
        return "FIFO";
    }

    @Override
    public PageResult run(List<Integer> references, int frameCount) {
        if (frameCount <= 0) {
            throw new IllegalArgumentException("Frame count must be greater than zero");
        }

        Integer[] frames = new Integer[frameCount];
        Deque<Integer> insertionOrder = new ArrayDeque<>();
        List<PageStep> steps = new ArrayList<>();
        int faults = 0;
        int hits = 0;

        for (int page : references) {
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
                    evicted = insertionOrder.poll();
                    slot = PageFrames.indexOf(frames, evicted);
                }
                frames[slot] = page;
                insertionOrder.add(page);
            }

            steps.add(new PageStep(page, fault, PageFrames.snapshot(frames), evicted));
        }

        return new PageResult(name(), frameCount, references, steps, faults, hits);
    }
}
