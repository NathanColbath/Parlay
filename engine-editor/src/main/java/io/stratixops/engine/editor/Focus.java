package io.stratixops.engine.editor;

/**
 * The focus context a keyboard chord is interpreted in (STR-40).
 *
 * <p>ImGui owns the "is a text field capturing the keyboard" decision; the UI
 * layer passes {@link #TEXT} when {@code io.getWantTextInput()} is set so the
 * router can let the field handle the chord instead of the panel. The panel
 * kinds mirror {@link PanelKind}, with {@link #NONE} for "no panel owns focus".
 */
public enum Focus {

    /** No panel is focused. */
    NONE,
    /** The Hierarchy panel. */
    HIERARCHY,
    /** The Inspector panel. */
    INSPECTOR,
    /** The Project panel. */
    PROJECT,
    /** The Console panel. */
    CONSOLE,
    /** The Scene view. */
    SCENE,
    /** The Game view. */
    GAME,
    /** A focused text field; the field consumes editing keys. */
    TEXT;

    /**
     * Resolves the focus from a panel kind.
     *
     * @param kind the focused panel
     * @return the matching focus
     */
    public static Focus of(PanelKind kind) {
        if (kind == null) {
            return NONE;
        }
        return switch (kind) {
            case HIERARCHY -> HIERARCHY;
            case INSPECTOR -> INSPECTOR;
            case PROJECT -> PROJECT;
            case CONSOLE -> CONSOLE;
            case SCENE -> SCENE;
            case GAME -> GAME;
        };
    }
}
