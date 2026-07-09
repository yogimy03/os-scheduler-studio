package com.osscheduler.core.cpu;

import com.osscheduler.core.model.CpuProcess;

import java.util.Comparator;
import java.util.List;

/**
 * Shortest Job First, non preemptive.
 *
 * <p>When the CPU is free, pick the arrived process with the smallest burst
 * time and let it finish. This gives the best possible average waiting time for
 * a fixed set of jobs, at the cost of possibly starving long jobs.</p>
 */
public final class SjfScheduler extends AbstractNonPreemptiveScheduler {

    @Override
    public String name() {
        return "Shortest Job First";
    }

    @Override
    protected CpuProcess select(List<CpuProcess> ready, int currentTime) {
        // min() keeps the first element on ties, and the list is already in
        // arrival then id order, so ties break the way we expect.
        return ready.stream()
                .min(Comparator.comparingInt(CpuProcess::burstTime))
                .orElseThrow();
    }
}
