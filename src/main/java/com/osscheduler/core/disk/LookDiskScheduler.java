package com.osscheduler.core.disk;

import com.osscheduler.core.model.DiskDirection;
import com.osscheduler.core.model.DiskResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * LOOK disk scheduling.
 *
 * <p>Just like SCAN, but smarter about the ends. The head only travels as far as
 * the last request in each direction rather than all the way to the physical
 * edge of the disk. It "looks" ahead, sees there is nothing more to do, and
 * turns around early, which saves the wasted trips to the ends.</p>
 */
public final class LookDiskScheduler implements DiskScheduler {

    @Override
    public String name() {
        return "LOOK";
    }

    @Override
    public DiskResult schedule(int startHead, List<Integer> requests, int diskSize, DiskDirection direction) {
        List<Integer> path = new ArrayList<>();
        path.add(startHead);

        if (direction == DiskDirection.UP) {
            List<Integer> up = requests.stream().filter(c -> c >= startHead).sorted().toList();
            List<Integer> down = requests.stream().filter(c -> c < startHead)
                    .sorted(Comparator.reverseOrder()).toList();
            path.addAll(up);
            path.addAll(down);
        } else {
            List<Integer> down = requests.stream().filter(c -> c <= startHead)
                    .sorted(Comparator.reverseOrder()).toList();
            List<Integer> up = requests.stream().filter(c -> c > startHead).sorted().toList();
            path.addAll(down);
            path.addAll(up);
        }

        return new DiskResult(name(), startHead, path, DiskPaths.totalMovement(path));
    }
}
