package io.stratixops.engine.serialize;

import io.stratixops.engine.inspect.ComponentRegistry;
import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Scene;
import io.stratixops.engine.scene.UnsupportedComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class UnknownTypeTest {

    private static final String SCENE = """
            {
              "schema": "stratix.scene",
              "version": 1,
              "guid": "b3f1c2e0-9a44-4f1e-8b2d-0f6a5c7d9e01",
              "content": {
                "name": "Unknown",
                "background": "#1b1b2fff",
                "gameObjects": [
                  {
                    "guid": "11111111-1111-4111-8111-111111111111",
                    "name": "Mystery",
                    "active": true,
                    "layer": 0,
                    "tags": [],
                    "children": [],
                    "components": [
                      { "typeId": "transform", "enabled": true,
                        "properties": { "localPosition": {"x": 1.0, "y": 2.0},
                                        "localScale": {"x": 1.0, "y": 1.0},
                                        "localRotation": 0.0 } },
                      { "typeId": "futureComponent", "enabled": false,
                        "properties": { "power": 9000, "label": "beam" } }
                    ]
                  }
                ]
              }
            }
            """;

    @Test
    void unknownTypeBecomesAPlaceholder(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("unknown.scene");
        Files.writeString(file, SCENE);
        SceneIO io = new SceneIO(ComponentRegistry.withBuiltins());

        Scene scene = io.load(file);
        GameObject object = scene.gameObjects().get(0);
        UnsupportedComponent placeholder = object.getComponent(UnsupportedComponent.class);

        assertThat(placeholder).isNotNull();
        assertThat(placeholder.typeId()).isEqualTo("futureComponent");
        assertThat(placeholder.enabled()).isFalse();
        assertThat(placeholder.rawJson()).contains("futureComponent").contains("9000");
    }

    @Test
    void placeholderRoundTripsLosslessly(@TempDir Path dir) {
        Path first = dir.resolve("first.scene");
        Path second = dir.resolve("second.scene");
        try {
            Files.writeString(first, SCENE);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        SceneIO io = new SceneIO(ComponentRegistry.withBuiltins());
        io.save(io.load(first), second);

        Scene reloaded = io.load(second);
        UnsupportedComponent placeholder = reloaded.gameObjects().get(0)
                .getComponent(UnsupportedComponent.class);
        assertThat(placeholder).isNotNull();
        assertThat(placeholder.typeId()).isEqualTo("futureComponent");
        assertThat(placeholder.rawJson()).contains("9000").contains("beam");
    }

    @Test
    void unknownFieldsOnKnownComponentsAreIgnored(@TempDir Path dir) throws Exception {
        String json = SCENE.replace("\"localRotation\": 0.0",
                "\"localRotation\": 0.0, \"bogus\": { \"nested\": true }");
        Path file = dir.resolve("known.scene");
        Files.writeString(file, json);
        SceneIO io = new SceneIO(ComponentRegistry.withBuiltins());
        Scene scene = io.load(file);
        assertThat(scene.gameObjects()).hasSize(1);
        assertThat(scene.gameObjects().get(0).transform().localPosition().x).isEqualTo(1f);
    }
}
