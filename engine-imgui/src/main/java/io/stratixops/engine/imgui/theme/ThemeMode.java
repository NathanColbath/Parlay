package io.stratixops.engine.imgui.theme;

/** The two resolved themes. Dark is the default (design system section 1.1). */
public enum ThemeMode {

    DARK("dark"),
    LIGHT("light");

    private final String id;

    ThemeMode(String id) {
        this.id = id;
    }

    /** @return the {@code tokens.json} color sub-key for this mode. */
    public String id() {
        return id;
    }
}
