package com.osscheduler.core.cpu;

import com.osscheduler.core.model.CpuProcess;

import java.util.List;

/**
 * First Come First Serve.
 *
 * <p>The simplest rule there is: whoever asked first runs first, and runs to
 * completion. Because the ready list is already sorted by arrival time (then id
 * for ties), the correct choice is simply the first one in the list.</p>
 */
public final class FcfsScheduler extends AbstractNonPreemptiveScheduler {

    @Override
    public String name() {
        return "First Come First Serve";
    }

    @Override
    protected CpuProcess select(List<CpuProcess> ready, int currentTime) {
        return ready.get(0);
    }
}
