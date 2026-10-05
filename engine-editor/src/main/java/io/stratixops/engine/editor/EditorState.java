package io.stratixops.engine.editor;

import io.stratixops.engine.editor.command.Command;
import io.stratixops.engine.editor.command.CommandStack;
import io.stratixops.engine.editor.console.ConsoleModel;
import io.stratixops.engine.editor.inspect.InspectorModel;
import io.stratixops.engine.editor.play.PlayController;
import io.stratixops.engine.inspect.ComponentRegistry;
import io.stratixops.engine.scene.Scene;

import java.nio.file.Path;
import java.util.Objects;

/**
 * The aggregate editor session state the panels read and mutate (STR-23): the
 * open scene, selection, component registry, command history, transport,
 * console, and docking layout. It owns no ImGui or GL types so it can be
 * driven headlessly.
 */
public final class EditorState {

    private final ComponentRegistry registry;
    private final InspectorModel inspector;
    private final Selection selection = new Selection();
    private final CommandStack commands = new CommandStack();
    private final PlayController play = new PlayController();
    private final ConsoleModel console = new ConsoleModel();
    private DockLayout layout = DockLayout.defaultLayout();
    private Scene scene;
    private Path projectRoot;
    private boolean dirty;

    /**
     * @param registry the component registry (built-ins + discovered scripts)
     */
    public EditorState(ComponentRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.inspector = new InspectorModel(registry);
        this.scene = new Scene("Untitled");
    }

    /**
     * @return the component registry
     */
    public ComponentRegistry registry() {
        return registry;
    }

    /**
     * @return the Inspector field model
     */
    public InspectorModel inspector() {
        return inspector;
    }

    /**
     * @return the selection
     */
    public Selection selection() {
        return selection;
    }

    /**
     * @return the undo/redo stack
     */
    public CommandStack commands() {
        return commands;
    }

    /**
     * @return the Play transport
     */
    public PlayController play() {
        return play;
    }

    /**
     * @return the Console model
     */
    public ConsoleModel console() {
        return console;
    }

    /**
     * @return the active docking layout
     */
    public DockLayout layout() {
        return layout;
    }

    /**
     * Replaces the docking layout.
     *
     * @param layout the layout
     */
    public void setLayout(DockLayout layout) {
        this.layout = Objects.requireNonNull(layout, "layout");
    }

    /**
     * @return the open scene
     */
    public Scene scene() {
        return scene;
    }

    /**
     * Opens a scene, resetting selection and history and clearing the dirty
     * flag (a fresh document).
     *
     * @param scene the scene
     */
    public void openScene(Scene scene) {
        this.scene = Objects.requireNonNull(scene, "scene");
        selection.clear();
        commands.clear();
        dirty = false;
    }

    /**
     * @return the project root, or {@code null}
     */
    public Path projectRoot() {
        return projectRoot;
    }

    /**
     * Sets the project root.
     *
     * @param projectRoot the root
     */
    public void setProjectRoot(Path projectRoot) {
        this.projectRoot = projectRoot;
    }

    /**
     * @return true when there are unsaved edits
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Marks the document clean (after save).
     */
    public void markClean() {
        dirty = false;
    }

    /**
     * Marks the document dirty.
     */
    public void markDirty() {
        dirty = true;
    }

    /**
     * Applies a command through the undo stack and marks the document dirty.
     *
     * @param command the command
     */
    public void execute(Command command) {
        commands.execute(command);
        dirty = true;
    }

    /**
     * Undoes the last edit, if any.
     *
     * @return true when an edit was undone
     */
    public boolean undo() {
        if (commands.undo() == null) {
            return false;
        }
        dirty = true;
        return true;
    }

    /**
     * Redoes the last undone edit, if any.
     *
     * @return true when an edit was redone
     */
    public boolean redo() {
        if (commands.redo() == null) {
            return false;
        }
        dirty = true;
        return true;
    }
}
