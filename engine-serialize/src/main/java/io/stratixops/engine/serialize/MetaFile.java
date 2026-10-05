package io.stratixops.engine.serialize;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.stratixops.engine.scene.Guid;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The parsed contents of an asset's {@code <name>.<ext>.meta} sidecar
 * (ADR-0006 §7): the canonical GUID, the importer id and version that produced
 * it, and free-form importer settings.
 */
public final class MetaFile {

    private final Guid guid;
    private final String importer;
    private final int importerVersion;
    private final Map<String, Object> settings;

    /**
     * Creates a sidecar.
     *
     * @param guid            canonical asset identity
     * @param importer        importer id, e.g. {@code png}
     * @param importerVersion importer version
     * @param settings        importer settings (may be empty)
     */
    public MetaFile(Guid guid, String importer, int importerVersion, Map<String, Object> settings) {
        this.guid = guid;
        this.importer = importer;
        this.importerVersion = importerVersion;
        this.settings = Collections.unmodifiableMap(new LinkedHashMap<>(settings));
    }

    /**
     * @return the canonical asset GUID
     */
    public Guid guid() {
        return guid;
    }

    /**
     * @return the importer id
     */
    public String importer() {
        return importer;
    }

    /**
     * @return the importer version
     */
    public int importerVersion() {
        return importerVersion;
    }

    /**
     * @return the importer settings, unmodifiable
     */
    public Map<String, Object> settings() {
        return settings;
    }

    /**
     * Serializes this sidecar as a JSON object.
     *
     * @return the JSON object
     */
    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("guid", guid.value());
        root.addProperty("importer", importer);
        root.addProperty("importerVersion", importerVersion);
        JsonObject settingsJson = new JsonObject();
        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            settingsJson.add(entry.getKey(), JsonFiles.GSON.toJsonTree(entry.getValue()));
        }
        root.add("settings", settingsJson);
        return root;
    }

    /**
     * Parses a sidecar document.
     *
     * @param root the JSON object
     * @param file source path, used in error messages
     * @return the parsed sidecar
     */
    public static MetaFile fromJson(JsonObject root, String file) {
        if (!root.has("guid")) {
            throw new SerializationException("meta file " + file + " has no guid");
        }
        Guid guid = Guid.of(root.get("guid").getAsString());
        String importer = root.has("importer") ? root.get("importer").getAsString() : "raw";
        int version = root.has("importerVersion") ? root.get("importerVersion").getAsInt() : 0;
        Map<String, Object> settings = new LinkedHashMap<>();
        if (root.has("settings") && root.get("settings").isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("settings").entrySet()) {
                settings.put(entry.getKey(), JsonFiles.GSON.fromJson(entry.getValue(), Object.class));
            }
        }
        return new MetaFile(guid, importer, version, settings);
    }
}
