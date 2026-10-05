package io.stratixops.engine.scene;

import io.stratixops.engine.testutil.TestSprite;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BuiltinComponentsTest {

    @Test
    void spriteRendererDefaultsAndSetters() {
        SpriteRenderer renderer = new SpriteRenderer();
        assertThat(renderer.sprite()).isNull();
        assertThat(renderer.spriteHandle()).isNull();
        assertThat(renderer.offset().x).isZero();
        assertThat(renderer.tint()).isEqualTo(0xFFFFFFFF);
        assertThat(renderer.sortingOrder()).isZero();
        assertThat(renderer.flipX()).isFalse();
        assertThat(renderer.flipY()).isFalse();

        Guid spriteGuid = Guid.random();
        renderer.setSprite(AssetRef.of(spriteGuid));
        renderer.offset().set(1f, 2f);
        renderer.setTint(0x00FF00FF);
        renderer.setSortingOrder(4);
        renderer.setFlipX(true);
        renderer.setFlipY(true);
        renderer.setSpriteHandle(new TestSprite(8f, 8f));

        assertThat(renderer.sprite().guid()).isEqualTo(spriteGuid);
        assertThat(renderer.offset().x).isEqualTo(1f);
        assertThat(renderer.tint()).isEqualTo(0x00FF00FF);
        assertThat(renderer.sortingOrder()).isEqualTo(4);
        assertThat(renderer.flipX()).isTrue();
        assertThat(renderer.flipY()).isTrue();
        assertThat(renderer.spriteHandle().width()).isEqualTo(8f);

        renderer.setSprite(null);
        assertThat(renderer.sprite()).isNull();
    }

    @Test
    void cameraDefaultsAndLazyCamera2D() {
        Camera camera = new Camera();
        assertThat(camera.backgroundColor()).isEqualTo(0x1B1B2FFF);
        assertThat(camera.main()).isTrue();
        assertThat(camera.viewport().x).isEqualTo(640f);
        assertThat(camera.viewport().y).isEqualTo(360f);
        assertThat(camera.camera2D()).isSameAs(camera.camera2D());

        camera.setBackgroundColor(0x000000FF);
        camera.setMain(false);
        camera.viewport().set(320f, 180f);
        assertThat(camera.backgroundColor()).isEqualTo(0x000000FF);
        assertThat(camera.main()).isFalse();
    }

    @Test
    void audioSourceDefaultsAndClamping() {
        AudioSource source = new AudioSource();
        assertThat(source.clip()).isNull();
        assertThat(source.volume()).isEqualTo(1f);
        assertThat(source.loop()).isFalse();
        assertThat(source.playOnAwake()).isFalse();

        source.setClip(AssetRef.of(Guid.random()));
        source.setVolume(2f);
        assertThat(source.volume()).isEqualTo(1f);
        source.setVolume(-1f);
        assertThat(source.volume()).isZero();
        source.setLoop(true);
        source.setPlayOnAwake(true);
        assertThat(source.loop()).isTrue();
        assertThat(source.playOnAwake()).isTrue();
    }

    @Test
    void prefabHoldsIdentityAndRoot() {
        GameObject root = new GameObject();
        Prefab prefab = new Prefab("Enemy", root);
        assertThat(prefab.guid()).isNotNull();
        assertThat(prefab.name()).isEqualTo("Enemy");
        assertThat(prefab.root()).isSameAs(root);

        prefab.setName("Boss");
        GameObject replacement = new GameObject();
        prefab.setRoot(replacement);
        assertThat(prefab.name()).isEqualTo("Boss");
        assertThat(prefab.root()).isSameAs(replacement);
    }

    @Test
    void unsupportedComponentPreservesRawJson() {
        UnsupportedComponent component = new UnsupportedComponent("mystery", "{\"a\":1}");
        assertThat(component.typeId()).isEqualTo("mystery");
        assertThat(component.rawJson()).isEqualTo("{\"a\":1}");
    }
}
