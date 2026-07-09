package com.osscheduler.core;

import com.osscheduler.core.model.CpuProcess;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns the short text formats used on the command line and in the app's text
 * boxes into real objects. Kept in one place so both front ends parse input the
 * exact same way, with the same error messages.
 */
public final class InputParser {

    private InputParser() {
    }

    /**
     * Parses a process list such as {@code "P1,0,5,2; P2,2,3,1"}.
     *
     * <p>Each process is {@code id,arrival,burst} with an optional fourth number
     * for priority. Processes are separated by semicolons.</p>
     */
    public static List<CpuProcess> parseProcesses(String text) {
        List<CpuProcess> processes = new ArrayList<>();
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("No processes were given");
        }
        for (String chunk : text.split(";")) {
            String trimmed = chunk.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] parts = trimmed.split(",");
            if (parts.length < 3 || parts.length > 4) {
                throw new IllegalArgumentException(
                        "Each process needs id,arrival,burst and an optional priority, but got: " + trimmed);
            }
            String id = parts[0].trim();
            int arrival = parseInt(parts[1], "arrival time");
            int burst = parseInt(parts[2], "burst time");
            int priority = parts.length == 4 ? parseInt(parts[3], "priority") : 0;
            processes.add(new CpuProcess(id, arrival, burst, priority));
        }
        if (processes.isEmpty()) {
            throw new IllegalArgumentException("No processes were given");
        }
        return processes;
    }

    /**
     * Parses a list of whole numbers separated by commas or spaces, such as
     * {@code "98, 183, 37"} or {@code "7 0 1 2 0 3"}.
     */
    public static List<Integer> parseIntList(String text) {
        List<Integer> numbers = new ArrayList<>();
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("No numbers were given");
        }
        for (String token : text.split("[,\\s]+")) {
            String trimmed = token.trim();
            if (!trimmed.isEmpty()) {
                numbers.add(parseInt(trimmed, "value"));
            }
        }
        if (numbers.isEmpty()) {
            throw new IllegalArgumentException("No numbers were given");
        }
        return numbers;
    }

    private static int parseInt(String text, String what) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Could not read " + what + " from '" + text.trim() + "'");
        }
    }
}
