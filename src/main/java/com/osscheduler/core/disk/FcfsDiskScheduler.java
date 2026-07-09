package com.osscheduler.core.disk;

import com.osscheduler.core.model.DiskDirection;
import com.osscheduler.core.model.DiskResult;

import java.util.ArrayList;
import java.util.List;

/**
 * First Come First Serve disk scheduling.
 *
 * <p>The head simply visits the requests in the exact order they were asked for.
 * Fair and easy to reason about, but the head can bounce back and forth across
 * the disk and rack up a lot of movement.</p>
 */
public final class FcfsDiskScheduler implements DiskScheduler {

    @Override
    public String name() {
        return "FCFS";
    }

    @Override
    public DiskResult schedule(int startHead, List<Integer> requests, int diskSize, DiskDirection direction) {
        List<Integer> path = new ArrayList<>();
        path.add(startHead);
        path.addAll(requests);
        return new DiskResult(name(), startHead, path, DiskPaths.totalMovement(path));
    }
}
