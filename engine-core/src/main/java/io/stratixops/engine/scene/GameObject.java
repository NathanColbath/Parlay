package io.stratixops.engine.scene;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A named scene node with a stable {@link Guid}, a mandatory {@link Transform},
 * an ordered list of {@link Component}s, and a parent/child hierarchy.
 */
public final class GameObject {

    private final Guid guid;
    private String name;
    private boolean active = true;
    private int layer;
    private final Set<String> tags = new LinkedHashSet<>();
    private final List<Component> components = new ArrayList<>();
    private final List<GameObject> children = new ArrayList<>();
    private GameObject parent;
    private final Transform transform;

    /**
     * Creates an object with a fresh GUID and an "Untitled" name.
     */
    public GameObject() {
        this(Guid.random(), "Untitled");
    }

    /**
     * Creates an object with an explicit identity.
     *
     * @param guid stable identity
     * @param name display name
     */
    public GameObject(Guid guid, String name) {
        this.guid = Objects.requireNonNull(guid, "guid");
        this.name = Objects.requireNonNull(name, "name");
        this.transform = new Transform();
        addComponent(transform);
    }

    /**
     * @return the stable identity
     */
    public Guid guid() {
        return guid;
    }

    /**
     * @return the display name
     */
    public String name() {
        return name;
    }

    /**
     * Renames the object. Identity is unaffected.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    /**
     * @return this object's own active flag (ignores parents)
     */
    public boolean active() {
        return active;
    }

    /**
     * Sets this object's own active flag.
     *
     * @param active the new state
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return true when this object and every ancestor is active
     */
    public boolean activeInHierarchy() {
        if (!active) {
            return false;
        }
        return parent == null || parent.activeInHierarchy();
    }

    /**
     * @return the layer index
     */
    public int layer() {
        return layer;
    }

    /**
     * Sets the layer index.
     *
     * @param layer the layer
     */
    public void setLayer(int layer) {
        this.layer = layer;
    }

    /**
     * Mutable tag set, in insertion order.
     *
     * @return the tags
     */
    public Set<String> tags() {
        return tags;
    }

    /**
     * The mandatory transform.
     *
     * @return the transform
     */
    public Transform transform() {
        return transform;
    }

    /**
     * Ordered components. The returned list is live; attach with
     * {@link #addComponent(Component)}.
     *
     * @return the components
     */
    public List<Component> components() {
        return components;
    }

    /**
     * Attaches a component, setting its owner.
     *
     * @param component the component
     * @param <T>       component type
     * @return the same component, for chaining
     * @throws IllegalArgumentException if a second {@link Transform} is added
     */
    public <T extends Component> T addComponent(T component) {
        Objects.requireNonNull(component, "component");
        if (component instanceof Transform && hasTransform()) {
            throw new IllegalArgumentException("a GameObject can only have one Transform");
        }
        component.attach(this);
        components.add(component);
        return component;
    }

    /**
     * Detaches a component and drives its destroy hooks.
     *
     * @param component the component to remove
     * @return true when it was attached
     */
    public boolean removeComponent(Component component) {
        if (component == null || !components.remove(component)) {
            return false;
        }
        component.driveDestroy();
        component.detach();
        return true;
    }

    /**
     * Finds the first component of the given type.
     *
     * @param type component class
     * @param <T>  component type
     * @return the component, or {@code null}
     */
    public <T extends Component> T getComponent(Class<T> type) {
        for (Component c : components) {
            if (type.isInstance(c)) {
                return type.cast(c);
            }
        }
        return null;
    }

    private boolean hasTransform() {
        for (Component c : components) {
            if (c instanceof Transform) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the parent, or {@code null} for a root object
     */
    public GameObject parent() {
        return parent;
    }

    /**
     * Reparents this object, keeping both sides consistent. A cycle (parenting
     * under a descendant) is rejected.
     *
     * @param newParent the new parent, or {@code null} to detach
     * @throws IllegalArgumentException if {@code newParent} is this object or a descendant
     */
    public void setParent(GameObject newParent) {
        if (newParent == this) {
            throw new IllegalArgumentException("a GameObject cannot be its own parent");
        }
        for (GameObject cursor = newParent; cursor != null; cursor = cursor.parent) {
            if (cursor == this) {
                throw new IllegalArgumentException("cannot parent a GameObject under its own descendant");
            }
        }
        if (parent != null) {
            parent.children.remove(this);
        }
        this.parent = newParent;
        if (newParent != null && !newParent.children.contains(this)) {
            newParent.children.add(this);
        }
    }

    /**
     * Ordered children.
     *
     * @return the children
     */
    public List<GameObject> children() {
        return children;
    }

    // ------------------------------------------------------------------
    // Lifecycle driving — package-private so only Scene calls it.
    // ------------------------------------------------------------------

    void driveUpdate(float dt) {
        if (!activeInHierarchy()) {
            return;
        }
        for (Component c : new ArrayList<>(components)) {
            c.driveAwake();
            c.driveStart();
            c.driveUpdate(dt);
        }
    }

    void driveFixedUpdate(float dt) {
        if (!activeInHierarchy()) {
            return;
        }
        for (Component c : new ArrayList<>(components)) {
            c.driveAwake();
            c.driveFixedUpdate(dt);
        }
    }

    void driveLateUpdate(float dt) {
        if (!activeInHierarchy()) {
            return;
        }
        for (Component c : new ArrayList<>(components)) {
            c.driveAwake();
            c.driveLateUpdate(dt);
        }
    }

    void driveDestroy() {
        for (Component c : new ArrayList<>(components)) {
            c.driveDestroy();
        }
    }

    @Override
    public String toString() {
        return "GameObject[" + name + " " + guid + "]";
    }
}
