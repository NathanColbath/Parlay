package io.stratixops.engine.serialize;

import com.google.gson.JsonObject;
import io.stratixops.engine.inspect.ComponentRegistry;
import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Guid;
import io.stratixops.engine.scene.Scene;

import java.nio.file.Path;
import java.util.List;

/**
 * Reads and writes {@code .scene} files as versioned JSON (ADR-0006).
 *
 * <p>Round-trip is contractual: a scene written by {@link #save(Scene, Path)}
 * and read back by {@link #load(Path)} has identical state. Unknown component
 * types degrade to an {@code UnsupportedComponent} that round-trips losslessly.
 * A newer-than-supported version fails fast; an older version is migrated in
 * memory.
 */
public final class SceneIO {

    private final ComponentRegistry registry;
    private final Migrations migrations;

    /**
     * Creates a serializer with no migrations.
     *
     * @param registry the component registry used to resolve type ids
     */
    public SceneIO(ComponentRegistry registry) {
        this(registry, new Migrations());
    }

    /**
     * Creates a serializer.
     *
     * @param registry   the component registry
     * @param migrations the migration registry
     */
    public SceneIO(ComponentRegistry registry, Migrations migrations) {
        this.registry = registry;
        this.migrations = migrations;
    }

    /**
     * Saves a scene atomically.
     *
     * @param scene the scene
     * @param path  destination file
     */
    public void save(Scene scene, Path path) {
        JsonObject envelope = new JsonObject();
        envelope.addProperty("schema", Schemas.SCENE);
        envelope.addProperty("version", Schemas.CURRENT_VERSION);
        envelope.addProperty("guid", scene.guid().value());

        JsonObject content = new JsonObject();
        content.addProperty("name", scene.name());
        content.addProperty("background", ValueCodec.colorString(scene.background()));
        content.add("gameObjects", GameObjectJson.writeAll(scene.gameObjects(), registry));
        envelope.add("content", content);

        JsonFiles.writeAtomic(path, envelope);
    }

    /**
     * Loads a scene, migrating older versions and rejecting newer ones.
     *
     * @param path source file
     * @return the loaded scene
     */
    public Scene load(Path path) {
        JsonObject envelope = JsonFiles.parseObject(path);
        Envelopes.validate(envelope, Schemas.SCENE, path);
        migrations.migrateToCurrent(Schemas.SCENE, envelope, path.toString());

        JsonObject content = envelope.getAsJsonObject("content");
        Scene scene = new Scene(Guid.of(envelope.get("guid").getAsString()),
                content.has("name") ? content.get("name").getAsString() : "Scene");
        if (content.has("background")) {
            scene.setBackground(ValueCodec.parseColor(content.get("background").getAsString()));
        }
        List<GameObject> roots = GameObjectJson.readAll(
                content.has("gameObjects") ? content.getAsJsonArray("gameObjects") : new com.google.gson.JsonArray(),
                registry);
        for (GameObject root : roots) {
            scene.add(root);
        }
        return scene;
    }
}
