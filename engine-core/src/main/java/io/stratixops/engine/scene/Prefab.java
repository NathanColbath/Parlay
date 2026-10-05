package io.stratixops.engine.scene;

import java.util.Objects;

/**
 * A reusable object template with a stable {@link Guid}. The {@link #root()}
 * object and its descendants are the prefab's content.
 */
public final class Prefab {

    private final Guid guid;
    private String name;
    private GameObject root;

    /**
     * Creates a prefab with a fresh GUID.
     *
     * @param name the prefab name
     * @param root the root object
     */
    public Prefab(String name, GameObject root) {
        this(Guid.random(), name, root);
    }

    /**
     * Creates a prefab with an explicit identity.
     *
     * @param guid stable identity
     * @param name prefab name
     * @param root root object
     */
    public Prefab(Guid guid, String name, GameObject root) {
        this.guid = Objects.requireNonNull(guid, "guid");
        this.name = Objects.requireNonNull(name, "name");
        this.root = Objects.requireNonNull(root, "root");
    }

    /**
     * @return the stable identity
     */
    public Guid guid() {
        return guid;
    }

    /**
     * @return the prefab name
     */
    public String name() {
        return name;
    }

    /**
     * Renames the prefab. Identity is unaffected.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    /**
     * @return the root object
     */
    public GameObject root() {
        return root;
    }

    /**
     * Replaces the root object.
     *
     * @param root the new root
     */
    public void setRoot(GameObject root) {
        this.root = Objects.requireNonNull(root, "root");
    }
}
