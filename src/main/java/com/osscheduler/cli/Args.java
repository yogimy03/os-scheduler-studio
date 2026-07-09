package com.osscheduler.cli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A tiny command line argument parser, written by hand so the project has one
 * fewer dependency to explain.
 *
 * <p>It splits arguments into two buckets: positional values (like an algorithm
 * key) and {@code --flag value} options. Every flag in this tool expects a
 * value, so a flag written with no value after it (either at the end of the line
 * or right before another flag) is remembered as "given but empty" and produces
 * a clear error when it is read, rather than silently becoming a stray value.</p>
 */
final class Args {

    private final List<String> positionals;
    private final Map<String, String> options;
    private final Set<String> valueless;

    private Args(List<String> positionals, Map<String, String> options, Set<String> valueless) {
        this.positionals = positionals;
        this.options = options;
        this.valueless = valueless;
    }

    static Args parse(String[] tokens) {
        List<String> positionals = new ArrayList<>();
        Map<String, String> options = new HashMap<>();
        Set<String> valueless = new HashSet<>();
        int i = 0;
        while (i < tokens.length) {
            String token = tokens[i];
            if (token.startsWith("--")) {
                String key = token.substring(2);
                if (i + 1 < tokens.length && !tokens[i + 1].startsWith("--")) {
                    options.put(key, tokens[i + 1]);
                    i += 2;
                } else {
                    valueless.add(key);
                    i += 1;
                }
            } else {
                positionals.add(token);
                i += 1;
            }
        }
        return new Args(positionals, options, valueless);
    }

    String positional(int index, String what) {
        if (index >= positionals.size()) {
            throw new IllegalArgumentException("Missing " + what);
        }
        return positionals.get(index);
    }

    String required(String key) {
        checkHasValue(key);
        String value = options.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required option --" + key);
        }
        return value;
    }

    String getOr(String key, String fallback) {
        checkHasValue(key);
        return options.getOrDefault(key, fallback);
    }

    int intRequired(String key) {
        return toInt(key, required(key));
    }

    int intOr(String key, int fallback) {
        checkHasValue(key);
        String value = options.get(key);
        return value == null ? fallback : toInt(key, value);
    }

    private void checkHasValue(String key) {
        if (valueless.contains(key)) {
            throw new IllegalArgumentException("Option --" + key + " needs a value after it");
        }
    }

    private static int toInt(String key, String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Option --" + key + " must be a whole number, got '" + value + "'");
        }
    }
}
