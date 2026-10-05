package io.stratixops.engine.scene;

import java.util.UUID;

/**
 * A stable, opaque identity for scene objects and assets (ADR-0006).
 *
 * <p>A GUID is a lowercase, canonical UUID string. Identity — not a path — is
 * the canonical key used by scene/prefab references and {@code .meta} sidecars,
 * so renaming or moving an asset or object never breaks a reference.
 */
public final class Guid {

    private final String value;

    private Guid(String value) {
        this.value = value;
    }

    /**
     * Creates a fresh random (UUIDv4) GUID.
     *
     * @return a new GUID
     */
    public static Guid random() {
        return new Guid(UUID.randomUUID().toString());
    }

    /**
     * Wraps an existing GUID string. The value is normalized to lowercase.
     *
     * @param value a UUID string; must not be {@code null} or blank
     * @return the GUID
     * @throws IllegalArgumentException if the value is null/blank or not a UUID
     */
    public static Guid of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("guid must not be null or blank");
        }
        try {
            return new Guid(UUID.fromString(value.trim()).toString());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("not a valid guid: " + value, e);
        }
    }

    /**
     * The canonical lowercase UUID string.
     *
     * @return the GUID text
     */
    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Guid other)) {
            return false;
        }
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
