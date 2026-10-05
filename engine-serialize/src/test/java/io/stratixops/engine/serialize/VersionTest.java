package io.stratixops.engine.serialize;

import com.google.gson.JsonObject;
import io.stratixops.engine.inspect.ComponentRegistry;
import io.stratixops.engine.scene.Scene;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VersionTest {

    @Test
    void newerVersionFailsFast(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("future.scene");
        String json = """
                { "schema": "stratix.scene", "version": 99, "guid":
                  "b3f1c2e0-9a44-4f1e-8b2d-0f6a5c7d9e01", "content": {} }
                """;
        Files.writeString(file, json);
        SceneIO io = new SceneIO(ComponentRegistry.withBuiltins());
        assertThatThrownBy(() -> io.load(file))
                .isInstanceOf(SerializationException.class)
                .hasMessageContaining("future.scene")
                .hasMessageContaining("99");
    }

    @Test
    void wrongSchemaFailsFast(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("wrong.json");
        Files.writeString(file, "{\"schema\":\"stratix.prefab\",\"version\":1,\"content\":{}}");
        SceneIO io = new SceneIO(ComponentRegistry.withBuiltins());
        assertThatThrownBy(() -> io.load(file))
                .isInstanceOf(SerializationException.class)
                .hasMessageContaining("stratix.scene");
    }

    @Test
    void olderVersionIsMigratedInMemory(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("old.scene");
        String json = """
                { "schema": "stratix.scene", "version": 0,
                  "guid": "b3f1c2e0-9a44-4f1e-8b2d-0f6a5c7d9e01",
                  "content": { "name": "Old", "gameObjects": [] } }
                """;
        Files.writeString(file, json);

        Migrations migrations = new Migrations();
        migrations.register(Schemas.SCENE, new Migration() {
            @Override
            public int fromVersion() {
                return 0;
            }

            @Override
            public void migrate(JsonObject envelope) {
                envelope.addProperty("version", 1);
                envelope.getAsJsonObject("content").addProperty("name", "Migrated");
            }
        });
        SceneIO io = new SceneIO(ComponentRegistry.withBuiltins(), migrations);
        Scene scene = io.load(file);
        assertThat(scene.name()).isEqualTo("Migrated");
    }

    @Test
    void missingMigrationPathFailsFast(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("old.scene");
        Files.writeString(file, """
                { "schema": "stratix.scene", "version": 0,
                  "guid": "b3f1c2e0-9a44-4f1e-8b2d-0f6a5c7d9e01",
                  "content": { "name": "Old", "gameObjects": [] } }
                """);
        SceneIO io = new SceneIO(ComponentRegistry.withBuiltins());
        assertThatThrownBy(() -> io.load(file)).isInstanceOf(SerializationException.class);
    }

    @Test
    void migrationThatDoesNotAdvanceFailsFast(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("stuck.scene");
        Files.writeString(file, """
                { "schema": "stratix.scene", "version": 0,
                  "guid": "b3f1c2e0-9a44-4f1e-8b2d-0f6a5c7d9e01",
                  "content": { "name": "Old", "gameObjects": [] } }
                """);
        Migrations migrations = new Migrations();
        migrations.register(Schemas.SCENE, new Migration() {
            @Override
            public int fromVersion() {
                return 0;
            }

            @Override
            public void migrate(JsonObject envelope) {
            }
        });
        SceneIO io = new SceneIO(ComponentRegistry.withBuiltins(), migrations);
        assertThatThrownBy(() -> io.load(file))
                .isInstanceOf(SerializationException.class)
                .hasMessageContaining("did not advance");
    }

    @Test
    void duplicateMigrationIsRejected() {
        Migrations migrations = new Migrations();
        Migration migration = new Migration() {
            @Override
            public int fromVersion() {
                return 0;
            }

            @Override
            public void migrate(JsonObject envelope) {
                envelope.addProperty("version", 1);
            }
        };
        migrations.register(Schemas.SCENE, migration);
        assertThatThrownBy(() -> migrations.register(Schemas.SCENE, migration))
                .isInstanceOf(IllegalStateException.class);
    }
}
