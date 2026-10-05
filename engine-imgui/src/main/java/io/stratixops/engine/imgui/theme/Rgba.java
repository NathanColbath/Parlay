package io.stratixops.engine.imgui.theme;

/**
 * Helpers for the design system's packed {@code 0xRRGGBBAA} colors.
 *
 * <p>The packing matches the engine's public
 * {@link io.stratixops.engine.Renderer} color convention, so a token value can
 * be handed straight to a draw call without an intermediate representation.
 */
public final class Rgba {

    private Rgba() {
    }

    /** Packs four 0..255 channels into {@code 0xRRGGBBAA}. */
    public static int pack(int r, int g, int b, int a) {
        return ((r & 0xFF) << 24) | ((g & 0xFF) << 16) | ((b & 0xFF) << 8) | (a & 0xFF);
    }

    public static int red(int c) {
        return (c >>> 24) & 0xFF;
    }

    public static int green(int c) {
        return (c >>> 16) & 0xFF;
    }

    public static int blue(int c) {
        return (c >>> 8) & 0xFF;
    }

    public static int alpha(int c) {
        return c & 0xFF;
    }

    /**
     * Parses a design-token color string. Supported forms are {@code #RGB},
     * {@code #RRGGBB}, {@code #RRGGBBAA} and {@code rgba(r,g,b,a)} where the
     * alpha channel is a 0..1 fraction.
     *
     * @param value color string from {@code tokens.json}
     * @return packed {@code 0xRRGGBBAA}
     */
    public static int parse(String value) {
        if (value == null) {
            throw new IllegalArgumentException("color is null");
        }
        String v = value.trim();
        if (v.startsWith("#")) {
            return parseHex(v.substring(1));
        }
        if (v.startsWith("rgba(") && v.endsWith(")")) {
            String[] parts = split(v.substring(5, v.length() - 1));
            return pack(channel(parts[0], 255), channel(parts[1], 255), channel(parts[2], 255),
                    Math.round(Float.parseFloat(parts[3].trim()) * 255f));
        }
        if (v.startsWith("rgb(") && v.endsWith(")")) {
            String[] parts = split(v.substring(4, v.length() - 1));
            return pack(channel(parts[0], 255), channel(parts[1], 255), channel(parts[2], 255), 255);
        }
        throw new IllegalArgumentException("unsupported color: " + value);
    }

    private static int parseHex(String hex) {
        String h = hex.trim();
        if (h.length() == 3) {
            int r = digit(h, 0);
            int g = digit(h, 1);
            int b = digit(h, 2);
            return pack(r * 17, g * 17, b * 17, 255);
        }
        if (h.length() == 6) {
            return pack(byteAt(h, 0), byteAt(h, 2), byteAt(h, 4), 255);
        }
        if (h.length() == 8) {
            return pack(byteAt(h, 0), byteAt(h, 2), byteAt(h, 4), byteAt(h, 6));
        }
        throw new IllegalArgumentException("unsupported hex color: #" + hex);
    }

    private static String[] split(String csv) {
        String[] parts = csv.split(",");
        if (parts.length != 4 && parts.length != 3) {
            throw new IllegalArgumentException("expected r,g,b[,a] but got: " + csv);
        }
        return parts;
    }

    private static int channel(String s, int fallback) {
        String t = s.trim();
        return t.isEmpty() ? fallback : Math.round(Float.parseFloat(t));
    }

    private static int digit(String h, int index) {
        int d = Character.digit(h.charAt(index), 16);
        if (d < 0) {
            throw new IllegalArgumentException("bad hex digit: " + h);
        }
        return d;
    }

    private static int byteAt(String h, int index) {
        return (digit(h, index) << 4) | digit(h, index + 1);
    }

    /**
     * Returns a copy of the color with its alpha channel replaced.
     *
     * @param color packed {@code 0xRRGGBBAA}
     * @param alpha 0..1 opacity
     * @return packed color with the new alpha
     */
    public static int withAlpha(int color, float alpha) {
        int a = Math.round(Math.max(0f, Math.min(1f, alpha)) * 255f);
        return (color & 0xFFFFFF00) | (a & 0xFF);
    }

    /** Converts a packed color to ImGui's normalized RGBA float order. */
    public static float[] toFloats(int color) {
        return new float[] {
            red(color) / 255f, green(color) / 255f, blue(color) / 255f, alpha(color) / 255f
        };
    }

    /** Renders a packed color as {@code #RRGGBBAA}. */
    public static String toHex(int color) {
        return String.format("#%02X%02X%02X%02X", red(color), green(color), blue(color), alpha(color));
    }
}
