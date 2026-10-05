package io.stratixops.engine.render;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL30.GL_DRAW_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_READ_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glBlitFramebuffer;
import static org.lwjgl.opengl.GL11.glViewport;

/**
 * The fixed logical render target (default 640x360). The scene is drawn at this
 * resolution, then presented to the physical window with integer scaling and
 * centered letterbox bars (AC1.2). Nearest filtering keeps pixel art crisp.
 *
 * <p>Backed by the shared {@link GlFramebuffer}; this class adds only the
 * blit-to-window presentation step.
 */
final class LogicalViewport implements AutoCloseable {

    private final GlFramebuffer framebuffer;

    LogicalViewport(int width, int height) {
        framebuffer = new GlFramebuffer(width, height);
    }

    int width() {
        return framebuffer.width();
    }

    int height() {
        return framebuffer.height();
    }

    /**
     * Binds the logical framebuffer and sets the viewport to its full size.
     */
    void bind() {
        framebuffer.bind();
    }

    /**
     * Presents the logical framebuffer into the default framebuffer at integer
     * scale, letterboxing the remainder with black.
     *
     * @param windowWidth   physical window width
     * @param windowHeight  physical window height
     */
    void present(int windowWidth, int windowHeight) {
        int width = framebuffer.width();
        int height = framebuffer.height();
        LogicalViewportLayout.Rect dest =
            LogicalViewportLayout.compute(windowWidth, windowHeight, width, height);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, windowWidth, windowHeight);
        glClearColor(0f, 0f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT);

        int destX0 = dest.x();
        int destY0 = windowHeight - (dest.y() + dest.height());
        int destX1 = destX0 + dest.width();
        int destY1 = destY0 + dest.height();

        glBindFramebuffer(GL_READ_FRAMEBUFFER, framebuffer.framebufferId());
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        glBlitFramebuffer(0, 0, width, height,
            destX0, destY0, destX1, destY1,
            GL_COLOR_BUFFER_BIT, GL_NEAREST);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    public void close() {
        framebuffer.close();
    }
}
