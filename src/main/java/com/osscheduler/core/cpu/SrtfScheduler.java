package com.osscheduler.core.cpu;

import java.util.Comparator;
import java.util.List;

/**
 * Shortest Remaining Time First, the preemptive version of Shortest Job First.
 *
 * <p>At every time unit the CPU runs whichever arrived process has the least
 * work left. If a brand new short job arrives it can push aside a longer job
 * that was already running. This gives the lowest average waiting time of any
 * algorithm, but a running job can be interrupted many times.</p>
 */
public final class SrtfScheduler extends AbstractPreemptiveScheduler {

    @Override
    public String name() {
        return "Shortest Remaining Time First";
    }

    @Override
    protected Candidate pick(List<Candidate> ready) {
        return ready.stream()
                .min(Comparator.comparingInt(Candidate::remaining))
                .orElseThrow();
    }
}
