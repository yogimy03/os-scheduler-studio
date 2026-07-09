package com.osscheduler.viz;

import com.osscheduler.core.model.DiskResult;

import java.util.stream.Collectors;

/**
 * Renders a disk schedule as plain text for the command line.
 */
public final class AsciiDiskRenderer {

    private AsciiDiskRenderer() {
    }

    public static String render(DiskResult result) {
        String order = result.path().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(" -> "));
        return result.algorithmName() + "\n"
                + "=".repeat(result.algorithmName().length()) + "\n\n"
                + "Head path           : " + order + "\n"
                + "Total head movement : " + result.totalHeadMovement();
    }
}
