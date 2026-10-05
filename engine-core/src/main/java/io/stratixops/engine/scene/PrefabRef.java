package io.stratixops.engine.scene;

import java.util.Objects;

/**
 * A GUID reference to a prefab.
 *
 * <p>Serialized as {@code { "$ref": "<guid>" }} (ADR-0006). A {@code null}
 * reference means "unset".
 */
public final class PrefabRef {

    private final Guid guid;

    /**
     * Creates a reference to the given prefab.
     *
     * @param guid prefab GUID, or {@code null} for an empty reference
     */
    public PrefabRef(Guid guid) {
        this.guid = guid;
    }

    /**
     * Creates a reference to the given prefab.
     *
     * @param guid prefab GUID, or {@code null}
     * @return the reference
     */
    public static PrefabRef of(Guid guid) {
        return new PrefabRef(guid);
    }

    /**
     * @return the referenced prefab GUID, or {@code null}
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
        return o instanceof PrefabRef other && Objects.equals(guid, other.guid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(guid);
    }

    @Override
    public String toString() {
        return guid == null ? "PrefabRef[none]" : "PrefabRef[" + guid + "]";
    }
}
