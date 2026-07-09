package com.osscheduler.viz;

import java.util.List;

/**
 * Renders a simple bordered text table, the kind you see in the command line
 * output. Kept deliberately tiny so there is no dependency to explain.
 */
public final class AsciiTable {

    private AsciiTable() {
    }

    public static String render(List<String> headers, List<List<String>> rows) {
        int columns = headers.size();
        int[] widths = new int[columns];
        for (int c = 0; c < columns; c++) {
            widths[c] = headers.get(c).length();
        }
        for (List<String> row : rows) {
            for (int c = 0; c < columns; c++) {
                widths[c] = Math.max(widths[c], row.get(c).length());
            }
        }

        StringBuilder out = new StringBuilder();
        String separator = borderLine(widths);
        out.append(separator).append('\n');
        out.append(rowLine(headers, widths)).append('\n');
        out.append(separator).append('\n');
        for (List<String> row : rows) {
            out.append(rowLine(row, widths)).append('\n');
        }
        out.append(separator);
        return out.toString();
    }

    private static String borderLine(int[] widths) {
        StringBuilder line = new StringBuilder("+");
        for (int width : widths) {
            line.append("-".repeat(width + 2)).append('+');
        }
        return line.toString();
    }

    private static String rowLine(List<String> cells, int[] widths) {
        StringBuilder line = new StringBuilder("|");
        for (int c = 0; c < cells.size(); c++) {
            String cell = cells.get(c);
            int padding = widths[c] - cell.length();
            line.append(' ').append(cell).append(" ".repeat(padding)).append(" |");
        }
        return line.toString();
    }
}
