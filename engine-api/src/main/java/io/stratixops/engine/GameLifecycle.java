package io.stratixops.engine;

/**
 * The game's lifecycle. Implement this and hand an instance to
 * {@link Engine#run(GameLifecycle, EngineConfig)}; there is no init order to
 * remember and no base class to extend.
 *
 * <p>The engine calls {@link #onCreate(EngineContext)} once, then
 * {@link #onUpdate(float)} and {@link #onRender(Renderer, float)} each frame,
 * then {@link #onDispose()} on exit.
 */
public interface GameLifecycle {

    /**
     * Called once before the first frame. Load resources and build game state.
     *
     * @param ctx engine capabilities
     */
    void onCreate(EngineContext ctx);

    /**
     * Fixed-step update. Game rules live here; do not draw.
     *
     * @param dt seconds since the previous update
     */
    default void onUpdate(float dt) {
    }

    /**
     * Draws a frame. Called after the frame's update(s).
     *
     * @param r     renderer
     * @param alpha interpolation factor in {@code [0,1)} between the previous
     *              and current update states, supplied by the engine. Forward it
     *              to entity/scene render calls for smooth motion at any display
     *              refresh rate; ignore it if your game does not interpolate.
     */
    void onRender(Renderer r, float alpha);

    /**
     * Called once on exit. Release game-owned resources; engine assets are
     * disposed for you.
     */
    default void onDispose() {
    }
}
