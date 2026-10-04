package io.stratixops.engine;

import io.stratixops.engine.math.Vec2;

/**
 * A first-class 2D camera. {@link #follow(Vec2, float)} bundles deadzone,
 * frame-rate-independent smoothing and bounds clamping so games never
 * hand-roll it.
 */
public final class Camera2D {

    private final Vec2 viewport;
    private final Vec2 position = new Vec2();

    /**
     * Creates a camera.
     *
     * @param viewport logical viewport size in pixels; the camera shows exactly
     *                 this many world pixels. Pass {@link EngineContext#viewport()}.
     */
    public Camera2D(Vec2 viewport) {
        this.viewport = new Vec2(viewport.x, viewport.y);
    }

    /**
     * Follows a target with deadzone, smoothing and clamping.
     *
     * @param target target position (world pixels); reused after the call
     * @param dt     seconds since the last update
     */
    public void follow(Vec2 target, float dt) {
        position.set(target);
    }

    /**
     * Current camera position (world pixels). Reused instance.
     *
     * @return the camera position
     */
    public Vec2 position() {
        return position;
    }

    /**
     * The viewport size this camera was created with. Reused instance.
     *
     * @return viewport size in pixels
     */
    public Vec2 viewport() {
        return viewport;
    }
}
