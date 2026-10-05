package io.stratixops.engine.serialize;

import io.stratixops.engine.inspect.ComponentRegistry;
import io.stratixops.engine.scene.Camera;
import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Prefab;
import io.stratixops.engine.scene.SpriteRenderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PrefabIOTest {

    @Test
    void roundTripsRootAndChildren(@TempDir Path dir) {
        GameObject root = new GameObject();
        root.setName("Tower");
        root.addComponent(new Camera());
        GameObject turret = new GameObject();
        turret.setName("Turret");
        turret.setParent(root);
        turret.addComponent(new SpriteRenderer());

        Prefab prefab = new Prefab("Tower", root);
        Path file = dir.resolve("tower.prefab");
        PrefabIO io = new PrefabIO(ComponentRegistry.withBuiltins());
        io.save(prefab, file);

        Prefab loaded = io.load(file);
        assertThat(loaded.guid()).isEqualTo(prefab.guid());
        assertThat(loaded.name()).isEqualTo("Tower");
        assertThat(loaded.root().name()).isEqualTo("Tower");
        assertThat(loaded.root().guid()).isEqualTo(root.guid());
        assertThat(loaded.root().getComponent(Camera.class)).isNotNull();
        assertThat(loaded.root().children()).hasSize(1);
        assertThat(loaded.root().children().get(0).name()).isEqualTo("Turret");
    }

    @Test
    void rejectsWrongSchema(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("bad.prefab");
        Files.writeString(file, "{\"schema\":\"stratix.scene\",\"version\":1,\"content\":{}}");
        PrefabIO io = new PrefabIO(ComponentRegistry.withBuiltins());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> io.load(file))
                .isInstanceOf(SerializationException.class);
    }
}
