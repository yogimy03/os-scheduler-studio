package com.osscheduler.core.disk;

import com.osscheduler.core.model.DiskDirection;
import com.osscheduler.core.model.DiskResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Circular LOOK (C-LOOK) disk scheduling.
 *
 * <p>The circular cousin of LOOK. The head sweeps in one direction to the last
 * request, then jumps straight to the first request on the far side and carries
 * on in the same direction. It combines the fairness of C-SCAN with the "do not
 * waste trips to the edges" idea of LOOK.</p>
 */
public final class ClookDiskScheduler implements DiskScheduler {

    @Override
    public String name() {
        return "C-LOOK";
    }

    @Override
    public DiskResult schedule(int startHead, List<Integer> requests, int diskSize, DiskDirection direction) {
        List<Integer> path = new ArrayList<>();
        path.add(startHead);

        if (direction == DiskDirection.UP) {
            List<Integer> up = requests.stream().filter(c -> c >= startHead).sorted().toList();
            List<Integer> down = requests.stream().filter(c -> c < startHead).sorted().toList();
            path.addAll(up);
            path.addAll(down); // jump to the lowest request, then continue upward
        } else {
            List<Integer> down = requests.stream().filter(c -> c <= startHead)
                    .sorted(Comparator.reverseOrder()).toList();
            List<Integer> up = requests.stream().filter(c -> c > startHead)
                    .sorted(Comparator.reverseOrder()).toList();
            path.addAll(down);
            path.addAll(up); // jump to the highest request, then continue downward
        }

        return new DiskResult(name(), startHead, path, DiskPaths.totalMovement(path));
    }
}
