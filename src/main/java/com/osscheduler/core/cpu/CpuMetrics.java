package com.osscheduler.core.cpu;

import com.osscheduler.core.model.CpuProcess;
import com.osscheduler.core.model.GanttSegment;
import com.osscheduler.core.model.ProcessMetrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns a finished timeline into per process metrics.
 *
 * <p>Every CPU algorithm ends up here. Once we know when each process first
 * touched the CPU and when it finished, the four textbook numbers fall straight
 * out of the definitions, so there is exactly one place that computes them and
 * no algorithm can get the arithmetic wrong on its own.</p>
 */
final class CpuMetrics {

    private CpuMetrics() {
    }

    static List<ProcessMetrics> from(List<CpuProcess> processes, List<GanttSegment> timeline) {
        Map<String, Integer> firstStart = new HashMap<>();
        Map<String, Integer> completion = new HashMap<>();

        for (GanttSegment segment : timeline) {
            if (segment.isIdle()) {
                continue;
            }
            firstStart.putIfAbsent(segment.label(), segment.start());
            completion.put(segment.label(), segment.end()); // last block wins
        }

        List<ProcessMetrics> result = new ArrayList<>();
        for (CpuProcess process : processes) {
            int completed = completion.getOrDefault(process.id(), process.arrivalTime());
            int started = firstStart.getOrDefault(process.id(), process.arrivalTime());
            int turnaround = completed - process.arrivalTime();
            int waiting = turnaround - process.burstTime();
            int response = started - process.arrivalTime();
            result.add(new ProcessMetrics(
                    process.id(),
                    process.arrivalTime(),
                    process.burstTime(),
                    completed,
                    turnaround,
                    waiting,
                    response));
        }
        return result;
    }
}
