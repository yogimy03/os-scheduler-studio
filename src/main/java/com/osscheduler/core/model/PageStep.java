package com.osscheduler.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A snapshot of memory after handling one page reference.
 *
 * @param reference   the page number that was requested at this step
 * @param fault       true if the page was not already in memory (a page fault)
 * @param framesAfter the contents of the frames right after this step, in slot
 *                    order; {@code null} entries mean the slot is still empty
 * @param evicted     the page that was thrown out to make room, or {@code null}
 *                    if nothing had to be evicted
 */
public record PageStep(
        int reference,
        boolean fault,
        List<Integer> framesAfter,
        Integer evicted) {

    public PageStep {
        // A frame slot can legitimately be empty (null), and List.copyOf refuses
        // null elements, so we defend the list with our own unmodifiable copy.
        framesAfter = Collections.unmodifiableList(new ArrayList<>(framesAfter));
    }

    /** True when the requested page was already in memory. */
    public boolean isHit() {
        return !fault;
    }
}
