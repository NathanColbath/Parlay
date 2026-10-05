package io.stratixops.engine.editor.console;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * The Console backing model: a bounded, filterable log with levels and
 * {@code file:line} sources (STR-23). Filtering is by minimum level and a
 * case-insensitive text match over message and source.
 */
public final class ConsoleModel {

    private static final int DEFAULT_CAPACITY = 1000;

    private final List<ConsoleEntry> entries = new ArrayList<>();
    private final int capacity;
    private LogLevel minLevel = LogLevel.INFO;
    private String search = "";

    /**
     * Creates a console with the default capacity.
     */
    public ConsoleModel() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Creates a console with an explicit capacity.
     *
     * @param capacity the maximum retained entries, at least 1
     */
    public ConsoleModel(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be >= 1");
        }
        this.capacity = capacity;
    }

    /**
     * Appends an entry, evicting the oldest when full.
     *
     * @param entry the entry
     */
    public void add(ConsoleEntry entry) {
        Objects.requireNonNull(entry, "entry");
        entries.add(entry);
        while (entries.size() > capacity) {
            entries.remove(0);
        }
    }

    /**
     * Appends a plain message.
     *
     * @param level   the severity
     * @param message the message
     */
    public void log(LogLevel level, String message) {
        add(new ConsoleEntry(level, message));
    }

    /**
     * Appends an info message.
     *
     * @param message the message
     */
    public void info(String message) {
        log(LogLevel.INFO, message);
    }

    /**
     * Appends a warning.
     *
     * @param message the message
     */
    public void warn(String message) {
        log(LogLevel.WARNING, message);
    }

    /**
     * Appends an error, capturing a throwable's stack trace.
     *
     * @param message   the message
     * @param throwable the throwable
     */
    public void error(String message, Throwable throwable) {
        add(ConsoleEntry.of(LogLevel.ERROR, message, throwable));
    }

    /**
     * Appends an error.
     *
     * @param message the message
     */
    public void error(String message) {
        log(LogLevel.ERROR, message);
    }

    /**
     * @return every retained entry, oldest first (unmodifiable copy)
     */
    public List<ConsoleEntry> entries() {
        return List.copyOf(entries);
    }

    /**
     * @return entries passing the active filters, oldest first
     */
    public List<ConsoleEntry> filtered() {
        List<ConsoleEntry> result = new ArrayList<>();
        String needle = search.toLowerCase(Locale.ROOT);
        for (ConsoleEntry entry : entries) {
            if (entry.level().ordinal() < minLevel.ordinal()) {
                continue;
            }
            if (!needle.isEmpty() && !matches(entry, needle)) {
                continue;
            }
            result.add(entry);
        }
        return result;
    }

    private static boolean matches(ConsoleEntry entry, String needle) {
        if (entry.message() != null && entry.message().toLowerCase(Locale.ROOT).contains(needle)) {
            return true;
        }
        return entry.source() != null && entry.source().toLowerCase(Locale.ROOT).contains(needle);
    }

    /**
     * @return the minimum displayed level
     */
    public LogLevel minLevel() {
        return minLevel;
    }

    /**
     * Sets the minimum displayed level.
     *
     * @param minLevel the level
     */
    public void setMinLevel(LogLevel minLevel) {
        this.minLevel = Objects.requireNonNull(minLevel, "minLevel");
    }

    /**
     * @return the active search text
     */
    public String search() {
        return search;
    }

    /**
     * Sets the search text.
     *
     * @param search the text
     */
    public void setSearch(String search) {
        this.search = search == null ? "" : search;
    }

    /**
     * @param level the level
     * @return the number of retained entries at exactly that level
     */
    public int count(LogLevel level) {
        int total = 0;
        for (ConsoleEntry entry : entries) {
            if (entry.level() == level) {
                total++;
            }
        }
        return total;
    }

    /**
     * @return the retained entry counts by level
     */
    public Map<LogLevel, Integer> counts() {
        Map<LogLevel, Integer> counts = new EnumMap<>(LogLevel.class);
        for (LogLevel level : LogLevel.values()) {
            counts.put(level, 0);
        }
        for (ConsoleEntry entry : entries) {
            counts.merge(entry.level(), 1, Integer::sum);
        }
        return counts;
    }

    /**
     * Clears all entries (filters are preserved).
     */
    public void clear() {
        entries.clear();
    }
}
