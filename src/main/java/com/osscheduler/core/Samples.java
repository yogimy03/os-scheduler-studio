package com.osscheduler.core;

import com.osscheduler.core.model.CpuProcess;

import java.util.List;

/**
 * Ready made example inputs, so the demo command and the "load sample" button in
 * the app always have something sensible and consistent to show.
 */
public final class Samples {

    private Samples() {
    }

    /** Five processes with a mix of arrival times, bursts and priorities. */
    public static List<CpuProcess> processes() {
        return List.of(
                new CpuProcess("P1", 0, 7, 3),
                new CpuProcess("P2", 2, 4, 1),
                new CpuProcess("P3", 4, 1, 4),
                new CpuProcess("P4", 5, 4, 2),
                new CpuProcess("P5", 6, 3, 5));
    }

    /** The text form of {@link #processes()}, matching {@link InputParser}. */
    public static String processesText() {
        return "P1,0,7,3; P2,2,4,1; P3,4,1,4; P4,5,4,2; P5,6,3,5";
    }

    /** A classic disk request queue with the head starting at cylinder 53. */
    public static int diskStartHead() {
        return 53;
    }

    public static List<Integer> diskRequests() {
        return List.of(98, 183, 37, 122, 14, 124, 65, 67);
    }

    public static String diskRequestsText() {
        return "98, 183, 37, 122, 14, 124, 65, 67";
    }

    public static int diskSize() {
        return 200;
    }

    /** A page reference string that shows off hits, faults and eviction. */
    public static List<Integer> pageReferences() {
        return List.of(7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2, 1, 2, 0, 1, 7, 0, 1);
    }

    public static String pageReferencesText() {
        return "7 0 1 2 0 3 0 4 2 3 0 3 2 1 2 0 1 7 0 1";
    }

    public static int pageFrames() {
        return 3;
    }
}
