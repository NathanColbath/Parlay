package io.stratixops.engine.inspect;

import io.stratixops.engine.math.Vec2;
import io.stratixops.engine.scene.AssetRef;
import io.stratixops.engine.scene.ObjectRef;
import io.stratixops.engine.scene.PrefabRef;

import java.util.List;

/**
 * The closed set of field kinds the descriptor model and the serializer
 * understand (ADR-0007). Keeping this set explicit is what lets the file format
 * stay stable and the Inspector draw a control for every field.
 */
public enum PropertyKind {

    /** {@code boolean}. */
    BOOLEAN,
    /** {@code byte}/{@code short}/{@code int}. */
    INT,
    /** {@code long}. */
    LONG,
    /** {@code float}. */
    FLOAT,
    /** {@code double}. */
    DOUBLE,
    /** {@link String}. */
    STRING,
    /** An {@code enum}. */
    ENUM,
    /** {@link Vec2}. */
    VEC2,
    /** Packed {@code 0xRRGGBBAA} {@code int} marked {@link Color}. */
    COLOR,
    /** {@link AssetRef} (asset GUID). */
    ASSET,
    /** {@link ObjectRef} (GameObject GUID). */
    GAMEOBJECT,
    /** {@link PrefabRef} (prefab GUID). */
    PREFAB,
    /** A {@code List<T>} of any other supported kind. */
    LIST,
    /** A type with no Phase 1 mapping; omitted from serialization. */
    UNSUPPORTED;

    /**
     * @return true when this kind can be written and read back
     */
    public boolean isSupported() {
        return this != UNSUPPORTED;
    }

    /**
     * Resolves the kind for a declared field type.
     *
     * @param type           the declared field type
     * @param colorAnnotated whether the field carries {@link Color}
     * @return the kind, or {@link #UNSUPPORTED}
     */
    public static PropertyKind fromType(Class<?> type, boolean colorAnnotated) {
        if (type == boolean.class || type == Boolean.class) {
            return BOOLEAN;
        }
        if (type == byte.class || type == short.class || type == int.class
                || type == Byte.class || type == Short.class || type == Integer.class) {
            return colorAnnotated ? COLOR : INT;
        }
        if (type == long.class || type == Long.class) {
            return LONG;
        }
        if (type == float.class || type == Float.class) {
            return FLOAT;
        }
        if (type == double.class || type == Double.class) {
            return DOUBLE;
        }
        if (type == String.class) {
            return STRING;
        }
        if (type.isEnum()) {
            return ENUM;
        }
        if (type == Vec2.class) {
            return VEC2;
        }
        if (type == AssetRef.class) {
            return ASSET;
        }
        if (type == ObjectRef.class) {
            return GAMEOBJECT;
        }
        if (type == PrefabRef.class) {
            return PREFAB;
        }
        if (type == List.class) {
            return LIST;
        }
        return UNSUPPORTED;
    }
}
