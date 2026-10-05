package io.stratixops.engine.serialize;

import com.google.gson.JsonObject;

import java.nio.file.Path;

/**
 * Shared validation for the versioned JSON envelope
 * ({@code schema}/{@code version}/{@code guid}/{@code content}).
 */
final class Envelopes {

    private Envelopes() {
    }

    static void validate(JsonObject envelope, String expectedSchema, Path path) {
        if (!envelope.has("schema") || !envelope.has("version")) {
            throw new SerializationException("missing schema/version in " + path);
        }
        String schema = envelope.get("schema").getAsString();
        if (!expectedSchema.equals(schema)) {
            throw new SerializationException(
                    "expected schema " + expectedSchema + " but found " + schema + " in " + path);
        }
        int version = envelope.get("version").getAsInt();
        if (version > Schemas.CURRENT_VERSION) {
            throw new SerializationException("unsupported version " + version + " for " + schema
                    + " in " + path + " (this build supports up to " + Schemas.CURRENT_VERSION + ")");
        }
    }
}
