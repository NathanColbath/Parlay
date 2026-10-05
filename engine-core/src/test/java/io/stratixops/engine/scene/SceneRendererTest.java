package io.stratixops.engine.scene;

import io.stratixops.engine.Sprite;
import io.stratixops.engine.testutil.RecordingRenderer;
import io.stratixops.engine.testutil.TestSprite;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SceneRendererTest {

    private final SceneRenderer renderer = new SceneRenderer();

    private GameObject spriteObject(Scene scene, float x, float y, int layer, int order, Sprite sprite) {
        GameObject object = scene.add(new GameObject());
        object.setLayer(layer);
        object.transform().localPosition().set(x, y);
        SpriteRenderer spriteRenderer = object.addComponent(new SpriteRenderer());
        spriteRenderer.setSpriteHandle(sprite);
        spriteRenderer.setSortingOrder(order);
        return object;
    }

    @Test
    void clearsWithSceneBackground() {
        Scene scene = new Scene("s");
        scene.setBackground(0x12345678);
        RecordingRenderer target = new RecordingRenderer();
        renderer.render(scene, target);
        assertThat(target.clearColor).isEqualTo(0x12345678);
        assertThat(target.pushes).isZero();
        assertThat(target.pops).isZero();
    }

    @Test
    void pushesMainCameraAndPops() {
        Scene scene = new Scene("s");
        GameObject cameraObject = scene.add(new GameObject());
        cameraObject.transform().localPosition().set(32f, 64f);
        cameraObject.addComponent(new Camera());

        RecordingRenderer target = new RecordingRenderer();
        renderer.render(scene, target);
        assertThat(target.pushes).isEqualTo(1);
        assertThat(target.pops).isEqualTo(1);
        assertThat(target.camera.position().x).isEqualTo(32f);
        assertThat(target.camera.position().y).isEqualTo(64f);
        assertThat(target.events).startsWith("clear", "push");
        assertThat(target.events).endsWith("pop");
    }

    @Test
    void drawsSpritesInLayerThenOrderOrder() {
        Scene scene = new Scene("s");
        Sprite sprite = new TestSprite(1f, 1f);
        spriteObject(scene, 10f, 10f, 0, 5, sprite);
        spriteObject(scene, 20f, 20f, 0, 1, sprite);
        spriteObject(scene, 30f, 30f, -1, 0, sprite);

        RecordingRenderer target = new RecordingRenderer();
        renderer.render(scene, target);

        assertThat(target.positions).hasSize(3);
        assertThat(target.positions.get(0)).containsExactly(30f, 30f);
        assertThat(target.positions.get(1)).containsExactly(20f, 20f);
        assertThat(target.positions.get(2)).containsExactly(10f, 10f);
    }

    @Test
    void appliesSpriteOffset() {
        Scene scene = new Scene("s");
        GameObject object = scene.add(new GameObject());
        SpriteRenderer spriteRenderer = object.addComponent(new SpriteRenderer());
        spriteRenderer.setSpriteHandle(new TestSprite(1f, 1f));
        spriteRenderer.offset().set(3f, 4f);
        object.transform().localPosition().set(1f, 2f);

        RecordingRenderer target = new RecordingRenderer();
        renderer.render(scene, target);
        assertThat(target.positions.get(0)).containsExactly(4f, 6f);
    }

    @Test
    void skipsInactiveAndUnresolvedSprites() {
        Scene scene = new Scene("s");
        GameObject inactive = spriteObject(scene, 0f, 0f, 0, 0, new TestSprite(1f, 1f));
        inactive.setActive(false);
        GameObject child = spriteObject(scene, 0f, 0f, 0, 0, new TestSprite(1f, 1f));
        child.setParent(inactive);

        GameObject noSprite = scene.add(new GameObject());
        noSprite.addComponent(new SpriteRenderer());

        GameObject disabled = spriteObject(scene, 0f, 0f, 0, 0, new TestSprite(1f, 1f));
        disabled.getComponent(SpriteRenderer.class).setEnabled(false);

        RecordingRenderer target = new RecordingRenderer();
        renderer.render(scene, target);
        assertThat(target.sprites).isEmpty();
    }

    @Test
    void picksMainCameraOverFirst() {
        Scene scene = new Scene("s");
        GameObject first = scene.add(new GameObject());
        Camera secondary = first.addComponent(new Camera());
        secondary.setMain(false);
        first.transform().localPosition().set(1f, 1f);

        GameObject second = scene.add(new GameObject());
        second.addComponent(new Camera());
        second.transform().localPosition().set(9f, 9f);

        RecordingRenderer target = new RecordingRenderer();
        renderer.render(scene, target);
        assertThat(target.camera.position().x).isEqualTo(9f);
    }
}
