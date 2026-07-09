package com.osscheduler.viz;

/**
 * Tiny helpers shared by the SVG renderers so the same escaping and clamping
 * logic is not copied into every file.
 */
public final class SvgSupport {

    private SvgSupport() {
    }

    /** Escapes the characters that would otherwise break the SVG or XML markup. */
    public static String escape(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    /** Keeps a value inside the given range. */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
