package com.osscheduler.cli;

import com.osscheduler.core.InputParser;
import com.osscheduler.core.Samples;
import com.osscheduler.core.cpu.CpuAlgorithm;
import com.osscheduler.core.cpu.CpuScheduler;
import com.osscheduler.core.cpu.EngineOptions;
import com.osscheduler.core.disk.DiskAlgorithm;
import com.osscheduler.core.disk.DiskInputs;
import com.osscheduler.core.disk.DiskScheduler;
import com.osscheduler.core.model.CpuProcess;
import com.osscheduler.core.model.DiskDirection;
import com.osscheduler.core.model.DiskResult;
import com.osscheduler.core.model.PageResult;
import com.osscheduler.core.model.ScheduleResult;
import com.osscheduler.core.page.PageAlgorithm;
import com.osscheduler.viz.AsciiDiskRenderer;
import com.osscheduler.viz.AsciiGanttRenderer;
import com.osscheduler.viz.AsciiPageRenderer;
import com.osscheduler.viz.SvgBarChartRenderer;
import com.osscheduler.viz.SvgDiskRenderer;
import com.osscheduler.viz.SvgGanttRenderer;
import com.osscheduler.viz.SvgPageRenderer;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * The command line interface. It runs any single algorithm and prints the
 * result as text, or runs the full {@code demo} which exercises every algorithm
 * on the sample inputs and also writes the SVG charts used in the README.
 */
public final class Cli {

    private final PrintStream out;
    private final PrintStream err;

    public Cli(PrintStream out, PrintStream err) {
        this.out = out;
        this.err = err;
    }

    /** Runs the tool and returns a process exit code (0 means success). */
    public int run(String[] args) {
        if (args.length == 0) {
            printHelp();
            return 0;
        }
        String command = args[0].toLowerCase(java.util.Locale.ROOT);
        String[] rest = new String[args.length - 1];
        System.arraycopy(args, 1, rest, 0, rest.length);

        try {
            return switch (command) {
                case "help", "-h", "--help" -> {
                    printHelp();
                    yield 0;
                }
                case "list" -> runList();
                case "cpu" -> runCpu(Args.parse(rest));
                case "disk" -> runDisk(Args.parse(rest));
                case "page" -> runPage(Args.parse(rest));
                case "demo" -> runDemo(Args.parse(rest));
                default -> {
                    err.println("Unknown command: " + command);
                    printHelp();
                    yield 1;
                }
            };
        } catch (IllegalArgumentException ex) {
            err.println("Error: " + ex.getMessage());
            return 1;
        } catch (IOException ex) {
            err.println("Could not write output file: " + ex.getMessage());
            return 1;
        }
    }

    private int runList() {
        out.println("CPU scheduling algorithms:");
        for (CpuAlgorithm algorithm : CpuAlgorithm.values()) {
            out.printf("  %-11s %s%n", algorithm.key(), algorithm.displayName());
        }
        out.println();
        out.println("Disk scheduling algorithms:");
        for (DiskAlgorithm algorithm : DiskAlgorithm.values()) {
            out.printf("  %-11s %s%n", algorithm.key(), algorithm.displayName());
        }
        out.println();
        out.println("Page replacement algorithms:");
        for (PageAlgorithm algorithm : PageAlgorithm.values()) {
            out.printf("  %-11s %s%n", algorithm.key(), algorithm.displayName());
        }
        return 0;
    }

    private int runCpu(Args args) throws IOException {
        CpuAlgorithm algorithm = CpuAlgorithm.fromKey(args.positional(0, "algorithm key"))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown CPU algorithm. Run 'list' to see the keys."));
        List<CpuProcess> processes = InputParser.parseProcesses(args.required("processes"));
        int quantum = args.intOr("quantum", 2);
        CpuScheduler scheduler = algorithm.create(new EngineOptions(quantum));
        ScheduleResult result = scheduler.schedule(processes);
        out.println(AsciiGanttRenderer.render(result));
        writeSvgIfRequested(args, SvgGanttRenderer.render(result));
        return 0;
    }

    private int runDisk(Args args) throws IOException {
        DiskAlgorithm algorithm = DiskAlgorithm.fromKey(args.positional(0, "algorithm key"))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown disk algorithm. Run 'list' to see the keys."));
        int head = args.intRequired("head");
        int size = args.intOr("size", 200);
        DiskDirection direction = parseDirection(args.getOr("dir", "up"));
        List<Integer> requests = InputParser.parseIntList(args.required("requests"));
        DiskInputs.validate(head, requests, size);
        DiskScheduler scheduler = algorithm.create();
        DiskResult result = scheduler.schedule(head, requests, size, direction);
        out.println(AsciiDiskRenderer.render(result));
        writeSvgIfRequested(args, SvgDiskRenderer.render(result, size - 1));
        return 0;
    }

    private int runPage(Args args) throws IOException {
        PageAlgorithm algorithm = PageAlgorithm.fromKey(args.positional(0, "algorithm key"))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown page algorithm. Run 'list' to see the keys."));
        int frames = args.intRequired("frames");
        List<Integer> references = InputParser.parseIntList(args.required("refs"));
        PageResult result = algorithm.create().run(references, frames);
        out.println(AsciiPageRenderer.render(result));
        writeSvgIfRequested(args, SvgPageRenderer.render(result));
        return 0;
    }

    private int runDemo(Args args) throws IOException {
        Path outDir = Path.of(args.getOr("out", "docs/images"));
        Files.createDirectories(outDir);
        int quantum = args.intOr("quantum", 2);

        out.println("OS Scheduler Studio demo");
        out.println("Writing charts to: " + outDir.toAbsolutePath());
        out.println();

        out.println("############  CPU SCHEDULING  ############");
        out.println("Sample processes: " + Samples.processesText());
        out.println();
        List<String> cpuNames = new ArrayList<>();
        List<Double> cpuWaiting = new ArrayList<>();
        for (CpuAlgorithm algorithm : CpuAlgorithm.values()) {
            ScheduleResult result = algorithm.create(new EngineOptions(quantum))
                    .schedule(Samples.processes());
            out.println(AsciiGanttRenderer.render(result));
            out.println();
            Files.writeString(outDir.resolve("cpu-" + algorithm.key() + ".svg"),
                    SvgGanttRenderer.render(result));
            cpuNames.add(result.algorithmName());
            cpuWaiting.add(result.averageWaitingTime());
        }
        Files.writeString(outDir.resolve("cpu-comparison.svg"),
                SvgBarChartRenderer.render(
                        "Average waiting time by algorithm (lower is better)", cpuNames, cpuWaiting));

        out.println("############  DISK SCHEDULING  ############");
        out.printf("Head starts at %d, disk size %d, requests: %s%n%n",
                Samples.diskStartHead(), Samples.diskSize(), Samples.diskRequestsText());
        List<String> diskNames = new ArrayList<>();
        List<Double> diskMovement = new ArrayList<>();
        for (DiskAlgorithm algorithm : DiskAlgorithm.values()) {
            DiskResult result = algorithm.create().schedule(
                    Samples.diskStartHead(), Samples.diskRequests(),
                    Samples.diskSize(), DiskDirection.UP);
            out.println(AsciiDiskRenderer.render(result));
            out.println();
            Files.writeString(outDir.resolve("disk-" + algorithm.key() + ".svg"),
                    SvgDiskRenderer.render(result, Samples.diskSize() - 1));
            diskNames.add(result.algorithmName());
            diskMovement.add((double) result.totalHeadMovement());
        }
        Files.writeString(outDir.resolve("disk-comparison.svg"),
                SvgBarChartRenderer.render(
                        "Total head movement by algorithm (lower is better)", diskNames, diskMovement));

        out.println("############  PAGE REPLACEMENT  ############");
        out.printf("Frames: %d, reference string: %s%n%n",
                Samples.pageFrames(), Samples.pageReferencesText());
        List<String> pageNames = new ArrayList<>();
        List<Double> pageFaults = new ArrayList<>();
        for (PageAlgorithm algorithm : PageAlgorithm.values()) {
            PageResult result = algorithm.create()
                    .run(Samples.pageReferences(), Samples.pageFrames());
            out.println(AsciiPageRenderer.render(result));
            out.println();
            Files.writeString(outDir.resolve("page-" + algorithm.key() + ".svg"),
                    SvgPageRenderer.render(result));
            pageNames.add(result.algorithmName());
            pageFaults.add((double) result.pageFaults());
        }
        Files.writeString(outDir.resolve("page-comparison.svg"),
                SvgBarChartRenderer.render(
                        "Page faults by algorithm (lower is better)", pageNames, pageFaults));

        out.println("Done. SVG charts were written to " + outDir.toAbsolutePath());
        return 0;
    }

    private void writeSvgIfRequested(Args args, String svg) throws IOException {
        String path = args.getOr("svg", null);
        if (path != null) {
            Path target = Path.of(path);
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }
            Files.writeString(target, svg);
            out.println();
            out.println("Saved chart to " + target.toAbsolutePath());
        }
    }

    private static DiskDirection parseDirection(String text) {
        return switch (text.toLowerCase(java.util.Locale.ROOT)) {
            case "up", "right", "high" -> DiskDirection.UP;
            case "down", "left", "low" -> DiskDirection.DOWN;
            default -> throw new IllegalArgumentException(
                    "Direction must be 'up' or 'down', but got '" + text + "'");
        };
    }

    private void printHelp() {
        out.println("""
                OS Scheduler Studio - command line tool

                Usage:
                  demo [--out DIR] [--quantum N]
                      Run every algorithm on the built in samples, print the
                      results, and write SVG charts (default folder: docs/images).

                  cpu  <algo> --processes "P1,0,5; P2,2,3,1" [--quantum N] [--svg FILE]
                      Run one CPU algorithm. A process is id,arrival,burst with an
                      optional fourth priority number. Processes are split by ';'.

                  disk <algo> --head H --requests "98,183,37" [--size N] [--dir up|down] [--svg FILE]
                      Run one disk algorithm.

                  page <algo> --frames N --refs "7 0 1 2 0 3" [--svg FILE]
                      Run one page replacement algorithm.

                  list
                      Show every algorithm and its key.

                Examples:
                  demo
                  cpu rr --processes "P1,0,5; P2,1,3; P3,2,1" --quantum 2
                  disk sstf --head 53 --requests "98,183,37,122,14,124,65,67"
                  page lru --frames 3 --refs "7 0 1 2 0 3 0 4 2 3 0 3 2"
                """);
    }
}
