package io.stratixops.engine.serialize;

import com.google.gson.JsonObject;

/**
 * An in-memory upgrade for one schema version (ADR-0006 §3). A migration reads
 * an envelope at {@link #fromVersion()}, rewrites its {@code content}, and
 * bumps the envelope {@code version} to {@code fromVersion() + 1}.
 *
 * <p>Older files are migrated in memory and written back only on an explicit
 * save; newer-than-supported files fail fast and are never rewritten.
 */
public interface Migration {

    /**
     * @return the schema version this migration upgrades from
     */
    int fromVersion();

    /**
     * Rewrites the envelope in place.
     *
     * @param envelope the full envelope ({@code schema}/{@code version}/{@code guid}/{@code content})
     */
    void migrate(JsonObject envelope);
}
