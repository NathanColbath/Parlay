package io.stratixops.engine.runtime;

/**
 * The engine's fixed-timestep game loop (ADR-0003).
 *
 * <p>Each call to {@link #frame(float, Step)} advances the simulation by whole
 * {@link #FIXED_DT} steps and then renders once with an interpolation factor
 * {@code alpha} in {@code [0,1)}. Long frames are clamped to
 * {@link #MAX_FRAME_TIME} so a stall recovers instead of spiralling
 * (AC2.2). Gameplay therefore runs at a constant 60&nbsp;Hz regardless of the
 * display refresh rate (AC2.1).
 *
 * <p>The class is pure timing math with no clock or I/O of its own: the
 * backend supplies the real frame delta, and tests supply fake ones, which is
 * what keeps it headless-testable.
 *
 * <pre>{@code
 * GameLoop loop = new GameLoop();
 * loop.frame(frameDelta, new GameLoop.Step() {
 *     public void update(float dt) { game.onUpdate(dt); }
 *     public void render(float alpha) { renderer.begin(); game.onRender(renderer, alpha); }
 * });
 * }</pre>
 */
public final class GameLoop {

    /** Fixed simulation step, in seconds (60&nbsp;Hz). */
    public static final float FIXED_DT = 1f / 60f;

    /** Largest frame delta consumed in one call, in seconds; the spiral-of-death guard. */
    public static final float MAX_FRAME_TIME = 0.25f;

    private static final double FIXED_DT_DOUBLE = 1.0 / 60.0;
    private static final double EPSILON = 1e-9;

    /** The per-frame callbacks the loop drives. */
    public interface Step {

        /**
         * Advances the simulation by one fixed step. Called zero or more times
         * per frame.
         *
         * @param dt the fixed step in seconds ({@link #FIXED_DT})
         */
        void update(float dt);

        /**
         * Renders one frame.
         *
         * @param alpha interpolation factor in {@code [0,1)}
         */
        void render(float alpha);
    }

    private double accumulator;

    /**
     * Advances the loop by one real frame and drives the given callbacks.
     *
     * @param frameTime seconds elapsed since the previous frame; negative
     *                  values are treated as zero, large values are clamped
     * @param step      update/render callbacks
     */
    public void frame(float frameTime, Step step) {
        float clamped = frameTime < 0f ? 0f : Math.min(frameTime, MAX_FRAME_TIME);
        accumulator += clamped;

        while (accumulator + EPSILON >= FIXED_DT_DOUBLE) {
            step.update(FIXED_DT);
            accumulator -= FIXED_DT_DOUBLE;
        }
        if (accumulator < 0.0) {
            accumulator = 0.0;
        }

        step.render((float) (accumulator / FIXED_DT_DOUBLE));
    }

    /**
     * The interpolation factor that would be passed to {@link Step#render(float)}
     * for the current accumulator state, in {@code [0,1)}.
     *
     * @return current alpha
     */
    public float alpha() {
        return (float) (accumulator / FIXED_DT_DOUBLE);
    }

    /**
     * Leftover time not yet consumed by an update, in seconds.
     *
     * @return the accumulated remainder
     */
    public float accumulator() {
        return (float) accumulator;
    }

    /** Drops any accumulated time (for example after a load screen). */
    public void reset() {
        accumulator = 0.0;
    }
}
