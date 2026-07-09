package com.osscheduler.core.cpu;

import java.util.Comparator;
import java.util.List;

/**
 * Longest Remaining Time First, the preemptive version of Longest Job First.
 *
 * <p>At every time unit the CPU runs whichever arrived process has the most work
 * left. Like its non preemptive cousin it is mainly a teaching contrast, since
 * it produces long waiting times and a lot of context switching.</p>
 */
public final class LrtfScheduler extends AbstractPreemptiveScheduler {

    @Override
    public String name() {
        return "Longest Remaining Time First";
    }

    @Override
    protected Candidate pick(List<Candidate> ready) {
        return ready.stream()
                .max(Comparator.comparingInt(Candidate::remaining))
                .orElseThrow();
    }
}
