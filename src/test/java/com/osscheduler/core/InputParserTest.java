package com.osscheduler.core;

import com.osscheduler.core.model.CpuProcess;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Checks the shared text parsing used by the command line and the app.
 */
class InputParserTest {

    @Test
    void parsesProcessesWithAndWithoutPriority() {
        List<CpuProcess> processes = InputParser.parseProcesses("P1,0,5; P2,2,3,1");
        assertEquals(2, processes.size());
        assertEquals(new CpuProcess("P1", 0, 5, 0), processes.get(0));
        assertEquals(new CpuProcess("P2", 2, 3, 1), processes.get(1));
    }

    @Test
    void trimsWhitespaceAroundProcessFields() {
        List<CpuProcess> processes = InputParser.parseProcesses("  A , 1 , 4 , 2 ");
        assertEquals(new CpuProcess("A", 1, 4, 2), processes.get(0));
    }

    @Test
    void parsesNumbersSeparatedByCommasOrSpaces() {
        assertEquals(List.of(98, 183, 37), InputParser.parseIntList("98, 183, 37"));
        assertEquals(List.of(7, 0, 1, 2), InputParser.parseIntList("7 0 1 2"));
    }

    @Test
    void rejectsEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> InputParser.parseProcesses("   "));
        assertThrows(IllegalArgumentException.class, () -> InputParser.parseIntList(""));
    }

    @Test
    void rejectsMalformedProcess() {
        assertThrows(IllegalArgumentException.class, () -> InputParser.parseProcesses("P1,0"));
        assertThrows(IllegalArgumentException.class, () -> InputParser.parseProcesses("P1,zero,5"));
    }

    @Test
    void rejectsInvalidProcessValues() {
        assertThrows(IllegalArgumentException.class, () -> InputParser.parseProcesses("P1,0,0"));
        assertThrows(IllegalArgumentException.class, () -> InputParser.parseProcesses("P1,-1,5"));
    }
}
