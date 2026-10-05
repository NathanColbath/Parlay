package io.stratixops.engine.serialize;

import io.stratixops.engine.inspect.ComponentRegistry;
import io.stratixops.engine.math.Vec2;
import io.stratixops.engine.scene.AssetRef;
import io.stratixops.engine.scene.Camera;
import io.stratixops.engine.scene.Collider2D;
import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Guid;
import io.stratixops.engine.scene.ObjectRef;
import io.stratixops.engine.scene.PrefabRef;
import io.stratixops.engine.scene.Scene;
import io.stratixops.engine.scene.SpriteRenderer;
import io.stratixops.engine.scene.Transform;
import io.stratixops.engine.serialize.fixture.SerialScriptBehaviour;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SceneIORoundTripTest {

    private ComponentRegistry registry() {
        ComponentRegistry registry = ComponentRegistry.withBuiltins();
        registry.register(SerialScriptBehaviour.class);
        return registry;
    }

    private Scene buildScene() {
        Scene scene = new Scene("First Light");
        scene.setBackground(0x102030FF);

        GameObject player = scene.add(new GameObject(Guid.random(), "Player"));
        player.setLayer(2);
        player.tags().add("Player");
        player.transform().localPosition().set(40f, 80f);
        player.transform().localScale().set(2f, 2f);
        player.transform().setLocalRotation(0.25f);

        Guid spriteGuid = Guid.random();
        SpriteRenderer spriteRenderer = player.addComponent(new SpriteRenderer());
        spriteRenderer.setSprite(AssetRef.of(spriteGuid));
        spriteRenderer.offset().set(1f, -1f);
        spriteRenderer.setTint(0x00FF00FF);
        spriteRenderer.setSortingOrder(3);
        spriteRenderer.setFlipX(true);

        SerialScriptBehaviour behaviour = player.addComponent(new SerialScriptBehaviour());
        behaviour.setLabel("hero");
        behaviour.speed = 3.5f;
        behaviour.offset = new Vec2(7f, 8f);
        behaviour.mode = SerialScriptBehaviour.Mode.IDLE;
        behaviour.setSprite(AssetRef.of(Guid.random()));
        behaviour.setTarget(ObjectRef.of(player.guid()));
        behaviour.setPrefab(PrefabRef.of(Guid.random()));
        behaviour.path().add(AssetRef.of(Guid.random()));
        behaviour.path().add(AssetRef.of(Guid.random()));
        behaviour.runtimeOnly = "changed";

        GameObject cameraObject = scene.add(new GameObject());
        cameraObject.setName("Main Camera");
        Camera camera = cameraObject.addComponent(new Camera());
        camera.setBackgroundColor(0x000000FF);
        camera.setMain(true);
        camera.viewport().set(320f, 180f);

        GameObject child = new GameObject();
        child.setName("Gun");
        child.setParent(player);
        child.transform().localPosition().set(5f, 0f);
        Collider2D collider = child.addComponent(new Collider2D());
        collider.size().set(4f, 4f);
        collider.setTrigger(true);

        return scene;
    }

    @Test
    void roundTripsWithIdenticalState(@TempDir Path dir) {
        Path file = dir.resolve("level.scene");
        Scene original = buildScene();
        SceneIO io = new SceneIO(registry());
        io.save(original, file);

        Scene loaded = io.load(file);

        assertThat(loaded.guid()).isEqualTo(original.guid());
        assertThat(loaded.name()).isEqualTo("First Light");
        assertThat(loaded.background()).isEqualTo(0x102030FF);
        assertThat(loaded.gameObjects()).hasSize(2);

        GameObject player = loaded.findByName("Player");
        assertThat(player).isNotNull();
        assertThat(player.layer()).isEqualTo(2);
        assertThat(player.tags()).containsExactly("Player");
        assertThat(player.transform().localPosition().x).isEqualTo(40f);
        assertThat(player.transform().localPosition().y).isEqualTo(80f);
        assertThat(player.transform().localScale().x).isEqualTo(2f);
        assertThat(player.transform().localRotation()).isEqualTo(0.25f);

        SpriteRenderer spriteRenderer = player.getComponent(SpriteRenderer.class);
        assertThat(spriteRenderer.sprite().guid()).isNotNull();
        assertThat(spriteRenderer.offset().x).isEqualTo(1f);
        assertThat(spriteRenderer.tint()).isEqualTo(0x00FF00FF);
        assertThat(spriteRenderer.sortingOrder()).isEqualTo(3);
        assertThat(spriteRenderer.flipX()).isTrue();

        SerialScriptBehaviour behaviour = player.getComponent(SerialScriptBehaviour.class);
        assertThat(behaviour).isNotNull();
        assertThat(behaviour.label()).isEqualTo("hero");
        assertThat(behaviour.speed).isEqualTo(3.5f);
        assertThat(behaviour.offset.x).isEqualTo(7f);
        assertThat(behaviour.mode).isEqualTo(SerialScriptBehaviour.Mode.IDLE);
        assertThat(behaviour.sprite().guid()).isNotNull();
        assertThat(behaviour.target().guid()).isEqualTo(player.guid());
        assertThat(behaviour.prefab().isSet()).isTrue();
        assertThat(behaviour.path()).hasSize(2);
        assertThat(behaviour.runtimeOnly).isEqualTo("temp");

        GameObject child = loaded.findByName("Gun");
        assertThat(child.parent()).isSameAs(player);
        assertThat(player.children()).containsExactly(child);
        Collider2D collider = child.getComponent(Collider2D.class);
        assertThat(collider.size().x).isEqualTo(4f);
        assertThat(collider.trigger()).isTrue();
    }

    @Test
    void writesVersionedEnvelopeWithRefs(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("level.scene");
        SceneIO io = new SceneIO(registry());
        io.save(buildScene(), file);

        String json = Files.readString(file);
        assertThat(json).contains("\"schema\": \"stratix.scene\"");
        assertThat(json).contains("\"version\": 1");
        assertThat(json).contains("\"content\"");
        assertThat(json).contains("\"gameObjects\"");
        assertThat(json).contains("\"typeId\"");
        assertThat(json).contains("$asset");
        assertThat(json).contains("$ref");
        assertThat(json).doesNotContain("\"runtimeOnly\"");
        assertThat(json).doesNotContain("\"hidden\"");
        assertThat(json).contains("#102030ff");
    }

    @Test
    void saveIsDeterministicAndKeepsBackup(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("level.scene");
        SceneIO io = new SceneIO(registry());
        Scene scene = buildScene();
        io.save(scene, file);
        String first = Files.readString(file);
        io.save(scene, file);
        String second = Files.readString(file);
        assertThat(second).isEqualTo(first);
        assertThat(Files.exists(dir.resolve("level.scene.bak"))).isTrue();
    }

    @Test
    void assetsAndRefsRoundTrip(@TempDir Path dir) {
        Path file = dir.resolve("refs.scene");
        Scene scene = new Scene("refs");
        GameObject object = scene.add(new GameObject());
        SerialScriptBehaviour behaviour = object.addComponent(new SerialScriptBehaviour());
        Guid assetGuid = Guid.random();
        Guid objectGuid = Guid.random();
        Guid prefabGuid = Guid.random();
        behaviour.setSprite(AssetRef.of(assetGuid));
        behaviour.setTarget(ObjectRef.of(objectGuid));
        behaviour.setPrefab(PrefabRef.of(prefabGuid));

        SceneIO io = new SceneIO(registry());
        io.save(scene, file);
        SerialScriptBehaviour loaded = io.load(file)
                .findObject(object.guid()).getComponent(SerialScriptBehaviour.class);

        assertThat(loaded.sprite().guid()).isEqualTo(assetGuid);
        assertThat(loaded.target().guid()).isEqualTo(objectGuid);
        assertThat(loaded.prefab().guid()).isEqualTo(prefabGuid);
    }

    @Test
    void resettingReferencesToNullRoundTrips(@TempDir Path dir) {
        Path file = dir.resolve("nulls.scene");
        Scene scene = new Scene("nulls");
        GameObject object = scene.add(new GameObject());
        object.addComponent(new SerialScriptBehaviour());
        SceneIO io = new SceneIO(registry());
        io.save(scene, file);

        SerialScriptBehaviour loaded = io.load(file)
                .findObject(object.guid()).getComponent(SerialScriptBehaviour.class);
        assertThat(loaded.sprite()).isNotNull();
        assertThat(loaded.sprite().isSet()).isFalse();
        assertThat(loaded.target()).isNotNull();
        assertThat(loaded.target().isSet()).isFalse();
        assertThat(loaded.prefab()).isNotNull();
        assertThat(loaded.prefab().isSet()).isFalse();
    }

    @Test
    void transformIsNotDuplicatedOnLoad(@TempDir Path dir) {
        Path file = dir.resolve("transform.scene");
        Scene scene = new Scene("t");
        GameObject object = scene.add(new GameObject());
        object.transform().localPosition().set(3f, 4f);
        SceneIO io = new SceneIO(registry());
        io.save(scene, file);
        Scene loaded = io.load(file);
        GameObject loadedObject = loaded.gameObjects().get(0);
        long transforms = loadedObject.components().stream()
                .filter(Transform.class::isInstance).count();
        assertThat(transforms).isEqualTo(1);
        assertThat(loadedObject.transform().localPosition().x).isEqualTo(3f);
    }
}
