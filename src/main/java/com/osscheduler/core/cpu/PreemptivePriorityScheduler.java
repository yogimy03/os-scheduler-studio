package com.osscheduler.core.cpu;

import java.util.Comparator;
import java.util.List;

/**
 * Priority scheduling, preemptive.
 *
 * <p>Same rule as the non preemptive version (smaller priority number is more
 * important), but checked at every time unit. If a more important process
 * arrives while a less important one is running, the CPU switches to it
 * immediately.</p>
 */
public final class PreemptivePriorityScheduler extends AbstractPreemptiveScheduler {

    @Override
    public String name() {
        return "Priority (preemptive)";
    }

    @Override
    protected Candidate pick(List<Candidate> ready) {
        return ready.stream()
                .min(Comparator.comparingInt((Candidate c) -> c.process().priority()))
                .orElseThrow();
    }
}
