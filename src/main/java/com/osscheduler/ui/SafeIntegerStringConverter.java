package com.osscheduler.ui;

import javafx.util.StringConverter;

/**
 * Converts between text and whole numbers for the editable table cells, without
 * throwing when the user types something that is not a number.
 *
 * <p>The built in converter throws on bad input, which shows up as an ugly error
 * in the console. This one simply returns {@code null} instead, and the table is
 * set up to ignore a {@code null} edit and keep the old value.</p>
 */
final class SafeIntegerStringConverter extends StringConverter<Integer> {

    @Override
    public String toString(Integer value) {
        return value == null ? "" : value.toString();
    }

    @Override
    public Integer fromString(String text) {
        if (text == null) {
            return null;
        }
        try {
            return Integer.valueOf(text.trim());
        } catch (NumberFormatException ex) {
            return null; // rejected; the table keeps the previous value
        }
    }
}
