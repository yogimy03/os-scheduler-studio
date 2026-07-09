package com.osscheduler.core.page;

import java.util.ArrayList;
import java.util.List;

/**
 * Small shared helpers for working with an array of page frames.
 *
 * <p>Frames are modelled as an {@code Integer[]} where a {@code null} slot means
 * "empty". Using boxed integers lets us tell an empty slot apart from a slot
 * that happens to hold page number zero.</p>
 */
final class PageFrames {

    private PageFrames() {
    }

    /** True if the page currently sits in one of the frames. */
    static boolean contains(Integer[] frames, int page) {
        return indexOf(frames, page) >= 0;
    }

    /** The slot holding the page, or -1 if it is not loaded. */
    static int indexOf(Integer[] frames, int page) {
        for (int i = 0; i < frames.length; i++) {
            if (frames[i] != null && frames[i] == page) {
                return i;
            }
        }
        return -1;
    }

    /** The first empty slot, or -1 if every frame is occupied. */
    static int firstEmpty(Integer[] frames) {
        for (int i = 0; i < frames.length; i++) {
            if (frames[i] == null) {
                return i;
            }
        }
        return -1;
    }

    /** An independent copy of the current frame contents, keeping empty slots. */
    static List<Integer> snapshot(Integer[] frames) {
        List<Integer> copy = new ArrayList<>(frames.length);
        for (Integer frame : frames) {
            copy.add(frame);
        }
        return copy;
    }
}
