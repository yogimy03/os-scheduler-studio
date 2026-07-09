package com.osscheduler.core.cpu;

import com.osscheduler.core.model.CpuProcess;
import com.osscheduler.core.model.ProcessMetrics;
import com.osscheduler.core.model.ScheduleResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the CPU algorithms against results worked out by hand, plus a few
 * invariants that must hold for every schedule no matter the algorithm.
 */
class CpuSchedulerTest {

    private static final double EPS = 1e-9;

    /** A small set used by several tests: P1(0,5), P2(1,3), P3(2,1). */
    private static List<CpuProcess> small() {
        return List.of(
                new CpuProcess("P1", 0, 5),
                new CpuProcess("P2", 1, 3),
                new CpuProcess("P3", 2, 1));
    }

    private static Map<String, ProcessMetrics> byId(ScheduleResult result) {
        return result.metrics().stream()
                .collect(Collectors.toMap(ProcessMetrics::id, Function.identity()));
    }

    @Test
    void fcfsRunsInArrivalOrder() {
        ScheduleResult result = new FcfsScheduler().schedule(small());
        Map<String, ProcessMetrics> m = byId(result);
        assertEquals(5, m.get("P1").completionTime());
        assertEquals(8, m.get("P2").completionTime());
        assertEquals(9, m.get("P3").completionTime());
        assertEquals(10.0 / 3.0, result.averageWaitingTime(), EPS);
    }

    @Test
    void sjfPicksShortestReadyJob() {
        ScheduleResult result = new SjfScheduler().schedule(small());
        Map<String, ProcessMetrics> m = byId(result);
        // P1 runs 0-5, then the shortest ready job P3 (5-6), then P2 (6-9).
        assertEquals(6, m.get("P3").completionTime());
        assertEquals(9, m.get("P2").completionTime());
        assertEquals(8.0 / 3.0, result.averageWaitingTime(), EPS);
    }

    @Test
    void srtfPreemptsForShorterArrivals() {
        ScheduleResult result = new SrtfScheduler().schedule(small());
        Map<String, ProcessMetrics> m = byId(result);
        assertEquals(9, m.get("P1").completionTime());
        assertEquals(5, m.get("P2").completionTime());
        assertEquals(3, m.get("P3").completionTime());
        assertEquals(5.0 / 3.0, result.averageWaitingTime(), EPS);
    }

    @Test
    void roundRobinRotatesInQuantumSlices() {
        ScheduleResult result = new RoundRobinScheduler(2).schedule(small());
        Map<String, ProcessMetrics> m = byId(result);
        assertEquals(9, m.get("P1").completionTime());
        assertEquals(8, m.get("P2").completionTime());
        assertEquals(5, m.get("P3").completionTime());
        assertEquals(10.0 / 3.0, result.averageWaitingTime(), EPS);
        assertEquals("Round Robin (q=2)", result.algorithmName());
    }

    @Test
    void nonPreemptivePriorityRunsToCompletion() {
        List<CpuProcess> processes = List.of(
                new CpuProcess("P1", 0, 4, 2),
                new CpuProcess("P2", 1, 3, 1),
                new CpuProcess("P3", 2, 2, 3));
        ScheduleResult result = new PriorityScheduler().schedule(processes);
        Map<String, ProcessMetrics> m = byId(result);
        assertEquals(4, m.get("P1").completionTime());
        assertEquals(7, m.get("P2").completionTime());
        assertEquals(9, m.get("P3").completionTime());
        assertEquals(8.0 / 3.0, result.averageWaitingTime(), EPS);
    }

    @Test
    void preemptivePriorityInterruptsForHigherPriority() {
        List<CpuProcess> processes = List.of(
                new CpuProcess("P1", 0, 4, 2),
                new CpuProcess("P2", 1, 3, 1),
                new CpuProcess("P3", 2, 2, 3));
        ScheduleResult result = new PreemptivePriorityScheduler().schedule(processes);
        Map<String, ProcessMetrics> m = byId(result);
        assertEquals(7, m.get("P1").completionTime());
        assertEquals(4, m.get("P2").completionTime());
        assertEquals(9, m.get("P3").completionTime());
        assertEquals(8.0 / 3.0, result.averageWaitingTime(), EPS);
    }

    @Test
    void hrrnFavoursHighResponseRatio() {
        ScheduleResult result = new HrrnScheduler().schedule(small());
        Map<String, ProcessMetrics> m = byId(result);
        assertEquals(5, m.get("P1").completionTime());
        assertEquals(6, m.get("P3").completionTime());
        assertEquals(9, m.get("P2").completionTime());
        assertEquals(8.0 / 3.0, result.averageWaitingTime(), EPS);
    }

    @Test
    void idleTimeIsRecordedWhenNothingHasArrived() {
        List<CpuProcess> late = List.of(new CpuProcess("P1", 3, 2));
        ScheduleResult result = new FcfsScheduler().schedule(late);
        assertEquals(3, result.idleTime());
        assertEquals(5, result.totalTime());
        assertEquals(0, result.metrics().get(0).waitingTime());
        assertEquals(0, result.metrics().get(0).responseTime());
    }

    @Test
    void turnaroundAlwaysEqualsWaitingPlusBurst() {
        List<CpuProcess> processes = List.of(
                new CpuProcess("A", 0, 7, 3),
                new CpuProcess("B", 2, 4, 1),
                new CpuProcess("C", 4, 1, 4),
                new CpuProcess("D", 5, 4, 2));
        List<CpuScheduler> schedulers = List.of(
                new FcfsScheduler(), new SjfScheduler(), new SrtfScheduler(),
                new LjfScheduler(), new LrtfScheduler(), new HrrnScheduler(),
                new PriorityScheduler(), new PreemptivePriorityScheduler(),
                new RoundRobinScheduler(2), new RoundRobinScheduler(3));
        for (CpuScheduler scheduler : schedulers) {
            ScheduleResult result = scheduler.schedule(processes);
            for (ProcessMetrics metrics : result.metrics()) {
                assertEquals(metrics.turnaroundTime(), metrics.waitingTime() + metrics.burstTime(),
                        scheduler.name() + ": turnaround must equal waiting plus burst");
                assertTrue(metrics.waitingTime() >= 0, scheduler.name() + ": waiting cannot be negative");
                assertTrue(metrics.responseTime() >= 0, scheduler.name() + ": response cannot be negative");
                assertTrue(metrics.responseTime() <= metrics.waitingTime(),
                        scheduler.name() + ": response cannot exceed waiting");
            }
        }
    }

    @Test
    void everyProcessBurstAppearsInTheTimeline() {
        List<CpuProcess> processes = List.of(
                new CpuProcess("A", 0, 3),
                new CpuProcess("B", 1, 5),
                new CpuProcess("C", 2, 2));
        int totalBurst = 10;
        ScheduleResult result = new SrtfScheduler().schedule(processes);
        int busy = result.timeline().stream()
                .filter(s -> !s.isIdle())
                .mapToInt(s -> s.duration())
                .sum();
        assertEquals(totalBurst, busy);
    }
}
