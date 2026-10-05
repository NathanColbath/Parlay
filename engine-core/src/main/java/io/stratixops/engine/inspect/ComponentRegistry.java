package io.stratixops.engine.inspect;

import io.stratixops.engine.scene.Animator;
import io.stratixops.engine.scene.AudioSource;
import io.stratixops.engine.scene.Behaviour;
import io.stratixops.engine.scene.Camera;
import io.stratixops.engine.scene.Collider2D;
import io.stratixops.engine.scene.Component;
import io.stratixops.engine.scene.SpriteRenderer;
import io.stratixops.engine.scene.Transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The authoritative {@code typeId <-> Class} map and descriptor cache shared by
 * the serializer, the loader, and the Inspector (ADR-0007).
 *
 * <p>Registering a type validates the no-arg constructor and builds its
 * descriptor once. Programmer errors — duplicate {@code typeId}, a missing
 * no-arg constructor — fail fast; content errors (an unknown {@code typeId} at
 * load time) are handled by the serializer as placeholders.
 */
public final class ComponentRegistry {

    private final Map<String, ComponentDescriptor> byTypeId = new LinkedHashMap<>();
    private final Map<Class<? extends Component>, ComponentDescriptor> byClass = new LinkedHashMap<>();

    /** Creates an empty registry. */
    public ComponentRegistry() {
    }

    /**
     * Creates a registry with the built-in P0 components registered.
     *
     * @return a registry ready for use
     */
    public static ComponentRegistry withBuiltins() {
        ComponentRegistry registry = new ComponentRegistry();
        registry.register(Transform.class);
        registry.register(SpriteRenderer.class);
        registry.register(Animator.class);
        registry.register(Camera.class);
        registry.register(AudioSource.class);
        registry.register(Collider2D.class);
        return registry;
    }

    /**
     * Registers a component type under its default {@code typeId}
     * ({@code behaviour:<fqcn>} for {@link Behaviour}s, otherwise the
     * decapitalized simple name).
     *
     * @param type the component class
     * @return the built descriptor
     */
    public ComponentDescriptor register(Class<? extends Component> type) {
        return register(type, defaultTypeId(type));
    }

    /**
     * Registers a component type under an explicit {@code typeId}.
     *
     * @param type   the component class
     * @param typeId the stable type id
     * @return the built descriptor
     * @throws IllegalStateException on a duplicate {@code typeId} or class
     */
    public ComponentDescriptor register(Class<? extends Component> type, String typeId) {
        if (byTypeId.containsKey(typeId)) {
            throw new IllegalStateException("duplicate typeId: " + typeId);
        }
        if (byClass.containsKey(type)) {
            throw new IllegalStateException("component already registered: " + type.getName());
        }
        ComponentDescriptor descriptor = ComponentDescriptor.build(type, typeId);
        byTypeId.put(typeId, descriptor);
        byClass.put(type, descriptor);
        return descriptor;
    }

    /**
     * Registers every type in the collection.
     *
     * @param types the component classes
     */
    public void registerAll(Collection<Class<? extends Component>> types) {
        for (Class<? extends Component> type : types) {
            register(type);
        }
    }

    /**
     * The default {@code typeId} for a class.
     *
     * @param type the component class
     * @return the stable type id
     */
    public static String defaultTypeId(Class<? extends Component> type) {
        if (Behaviour.class.isAssignableFrom(type)) {
            return "behaviour:" + type.getName();
        }
        String name = type.getSimpleName();
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * @param typeId the serialized type id
     * @return true when a type is registered under it
     */
    public boolean contains(String typeId) {
        return byTypeId.containsKey(typeId);
    }

    /**
     * @param type the component class
     * @return true when the class is registered
     */
    public boolean contains(Class<? extends Component> type) {
        return byClass.containsKey(type);
    }

    /**
     * Looks up a descriptor by class.
     *
     * @param type the component class
     * @return the descriptor, or {@code null}
     */
    public ComponentDescriptor descriptor(Class<? extends Component> type) {
        return byClass.get(type);
    }

    /**
     * Looks up a descriptor by serialized type id.
     *
     * @param typeId the type id
     * @return the descriptor, or {@code null}
     */
    public ComponentDescriptor descriptor(String typeId) {
        return byTypeId.get(typeId);
    }

    /**
     * @param type the component class
     * @return its registered type id
     * @throws IllegalStateException when the class is not registered
     */
    public String typeId(Class<? extends Component> type) {
        ComponentDescriptor descriptor = byClass.get(type);
        if (descriptor == null) {
            throw new IllegalStateException("unregistered component: " + type.getName());
        }
        return descriptor.typeId();
    }

    /**
     * @return all registered type ids, in registration order
     */
    public List<String> typeIds() {
        return Collections.unmodifiableList(new ArrayList<>(byTypeId.keySet()));
    }

    /**
     * @return all registered descriptors, in registration order
     */
    public List<ComponentDescriptor> descriptors() {
        return Collections.unmodifiableList(new ArrayList<>(byTypeId.values()));
    }

    /**
     * Instantiates a component by type id.
     *
     * @param typeId the type id
     * @return a fresh component
     * @throws IllegalStateException when the type id is unknown
     */
    public Component newInstance(String typeId) {
        ComponentDescriptor descriptor = byTypeId.get(typeId);
        if (descriptor == null) {
            throw new IllegalStateException("unknown typeId: " + typeId);
        }
        return descriptor.newInstance();
    }
}
