package com.osscheduler.core.cpu;

import com.osscheduler.core.model.CpuProcess;

import java.util.Comparator;
import java.util.List;

/**
 * Highest Response Ratio Next, non preemptive.
 *
 * <p>A fairer cousin of Shortest Job First. When the CPU is free, each waiting
 * process gets a response ratio:</p>
 *
 * <pre>ratio = (waiting time + burst time) / burst time</pre>
 *
 * <p>The process with the highest ratio runs next. Short jobs still get favoured
 * because burst time is on the bottom, but the longer a job waits the higher its
 * ratio climbs, so nothing is starved forever.</p>
 */
public final class HrrnScheduler extends AbstractNonPreemptiveScheduler {

    @Override
    public String name() {
        return "Highest Response Ratio Next";
    }

    @Override
    protected CpuProcess select(List<CpuProcess> ready, int currentTime) {
        return ready.stream()
                .max(Comparator.comparingDouble(p -> responseRatio(p, currentTime)))
                .orElseThrow();
    }

    private static double responseRatio(CpuProcess process, int currentTime) {
        int waiting = currentTime - process.arrivalTime();
        return (waiting + process.burstTime()) / (double) process.burstTime();
    }
}
