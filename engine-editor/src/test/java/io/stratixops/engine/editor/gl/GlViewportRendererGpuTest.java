package io.stratixops.engine.editor.gl;

import static org.assertj.core.api.Assertions.assertThat;

import io.stratixops.engine.Camera2D;
import io.stratixops.engine.render.DesktopAssets;
import io.stratixops.engine.render.GlRenderer;
import io.stratixops.engine.render.RenderTarget;
import io.stratixops.engine.render.platform.GlfwWindow;
import io.stratixops.engine.scene.Camera;
import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Guid;
import io.stratixops.engine.scene.Scene;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * GL cases for the Game-view path (STR-41): a runtime scene is submitted
 * through the shared {@code SceneRenderer} into a {@link RenderTarget} and the
 * resulting texture is read back. Tagged {@code gpu} (opt-in) so headless CI
 * stays green; pixel-level RenderTarget behavior is also covered by
 * engine-render's {@code RenderTargetGpuTest}.
 */
@Tag("gpu")
class GlViewportRendererGpuTest {

    @Test
    void rendersRuntimeSceneClearColorIntoTheTarget() {
        try (GlfwWindow window = new GlfwWindow("game-view-test", 320, 180, false, false);
                DesktopAssets assets = new DesktopAssets();
                GlRenderer renderer = new GlRenderer(assets, 640, 360);
                GlViewportRenderer viewport = new GlViewportRenderer(renderer, 320, 180)) {

            assertThat(viewport.isReady()).isTrue();
            assertThat(viewport.targetWidth()).isEqualTo(320);
            assertThat(viewport.targetHeight()).isEqualTo(180);
            assertThat(viewport.textureId()).isNotZero();

            Scene runtime = new Scene("Runtime");
            runtime.setBackground(0x112233FF);
            GameObject cameraObject = runtime.add(new GameObject(Guid.random(), "Main Camera"));
            cameraObject.addComponent(new Camera());

            viewport.renderRuntime(runtime);

            int[] centre = viewport.target().readPixel(160, 90);
            assertThat(centre).containsExactly(0x11, 0x22, 0x33, 0xFF);
        }
    }

    @Test
    void nullSceneIsIgnored() {
        try (GlfwWindow window = new GlfwWindow("game-view-null", 320, 180, false, false);
                DesktopAssets assets = new DesktopAssets();
                GlRenderer renderer = new GlRenderer(assets, 640, 360);
                GlViewportRenderer viewport = new GlViewportRenderer(renderer, 64, 64)) {

            viewport.renderRuntime(null);
            assertThat(viewport.isReady()).isTrue();
        }
    }

    @Test
    void presentsThroughTheSharedCamera2DContract() {
        try (GlfwWindow window = new GlfwWindow("game-view-camera", 320, 180, false, false);
                DesktopAssets assets = new DesktopAssets();
                GlRenderer renderer = new GlRenderer(assets, 640, 360);
                GlViewportRenderer viewport = new GlViewportRenderer(renderer, 32, 32)) {

            Scene runtime = new Scene("Runtime");
            runtime.setBackground(0x0A0B0CFF);
            GameObject cameraObject = runtime.add(new GameObject(Guid.random(), "Main Camera"));
            Camera camera = cameraObject.addComponent(new Camera());
            cameraObject.transform().localPosition().set(1000f, 1000f);
            Camera2D camera2D = camera.camera2D();
            assertThat(camera2D).isNotNull();

            viewport.renderRuntime(runtime);
            assertThat(viewport.textureId()).isNotZero();
        }
    }
}
