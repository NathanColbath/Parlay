package io.stratixops.engine.render;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glCheckFramebufferStatus;
import static org.lwjgl.opengl.GL30.glDeleteFramebuffers;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL30.glGenFramebuffers;

import java.nio.ByteBuffer;

/**
 * A color-only offscreen framebuffer with a nearest-sampled RGBA8 texture.
 *
 * <p>Shared by the runtime's logical viewport and the editor's offscreen
 * {@link RenderTarget} so the FBO/texture setup (and its completeness check)
 * lives in exactly one place. GL-only; requires a current 3.3 core context.
 */
final class GlFramebuffer implements AutoCloseable {

    private final int width;
    private final int height;
    private final int framebufferId;
    private final int textureId;
    private boolean closed;

    /**
     * Creates an offscreen color framebuffer.
     *
     * @param width  target width in pixels, positive
     * @param height target height in pixels, positive
     */
    GlFramebuffer(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("render target size must be positive but was " + width + "x" + height);
        }
        this.width = width;
        this.height = height;

        framebufferId = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);

        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE,
            (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0);

        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            glDeleteFramebuffers(framebufferId);
            glDeleteTextures(textureId);
            closed = true;
            throw new IllegalStateException("Offscreen framebuffer incomplete: 0x" + Integer.toHexString(status));
        }
    }

    /** @return the GL framebuffer handle */
    int framebufferId() {
        return framebufferId;
    }

    /** @return the GL color texture handle, suitable for {@code ImGui::Image} */
    int textureId() {
        return textureId;
    }

    /** @return target width in pixels */
    int width() {
        return width;
    }

    /** @return target height in pixels */
    int height() {
        return height;
    }

    /** Binds the framebuffer and sets the GL viewport to its full size. */
    void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);
        glViewport(0, 0, width, height);
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        glDeleteFramebuffers(framebufferId);
        glDeleteTextures(textureId);
    }
}
