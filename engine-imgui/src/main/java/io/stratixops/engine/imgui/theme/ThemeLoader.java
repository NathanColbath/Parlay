package io.stratixops.engine.imgui.theme;

import java.util.EnumMap;
import java.util.Map;

/**
 * Builds {@link Theme} instances from the shipped token document.
 *
 * <p>All tokens come from a single classpath resource; no hex value, size, or
 * radius may appear in code that is not in that file (design system AC1).
 */
public final class ThemeLoader {

    /** Default token resource: the design system's machine-readable source of truth. */
    public static final String DEFAULT_RESOURCE = "/io/stratixops/engine/imgui/tokens.json";

    private ThemeLoader() {
    }

    /** Loads the default theme for a mode. */
    public static Theme load(ThemeMode mode) {
        return load(mode, DEFAULT_RESOURCE);
    }

    /** Loads a theme for a mode from a named resource. */
    public static Theme load(ThemeMode mode, String resource) {
        return build(TokenDocument.fromResource(resource), mode);
    }

    /** Loads both themes from the same document, sharing one parse. */
    public static Map<ThemeMode, Theme> loadAll(String resource) {
        TokenDocument document = TokenDocument.fromResource(resource);
        Map<ThemeMode, Theme> themes = new EnumMap<>(ThemeMode.class);
        for (ThemeMode mode : ThemeMode.values()) {
            themes.put(mode, build(document, mode));
        }
        return themes;
    }

    static Theme build(TokenDocument document, ThemeMode mode) {
        ColorTokens color = new ColorTokens(document, "color/" + mode.id() + "/");
        SpaceTokens space = new SpaceTokens(document);
        SizeTokens size = new SizeTokens(document);
        return new Theme(
                mode,
                color,
                new TypographyTokens(document),
                space,
                size,
                new RadiusTokens(document),
                new ElevationTokens(document, mode, color),
                new MotionTokens(document),
                new IconTokens(size));
    }
}
