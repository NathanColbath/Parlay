package io.stratixops.engine.scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A runtime scene: a list of root {@link GameObject}s plus scene-wide settings
 * (name, background color) and a stable {@link Guid}.
 *
 * <p>{@link #update(float)} / {@link #fixedUpdate(float)} /
 * {@link #lateUpdate(float)} drive the component lifecycle on every active
 * object. Drawing is the responsibility of {@link SceneRenderer}, the single
 * submission path shared by the runtime and the editor.
 */
public final class Scene {

    /** Default background color ({@code 0x1B1B2FFF}). */
    public static final int DEFAULT_BACKGROUND = 0x1B1B2FFF;

    private final Guid guid;
    private String name;
    private int background = DEFAULT_BACKGROUND;
    private final List<GameObject> gameObjects = new ArrayList<>();

    /**
     * Creates a scene with a fresh GUID.
     *
     * @param name the scene name
     */
    public Scene(String name) {
        this(Guid.random(), name);
    }

    /**
     * Creates a scene with an explicit identity.
     *
     * @param guid stable identity
     * @param name scene name
     */
    public Scene(Guid guid, String name) {
        this.guid = Objects.requireNonNull(guid, "guid");
        this.name = Objects.requireNonNull(name, "name");
    }

    /**
     * @return the stable identity
     */
    public Guid guid() {
        return guid;
    }

    /**
     * @return the scene name
     */
    public String name() {
        return name;
    }

    /**
     * Renames the scene. Identity is unaffected.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    /**
     * @return the packed background color
     */
    public int background() {
        return background;
    }

    /**
     * Sets the background color.
     *
     * @param background packed {@code 0xRRGGBBAA}
     */
    public void setBackground(int background) {
        this.background = background;
    }

    /**
     * The live root-object list.
     *
     * @return the root objects
     */
    public List<GameObject> gameObjects() {
        return gameObjects;
    }

    /**
     * Adds a root object.
     *
     * @param object the object
     * @param <T>    the concrete type
     * @return the same object
     */
    public <T extends GameObject> T add(T object) {
        gameObjects.add(Objects.requireNonNull(object, "object"));
        return object;
    }

    /**
     * Removes a root object and drives its destroy hooks.
     *
     * @param object the object
     * @return true when it was present
     */
    public boolean remove(GameObject object) {
        if (!gameObjects.remove(object)) {
            return false;
        }
        object.driveDestroy();
        return true;
    }

    /**
     * Finds an object anywhere in the scene by GUID.
     *
     * @param guid the identity
     * @return the object, or {@code null}
     */
    public GameObject findObject(Guid guid) {
        for (GameObject root : gameObjects) {
            GameObject found = findInSubtree(root, guid);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * Finds the first object (depth-first) with the given name.
     *
     * @param name the name
     * @return the object, or {@code null}
     */
    public GameObject findByName(String name) {
        for (GameObject root : gameObjects) {
            GameObject found = findByNameInSubtree(root, name);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private GameObject findInSubtree(GameObject object, Guid target) {
        if (object.guid().equals(target)) {
            return object;
        }
        for (GameObject child : object.children()) {
            GameObject found = findInSubtree(child, target);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private GameObject findByNameInSubtree(GameObject object, String target) {
        if (object.name().equals(target)) {
            return object;
        }
        for (GameObject child : object.children()) {
            GameObject found = findByNameInSubtree(child, target);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * Drives {@code awake}/{@code start}/{@code update} on every active object.
     *
     * @param dt seconds elapsed
     */
    public void update(float dt) {
        for (GameObject object : new ArrayList<>(gameObjects)) {
            object.driveUpdate(dt);
        }
    }

    /**
     * Drives {@code fixedUpdate} on every active object.
     *
     * @param dt fixed seconds step
     */
    public void fixedUpdate(float dt) {
        for (GameObject object : new ArrayList<>(gameObjects)) {
            object.driveFixedUpdate(dt);
        }
    }

    /**
     * Drives {@code lateUpdate} on every active object.
     *
     * @param dt seconds elapsed
     */
    public void lateUpdate(float dt) {
        for (GameObject object : new ArrayList<>(gameObjects)) {
            object.driveLateUpdate(dt);
        }
    }

    /**
     * Destroys every object (firing {@code onDestroy}) and clears the scene.
     */
    public void dispose() {
        for (GameObject object : new ArrayList<>(gameObjects)) {
            object.driveDestroy();
        }
        gameObjects.clear();
    }
}
