package io.stratixops.engine.editor.gl;

import io.stratixops.engine.editor.view.ViewportRenderer;
import io.stratixops.engine.render.GlRenderer;
import io.stratixops.engine.render.RenderTarget;
import io.stratixops.engine.scene.Scene;
import io.stratixops.engine.scene.SceneRenderer;

import java.util.Objects;

/**
 * GL implementation of the Game-view seam (STR-41). It owns a fixed 640x360
 * {@code engine-render} {@link RenderTarget} and submits the runtime scene
 * through the shared {@link SceneRenderer}, so the editor's Game view renders
 * exactly like the standalone runtime (ADR-0005 section 5). The target's
 * texture is presented by {@code GameViewPanel} with aspect preserved.
 *
 * <p>GL-only and excluded from the headless coverage gate (ADR-0008 section 7);
 * the target lifecycle itself is exercised by the {@code gpu}-tagged test.
 */
public final class GlViewportRenderer implements ViewportRenderer {

    /** The runtime logical viewport width (ADR-0002). */
    public static final int LOGICAL_WIDTH = 640;
    /** The runtime logical viewport height (ADR-0002). */
    public static final int LOGICAL_HEIGHT = 360;

    private final GlRenderer renderer;
    private final SceneRenderer sceneRenderer = new SceneRenderer();
    private final RenderTarget target;
    private boolean closed;

    /**
     * Creates a 640x360 runtime target. Requires a current GL context.
     *
     * @param renderer the shared scene renderer
     */
    public GlViewportRenderer(GlRenderer renderer) {
        this(renderer, LOGICAL_WIDTH, LOGICAL_HEIGHT);
    }

    /**
     * Creates a runtime target of an explicit size (tests).
     *
     * @param renderer the shared scene renderer
     * @param width    target width in pixels
     * @param height   target height in pixels
     */
    public GlViewportRenderer(GlRenderer renderer, int width, int height) {
        this.renderer = Objects.requireNonNull(renderer, "renderer");
        this.target = renderer.createRenderTarget(width, height);
    }

    @Override
    public boolean isReady() {
        return !closed;
    }

    @Override
    public int textureId() {
        return target.textureId();
    }

    @Override
    public int targetWidth() {
        return target.width();
    }

    @Override
    public int targetHeight() {
        return target.height();
    }

    @Override
    public void renderRuntime(Scene scene) {
        if (closed || scene == null) {
            return;
        }
        target.begin();
        sceneRenderer.render(scene, target);
        target.end();
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        target.close();
    }

    /**
     * The backing target, for {@code gpu}-tagged readback tests.
     *
     * @return the runtime render target
     */
    RenderTarget target() {
        return target;
    }
}
