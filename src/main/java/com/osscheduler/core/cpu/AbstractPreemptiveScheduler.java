package com.osscheduler.core.cpu;

import com.osscheduler.core.model.CpuProcess;
import com.osscheduler.core.model.GanttSegment;
import com.osscheduler.core.model.ProcessMetrics;
import com.osscheduler.core.model.ScheduleResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared machinery for algorithms that can pause a process mid way.
 *
 * <p>These schedulers advance the clock one time unit at a time. At every tick
 * they look at every process that has arrived and still has work left, pick the
 * best one, and run it for a single unit. The next tick they are free to switch
 * to a different process. SRTF, LRTF and preemptive priority all share this
 * loop; they differ only in {@link #pick}.</p>
 *
 * <p>Round Robin is deliberately <em>not</em> built on this class, because it
 * cares about the order processes sit in a queue rather than a "best right now"
 * rule, so it gets its own dedicated implementation.</p>
 */
public abstract class AbstractPreemptiveScheduler implements CpuScheduler {

    /** A process paired with how much CPU work it still has left. */
    protected record Candidate(CpuProcess process, int remaining) {
    }

    /** Tie breaker for candidates: earliest arrival, then id order. */
    static final Comparator<Candidate> ARRIVAL_THEN_ID =
            Comparator.comparingInt((Candidate c) -> c.process().arrivalTime())
                    .thenComparing(c -> c.process().id());

    /**
     * Choose which candidate gets the CPU for the next single time unit.
     *
     * @param ready candidates that have arrived and still have work, already in
     *              arrival then id order so ties resolve predictably
     * @return the candidate to run for one unit
     */
    protected abstract Candidate pick(List<Candidate> ready);

    @Override
    public ScheduleResult schedule(List<CpuProcess> processes) {
        List<CpuProcess> ordered = new ArrayList<>(processes);
        ordered.sort(AbstractNonPreemptiveScheduler.ARRIVAL_THEN_ID);

        Map<String, Integer> remaining = new HashMap<>();
        for (CpuProcess process : ordered) {
            remaining.put(process.id(), process.burstTime());
        }

        List<GanttSegment> raw = new ArrayList<>();
        int time = 0;
        int completed = 0;
        int total = ordered.size();

        while (completed < total) {
            final int now = time;
            List<Candidate> ready = new ArrayList<>();
            for (CpuProcess process : ordered) {
                int left = remaining.get(process.id());
                if (process.arrivalTime() <= now && left > 0) {
                    ready.add(new Candidate(process, left));
                }
            }

            if (ready.isEmpty()) {
                // Nobody is ready, so record a single idle tick and move on.
                raw.add(new GanttSegment(GanttSegment.IDLE, time, time + 1));
                time++;
                continue;
            }

            Candidate chosen = pick(ready);
            raw.add(new GanttSegment(chosen.process().id(), time, time + 1));
            int left = remaining.get(chosen.process().id()) - 1;
            remaining.put(chosen.process().id(), left);
            time++;
            if (left == 0) {
                completed++;
            }
        }

        List<GanttSegment> merged = Timeline.merge(raw);
        List<ProcessMetrics> metrics = CpuMetrics.from(processes, merged);
        return new ScheduleResult(name(), merged, metrics);
    }
}
