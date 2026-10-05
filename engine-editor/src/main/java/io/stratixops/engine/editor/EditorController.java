package io.stratixops.engine.editor;

import io.stratixops.engine.editor.command.EditCommands;
import io.stratixops.engine.editor.play.SceneCloner;
import io.stratixops.engine.inspect.ComponentDescriptor;
import io.stratixops.engine.inspect.FieldDescriptor;
import io.stratixops.engine.scene.Component;
import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Guid;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The headless command facade the panels call: object creation, selection
 * edits, copy/paste, component/field edits, and reparenting, all routed through
 * the undo stack (STR-23).
 */
public final class EditorController {

    private final EditorState state;
    private final List<GameObject> clipboard = new ArrayList<>();

    /**
     * @param state the editor session
     */
    public EditorController(EditorState state) {
        this.state = Objects.requireNonNull(state, "state");
    }

    /**
     * @return the editor session
     */
    public EditorState state() {
        return state;
    }

    /**
     * Creates a root-level object with a unique name and selects it.
     *
     * @param name the preferred name
     * @return the created object
     */
    public GameObject createGameObject(String name) {
        return create(name, null);
    }

    /**
     * Creates an object as a child of the primary selection and selects it.
     *
     * @param name the preferred name
     * @return the created object
     */
    public GameObject createChild(String name) {
        return create(name, state.selection().primary());
    }

    private GameObject create(String name, GameObject parent) {
        String base = (name == null || name.isBlank()) ? "GameObject" : name;
        GameObject object = new GameObject(Guid.random(), uniqueName(base));
        state.execute(EditCommands.create(state.scene(), object, parent));
        state.selection().select(object);
        return object;
    }

    /**
     * Deletes the current selection.
     *
     * @return true when anything was deleted
     */
    public boolean deleteSelection() {
        List<GameObject> targets = state.selection().objects();
        if (targets.isEmpty()) {
            return false;
        }
        state.execute(EditCommands.delete(state.scene(), targets));
        state.selection().clear();
        return true;
    }

    /**
     * Duplicates the current selection in place and selects the copies.
     *
     * @return true when anything was duplicated
     */
    public boolean duplicateSelection() {
        List<GameObject> targets = state.selection().objects();
        if (targets.isEmpty()) {
            return false;
        }
        List<GameObject> clones = new ArrayList<>();
        for (GameObject source : targets) {
            GameObject clone = SceneCloner.duplicate(source, state.registry());
            state.execute(EditCommands.duplicate(state.scene(), source, clone));
            clones.add(clone);
        }
        state.selection().set(clones);
        return true;
    }

    /**
     * Copies the current selection into the clipboard.
     *
     * @return the number of copied roots
     */
    public int copySelection() {
        clipboard.clear();
        for (GameObject source : state.selection().objects()) {
            clipboard.add(SceneCloner.duplicate(source, state.registry()));
        }
        return clipboard.size();
    }

    /**
     * Pastes the clipboard under the primary selection (or at root) and selects
     * the pasted objects.
     *
     * @return true when anything was pasted
     */
    public boolean paste() {
        if (clipboard.isEmpty()) {
            return false;
        }
        GameObject parent = state.selection().primary();
        List<GameObject> pasted = new ArrayList<>();
        for (GameObject clip : clipboard) {
            GameObject clone = SceneCloner.duplicate(clip, state.registry());
            state.execute(EditCommands.paste(state.scene(), clone, parent));
            pasted.add(clone);
        }
        state.selection().set(pasted);
        return true;
    }

    /**
     * @return true when the clipboard holds nothing
     */
    public boolean clipboardEmpty() {
        return clipboard.isEmpty();
    }

    /**
     * @return the number of roots in the clipboard
     */
    public int clipboardSize() {
        return clipboard.size();
    }

    /**
     * Renames an object.
     *
     * @param object  the object
     * @param newName the new name
     * @return true when the name changed
     */
    public boolean rename(GameObject object, String newName) {
        if (object == null || newName == null || object.name().equals(newName)) {
            return false;
        }
        state.execute(EditCommands.rename(object, newName));
        return true;
    }

    /**
     * Reparents an object.
     *
     * @param object the object
     * @param parent the new parent or {@code null}
     * @return true when the parent changed
     */
    public boolean reparent(GameObject object, GameObject parent) {
        if (object == null || object.parent() == parent) {
            return false;
        }
        state.execute(EditCommands.reparent(state.scene(), object, parent));
        return true;
    }

    /**
     * Enables or disables a component.
     *
     * @param component the component
     * @param value     the new state
     * @return true when the state changed
     */
    public boolean setEnabled(Component component, boolean value) {
        if (component == null || component.enabled() == value) {
            return false;
        }
        state.execute(EditCommands.setEnabled(component, value));
        return true;
    }

    /**
     * Writes an Inspector field.
     *
     * @param component the component
     * @param field     the descriptor
     * @param value     the new value
     */
    public void setField(Component component, FieldDescriptor field, Object value) {
        state.execute(EditCommands.setField(component, field, value));
    }

    /**
     * Adds a registered component to an object and selects the object.
     *
     * @param object the object
     * @param type   the component class
     * @param <T>    the component type
     * @return the new component
     */
    public <T extends Component> T addComponent(GameObject object, Class<T> type) {
        ComponentDescriptor descriptor = state.registry().descriptor(type);
        if (descriptor == null) {
            throw new IllegalStateException("unregistered component: " + type.getName());
        }
        T component = type.cast(descriptor.newInstance());
        state.execute(EditCommands.addComponent(object, component));
        state.selection().select(object);
        return component;
    }

    /**
     * Removes a component from an object.
     *
     * @param object    the object
     * @param component the component
     * @return true when the component was attached
     */
    public boolean removeComponent(GameObject object, Component component) {
        if (object == null || component == null || !object.components().contains(component)) {
            return false;
        }
        state.execute(EditCommands.removeComponent(object, component));
        return true;
    }

    private String uniqueName(String base) {
        Set<String> names = new LinkedHashSet<>();
        collectNames(names);
        if (!names.contains(base)) {
            return base;
        }
        int suffix = 1;
        while (names.contains(base + " (" + suffix + ")")) {
            suffix++;
        }
        return base + " (" + suffix + ")";
    }

    private void collectNames(Set<String> names) {
        for (GameObject root : state.scene().gameObjects()) {
            collectNames(root, names);
        }
    }

    private static void collectNames(GameObject object, Set<String> names) {
        names.add(object.name());
        for (GameObject child : object.children()) {
            collectNames(child, names);
        }
    }
}
