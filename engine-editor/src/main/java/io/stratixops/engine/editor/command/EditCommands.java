package io.stratixops.engine.editor.command;

import io.stratixops.engine.editor.inspect.FieldValues;
import io.stratixops.engine.editor.play.SceneCloner;
import io.stratixops.engine.inspect.ComponentRegistry;
import io.stratixops.engine.inspect.FieldDescriptor;
import io.stratixops.engine.scene.Component;
import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Scene;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Factory for every undoable edit the editor performs (STR-23). Commands
 * capture the prior state eagerly and mutate the live scene lists directly so
 * undo restores exact ordering without driving destroy hooks.
 */
public final class EditCommands {

    private EditCommands() {
    }

    private record Slot(GameObject object, GameObject parent, int index) {
    }

    /**
     * Adds an object to the scene, optionally under a parent.
     *
     * @param scene  the scene
     * @param object the new object
     * @param parent the parent, or {@code null} for a root
     * @return the command
     */
    public static Command create(Scene scene, GameObject object, GameObject parent) {
        return new SimpleCommand("Create " + object.name(),
                () -> attach(scene, object, parent, -1),
                () -> detach(scene, object));
    }

    /**
     * Adds an already-cloned subtree to the scene (paste).
     *
     * @param scene  the scene
     * @param object the cloned subtree root
     * @param parent the parent, or {@code null} for a root
     * @return the command
     */
    public static Command paste(Scene scene, GameObject object, GameObject parent) {
        return new SimpleCommand("Paste " + object.name(),
                () -> attach(scene, object, parent, -1),
                () -> detach(scene, object));
    }

    /**
     * Deletes the given objects. Descendants of a deleted object are covered by
     * their ancestor and skipped.
     *
     * @param scene   the scene
     * @param objects the objects
     * @return the command
     */
    public static Command delete(Scene scene, List<GameObject> objects) {
        List<Slot> slots = slotsOf(scene, topLevel(objects));
        List<Slot> removal = new ArrayList<>(slots);
        removal.sort(Comparator.comparingInt(Slot::index).reversed());
        return new SimpleCommand(slots.size() == 1 ? "Delete " + slots.get(0).object().name() : "Delete",
                () -> {
                    for (Slot slot : removal) {
                        containerOf(scene, slot.object()).remove(slot.object());
                    }
                },
                () -> {
                    for (Slot slot : slots) {
                        insert(scene, slot.object(), slot.parent(), slot.index());
                    }
                });
    }

    /**
     * Renames an object.
     *
     * @param object  the object
     * @param newName the new name
     * @return the command
     */
    public static Command rename(GameObject object, String newName) {
        String old = object.name();
        return new SimpleCommand("Rename " + old,
                () -> object.setName(newName),
                () -> object.setName(old));
    }

    /**
     * Reparents an object (append).
     *
     * @param scene     the scene
     * @param object    the object
     * @param newParent the new parent, or {@code null} for root
     * @return the command
     */
    public static Command reparent(Scene scene, GameObject object, GameObject newParent) {
        GameObject oldParent = object.parent();
        int oldIndex = containerOf(scene, object).indexOf(object);
        return new SimpleCommand("Reparent " + object.name(),
                () -> insert(scene, object, newParent, -1),
                () -> insert(scene, object, oldParent, oldIndex));
    }

    /**
     * Enables or disables a component.
     *
     * @param component the component
     * @param value     the new enabled state
     * @return the command
     */
    public static Command setEnabled(Component component, boolean value) {
        boolean old = component.enabled();
        return new SimpleCommand((value ? "Enable " : "Disable ") + component.getClass().getSimpleName(),
                () -> component.setEnabled(value),
                () -> component.setEnabled(old));
    }

    /**
     * Writes a descriptor-mapped field (Inspector edit).
     *
     * @param component the component
     * @param field     the field descriptor
     * @param value     the new value
     * @return the command
     */
    public static Command setField(Component component, FieldDescriptor field, Object value) {
        Object old = FieldValues.snapshot(field, field.get(component));
        Object next = FieldValues.snapshot(field, value);
        return new SimpleCommand("Edit " + field.displayName(),
                () -> FieldValues.set(component, field, next),
                () -> FieldValues.set(component, field, old));
    }

    /**
     * Duplicates a subtree in place, inserted directly after the source.
     *
     * @param scene    the scene
     * @param source   the subtree root
     * @param registry the component registry
     * @return the command
     */
    public static Command duplicate(Scene scene, GameObject source, ComponentRegistry registry) {
        return duplicate(scene, source, SceneCloner.duplicate(source, registry));
    }

    /**
     * Inserts a pre-built clone directly after an existing object.
     *
     * @param scene    the scene
     * @param source   the reference object
     * @param clone    the clone to insert
     * @return the command
     */
    public static Command duplicate(Scene scene, GameObject source, GameObject clone) {
        GameObject parent = source.parent();
        int index = containerOf(scene, source).indexOf(source) + 1;
        return new SimpleCommand("Duplicate " + source.name(),
                () -> insert(scene, clone, parent, index),
                () -> detach(scene, clone));
    }

    private static List<Slot> slotsOf(Scene scene, List<GameObject> objects) {
        List<Slot> slots = new ArrayList<>();
        for (GameObject object : objects) {
            slots.add(new Slot(object, object.parent(), containerOf(scene, object).indexOf(object)));
        }
        return slots;
    }

    private static List<GameObject> topLevel(List<GameObject> objects) {
        List<GameObject> result = new ArrayList<>();
        Set<GameObject> set = new HashSet<>(objects);
        for (GameObject object : objects) {
            boolean covered = false;
            for (GameObject cursor = object.parent(); cursor != null; cursor = cursor.parent()) {
                if (set.contains(cursor)) {
                    covered = true;
                    break;
                }
            }
            if (!covered) {
                result.add(object);
            }
        }
        return result;
    }

    private static List<GameObject> containerOf(Scene scene, GameObject object) {
        return object.parent() != null ? object.parent().children() : scene.gameObjects();
    }

    private static void attach(Scene scene, GameObject object, GameObject parent, int index) {
        insert(scene, object, parent, index);
    }

    private static void insert(Scene scene, GameObject object, GameObject parent, int index) {
        if (parent == null) {
            if (object.parent() != null) {
                object.setParent(null);
            }
            List<GameObject> roots = scene.gameObjects();
            roots.remove(object);
            roots.add(index < 0 ? roots.size() : Math.min(index, roots.size()), object);
            return;
        }
        scene.gameObjects().remove(object);
        object.setParent(parent);
        List<GameObject> children = parent.children();
        children.remove(object);
        children.add(index < 0 ? children.size() : Math.min(index, children.size()), object);
    }

    private static void detach(Scene scene, GameObject object) {
        containerOf(scene, object).remove(object);
    }
}
