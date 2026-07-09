package com.osscheduler.core.cpu;

import com.osscheduler.core.model.CpuProcess;
import com.osscheduler.core.model.ScheduleResult;

import java.util.List;

/**
 * The common contract that every CPU scheduling algorithm follows.
 *
 * <p>Give it a list of processes, get back a {@link ScheduleResult}. Because
 * every algorithm speaks the same language, the user interface and the command
 * line tool can run any of them without knowing the details.</p>
 */
public interface CpuScheduler {

    /** The name shown in menus, tables and chart titles. */
    String name();

    /**
     * Work out the order in which the processes run and all of their metrics.
     *
     * @param processes the jobs to schedule; the list is never modified
     * @return the timeline and metrics produced by this algorithm
     */
    ScheduleResult schedule(List<CpuProcess> processes);
}
