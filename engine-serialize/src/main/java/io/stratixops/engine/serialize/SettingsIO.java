package io.stratixops.engine.serialize;

import com.google.gson.JsonObject;

import java.nio.file.Path;

/**
 * Typed loaders for {@code ProjectSettings/*.json} (ADR-0006 §10). Each file is
 * its own {@code stratix.settings.*} envelope. Opening a file with an
 * unsupported version fails fast rather than silently rewriting user data.
 */
public final class SettingsIO {

    private final Migrations migrations;

    /** Creates a settings reader with no migrations. */
    public SettingsIO() {
        this(new Migrations());
    }

    /**
     * Creates a settings reader.
     *
     * @param migrations the migration registry
     */
    public SettingsIO(Migrations migrations) {
        this.migrations = migrations;
    }

    /**
     * Typed window settings.
     *
     * @param title  window title
     * @param width  physical width
     * @param height physical height
     * @param vsync  vsync enabled
     */
    public record WindowSettings(String title, int width, int height, boolean vsync) {
    }

    /**
     * Loads window settings.
     *
     * @param path source file
     * @return the settings, or defaults when the file does not exist
     */
    public WindowSettings loadWindow(Path path) {
        if (!java.nio.file.Files.exists(path)) {
            return new WindowSettings("Stratix Game", 1280, 720, true);
        }
        JsonObject content = loadContent(path, Schemas.SETTINGS_WINDOW);
        return new WindowSettings(
                content.has("title") ? content.get("title").getAsString() : "Stratix Game",
                content.has("width") ? content.get("width").getAsInt() : 1280,
                content.has("height") ? content.get("height").getAsInt() : 720,
                !content.has("vsync") || content.get("vsync").getAsBoolean());
    }

    /**
     * Saves window settings atomically.
     *
     * @param settings the settings
     * @param path     destination file
     */
    public void saveWindow(WindowSettings settings, Path path) {
        JsonObject content = new JsonObject();
        content.addProperty("title", settings.title());
        content.addProperty("width", settings.width());
        content.addProperty("height", settings.height());
        content.addProperty("vsync", settings.vsync());
        saveContent(content, Schemas.SETTINGS_WINDOW, path);
    }

    /**
     * Loads the envelope content of a settings file, validating and migrating
     * the version.
     *
     * @param path   source file
     * @param schema expected schema id
     * @return the content object
     */
    public JsonObject loadContent(Path path, String schema) {
        JsonObject envelope = JsonFiles.parseObject(path);
        Envelopes.validate(envelope, schema, path);
        migrations.migrateToCurrent(schema, envelope, path.toString());
        return envelope.getAsJsonObject("content");
    }

    /**
     * Saves a settings envelope atomically.
     *
     * @param content the content object
     * @param schema  schema id
     * @param path    destination file
     */
    public void saveContent(JsonObject content, String schema, Path path) {
        JsonObject envelope = new JsonObject();
        envelope.addProperty("schema", schema);
        envelope.addProperty("version", Schemas.CURRENT_VERSION);
        envelope.add("content", content);
        JsonFiles.writeAtomic(path, envelope);
    }
}
