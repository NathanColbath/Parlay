package io.stratixops.engine.imgui.theme;

/**
 * Elevation tokens ({@code elev.*}). Shadows are resolved for the theme mode,
 * since {@code elev.1}..{@code elev.3} carry distinct dark/light values.
 */
public final class ElevationTokens {

    public final Shadow ELEV_0;
    public final Shadow ELEV_1;
    public final Shadow ELEV_2;
    public final Shadow ELEV_3;

    /** Border color token referenced by {@code elev.3}, resolved to a packed color. */
    public final int ELEV_3_BORDER;

    ElevationTokens(TokenDocument doc, ThemeMode mode, ColorTokens colors) {
        ELEV_0 = Shadow.parse(doc.string("elev/elev.0/shadow"));
        ELEV_1 = Shadow.parse(doc.string("elev/elev.1/" + mode.id()));
        ELEV_2 = Shadow.parse(doc.string("elev/elev.2/" + mode.id()));
        ELEV_3 = Shadow.parse(doc.string("elev/elev.3/" + mode.id()));
        ELEV_3_BORDER = resolveColorReference(doc.string("elev/elev.3/border"), colors);
    }

    private static int resolveColorReference(String reference, ColorTokens colors) {
        return switch (reference) {
            case "accent.default" -> colors.ACCENT_DEFAULT;
            default -> throw new IllegalArgumentException("unknown color reference: " + reference);
        };
    }

    /** @return every token path covered by this token group. */
    public static String[] paths() {
        return new String[] {
            "elev.0.shadow",
            "elev.1.dark", "elev.1.light",
            "elev.2.dark", "elev.2.light",
            "elev.3.dark", "elev.3.light", "elev.3.border"
        };
    }

    /** @return the shadow for a level 0..3. */
    public Shadow level(int index) {
        return switch (index) {
            case 0 -> ELEV_0;
            case 1 -> ELEV_1;
            case 2 -> ELEV_2;
            case 3 -> ELEV_3;
            default -> throw new IllegalArgumentException("elevation level out of range: " + index);
        };
    }
}
