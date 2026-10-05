package io.stratixops.engine.render;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryUtil;

/**
 * An immutable GL texture with its pixel dimensions. GL-only; owned by
 * {@code DesktopAssets} and disposed with it.
 */
final class GlTexture implements AutoCloseable {

    private int id;
    private final int width;
    private final int height;
    private final boolean nearest;

    private GlTexture(int id, int width, int height, boolean nearest) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.nearest = nearest;
    }

    /**
     * Creates a texture from tightly packed ARGB pixels (row-major).
     *
     * @param argb   {@code width * height} pixels
     * @param width  texture width
     * @param height texture height
     * @param nearest whether to use nearest (crisp) filtering
     * @return the new texture
     */
    static GlTexture fromPixels(int[] argb, int width, int height, boolean nearest) {
        if (argb.length < width * height) {
            throw new IllegalArgumentException(
                "pixel buffer too small: " + argb.length + " < " + (width * height));
        }
        ByteBuffer buffer = MemoryUtil.memAlloc(width * height * 4);
        try {
            for (int i = 0; i < width * height; i++) {
                int pixel = argb[i];
                buffer.put(i * 4, (byte) (pixel >>> 16));       // R
                buffer.put(i * 4 + 1, (byte) (pixel >>> 8));    // G
                buffer.put(i * 4 + 2, (byte) pixel);            // B
                buffer.put(i * 4 + 3, (byte) (pixel >>> 24));   // A
            }
            return create(buffer, width, height, nearest);
        } finally {
            MemoryUtil.memFree(buffer);
        }
    }

    /**
     * Creates a texture from an RGBA byte buffer (already in R,G,B,A order).
     *
     * @param rgba    {@code width * height * 4} bytes
     * @param width   texture width
     * @param height  texture height
     * @param nearest whether to use nearest filtering
     * @return the new texture
     */
    static GlTexture fromRgba(ByteBuffer rgba, int width, int height, boolean nearest) {
        return create(rgba, width, height, nearest);
    }

    /**
     * Creates the 1x1 opaque white texture used by untextured draws.
     *
     * @return a white texture
     */
    static GlTexture white() {
        return fromPixels(new int[] {0xFFFFFFFF}, 1, 1, true);
    }

    private static GlTexture create(ByteBuffer data, int width, int height, boolean nearest) {
        int id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, nearest ? GL_NEAREST : GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, nearest ? GL_NEAREST : GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
        glBindTexture(GL_TEXTURE_2D, 0);
        return new GlTexture(id, width, height, nearest);
    }

    void bind(int unit) {
        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_2D, id);
    }

    int id() {
        return id;
    }

    int width() {
        return width;
    }

    int height() {
        return height;
    }

    boolean nearest() {
        return nearest;
    }

    @Override
    public void close() {
        if (id != 0) {
            glDeleteTextures(id);
            id = 0;
        }
    }
}
