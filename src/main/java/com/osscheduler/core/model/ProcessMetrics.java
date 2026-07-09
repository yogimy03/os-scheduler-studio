package com.osscheduler.core.model;

/**
 * The four textbook numbers we care about for a single process once the
 * schedule has been worked out.
 *
 * <ul>
 *   <li><b>Completion time</b> is when the process finished.</li>
 *   <li><b>Turnaround time</b> is completion time minus arrival time, so the
 *       total time the process spent in the system.</li>
 *   <li><b>Waiting time</b> is turnaround time minus burst time, so the time
 *       spent waiting rather than running.</li>
 *   <li><b>Response time</b> is the first moment the process got the CPU minus
 *       its arrival time.</li>
 * </ul>
 *
 * @param id             the process id
 * @param arrivalTime    when the process arrived
 * @param burstTime      how long the process needed the CPU
 * @param completionTime when the process finished
 * @param turnaroundTime completion time minus arrival time
 * @param waitingTime    turnaround time minus burst time
 * @param responseTime   first time on the CPU minus arrival time
 */
public record ProcessMetrics(
        String id,
        int arrivalTime,
        int burstTime,
        int completionTime,
        int turnaroundTime,
        int waitingTime,
        int responseTime) {
}
