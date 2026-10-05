package io.stratixops.engine.editor.view;

import io.stratixops.engine.scene.Scene;

/**
 * The Game-view offscreen render seam (STR-41). The editor depends on this
 * tiny interface rather than on GL types, so the panel layer and its tests can
 * run headless; the GL implementation lives in the excluded
 * {@code io.stratixops.engine.editor.gl} package and wraps an
 * {@code engine-render} {@code RenderTarget}.
 *
 * <p>One implementation renders the runtime scene through the shared
 * {@code SceneRenderer} into a fixed 640x360 target and exposes its texture for
 * {@code ImGui::Image}, so the Game view can never diverge from the runtime
 * submission path (ADR-0005 section 5, ADR-0008).
 */
public interface ViewportRenderer extends AutoCloseable {

    /** @return true when a texture is available to present */
    boolean isReady();

    /** @return the GL texture id of the runtime target, or {@code 0} */
    int textureId();

    /** @return the runtime target width in pixels */
    int targetWidth();

    /** @return the runtime target height in pixels */
    int targetHeight();

    /**
     * Renders a runtime scene into the offscreen target. Must be called on the
     * GL thread before the ImGui frame is submitted.
     *
     * @param scene the runtime scene
     */
    void renderRuntime(Scene scene);

    @Override
    void close();
}
