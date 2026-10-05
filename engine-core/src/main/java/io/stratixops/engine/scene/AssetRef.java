package io.stratixops.engine.scene;

import java.util.Objects;

/**
 * A GUID reference to an imported asset (sprite, atlas, audio, font).
 *
 * <p>Serialized as {@code { "$asset": "<guid>" }} (ADR-0006). A {@code null}
 * reference means "unset".
 */
public final class AssetRef {

    private final Guid guid;

    /**
     * Creates a reference to the given asset.
     *
     * @param guid asset GUID, or {@code null} for an empty reference
     */
    public AssetRef(Guid guid) {
        this.guid = guid;
    }

    /**
     * Creates a reference to the given asset.
     *
     * @param guid asset GUID, or {@code null}
     * @return the reference
     */
    public static AssetRef of(Guid guid) {
        return new AssetRef(guid);
    }

    /**
     * @return the referenced asset GUID, or {@code null}
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
        return o instanceof AssetRef other && Objects.equals(guid, other.guid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(guid);
    }

    @Override
    public String toString() {
        return guid == null ? "AssetRef[none]" : "AssetRef[" + guid + "]";
    }
}
