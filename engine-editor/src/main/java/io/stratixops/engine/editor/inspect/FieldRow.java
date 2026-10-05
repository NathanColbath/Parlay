package io.stratixops.engine.editor.inspect;

import io.stratixops.engine.inspect.FieldDescriptor;
import io.stratixops.engine.scene.Component;

/**
 * One Inspector field row: the owning component, its descriptor, and the
 * optional section header (STR-23, ADR-0007).
 *
 * @param header    the section header, or {@code null}
 * @param component the owning component
 * @param field     the field descriptor
 */
public record FieldRow(String header, Component component, FieldDescriptor field) {
}
