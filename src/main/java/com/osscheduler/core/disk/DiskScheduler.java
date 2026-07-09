package com.osscheduler.core.disk;

import com.osscheduler.core.model.DiskDirection;
import com.osscheduler.core.model.DiskResult;

import java.util.List;

/**
 * The common contract for every disk scheduling algorithm.
 *
 * <p>A disk has a read/write head that sits over one cylinder at a time. Moving
 * it costs time, so the goal is to service a batch of pending requests while
 * moving the head as little as possible. Each algorithm decides the order in a
 * different way.</p>
 */
public interface DiskScheduler {

    /** The name shown in menus and chart titles. */
    String name();

    /**
     * Work out the order the head visits the requested cylinders.
     *
     * @param startHead the cylinder the head starts on
     * @param requests  the cylinders that need servicing (order may not matter)
     * @param diskSize  the number of cylinders, so valid positions are
     *                  {@code 0} to {@code diskSize - 1}; used by SCAN and C-SCAN
     * @param direction which way the head sweeps first; used by the sweep style
     *                  algorithms
     * @return the head's full journey and how far it travelled
     */
    DiskResult schedule(int startHead, List<Integer> requests, int diskSize, DiskDirection direction);
}
