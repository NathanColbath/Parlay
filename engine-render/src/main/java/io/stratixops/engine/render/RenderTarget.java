package io.stratixops.engine.render;

import io.stratixops.engine.Camera2D;
import io.stratixops.engine.Renderer;
import io.stratixops.engine.Sprite;

/**
 * An offscreen color surface the editor (and any non-window consumer) renders
 * the scene into, then presents as an image without opening a second window or
 * GL context (ADR-0005 §4, ADR-0008 §3).
 *
 * <p>A target is created by {@link GlRenderer#createRenderTarget(int, int)} and
 * is itself a {@link Renderer}: draw into it between {@link #begin()} and
 * {@link #end()}, then hand {@link #textureId()} to {@code ImGui::Image}. All
 * drawing flows through the owning renderer's single shader, batch and camera
 * stack (ADR-0008 "one submission path"), so the runtime Game view and the
 * editor Scene view cannot diverge. The color texture uses nearest sampling and
 * the target's own logical size drives the projection, so the image is never
 * distorted.
 *
 * <p>Targets are single-threaded and must be used on the GL thread. GL-only;
 * requires a current 3.3 core context.
 */
public final class RenderTarget implements Renderer, AutoCloseable {

    private final GlRenderer renderer;
    private final GlFramebuffer framebuffer;
    private boolean active;
    private boolean closed;

    RenderTarget(GlRenderer renderer, int width, int height) {
        this.renderer = renderer;
        this.framebuffer = new GlFramebuffer(width, height);
    }

    /**
     * Binds this target. Every draw on this instance (or the owning
     * {@link GlRenderer}) between this call and {@link #end()} lands in the
     * offscreen texture.
     *
     * @throws IllegalStateException if the target is closed or already active
     */
    public void begin() {
        if (closed) {
            throw new IllegalStateException("RenderTarget is closed");
        }
        if (active) {
            throw new IllegalStateException("RenderTarget is already active");
        }
        active = true;
        renderer.beginTarget(this);
    }

    /**
     * Flushes and unbinds this target, restoring the default framebuffer. Draw
     * calls on this instance are rejected until the next {@link #begin()}.
     */
    public void end() {
        if (closed) {
            throw new IllegalStateException("RenderTarget is closed");
        }
        if (!active) {
            return;
        }
        renderer.endTarget();
        active = false;
    }

    /** @return true between {@link #begin()} and {@link #end()} */
    public boolean isActive() {
        return active;
    }

    /** @return the GL color texture handle, presentable to {@code ImGui::Image} */
    public int textureId() {
        return framebuffer.textureId();
    }

    /** @return target width in logical pixels */
    public int width() {
        return framebuffer.width();
    }

    /** @return target height in logical pixels */
    public int height() {
        return framebuffer.height();
    }

    GlFramebuffer framebuffer() {
        return framebuffer;
    }

    @Override
    public void clear(int rgba) {
        requireActive();
        renderer.clear(rgba);
    }

    @Override
    public void draw(Sprite sprite, float x, float y) {
        requireActive();
        renderer.draw(sprite, x, y);
    }

    @Override
    public void fillRect(float x, float y, float w, float h, int rgba) {
        requireActive();
        renderer.fillRect(x, y, w, h, rgba);
    }

    @Override
    public void drawText(String text, float x, float y) {
        requireActive();
        renderer.drawText(text, x, y);
    }

    @Override
    public void pushCamera(Camera2D camera) {
        requireActive();
        renderer.pushCamera(camera);
    }

    @Override
    public void popCamera() {
        requireActive();
        renderer.popCamera();
    }

    private void requireActive() {
        if (!active) {
            throw new IllegalStateException("RenderTarget.begin() must be called before drawing");
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        if (active) {
            end();
        }
        closed = true;
        framebuffer.close();
    }
}
