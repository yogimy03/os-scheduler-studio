package com.osscheduler.ui;

import com.osscheduler.core.model.DiskResult;
import com.osscheduler.core.model.GanttSegment;
import com.osscheduler.core.model.ScheduleResult;
import com.osscheduler.viz.Palette;
import com.osscheduler.viz.SvgSupport;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

/**
 * Draws the Gantt charts, disk seek graphs and comparison bars onto a JavaFX
 * {@link Canvas}. It mirrors the SVG renderers so the desktop app and the
 * exported images look the same.
 */
final class Charts {

    private static final Color BACKGROUND = Color.web("#111827");
    private static final Color TEXT = Color.web("#f9fafb");
    private static final Color MUTED = Color.web("#9ca3af");
    private static final Color GRID = Color.web("#374151");

    private Charts() {
    }

    static void drawGantt(Canvas canvas, ScheduleResult result) {
        List<GanttSegment> timeline = result.timeline();
        int total = Math.max(1, result.totalTime());
        int startTime = timeline.isEmpty() ? 0 : timeline.get(0).start();
        double pxPerUnit = SvgSupport.clamp(760.0 / total, 16.0, 54.0);

        double left = 30;
        double barTop = 30;
        double barHeight = 66;
        double chartWidth = total * pxPerUnit;

        canvas.setWidth(left + chartWidth + 30);
        canvas.setHeight(barTop + barHeight + 60);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        background(gc, canvas);

        List<String> labels = new ArrayList<>();
        for (GanttSegment segment : timeline) {
            if (!segment.isIdle()) {
                labels.add(segment.label());
            }
        }
        Map<String, String> colors = Palette.assign(labels);

        for (GanttSegment segment : timeline) {
            double x = left + (segment.start() - startTime) * pxPerUnit;
            double w = Math.max(1, segment.duration() * pxPerUnit);
            Color fill = segment.isIdle()
                    ? Color.web(Palette.IDLE_COLOR)
                    : Color.web(colors.get(segment.label()));
            gc.setFill(fill);
            gc.fillRoundRect(x, barTop, w, barHeight, 10, 10);
            gc.setStroke(Color.web("#0b1220"));
            gc.setLineWidth(2);
            gc.strokeRoundRect(x, barTop, w, barHeight, 10, 10);
            if (w >= 24) {
                gc.setFill(TEXT);
                gc.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText(segment.label(), x + w / 2, barTop + barHeight / 2 + 5);
            }
        }

        // Time axis.
        gc.setStroke(GRID);
        gc.setLineWidth(1.5);
        double axisY = barTop + barHeight + 6;
        gc.strokeLine(left, axisY, left + chartWidth, axisY);
        gc.setFill(MUTED);
        gc.setFont(Font.font("System", 12));
        gc.setTextAlign(TextAlignment.CENTER);

        List<Integer> boundaries = new ArrayList<>();
        boundaries.add(startTime);
        for (GanttSegment segment : timeline) {
            boundaries.add(segment.end());
        }
        for (int boundary : boundaries) {
            double x = left + (boundary - startTime) * pxPerUnit;
            gc.setStroke(GRID);
            gc.strokeLine(x, axisY - 4, x, axisY + 5);
            gc.fillText(Integer.toString(boundary), x, axisY + 20);
        }
    }

    static void drawSeek(Canvas canvas, DiskResult result, int axisMax) {
        List<Integer> path = result.path();
        int stops = path.size();
        int maxValue = Math.max(1, Math.max(axisMax,
                path.stream().mapToInt(Integer::intValue).max().orElse(1)));

        double left = 50;
        double plotWidth = 700;
        double topAxis = 46;
        double rowHeight = 32;

        canvas.setWidth(left + plotWidth + 60);
        canvas.setHeight(topAxis + stops * rowHeight + 30);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        background(gc, canvas);

        gc.setStroke(GRID);
        gc.setLineWidth(1.5);
        gc.strokeLine(left, topAxis, left + plotWidth, topAxis);

        gc.setFont(Font.font("System", 11));
        gc.setTextAlign(TextAlignment.CENTER);
        for (int position : new TreeSet<>(path)) {
            double x = left + (position / (double) maxValue) * plotWidth;
            gc.setStroke(Color.web("#1f2937"));
            gc.strokeLine(x, topAxis, x, canvas.getHeight() - 20);
            gc.setFill(MUTED);
            gc.fillText(Integer.toString(position), x, topAxis - 8);
        }

        gc.setStroke(Color.web("#4f9dde"));
        gc.setLineWidth(2.5);
        double prevX = 0;
        double prevY = 0;
        for (int i = 0; i < stops; i++) {
            double x = left + (path.get(i) / (double) maxValue) * plotWidth;
            double y = topAxis + 16 + i * rowHeight;
            if (i > 0) {
                gc.strokeLine(prevX, prevY, x, y);
            }
            prevX = x;
            prevY = y;
        }

        gc.setTextAlign(TextAlignment.LEFT);
        for (int i = 0; i < stops; i++) {
            double x = left + (path.get(i) / (double) maxValue) * plotWidth;
            double y = topAxis + 16 + i * rowHeight;
            gc.setFill(i == 0 ? Color.web("#e0c341") : Color.web("#4f9dde"));
            gc.fillOval(x - 5, y - 5, 10, 10);
            gc.setFill(Color.web("#e5e7eb"));
            gc.setFont(Font.font("System", 12));
            gc.fillText(Integer.toString(path.get(i)), x + 10, y + 4);
        }
    }

    static void drawBarChart(Canvas canvas, String title, List<String> labels, List<Double> values) {
        if (labels.size() != values.size()) {
            throw new IllegalArgumentException("labels and values must be the same length");
        }
        int rows = labels.size();
        double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        if (max <= 0) {
            max = 1.0;
        }

        double left = 240;
        double plotWidth = 420;
        double top = 50;
        double rowHeight = 34;
        double barHeight = 22;

        canvas.setWidth(left + plotWidth + 80);
        canvas.setHeight(top + rows * rowHeight + 20);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        background(gc, canvas);

        gc.setFill(TEXT);
        gc.setFont(Font.font("System", FontWeight.BOLD, 16));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(title, 20, 30);

        gc.setFont(Font.font("System", 13));
        for (int i = 0; i < rows; i++) {
            double y = top + i * rowHeight;
            double value = values.get(i);
            double barWidth = Math.max(value > 0 ? 2 : 0, (value / max) * plotWidth);
            Color color = Color.web(Palette.COLORS.get(i % Palette.COLORS.size()));

            gc.setFill(Color.web("#e5e7eb"));
            gc.setTextAlign(TextAlignment.RIGHT);
            gc.fillText(labels.get(i), left - 12, y + barHeight - 5);

            gc.setFill(color);
            gc.fillRoundRect(left, y, barWidth, barHeight, 6, 6);

            gc.setFill(MUTED);
            gc.setTextAlign(TextAlignment.LEFT);
            gc.fillText(String.format(Locale.US, "%.2f", value), left + barWidth + 8, y + barHeight - 5);
        }
    }

    private static void background(GraphicsContext gc, Canvas canvas) {
        gc.setFill(BACKGROUND);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
}
