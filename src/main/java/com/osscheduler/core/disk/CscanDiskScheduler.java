package com.osscheduler.core.disk;

import com.osscheduler.core.model.DiskDirection;
import com.osscheduler.core.model.DiskResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Circular SCAN (C-SCAN) disk scheduling.
 *
 * <p>A fairer version of SCAN. The head sweeps in one direction to the end of
 * the disk, then jumps straight back to the other end without servicing anything
 * on the return, and sweeps in the same direction again. Because every request
 * is always approached from the same side, waiting times are more uniform than
 * plain SCAN, where the middle of the disk gets serviced twice as often.</p>
 *
 * <p>The long jump back across the disk counts towards total head movement. The
 * head always drives to the physical end in its sweep direction. The wrap jump
 * to the far end is only made when there are requests waiting on that far end to
 * service, since jumping across an empty side would be wasted movement.</p>
 */
public final class CscanDiskScheduler implements DiskScheduler {

    @Override
    public String name() {
        return "C-SCAN";
    }

    @Override
    public DiskResult schedule(int startHead, List<Integer> requests, int diskSize, DiskDirection direction) {
        List<Integer> path = new ArrayList<>();
        path.add(startHead);

        if (requests.isEmpty()) {
            return new DiskResult(name(), startHead, path, 0);
        }

        int topEnd = diskSize - 1;

        if (direction == DiskDirection.UP) {
            List<Integer> up = requests.stream().filter(c -> c >= startHead).sorted().toList();
            List<Integer> down = requests.stream().filter(c -> c < startHead).sorted().toList();
            path.addAll(up);
            int reached = up.isEmpty() ? startHead : up.get(up.size() - 1);
            if (reached < topEnd) {
                path.add(topEnd);        // reach the top end
            }
            if (!down.isEmpty()) {
                path.add(0);             // wrap back to the bottom end
                path.addAll(down);       // continue upward from the bottom
            }
        } else {
            List<Integer> down = requests.stream().filter(c -> c <= startHead)
                    .sorted(Comparator.reverseOrder()).toList();
            List<Integer> up = requests.stream().filter(c -> c > startHead)
                    .sorted(Comparator.reverseOrder()).toList();
            path.addAll(down);
            int reached = down.isEmpty() ? startHead : down.get(down.size() - 1);
            if (reached > 0) {
                path.add(0);             // reach the bottom end
            }
            if (!up.isEmpty()) {
                path.add(topEnd);        // wrap back to the top end
                path.addAll(up);         // continue downward from the top
            }
        }

        return new DiskResult(name(), startHead, path, DiskPaths.totalMovement(path));
    }
}
