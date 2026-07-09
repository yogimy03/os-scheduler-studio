package com.osscheduler.core.page;

import com.osscheduler.core.model.PageResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the page replacement algorithms. The long reference string is the
 * standard textbook example, whose fault counts for FIFO, LRU and Optimal are
 * well known (15, 12 and 9 with three frames).
 */
class PageReplacementTest {

    private static final List<Integer> STRING =
            List.of(7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2, 1, 2, 0, 1, 7, 0, 1);
    private static final int FRAMES = 3;

    @Test
    void fifoMatchesTextbook() {
        PageResult result = new FifoPageAlgorithm().run(STRING, FRAMES);
        assertEquals(15, result.pageFaults());
        assertEquals(5, result.pageHits());
    }

    @Test
    void lruMatchesTextbook() {
        assertEquals(12, new LruPageAlgorithm().run(STRING, FRAMES).pageFaults());
    }

    @Test
    void optimalMatchesTextbook() {
        assertEquals(9, new OptimalPageAlgorithm().run(STRING, FRAMES).pageFaults());
    }

    @Test
    void fifoEvictsTheOldestPage() {
        // 1,2,3 fill the frames; 1 and 2 hit; 4 evicts the oldest page, 1.
        PageResult result = new FifoPageAlgorithm().run(List.of(1, 2, 3, 1, 2, 4), 3);
        assertEquals(4, result.pageFaults());
        assertEquals(2, result.pageHits());
        assertEquals(List.of(4, 2, 3), result.steps().get(5).framesAfter());
        assertEquals(Integer.valueOf(1), result.steps().get(5).evicted());
    }

    @Test
    void lruKeepsRecentlyUsedPages() {
        // After 1,2,3,1: page 2 is the least recently used, so 4 evicts 2.
        PageResult result = new LruPageAlgorithm().run(List.of(1, 2, 3, 1, 4), 3);
        assertEquals(Integer.valueOf(2), result.steps().get(4).evicted());
    }

    @Test
    void hitsAndFaultsAlwaysCoverEveryReference() {
        for (PageAlgorithm algorithm : PageAlgorithm.values()) {
            PageResult result = algorithm.create().run(STRING, FRAMES);
            assertEquals(STRING.size(), result.pageFaults() + result.pageHits(),
                    algorithm.displayName() + ": every reference is a hit or a fault");
        }
    }

    @Test
    void optimalIsNeverWorseThanTheOthers() {
        int optimal = new OptimalPageAlgorithm().run(STRING, FRAMES).pageFaults();
        for (PageAlgorithm algorithm : PageAlgorithm.values()) {
            int faults = algorithm.create().run(STRING, FRAMES).pageFaults();
            assertTrue(optimal <= faults,
                    "Optimal (" + optimal + ") should not fault more than "
                            + algorithm.displayName() + " (" + faults + ")");
        }
    }

    @Test
    void firstReferencesAreAlwaysFaults() {
        PageResult result = new FifoPageAlgorithm().run(List.of(5, 6, 7), 3);
        assertEquals(3, result.pageFaults());
        assertEquals(0, result.pageHits());
    }
}
