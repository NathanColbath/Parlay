package io.stratixops.engine.scene;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SceneTest {

    @Test
    void defaultsAndSetters() {
        Scene scene = new Scene("Level 1");
        assertThat(scene.guid()).isNotNull();
        assertThat(scene.name()).isEqualTo("Level 1");
        assertThat(scene.background()).isEqualTo(Scene.DEFAULT_BACKGROUND);
        assertThat(scene.gameObjects()).isEmpty();

        scene.setName("Level 2");
        scene.setBackground(0x000000FF);
        assertThat(scene.name()).isEqualTo("Level 2");
        assertThat(scene.background()).isEqualTo(0x000000FF);
    }

    @Test
    void explicitIdentity() {
        Guid guid = Guid.random();
        Scene scene = new Scene(guid, "Fixed");
        assertThat(scene.guid()).isEqualTo(guid);
    }

    @Test
    void addRemoveAndFindAcrossHierarchy() {
        Scene scene = new Scene("find");
        GameObject root = scene.add(new GameObject());
        root.setName("Root");
        GameObject child = new GameObject();
        child.setName("Child");
        child.setParent(root);

        assertThat(scene.findObject(child.guid())).isSameAs(child);
        assertThat(scene.findByName("Child")).isSameAs(child);
        assertThat(scene.findByName("Missing")).isNull();
        assertThat(scene.findObject(Guid.random())).isNull();

        assertThat(scene.remove(child)).isFalse();
        assertThat(scene.remove(root)).isTrue();
        assertThat(scene.gameObjects()).isEmpty();
    }

    @Test
    void disposeDestroysAndClears() {
        Scene scene = new Scene("dispose");
        GameObject object = scene.add(new GameObject());
        scene.dispose();
        assertThat(scene.gameObjects()).isEmpty();
        assertThat(object.components()).isNotEmpty();
    }
}
