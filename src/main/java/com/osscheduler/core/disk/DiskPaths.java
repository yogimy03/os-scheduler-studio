package com.osscheduler.core.disk;

import java.util.List;

/**
 * Small shared helpers used by the disk scheduling algorithms.
 */
final class DiskPaths {

    private DiskPaths() {
    }

    /**
     * Total head movement for a journey: the sum of the absolute distances
     * between each pair of consecutive stops.
     */
    static int totalMovement(List<Integer> path) {
        int movement = 0;
        for (int i = 1; i < path.size(); i++) {
            movement += Math.abs(path.get(i) - path.get(i - 1));
        }
        return movement;
    }
}
