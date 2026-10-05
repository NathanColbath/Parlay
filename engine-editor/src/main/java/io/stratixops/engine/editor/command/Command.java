package io.stratixops.engine.editor.command;

/**
 * A reversible editor edit. Every mutation the GUI performs goes through a
 * command so undo/redo covers it uniformly (STR-23).
 */
public interface Command {

    /**
     * @return a short human-readable label, used in the Undo/Redo menu
     */
    String name();

    /**
     * Applies the edit.
     */
    void apply();

    /**
     * Reverts the edit, restoring the exact prior state.
     */
    void undo();
}
