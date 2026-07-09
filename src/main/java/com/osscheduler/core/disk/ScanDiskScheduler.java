package com.osscheduler.core.disk;

import com.osscheduler.core.model.DiskDirection;
import com.osscheduler.core.model.DiskResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * SCAN disk scheduling, also known as the elevator algorithm.
 *
 * <p>The head keeps moving in one direction, servicing every request it passes,
 * until it reaches the physical end of the disk. Then it reverses and services
 * the requests on the way back. Like a lift that goes all the way to the top
 * floor before coming down.</p>
 *
 * <p>The head only travels to the far end in its <em>starting</em> direction.
 * Once it reverses it stops at the last request rather than driving all the way
 * to the opposite end.</p>
 */
public final class ScanDiskScheduler implements DiskScheduler {

    @Override
    public String name() {
        return "SCAN";
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
            List<Integer> down = requests.stream().filter(c -> c < startHead)
                    .sorted(Comparator.reverseOrder()).toList();
            path.addAll(up);
            // SCAN always drives to the physical end in the sweep direction, even
            // if there is nothing left to service, before turning around.
            int reached = up.isEmpty() ? startHead : up.get(up.size() - 1);
            if (reached < topEnd) {
                path.add(topEnd);
            }
            path.addAll(down);
        } else {
            List<Integer> down = requests.stream().filter(c -> c <= startHead)
                    .sorted(Comparator.reverseOrder()).toList();
            List<Integer> up = requests.stream().filter(c -> c > startHead).sorted().toList();
            path.addAll(down);
            int reached = down.isEmpty() ? startHead : down.get(down.size() - 1);
            if (reached > 0) {
                path.add(0);
            }
            path.addAll(up);
        }

        return new DiskResult(name(), startHead, path, DiskPaths.totalMovement(path));
    }
}
