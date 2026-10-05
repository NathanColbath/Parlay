package io.stratixops.engine.imgui.theme;

/**
 * A parsed elevation shadow ({@code elev.*}).
 *
 * @param dx   x offset in logical pixels
 * @param dy   y offset in logical pixels
 * @param blur blur radius in logical pixels
 * @param rgba shadow color as packed {@code 0xRRGGBBAA}
 */
public record Shadow(int dx, int dy, int blur, int rgba) {

    /** The {@code elev.0} "none" shadow. */
    public static final Shadow NONE = new Shadow(0, 0, 0, 0);

    public boolean isNone() {
        return rgba == 0 && blur == 0;
    }

    /**
     * Parses a CSS-like shadow token such as
     * {@code "0 2px 8px rgba(0,0,0,0.35)"} or {@code "none"}.
     */
    public static Shadow parse(String value) {
        if (value == null || value.isBlank() || value.equalsIgnoreCase("none")) {
            return NONE;
        }
        String[] parts = value.trim().split("\\s+");
        if (parts.length < 4) {
            throw new IllegalArgumentException("unsupported shadow: " + value);
        }
        return new Shadow(px(parts[0]), px(parts[1]), px(parts[2]), Rgba.parse(parts[3]));
    }

    private static int px(String s) {
        String v = s.endsWith("px") ? s.substring(0, s.length() - 2) : s;
        return Math.round(Float.parseFloat(v));
    }
}
