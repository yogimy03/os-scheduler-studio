package com.osscheduler.core.cpu;

import com.osscheduler.core.model.CpuProcess;
import com.osscheduler.core.model.GanttSegment;
import com.osscheduler.core.model.ProcessMetrics;
import com.osscheduler.core.model.ScheduleResult;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Round Robin scheduling.
 *
 * <p>Processes wait in a first in first out queue. The one at the front runs for
 * at most a fixed slice of time called the <em>time quantum</em>. If it does not
 * finish within its slice it goes to the back of the queue and the next process
 * gets a turn. This shares the CPU fairly and keeps response times low, which is
 * why interactive systems like it.</p>
 *
 * <p>The tricky part is the order things join the queue. This implementation
 * uses the common textbook convention: when a process's slice ends, any
 * processes that arrived during that slice (up to and including the moment it
 * ends) are added to the queue first, and only then is the preempted process
 * placed at the back.</p>
 */
public final class RoundRobinScheduler implements CpuScheduler {

    private final int quantum;

    public RoundRobinScheduler(int quantum) {
        if (quantum <= 0) {
            throw new IllegalArgumentException("Time quantum must be greater than zero");
        }
        this.quantum = quantum;
    }

    public int quantum() {
        return quantum;
    }

    @Override
    public String name() {
        return "Round Robin (q=" + quantum + ")";
    }

    @Override
    public ScheduleResult schedule(List<CpuProcess> processes) {
        List<CpuProcess> arrivals = new ArrayList<>(processes);
        arrivals.sort(Comparator.comparingInt(CpuProcess::arrivalTime).thenComparing(CpuProcess::id));

        Map<String, Integer> remaining = new HashMap<>();
        for (CpuProcess process : arrivals) {
            remaining.put(process.id(), process.burstTime());
        }

        Deque<CpuProcess> queue = new ArrayDeque<>();
        List<GanttSegment> timeline = new ArrayList<>();
        int time = 0;
        int nextToArrive = 0;         // index into the arrival sorted list
        int completed = 0;
        int total = arrivals.size();

        // Seed the queue with everything already present at time zero.
        nextToArrive = enqueueArrived(arrivals, queue, nextToArrive, time);

        while (completed < total) {
            if (queue.isEmpty()) {
                // The CPU is idle until the next process arrives.
                int nextArrival = arrivals.get(nextToArrive).arrivalTime();
                timeline.add(new GanttSegment(GanttSegment.IDLE, time, nextArrival));
                time = nextArrival;
                nextToArrive = enqueueArrived(arrivals, queue, nextToArrive, time);
                continue;
            }

            CpuProcess current = queue.poll();
            int left = remaining.get(current.id());
            int run = Math.min(quantum, left);
            int start = time;
            time += run;
            left -= run;
            remaining.put(current.id(), left);
            timeline.add(new GanttSegment(current.id(), start, time));

            // Newly arrived processes join the queue before the preempted one.
            nextToArrive = enqueueArrived(arrivals, queue, nextToArrive, time);

            if (left > 0) {
                queue.add(current);
            } else {
                completed++;
            }
        }

        List<GanttSegment> merged = Timeline.merge(timeline);
        List<ProcessMetrics> metrics = CpuMetrics.from(processes, merged);
        return new ScheduleResult(name(), merged, metrics);
    }

    /**
     * Adds every not yet queued process whose arrival time is at or before
     * {@code time} to the back of the queue, and returns the new arrival index.
     */
    private static int enqueueArrived(List<CpuProcess> arrivals, Deque<CpuProcess> queue,
                                      int nextToArrive, int time) {
        while (nextToArrive < arrivals.size()
                && arrivals.get(nextToArrive).arrivalTime() <= time) {
            queue.add(arrivals.get(nextToArrive));
            nextToArrive++;
        }
        return nextToArrive;
    }
}
