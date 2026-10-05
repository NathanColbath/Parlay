package io.stratixops.engine.render;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glReadPixels;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

import io.stratixops.engine.render.platform.GlfwWindow;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.lwjgl.system.MemoryStack;

/**
 * GL cases for the offscreen {@link RenderTarget}. Tagged {@code gpu} so
 * headless CI stays green; run with {@code -Pstratix.qa.gpuTests=true} on a
 * machine with a display (and in the CI {@code gpu-tests} job under xvfb).
 */
@Tag("gpu")
class RenderTargetGpuTest {

    @Test
    void rendersClearColorIntoReusableTexture() {
        try (GlfwWindow window = new GlfwWindow("render-target-test", 320, 180, false, false);
             DesktopAssets assets = new DesktopAssets();
             GlRenderer renderer = new GlRenderer(assets, 640, 360);
             RenderTarget target = renderer.createRenderTarget(640, 360)) {

            assertThat(target.width()).isEqualTo(640);
            assertThat(target.height()).isEqualTo(360);
            assertThat(target.textureId()).isNotZero();
            assertThat(target.isActive()).isFalse();

            target.begin();
            assertThat(target.isActive()).isTrue();
            target.clear(0xFF0000FF);
            target.fillRect(0f, 0f, 640f, 360f, 0x00FF00FF);
            target.end();
            assertThat(target.isActive()).isFalse();

            int[] pixel = readTexel(target);
            assertThat(pixel).containsExactly(0, 255, 0, 255);
        }
    }

    @Test
    void drawWithoutBeginIsRejected() {
        try (GlfwWindow window = new GlfwWindow("render-target-guard", 320, 180, false, false);
             DesktopAssets assets = new DesktopAssets();
             GlRenderer renderer = new GlRenderer(assets, 640, 360);
             RenderTarget target = renderer.createRenderTarget(64, 64)) {

            assertThatIllegalStateException().isThrownBy(() -> target.clear(0xFF0000FF));
        }
    }

    @Test
    void nestedBeginIsRejected() {
        try (GlfwWindow window = new GlfwWindow("render-target-nested", 320, 180, false, false);
             DesktopAssets assets = new DesktopAssets();
             GlRenderer renderer = new GlRenderer(assets, 640, 360);
             RenderTarget target = renderer.createRenderTarget(64, 64)) {

            target.begin();
            assertThatIllegalStateException().isThrownBy(target::begin);
            target.end();
        }
    }

    private static int[] readTexel(RenderTarget target) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer buffer = stack.malloc(4);
            glBindFramebuffer(GL_FRAMEBUFFER, target.framebuffer().framebufferId());
            glReadPixels(0, 0, 1, 1, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            return new int[] {
                buffer.get(0) & 0xFF,
                buffer.get(1) & 0xFF,
                buffer.get(2) & 0xFF,
                buffer.get(3) & 0xFF,
            };
        }
    }
}
