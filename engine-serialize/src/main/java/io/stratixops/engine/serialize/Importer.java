package io.stratixops.engine.serialize;

import java.util.Map;

/**
 * Describes how an asset extension is imported and versioned (ADR-0006 §7).
 *
 * <p>{@link #version()} drives re-import when an importer changes; changing it
 * never changes the asset GUID.
 */
public interface Importer {

    /**
     * @return the stable importer id written to the {@code .meta} sidecar
     */
    String id();

    /**
     * @return the importer version; bump to force re-import
     */
    int version();

    /**
     * @param extension lowercase file extension without the dot
     * @return true when this importer handles the extension
     */
    boolean supports(String extension);

    /**
     * @return the default settings written to a fresh {@code .meta}
     */
    Map<String, Object> defaultSettings();
}
