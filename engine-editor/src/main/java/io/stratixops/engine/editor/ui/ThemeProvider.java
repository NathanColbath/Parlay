package io.stratixops.engine.editor.ui;

import io.stratixops.engine.imgui.theme.Theme;
import io.stratixops.engine.imgui.theme.ThemeLoader;
import io.stratixops.engine.imgui.theme.ThemeMode;

import java.util.Map;

/**
 * Holds the resolved editor themes and the active mode (STR-40). Tokens come
 * from the single engine-imgui token document, so the editor never invents a
 * colour or size (design system AC1).
 */
public final class ThemeProvider {

    private final Map<ThemeMode, Theme> themes =
            ThemeLoader.loadAll(ThemeLoader.DEFAULT_RESOURCE);
    private ThemeMode mode = ThemeMode.DARK;

    /** @return the active theme. */
    public Theme theme() {
        return themes.get(mode);
    }

    /** @return the active mode. */
    public ThemeMode mode() {
        return mode;
    }

    /** Switches between the dark and light themes in place. */
    public void toggle() {
        setMode(mode == ThemeMode.DARK ? ThemeMode.LIGHT : ThemeMode.DARK);
    }

    /**
     * Selects a theme mode.
     *
     * @param mode the mode
     */
    public void setMode(ThemeMode mode) {
        this.mode = mode;
    }
}
