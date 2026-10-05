package io.stratixops.engine.editor.console;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

/**
 * One Console line: level, message, optional {@code file:line} source, and an
 * optional captured stack trace (STR-23).
 */
public record ConsoleEntry(LogLevel level, String message, String source, String stack) {

    /**
     * Creates a plain entry with no source.
     *
     * @param level   the severity
     * @param message the message
     */
    public ConsoleEntry(LogLevel level, String message) {
        this(level, message, null, null);
    }

    /**
     * Creates an entry from a throwable, capturing the first
     * {@code file:line} frame as the source.
     *
     * @param level     the severity
     * @param message   the message
     * @param throwable the throwable, or {@code null}
     * @return the entry
     */
    public static ConsoleEntry of(LogLevel level, String message, Throwable throwable) {
        Objects.requireNonNull(level, "level");
        if (throwable == null) {
            return new ConsoleEntry(level, message, null, null);
        }
        return new ConsoleEntry(level, message, sourceOf(throwable), stackOf(throwable));
    }

    /**
     * @return the file name portion of {@link #source()}, or {@code null}
     */
    public String file() {
        if (source == null) {
            return null;
        }
        int colon = source.lastIndexOf(':');
        return colon < 0 ? source : source.substring(0, colon);
    }

    /**
     * @return the line number from {@link #source()}, or {@code -1}
     */
    public int line() {
        if (source == null) {
            return -1;
        }
        int colon = source.lastIndexOf(':');
        if (colon < 0) {
            return -1;
        }
        try {
            return Integer.parseInt(source.substring(colon + 1));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static String sourceOf(Throwable throwable) {
        for (StackTraceElement frame : throwable.getStackTrace()) {
            if (frame.getFileName() != null) {
                return frame.getFileName() + ":" + frame.getLineNumber();
            }
        }
        return null;
    }

    private static String stackOf(Throwable throwable) {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString().stripTrailing();
    }
}
