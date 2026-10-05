package io.stratixops.engine.editor.ui;

import io.stratixops.engine.editor.EditorState;
import io.stratixops.engine.editor.PanelKind;
import io.stratixops.engine.editor.command.Command;
import io.stratixops.engine.editor.console.LogLevel;
import io.stratixops.engine.imgui.drag.DragAndDrop;
import io.stratixops.engine.imgui.theme.Theme;

/**
 * Shared services and host callbacks the editor panels render against (STR-40).
 * It keeps the panels decoupled from the frame loop: a panel reads the editor
 * state, dispatches undoable edits through {@link EditorState#execute}, and asks
 * the host to rebuild the dockspace or quit through the callbacks it sets.
 */
public final class EditorContext {

    final EditorState state;
    final ThemeProvider themes;
    final EditorLayoutStore layouts;
    final DragAndDrop drag = new DragAndDrop();
    final CommandPaletteController palette;

    /** The panel that owned focus on the previous frame (set by the panels). */
    PanelKind focusedPanel = PanelKind.SCENE;
    /** Whether a text field captured the keyboard on the previous frame. */
    boolean textInput;

    /** Host hook: save the open scene. */
    Runnable onSaveScene = () -> {
    };
    /** Host hook: request application shutdown. */
    Runnable onQuit = () -> {
    };
    /** Host hook: rebuild the dock nodes from the current layout. */
    Runnable onRequestDockRebuild = () -> {
    };
    /** Host hook: switch the ImGui style to the active theme. */
    Runnable onThemeChanged = () -> {
    };

    EditorContext(EditorState state, ThemeProvider themes, EditorLayoutStore layouts) {
        this.state = state;
        this.themes = themes;
        this.layouts = layouts;
        this.palette = new CommandPaletteController(this);
    }

    /** @return the active theme. */
    Theme theme() {
        return themes.theme();
    }

    /**
     * Runs an undoable edit and marks the scene dirty.
     *
     * @param command the command
     */
    void execute(Command command) {
        state.execute(command);
    }

    /**
     * Appends a console entry.
     *
     * @param level   the severity
     * @param message the message
     */
    void log(LogLevel level, String message) {
        state.console().log(level, message);
    }

    /** Toggles dark/light and reapplies the ImGui style. */
    void toggleTheme() {
        themes.toggle();
        onThemeChanged.run();
    }
}
