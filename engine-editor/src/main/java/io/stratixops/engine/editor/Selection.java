package io.stratixops.engine.editor;

import io.stratixops.engine.scene.GameObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * The editor selection: an ordered set of objects plus one primary object
 * (the Inspector shows the primary). Order is stable so multi-object edits and
 * marquee selections are deterministic.
 */
public final class Selection {

    private final LinkedHashSet<GameObject> selected = new LinkedHashSet<>();
    private GameObject primary;

    /**
     * @return the selected objects in selection order (unmodifiable)
     */
    public List<GameObject> objects() {
        return Collections.unmodifiableList(new ArrayList<>(selected));
    }

    /**
     * @return the primary object, or {@code null}
     */
    public GameObject primary() {
        if (primary != null && !selected.contains(primary)) {
            primary = selected.isEmpty() ? null : selected.iterator().next();
        }
        return primary;
    }

    /**
     * @return the number of selected objects
     */
    public int size() {
        return selected.size();
    }

    /**
     * @return true when nothing is selected
     */
    public boolean isEmpty() {
        return selected.isEmpty();
    }

    /**
     * @param object the object
     * @return true when selected
     */
    public boolean contains(GameObject object) {
        return selected.contains(object);
    }

    /**
     * Replaces the selection with a single object.
     *
     * @param object the object, or {@code null} to clear
     */
    public void select(GameObject object) {
        selected.clear();
        if (object != null) {
            selected.add(object);
        }
        primary = object;
    }

    /**
     * Replaces the selection with an ordered list; the last object becomes
     * primary.
     *
     * @param objects the objects (null entries ignored)
     */
    public void set(List<GameObject> objects) {
        selected.clear();
        if (objects != null) {
            for (GameObject object : objects) {
                if (object != null) {
                    selected.add(object);
                }
            }
        }
        primary = objects == null || objects.isEmpty() ? null : objects.get(objects.size() - 1);
    }

    /**
     * Adds an object and makes it primary.
     *
     * @param object the object
     */
    public void add(GameObject object) {
        if (object == null) {
            return;
        }
        selected.add(object);
        primary = object;
    }

    /**
     * Removes an object; the primary falls back to the next selected object.
     *
     * @param object the object
     */
    public void remove(GameObject object) {
        selected.remove(object);
        if (primary == object) {
            primary = selected.isEmpty() ? null : selected.iterator().next();
        }
    }

    /**
     * Adds the object if absent, or removes it if present (Ctrl-click).
     *
     * @param object the object
     */
    public void toggle(GameObject object) {
        if (object == null) {
            return;
        }
        if (selected.contains(object)) {
            remove(object);
        } else {
            add(object);
        }
    }

    /**
     * Clears the selection.
     */
    public void clear() {
        selected.clear();
        primary = null;
    }
}
