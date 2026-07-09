package com.osscheduler.core.cpu;

import com.osscheduler.core.model.GanttSegment;

import java.util.ArrayList;
import java.util.List;

/**
 * Small helper for tidying up a list of Gantt blocks.
 *
 * <p>The preemptive algorithms build their timeline one time unit at a time, so
 * they produce a lot of tiny one unit blocks in a row for the same process. This
 * merges neighbouring blocks that share a label and touch each other, turning
 * {@code [P1 0-1][P1 1-2][P1 2-3]} into a single {@code [P1 0-3]}.</p>
 */
final class Timeline {

    private Timeline() {
    }

    static List<GanttSegment> merge(List<GanttSegment> raw) {
        List<GanttSegment> merged = new ArrayList<>();
        for (GanttSegment segment : raw) {
            if (segment.duration() == 0) {
                continue; // skip empty blocks, they carry no information
            }
            if (!merged.isEmpty()) {
                GanttSegment last = merged.get(merged.size() - 1);
                if (last.label().equals(segment.label()) && last.end() == segment.start()) {
                    merged.set(merged.size() - 1,
                            new GanttSegment(last.label(), last.start(), segment.end()));
                    continue;
                }
            }
            merged.add(segment);
        }
        return merged;
    }
}
