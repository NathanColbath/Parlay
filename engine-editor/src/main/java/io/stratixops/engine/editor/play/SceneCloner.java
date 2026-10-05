package io.stratixops.engine.editor.play;

import io.stratixops.engine.inspect.ComponentDescriptor;
import io.stratixops.engine.inspect.ComponentRegistry;
import io.stratixops.engine.inspect.FieldDescriptor;
import io.stratixops.engine.inspect.PropertyKind;
import io.stratixops.engine.scene.Component;
import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Guid;
import io.stratixops.engine.scene.ObjectRef;
import io.stratixops.engine.scene.Scene;
import io.stratixops.engine.scene.UnsupportedComponent;
import io.stratixops.engine.editor.inspect.FieldValues;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Deep-copies the canonical scene graph. Play mode runs on an independent copy
 * so the editor scene is never mutated and exit-Play restores it exactly
 * (STR-23, ADR-0008 section 4). Duplicate reuses the same mechanism with fresh
 * GUIDs and {@link ObjectRef} remapping.
 */
public final class SceneCloner {

    private SceneCloner() {
    }

    /**
     * Deep-copies a scene.
     *
     * @param source     the scene to copy
     * @param registry   the component registry (user behaviours included)
     * @param freshGuids assign new GUIDs and remap internal {@link ObjectRef}s
     * @return an independent scene
     */
    public static Scene deepCopy(Scene source, ComponentRegistry registry, boolean freshGuids) {
        Scene target = new Scene(freshGuids ? Guid.random() : source.guid(), source.name());
        target.setBackground(source.background());
        Map<GameObject, GameObject> copies = new IdentityHashMap<>();
        Map<Guid, Guid> remap = new HashMap<>();
        for (GameObject root : source.gameObjects()) {
            collect(root, copies, remap, registry, freshGuids);
        }
        for (Map.Entry<GameObject, GameObject> entry : copies.entrySet()) {
            GameObject old = entry.getKey();
            if (old.parent() != null && copies.containsKey(old.parent())) {
                entry.getValue().setParent(copies.get(old.parent()));
            }
        }
        for (GameObject root : source.gameObjects()) {
            target.add(copies.get(root));
        }
        remapObjectReferences(copies, remap, registry);
        return target;
    }

    /**
     * Duplicates an object subtree with fresh GUIDs, remapping references that
     * point inside the subtree.
     *
     * @param root     the subtree root
     * @param registry the component registry
     * @return a detached copy whose root has no parent
     */
    public static GameObject duplicate(GameObject root, ComponentRegistry registry) {
        Map<GameObject, GameObject> copies = new IdentityHashMap<>();
        Map<Guid, Guid> remap = new HashMap<>();
        collect(root, copies, remap, registry, true);
        for (Map.Entry<GameObject, GameObject> entry : copies.entrySet()) {
            GameObject old = entry.getKey();
            if (old.parent() != null && copies.containsKey(old.parent())) {
                entry.getValue().setParent(copies.get(old.parent()));
            }
        }
        remapObjectReferences(copies, remap, registry);
        return copies.get(root);
    }

    private static void collect(GameObject old, Map<GameObject, GameObject> copies,
                                Map<Guid, Guid> remap, ComponentRegistry registry, boolean freshGuids) {
        if (copies.containsKey(old)) {
            return;
        }
        Guid newGuid = freshGuids ? Guid.random() : old.guid();
        GameObject copy = new GameObject(newGuid, old.name());
        copy.setActive(old.active());
        copy.setLayer(old.layer());
        copy.tags().addAll(old.tags());
        copyComponents(old, copy, registry);
        copies.put(old, copy);
        remap.put(old.guid(), newGuid);
        for (GameObject child : old.children()) {
            collect(child, copies, remap, registry, freshGuids);
        }
    }

    private static void copyComponents(GameObject old, GameObject copy, ComponentRegistry registry) {
        ComponentDescriptor transform = registry.descriptor(io.stratixops.engine.scene.Transform.class);
        copyFields(old.transform(), copy.transform(), transform);
        copy.transform().setEnabled(old.transform().enabled());
        for (Component component : old.components()) {
            if (component instanceof io.stratixops.engine.scene.Transform) {
                continue;
            }
            if (component instanceof UnsupportedComponent unsupported) {
                UnsupportedComponent placeholder =
                        new UnsupportedComponent(unsupported.typeId(), unsupported.rawJson());
                placeholder.setEnabled(component.enabled());
                copy.addComponent(placeholder);
                continue;
            }
            ComponentDescriptor descriptor = registry.descriptor(component.getClass());
            if (descriptor == null) {
                throw new IllegalStateException(
                        "cannot copy unregistered component: " + component.getClass().getName());
            }
            Component duplicated = descriptor.newInstance();
            copyFields(component, duplicated, descriptor);
            duplicated.setEnabled(component.enabled());
            copy.addComponent(duplicated);
        }
    }

    private static void copyFields(Component source, Component target, ComponentDescriptor descriptor) {
        for (FieldDescriptor field : descriptor.serializedFields()) {
            FieldValues.set(target, field, FieldValues.snapshot(field, field.get(source)));
        }
    }

    private static void remapObjectReferences(Map<GameObject, GameObject> copies,
                                              Map<Guid, Guid> remap, ComponentRegistry registry) {
        for (GameObject copy : copies.values()) {
            for (Component component : copy.components()) {
                if (component instanceof UnsupportedComponent) {
                    continue;
                }
                ComponentDescriptor descriptor = registry.descriptor(component.getClass());
                if (descriptor == null) {
                    continue;
                }
                for (FieldDescriptor field : descriptor.serializedFields()) {
                    if (field.kind() != PropertyKind.GAMEOBJECT) {
                        continue;
                    }
                    Object value = field.get(component);
                    if (value instanceof ObjectRef ref && ref.isSet()) {
                        Guid mapped = remap.get(ref.guid());
                        if (mapped != null) {
                            field.set(component, new ObjectRef(mapped));
                        }
                    }
                }
            }
        }
    }
}
