package io.stratixops.engine.serialize;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.stratixops.engine.inspect.ComponentRegistry;
import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Guid;
import io.stratixops.engine.scene.Prefab;

import java.nio.file.Path;
import java.util.List;

/**
 * Reads and writes {@code .prefab} files using the same flat GameObject graph
 * and versioned envelope as scenes (ADR-0006).
 */
public final class PrefabIO {

    private final ComponentRegistry registry;
    private final Migrations migrations;

    /**
     * Creates a serializer with no migrations.
     *
     * @param registry the component registry
     */
    public PrefabIO(ComponentRegistry registry) {
        this(registry, new Migrations());
    }

    /**
     * Creates a serializer.
     *
     * @param registry   the component registry
     * @param migrations the migration registry
     */
    public PrefabIO(ComponentRegistry registry, Migrations migrations) {
        this.registry = registry;
        this.migrations = migrations;
    }

    /**
     * Saves a prefab atomically.
     *
     * @param prefab the prefab
     * @param path   destination file
     */
    public void save(Prefab prefab, Path path) {
        JsonObject envelope = new JsonObject();
        envelope.addProperty("schema", Schemas.PREFAB);
        envelope.addProperty("version", Schemas.CURRENT_VERSION);
        envelope.addProperty("guid", prefab.guid().value());

        JsonObject content = new JsonObject();
        content.addProperty("name", prefab.name());
        content.addProperty("root", prefab.root().guid().value());
        content.add("gameObjects", GameObjectJson.writeAll(List.of(prefab.root()), registry));
        envelope.add("content", content);

        JsonFiles.writeAtomic(path, envelope);
    }

    /**
     * Loads a prefab.
     *
     * @param path source file
     * @return the loaded prefab
     */
    public Prefab load(Path path) {
        JsonObject envelope = JsonFiles.parseObject(path);
        Envelopes.validate(envelope, Schemas.PREFAB, path);
        migrations.migrateToCurrent(Schemas.PREFAB, envelope, path.toString());

        JsonObject content = envelope.getAsJsonObject("content");
        JsonArray objects = content.has("gameObjects")
                ? content.getAsJsonArray("gameObjects") : new JsonArray();
        List<GameObject> roots = GameObjectJson.readAll(objects, registry);

        Guid rootGuid = content.has("root") ? Guid.of(content.get("root").getAsString()) : null;
        GameObject root = null;
        for (GameObject candidate : roots) {
            if (rootGuid == null || candidate.guid().equals(rootGuid)) {
                root = candidate;
                break;
            }
        }
        if (root == null) {
            throw new SerializationException("prefab " + path + " has no resolvable root object");
        }
        String name = content.has("name") ? content.get("name").getAsString() : root.name();
        return new Prefab(Guid.of(envelope.get("guid").getAsString()), name, root);
    }
}
