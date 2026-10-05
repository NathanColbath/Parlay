package io.stratixops.engine.editor;

/**
 * An editor action resolved from a keyboard chord (STR-40, per the STR-20 /
 * G&amp;TD resolution).
 *
 * <p>Kept deliberately free of ImGui so the routing table can be unit-tested
 * headlessly; {@code ShortcutRouter} maps a chord and focus context to one of
 * these, and the UI layer runs the matching editor command.
 */
public enum Shortcut {

    /** No shortcut matched; the key is left to the focused widget. */
    NONE,
    /** Enter: activate the primary selection (open scene, focus view, ...). */
    ACTIVATE,
    /** F2: begin inline rename of the primary selection. */
    RENAME,
    /** Delete: delete the selected objects. */
    DELETE,
    /** Ctrl+D: duplicate the primary selection. */
    DUPLICATE,
    /** Ctrl+Z: undo the last edit. */
    UNDO,
    /** Ctrl+Y / Ctrl+Shift+Z: redo the last undone edit. */
    REDO,
    /** Ctrl+S: save the open scene. */
    SAVE,
    /** Ctrl+A outside a text field: select every item in the focused panel. */
    SELECT_ALL_PANEL,
    /** Ctrl+Shift+P: open the command palette from anywhere. */
    OPEN_COMMAND_PALETTE,
    /** Space: toggle Play/Pause. */
    PLAY_TOGGLE
}
