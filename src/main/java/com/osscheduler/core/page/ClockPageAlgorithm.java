package com.osscheduler.core.page;

import com.osscheduler.core.model.PageResult;
import com.osscheduler.core.model.PageStep;

import java.util.ArrayList;
import java.util.List;

/**
 * Clock page replacement, also known as the Second Chance algorithm.
 *
 * <p>A practical approximation of LRU that most real operating systems use. The
 * frames are arranged in a circle with a moving hand. Each frame has a reference
 * bit that is set to 1 whenever its page is used. To find a victim, the hand
 * sweeps forward: if it points at a page with a reference bit of 1 it gives that
 * page a second chance by clearing the bit and moving on; the first page it
 * finds with a bit of 0 gets evicted.</p>
 *
 * <p>Convention used here: a page that is loaded or hit has its reference bit set
 * to 1. While there are still empty frames the hand stays where it is; once the
 * frames are full and a page has to be evicted, the hand advances one slot past
 * the victim it just replaced, ready for the next search.</p>
 */
public final class ClockPageAlgorithm implements PageReplacementAlgorithm {

    @Override
    public String name() {
        return "Clock";
    }

    @Override
    public PageResult run(List<Integer> references, int frameCount) {
        if (frameCount <= 0) {
            throw new IllegalArgumentException("Frame count must be greater than zero");
        }

        Integer[] frames = new Integer[frameCount];
        boolean[] referenceBit = new boolean[frameCount];
        List<PageStep> steps = new ArrayList<>();
        int hand = 0;
        int faults = 0;
        int hits = 0;

        for (int page : references) {
            boolean fault;
            Integer evicted = null;

            int existing = PageFrames.indexOf(frames, page);
            if (existing >= 0) {
                fault = false;
                hits++;
                referenceBit[existing] = true; // second chance granted on next sweep
            } else {
                fault = true;
                faults++;
                int slot = PageFrames.firstEmpty(frames);
                if (slot < 0) {
                    // Sweep the hand, clearing reference bits, until we find a 0.
                    while (referenceBit[hand]) {
                        referenceBit[hand] = false;
                        hand = (hand + 1) % frameCount;
                    }
                    slot = hand;
                    evicted = frames[slot];
                    hand = (hand + 1) % frameCount;
                }
                frames[slot] = page;
                referenceBit[slot] = true;
            }

            steps.add(new PageStep(page, fault, PageFrames.snapshot(frames), evicted));
        }

        return new PageResult(name(), frameCount, references, steps, faults, hits);
    }
}
