package io.stratixops.engine.scene;

import java.util.Objects;

/**
 * A GUID reference to a {@link GameObject} in the same scene.
 *
 * <p>Serialized as {@code { "$ref": "<guid>" }} (ADR-0006). References are
 * GUID-keyed and never nested, which keeps files acyclic and diff-stable.
 */
public final class ObjectRef {

    private final Guid guid;

    /**
     * Creates a reference to the given object.
     *
     * @param guid object GUID, or {@code null} for an empty reference
     */
    public ObjectRef(Guid guid) {
        this.guid = guid;
    }

    /**
     * Creates a reference to the given object.
     *
     * @param guid object GUID, or {@code null}
     * @return the reference
     */
    public static ObjectRef of(Guid guid) {
        return new ObjectRef(guid);
    }

    /**
     * @return the referenced object GUID, or {@code null}
     */
    public Guid guid() {
        return guid;
    }

    /**
     * @return true when a GUID is set
     */
    public boolean isSet() {
        return guid != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof ObjectRef other && Objects.equals(guid, other.guid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(guid);
    }

    @Override
    public String toString() {
        return guid == null ? "ObjectRef[none]" : "ObjectRef[" + guid + "]";
    }
}
