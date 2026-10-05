package io.stratixops.engine.serialize;

import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Registry of {@link Migration}s, keyed by schema and source version. Applies
 * the chain from a file's version up to {@link Schemas#CURRENT_VERSION}
 * (ADR-0006 §3).
 */
public final class Migrations {

    private final Map<String, NavigableMap<Integer, Migration>> bySchema = new LinkedHashMap<>();

    /** Creates an empty registry. */
    public Migrations() {
    }

    /**
     * Registers a migration.
     *
     * @param schema    the schema it applies to
     * @param migration the migration
     * @throws IllegalStateException on a duplicate {@code schema}/{@code fromVersion}
     */
    public void register(String schema, Migration migration) {
        NavigableMap<Integer, Migration> versions =
                bySchema.computeIfAbsent(schema, key -> new TreeMap<>());
        if (versions.containsKey(migration.fromVersion())) {
            throw new IllegalStateException(
                    "duplicate migration for " + schema + " from version " + migration.fromVersion());
        }
        versions.put(migration.fromVersion(), migration);
    }

    /**
     * Applies migrations to bring an envelope up to the current version.
     *
     * @param schema   the schema id
     * @param envelope the envelope to rewrite in place
     * @param file     source path for error messages
     * @return the resulting (current) version
     * @throws SerializationException when no migration path exists
     */
    public int migrateToCurrent(String schema, JsonObject envelope, String file) {
        int version = envelope.get("version").getAsInt();
        while (version < Schemas.CURRENT_VERSION) {
            Migration migration = bySchema.getOrDefault(schema, new TreeMap<>()).get(version);
            if (migration == null) {
                throw new SerializationException(
                        "no migration for " + schema + " from version " + version + " (" + file + ")");
            }
            migration.migrate(envelope);
            int next = envelope.get("version").getAsInt();
            if (next <= version) {
                throw new SerializationException(
                        "migration from version " + version + " did not advance (" + file + ")");
            }
            version = next;
        }
        return version;
    }
}
