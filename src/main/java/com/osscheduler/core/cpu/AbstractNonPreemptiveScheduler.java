package com.osscheduler.core.cpu;

import com.osscheduler.core.model.CpuProcess;
import com.osscheduler.core.model.GanttSegment;
import com.osscheduler.core.model.ProcessMetrics;
import com.osscheduler.core.model.ScheduleResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Shared machinery for algorithms that never interrupt a running process.
 *
 * <p>These schedulers all follow the same loop: when the CPU is free, look at
 * everything that has arrived, pick one process, and run it all the way to the
 * end. The only thing that changes between FCFS, SJF, LJF, HRRN and priority is
 * <em>how they pick</em>, so subclasses only implement {@link #select}.</p>
 */
public abstract class AbstractNonPreemptiveScheduler implements CpuScheduler {

    /** Consistent tie breaker used everywhere: earliest arrival, then id order. */
    static final Comparator<CpuProcess> ARRIVAL_THEN_ID =
            Comparator.comparingInt(CpuProcess::arrivalTime).thenComparing(CpuProcess::id);

    /**
     * Choose the next process to run from the ones that have already arrived.
     *
     * @param ready       processes that have arrived and not yet run, already in
     *                    arrival then id order so ties resolve predictably
     * @param currentTime the current clock value (HRRN needs it)
     * @return the process to run next
     */
    protected abstract CpuProcess select(List<CpuProcess> ready, int currentTime);

    @Override
    public ScheduleResult schedule(List<CpuProcess> processes) {
        List<CpuProcess> remaining = new ArrayList<>(processes);
        remaining.sort(ARRIVAL_THEN_ID);

        List<GanttSegment> timeline = new ArrayList<>();
        int time = 0;

        while (!remaining.isEmpty()) {
            final int now = time;
            List<CpuProcess> ready = remaining.stream()
                    .filter(p -> p.arrivalTime() <= now)
                    .toList();

            if (ready.isEmpty()) {
                // Nothing has arrived yet, so the CPU waits for the next arrival.
                int nextArrival = remaining.stream()
                        .mapToInt(CpuProcess::arrivalTime)
                        .min()
                        .orElseThrow();
                timeline.add(new GanttSegment(GanttSegment.IDLE, time, nextArrival));
                time = nextArrival;
                continue;
            }

            CpuProcess chosen = select(ready, now);
            int start = time;
            int end = start + chosen.burstTime();
            timeline.add(new GanttSegment(chosen.id(), start, end));
            time = end;
            remaining.remove(chosen);
        }

        List<GanttSegment> merged = Timeline.merge(timeline);
        List<ProcessMetrics> metrics = CpuMetrics.from(processes, merged);
        return new ScheduleResult(name(), merged, metrics);
    }
}
