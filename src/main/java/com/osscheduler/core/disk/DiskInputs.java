package com.osscheduler.core.disk;

import java.util.List;

/**
 * Validation for disk scheduling input, shared by the command line and the app
 * so both reject impossible requests the same way and with the same messages.
 *
 * <p>A cylinder only exists if it is inside the disk, so the head and every
 * request must sit between {@code 0} and {@code diskSize - 1}. Without this
 * check the sweep algorithms could walk to a cylinder that does not exist and
 * report a meaningless head movement.</p>
 */
public final class DiskInputs {

    private DiskInputs() {
    }

    public static void validate(int head, List<Integer> requests, int diskSize) {
        if (diskSize < 1) {
            throw new IllegalArgumentException("Disk size must be at least 1 cylinder");
        }
        int last = diskSize - 1;
        if (head < 0 || head > last) {
            throw new IllegalArgumentException(
                    "Start head " + head + " must be between 0 and " + last);
        }
        for (int request : requests) {
            if (request < 0 || request > last) {
                throw new IllegalArgumentException(
                        "Request " + request + " must be between 0 and " + last
                                + " (increase the disk size if you meant a larger disk)");
            }
        }
    }
}
