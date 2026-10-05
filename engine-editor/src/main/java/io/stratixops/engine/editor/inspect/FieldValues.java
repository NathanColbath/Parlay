package io.stratixops.engine.editor.inspect;

import io.stratixops.engine.inspect.FieldDescriptor;
import io.stratixops.engine.inspect.PropertyKind;
import io.stratixops.engine.math.Vec2;
import io.stratixops.engine.scene.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Value copy semantics for Inspector edits and Play-mode cloning, mirroring the
 * serializer's field handling. {@link Vec2} and {@link List} fields are mutable
 * and often {@code final}, so they are copied in place rather than reassigned.
 * All other supported kinds are immutable value types and may be shared.
 */
public final class FieldValues {

    private FieldValues() {
    }

    /**
     * Takes a detached snapshot of a field value for undo and cloning.
     *
     * @param field the field descriptor
     * @param value the current value
     * @return a value safe to store independently of the source
     */
    public static Object snapshot(FieldDescriptor field, Object value) {
        if (field.kind() == PropertyKind.VEC2 && value instanceof Vec2 vec) {
            return new Vec2(vec.x, vec.y);
        }
        if (field.kind() == PropertyKind.LIST && value instanceof List<?> list) {
            return new ArrayList<>(list);
        }
        return value;
    }

    /**
     * Writes a value into a component, respecting {@link Vec2}/{@link List}
     * in-place copy semantics.
     *
     * @param target the component
     * @param field  the field descriptor
     * @param value  the value to write
     */
    public static void set(Component target, FieldDescriptor field, Object value) {
        switch (field.kind()) {
            case VEC2 -> {
                Object existing = field.get(target);
                if (existing instanceof Vec2 vec && value instanceof Vec2 source) {
                    vec.set(source.x, source.y);
                } else {
                    field.set(target, value);
                }
            }
            case LIST -> {
                Object existing = field.get(target);
                if (existing instanceof List<?> list && value instanceof List<?> source) {
                    @SuppressWarnings("unchecked")
                    List<Object> raw = (List<Object>) list;
                    raw.clear();
                    raw.addAll(source);
                } else {
                    field.set(target, value);
                }
            }
            default -> field.set(target, value);
        }
    }
}
