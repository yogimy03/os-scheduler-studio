package com.osscheduler.core.cpu;

import com.osscheduler.core.model.CpuProcess;

import java.util.Comparator;
import java.util.List;

/**
 * Priority scheduling, non preemptive.
 *
 * <p>Every process carries a priority number. This app follows the common
 * textbook convention that a <em>smaller</em> number means a more important job,
 * so priority 0 outranks priority 5. When the CPU is free the most important
 * arrived process runs to completion. Equal priorities fall back to arrival
 * time, then id.</p>
 */
public final class PriorityScheduler extends AbstractNonPreemptiveScheduler {

    @Override
    public String name() {
        return "Priority (non-preemptive)";
    }

    @Override
    protected CpuProcess select(List<CpuProcess> ready, int currentTime) {
        return ready.stream()
                .min(Comparator.comparingInt(CpuProcess::priority))
                .orElseThrow();
    }
}
