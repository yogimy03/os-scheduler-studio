package com.osscheduler.core.model;

import java.util.List;

/**
 * The outcome of running one disk scheduling algorithm.
 *
 * <p>The {@link #path} is the full journey of the read/write head. It starts
 * with the head's initial position and then lists every position it stopped at,
 * in order. Some algorithms add turning points that were not actual requests
 * (for example SCAN walking all the way to the last cylinder), and those show up
 * in the path too.</p>
 *
 * @param algorithmName     a friendly name such as {@code "SSTF"}
 * @param startHead         where the head began
 * @param path              every head position visited, starting position first
 * @param totalHeadMovement the sum of the distances between consecutive stops
 */
public record DiskResult(
        String algorithmName,
        int startHead,
        List<Integer> path,
        int totalHeadMovement) {

    public DiskResult {
        path = List.copyOf(path);
    }

    /** The requests in the order the algorithm actually serviced them. */
    public List<Integer> serviceOrder() {
        // Everything after the starting position is a stop the head made.
        return path.subList(1, path.size());
    }
}
