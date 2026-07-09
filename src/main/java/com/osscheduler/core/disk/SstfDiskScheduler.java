package com.osscheduler.core.disk;

import com.osscheduler.core.model.DiskDirection;
import com.osscheduler.core.model.DiskResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Shortest Seek Time First disk scheduling.
 *
 * <p>From wherever the head is, always jump to the nearest pending request. This
 * cuts total movement compared with FCFS, but requests far from the current
 * position can be left waiting for a long time (a form of starvation). When two
 * requests are equally close, this implementation picks the lower cylinder so
 * the result is always deterministic.</p>
 */
public final class SstfDiskScheduler implements DiskScheduler {

    @Override
    public String name() {
        return "SSTF";
    }

    @Override
    public DiskResult schedule(int startHead, List<Integer> requests, int diskSize, DiskDirection direction) {
        List<Integer> pending = new ArrayList<>(requests);
        List<Integer> path = new ArrayList<>();
        path.add(startHead);

        int head = startHead;
        while (!pending.isEmpty()) {
            int bestIndex = 0;
            int bestDistance = Math.abs(pending.get(0) - head);
            for (int i = 1; i < pending.size(); i++) {
                int distance = Math.abs(pending.get(i) - head);
                // A strictly closer request always wins. On a tie the explicit
                // second condition picks the lower cylinder number, so the choice
                // does not depend on the order the requests were given in.
                if (distance < bestDistance
                        || (distance == bestDistance && pending.get(i) < pending.get(bestIndex))) {
                    bestIndex = i;
                    bestDistance = distance;
                }
            }
            head = pending.remove(bestIndex);
            path.add(head);
        }

        return new DiskResult(name(), startHead, path, DiskPaths.totalMovement(path));
    }
}
