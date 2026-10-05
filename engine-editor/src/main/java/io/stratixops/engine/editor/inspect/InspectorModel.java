package io.stratixops.engine.editor.inspect;

import io.stratixops.engine.inspect.ComponentDescriptor;
import io.stratixops.engine.inspect.ComponentRegistry;
import io.stratixops.engine.inspect.FieldDescriptor;
import io.stratixops.engine.scene.Component;
import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Transform;
import io.stratixops.engine.scene.UnsupportedComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builds the headless model the Inspector draws: the ordered components of an
 * object and the ordered, header-grouped field rows for each component
 * (STR-23, ADR-0007). Keeping this separate from ImGui keeps it unit-testable.
 */
public final class InspectorModel {

    private final ComponentRegistry registry;

    /**
     * @param registry the component registry
     */
    public InspectorModel(ComponentRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    /**
     * The inspector draw order: the mandatory Transform first, then the other
     * components in attachment order. Only supported components are returned.
     *
     * @param object the object, or {@code null}
     * @return the ordered components
     */
    public List<Component> componentsOf(GameObject object) {
        List<Component> result = new ArrayList<>();
        if (object == null) {
            return result;
        }
        result.add(object.transform());
        for (Component component : object.components()) {
            if (component instanceof Transform || component instanceof UnsupportedComponent) {
                continue;
            }
            if (descriptorOf(component) != null) {
                result.add(component);
            }
        }
        return result;
    }

    /**
     * @param component the component
     * @return true when the Inspector can draw it
     */
    public boolean isInspectable(Component component) {
        return descriptorOf(component) != null;
    }

    /**
     * Builds the supported field rows of a component in display order, tagging
     * each row with its {@code @Header} section when present.
     *
     * @param component the component
     * @return the rows
     */
    public List<FieldRow> rows(Component component) {
        List<FieldRow> rows = new ArrayList<>();
        ComponentDescriptor descriptor = descriptorOf(component);
        if (descriptor == null) {
            return rows;
        }
        String header = null;
        for (FieldDescriptor field : descriptor.fields()) {
            if (!field.kind().isSupported()) {
                continue;
            }
            if (field.header() != null) {
                header = field.header();
            }
            rows.add(new FieldRow(header, component, field));
        }
        return rows;
    }

    private ComponentDescriptor descriptorOf(Component component) {
        if (component == null || component instanceof UnsupportedComponent) {
            return null;
        }
        return registry.descriptor(component.getClass());
    }
}
