package io.stratixops.engine.editor.view;

import io.stratixops.engine.math.Vec2;

/**
 * The Scene-view editor camera (STR-41): an independent 2D camera with pan and
 * cursor-anchored zoom that is never the runtime camera, so the Game view and
 * the Scene view can show the same scene from different viewpoints
 * (ADR-0005 section 4).
 *
 * <p>Screen coordinates are viewport-local pixels with the origin at the
 * top-left, matching the engine's world convention (origin top-left, {@code +y}
 * down, units are pixels). The camera stores the world point shown at the
 * viewport centre plus a zoom factor; {@code zoom == 1} maps one world pixel to
 * one screen pixel. The class is pure and headless-testable.
 */
public final class EditorCamera {

    /** Smallest allowed zoom (very wide view). */
    public static final float MIN_ZOOM = 0.05f;
    /** Largest allowed zoom (very close view). */
    public static final float MAX_ZOOM = 32f;

    private final Vec2 position = new Vec2();
    private float zoom = 1f;
    private float viewportWidth = 1f;
    private float viewportHeight = 1f;

    /**
     * The world point shown at the centre of the viewport. Mutable.
     *
     * @return the camera centre in world pixels
     */
    public Vec2 position() {
        return position;
    }

    /** @return the zoom factor (pixels on screen per world pixel) */
    public float zoom() {
        return zoom;
    }

    /**
     * Sets the zoom factor, clamped to {@link #MIN_ZOOM}..{@link #MAX_ZOOM}.
     *
     * @param value the zoom factor
     */
    public void setZoom(float value) {
        zoom = clampZoom(value);
    }

    /** @return the viewport width in screen pixels */
    public float viewportWidth() {
        return viewportWidth;
    }

    /** @return the viewport height in screen pixels */
    public float viewportHeight() {
        return viewportHeight;
    }

    /**
     * Resizes the viewport. Values below one pixel are clamped so the mapping
     * never divides by zero.
     *
     * @param width  viewport width in screen pixels
     * @param height viewport height in screen pixels
     */
    public void setViewport(float width, float height) {
        viewportWidth = Math.max(1f, width);
        viewportHeight = Math.max(1f, height);
    }

    /**
     * Pans the camera by a screen-space drag delta. Dragging right moves the
     * world right (the camera moves left).
     *
     * @param deltaX horizontal drag in screen pixels
     * @param deltaY vertical drag in screen pixels
     */
    public void panScreen(float deltaX, float deltaY) {
        position.set(position.x - deltaX / zoom, position.y - deltaY / zoom);
    }

    /**
     * Zooms about a screen-space anchor, keeping the world point under the
     * anchor fixed.
     *
     * @param factor  multiplicative zoom (e.g. {@code 1.1} to zoom in)
     * @param screenX anchor x in viewport-local pixels
     * @param screenY anchor y in viewport-local pixels
     */
    public void zoomBy(float factor, float screenX, float screenY) {
        float worldX = screenToWorldX(screenX);
        float worldY = screenToWorldY(screenY);
        setZoom(zoom * factor);
        position.set(worldX - (screenX - viewportWidth * 0.5f) / zoom,
                worldY - (screenY - viewportHeight * 0.5f) / zoom);
    }

    /**
     * Maps a viewport-local screen x to world x.
     *
     * @param screenX screen x
     * @return world x
     */
    public float screenToWorldX(float screenX) {
        return position.x + (screenX - viewportWidth * 0.5f) / zoom;
    }

    /**
     * Maps a viewport-local screen y to world y.
     *
     * @param screenY screen y
     * @return world y
     */
    public float screenToWorldY(float screenY) {
        return position.y + (screenY - viewportHeight * 0.5f) / zoom;
    }

    /**
     * Maps a viewport-local screen point to world space.
     *
     * @param screenX screen x
     * @param screenY screen y
     * @return a fresh world point
     */
    public Vec2 screenToWorld(float screenX, float screenY) {
        return new Vec2(screenToWorldX(screenX), screenToWorldY(screenY));
    }

    /**
     * Maps world x to viewport-local screen x.
     *
     * @param worldX world x
     * @return screen x
     */
    public float worldToScreenX(float worldX) {
        return (worldX - position.x) * zoom + viewportWidth * 0.5f;
    }

    /**
     * Maps world y to viewport-local screen y.
     *
     * @param worldY world y
     * @return screen y
     */
    public float worldToScreenY(float worldY) {
        return (worldY - position.y) * zoom + viewportHeight * 0.5f;
    }

    /**
     * Maps a world point to viewport-local screen space.
     *
     * @param worldX world x
     * @param worldY world y
     * @return a fresh screen point
     */
    public Vec2 worldToScreen(float worldX, float worldY) {
        return new Vec2(worldToScreenX(worldX), worldToScreenY(worldY));
    }

    /**
     * Centers the camera on a world point without changing zoom.
     *
     * @param worldX world x
     * @param worldY world y
     */
    public void centerOn(float worldX, float worldY) {
        position.set(worldX, worldY);
    }

    /**
     * Frames a world rectangle with the given padding fraction around it.
     *
     * @param bounds           the rectangle to frame
     * @param paddingFraction  fraction of the rectangle added as margin on each
     *                         side (0.1 = 10%)
     */
    public void frame(ViewRect bounds, float paddingFraction) {
        float pad = Math.max(0f, paddingFraction);
        float width = Math.max(bounds.width(), 1f);
        float height = Math.max(bounds.height(), 1f);
        float fit = Math.min(viewportWidth / (width * (1f + 2f * pad)),
                viewportHeight / (height * (1f + 2f * pad)));
        setZoom(fit);
        position.set(bounds.centerX(), bounds.centerY());
    }

    /**
     * The world-space region currently visible in the viewport.
     *
     * @return the visible rectangle
     */
    public ViewRect visibleBounds() {
        return new ViewRect(screenToWorldX(0f), screenToWorldY(0f),
                screenToWorldX(viewportWidth), screenToWorldY(viewportHeight));
    }

    private static float clampZoom(float value) {
        if (!(value > 0f) || Float.isNaN(value)) {
            return MIN_ZOOM;
        }
        return Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, value));
    }
}
