package io.stratixops.engine.imgui.widget;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Display formatting for list view metadata (design system section 3.4).
 * Pure helpers so size/time formatting is unit-testable without ImGui.
 */
public final class AssetFormat {

    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private AssetFormat() {
    }

    /** @return a compact byte size such as {@code 0 B}, {@code 512 B}, {@code 1.5 KB}. */
    public static String size(long bytes) {
        if (bytes < 1024L) {
            return bytes + " B";
        }
        double value = bytes;
        String[] units = {"KB", "MB", "GB", "TB"};
        int unit = -1;
        while (value >= 1024.0 && unit < units.length - 1) {
            value /= 1024.0;
            unit++;
        }
        return String.format(java.util.Locale.ROOT, "%.1f %s", value, units[unit]);
    }

    /** @return a {@code yyyy-MM-dd HH:mm} timestamp, or {@code --} when unset. */
    public static String modified(long epochSeconds) {
        if (epochSeconds <= 0L) {
            return "--";
        }
        return TIMESTAMP.format(Instant.ofEpochSecond(epochSeconds));
    }
}
