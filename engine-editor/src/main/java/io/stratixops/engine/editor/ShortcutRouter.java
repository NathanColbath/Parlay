package io.stratixops.engine.editor;

import java.util.Objects;

/**
 * Pure keyboard-shortcut router (STR-40, STR-20 / G&amp;TD resolution).
 *
 * <p>Given the focused panel and the modifier state, it maps a logical key to a
 * {@link Shortcut}. It knows nothing about ImGui key codes or rendering, so it
 * is exhaustively unit-testable; the UI layer translates ImGui input into
 * {@link Key} and runs the returned action.
 *
 * <p>Text fields win by construction: apart from the global command-palette
 * chord ({@code Ctrl+Shift+P}), a chord delivered while a text field owns the
 * keyboard resolves to {@link Shortcut#NONE} so the field can handle it.
 */
public final class ShortcutRouter {

    /** The logical keys the router understands. */
    public enum Key {
        ENTER,
        F2,
        DELETE,
        A,
        D,
        Z,
        Y,
        S,
        P,
        SPACE
    }

    private ShortcutRouter() {
    }

    /**
     * Resolves a chord to an editor action.
     *
     * @param focus the focused panel context (never {@code null})
     * @param ctrl  whether Ctrl is held
     * @param shift whether Shift is held
     * @param key   the logical key, or {@code null}
     * @return the action, or {@link Shortcut#NONE}
     */
    public static Shortcut resolve(Focus focus, boolean ctrl, boolean shift, Key key) {
        return resolve(focus, ctrl, shift, false, key);
    }

    /**
     * Resolves a chord to an editor action, including the Alt modifier used by
     * the transport shortcuts ({@code Ctrl+P}, {@code Shift+P},
     * {@code Ctrl+Alt+P}).
     *
     * @param focus the focused panel context (never {@code null})
     * @param ctrl  whether Ctrl is held
     * @param shift whether Shift is held
     * @param alt   whether Alt is held
     * @param key   the logical key, or {@code null}
     * @return the action, or {@link Shortcut#NONE}
     */
    public static Shortcut resolve(Focus focus, boolean ctrl, boolean shift, boolean alt, Key key) {
        Objects.requireNonNull(focus, "focus");
        if (key == null) {
            return Shortcut.NONE;
        }
        // Global: the command palette is reachable even from a text field.
        if (ctrl && shift && !alt && key == Key.P) {
            return Shortcut.OPEN_COMMAND_PALETTE;
        }
        if (focus == Focus.TEXT) {
            return Shortcut.NONE;
        }
        // Transport: Ctrl+P play, Shift+P pause, Ctrl+Alt+P step.
        if (ctrl && !shift && alt && key == Key.P) {
            return Shortcut.STEP;
        }
        if (ctrl && !shift && !alt && key == Key.P) {
            return Shortcut.PLAY;
        }
        if (!ctrl && shift && !alt && key == Key.P) {
            return Shortcut.PAUSE;
        }
        if (ctrl && !shift && key == Key.Z) {
            return Shortcut.UNDO;
        }
        if (ctrl && shift && key == Key.Z) {
            return Shortcut.REDO;
        }
        if (ctrl && !shift && key == Key.Y) {
            return Shortcut.REDO;
        }
        if (ctrl && !shift && key == Key.S) {
            return Shortcut.SAVE;
        }
        if (ctrl && !shift && key == Key.A) {
            return Shortcut.SELECT_ALL_PANEL;
        }
        if (ctrl && !shift && key == Key.D) {
            return Shortcut.DUPLICATE;
        }
        if (key == Key.ENTER) {
            return Shortcut.ACTIVATE;
        }
        if (key == Key.F2) {
            return Shortcut.RENAME;
        }
        if (key == Key.DELETE) {
            return Shortcut.DELETE;
        }
        if (key == Key.SPACE && (focus == Focus.SCENE || focus == Focus.GAME)) {
            return Shortcut.PLAY_TOGGLE;
        }
        return Shortcut.NONE;
    }

    /**
     * @param shortcut the action
     * @param focus    the focused panel
     * @return whether the action applies to that panel's selection
     */
    public static boolean isSelectionAction(Shortcut shortcut, Focus focus) {
        return switch (shortcut) {
            case ACTIVATE, RENAME, DELETE, DUPLICATE, SELECT_ALL_PANEL ->
                    focus == Focus.HIERARCHY || focus == Focus.PROJECT
                            || focus == Focus.INSPECTOR;
            default -> false;
        };
    }
}
