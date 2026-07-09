package com.osscheduler.core.cpu;

import com.osscheduler.core.model.CpuProcess;

import java.util.Comparator;
import java.util.List;

/**
 * Longest Job First, non preemptive.
 *
 * <p>The mirror image of Shortest Job First: when the CPU is free, pick the
 * arrived process with the largest burst time. It is mostly studied as a
 * contrast, because it produces poor average waiting times.</p>
 */
public final class LjfScheduler extends AbstractNonPreemptiveScheduler {

    @Override
    public String name() {
        return "Longest Job First";
    }

    @Override
    protected CpuProcess select(List<CpuProcess> ready, int currentTime) {
        // max() keeps the first element on ties, which combined with the
        // arrival then id ordering gives a stable, predictable choice.
        return ready.stream()
                .max(Comparator.comparingInt(CpuProcess::burstTime))
                .orElseThrow();
    }
}
