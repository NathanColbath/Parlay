package io.stratixops.engine.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import io.stratixops.engine.EngineContext;
import io.stratixops.engine.Font;
import io.stratixops.engine.Sprite;
import io.stratixops.engine.TextureAtlas;
import io.stratixops.engine.audio.DefaultAudio;
import io.stratixops.engine.input.DefaultInput;
import io.stratixops.engine.math.Vec2;
import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Scene;
import io.stratixops.engine.scene.SceneRenderer;
import io.stratixops.engine.scene.SpriteRenderer;
import io.stratixops.engine.testutil.DegradingAssets;
import io.stratixops.engine.testutil.FakeAudioDevice;
import io.stratixops.engine.testutil.FakeInputDevice;
import io.stratixops.engine.testutil.FakeRenderer;
import org.junit.jupiter.api.Test;

/**
 * Missing/corrupt runtime content must degrade, never crash (ADR-0001). The
 * GL magenta placeholder lives in {@code engine-render}; this headless test
 * pins the contract the canonical scene model and {@link SceneRenderer} rely
 * on when the asset layer degrades.
 */
class AssetFailureTest {

    private static EngineContext degradingContext() {
        return new DefaultEngineContext(
            new DegradingAssets(),
            new DefaultInput(new FakeInputDevice()),
            new DefaultAudio(new FakeAudioDevice()),
            new Vec2(320f, 180f));
    }

    @Test
    void sceneDrawingDegradedAssetsRunsWithoutCrashing() {
        EngineContext ctx = degradingContext();

        Scene scene = new Scene("asset-failure");
        GameObject object = scene.add(new GameObject());
        object.setName("missing-sprite");
        SpriteRenderer spriteRenderer = object.addComponent(new SpriteRenderer());
        spriteRenderer.setSpriteHandle(ctx.assets().sprite("images/missing.png"));

        scene.update(1f / 60f);
        FakeRenderer renderer = new FakeRenderer();
        new SceneRenderer().render(scene, renderer);
        scene.dispose();

        assertThat(renderer.draws).isEqualTo(1);
        assertThat(scene.gameObjects()).isEmpty();
    }

    @Test
    void sceneDrawsNullSpriteHandleSafely() {
        Scene scene = new Scene("null-sprite");
        GameObject object = scene.add(new GameObject());
        object.addComponent(new SpriteRenderer());

        FakeRenderer renderer = new FakeRenderer();
        new SceneRenderer().render(scene, renderer);
        scene.dispose();

        assertThat(renderer.draws).isZero();
    }

    @Test
    void degradedHandlesAreNonNullAndUsable() {
        EngineContext ctx = degradingContext();

        Sprite sprite = ctx.assets().sprite("images/missing.png");
        TextureAtlas atlas = ctx.assets().atlas("missing-atlas.json");
        Font font = ctx.assets().font("missing.font");

        assertThat(sprite).isNotNull();
        assertThat(sprite.width()).isPositive();
        assertThat(sprite.height()).isPositive();
        assertThat(atlas.region("player")).isNotNull();
        assertThat(atlas.region("anything-else")).isNotNull();
        assertThat(font.lineHeight()).isPositive();
    }
}
