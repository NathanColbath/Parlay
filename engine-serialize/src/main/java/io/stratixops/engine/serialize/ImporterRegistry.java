package io.stratixops.engine.serialize;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps lowercase file extensions to {@link Importer}s (ADR-0006 §7). An
 * unregistered extension falls back to a raw importer with no settings.
 */
public final class ImporterRegistry {

    private final Map<String, Importer> byExtension = new LinkedHashMap<>();

    /** Creates an empty registry. */
    public ImporterRegistry() {
    }

    /**
     * Creates a registry with the built-in png/audio/json importers.
     *
     * @return a registry with the default importers
     */
    public static ImporterRegistry withDefaults() {
        ImporterRegistry registry = new ImporterRegistry();
        registry.register(new PngImporter());
        registry.register(new AudioImporter());
        registry.register(new JsonImporter());
        return registry;
    }

    /**
     * Registers an importer for every extension it supports.
     *
     * @param importer the importer
     */
    public void register(Importer importer) {
        for (String extension : new String[]{"png", "jpg", "jpeg", "wav", "ogg", "mp3", "json"}) {
            if (importer.supports(extension)) {
                byExtension.put(extension, importer);
            }
        }
    }

    /**
     * Looks up the importer for an extension.
     *
     * @param extension lowercase extension without the dot
     * @return the importer, or {@code null} when none handles it
     */
    public Importer forExtension(String extension) {
        return byExtension.get(extension);
    }

    /**
     * @return the registered extensions, in registration order
     */
    public java.util.Set<String> extensions() {
        return java.util.Collections.unmodifiableSet(byExtension.keySet());
    }
}
