package io.stratixops.engine;

import io.stratixops.engine.audio.Audio;
import io.stratixops.engine.input.Input;
import io.stratixops.engine.math.Vec2;

/**
 * Per-game capabilities handed to the lifecycle. Everything the game needs is
 * passed in; there are no global singletons to discover.
 */
public interface EngineContext {

    /**
     * Assets (sprites, atlases, fonts).
     *
     * @return the asset cache
     */
    Assets assets();

    /**
     * Input (named actions, axes, buttons).
     *
     * @return the input system
     */
    Input input();

    /**
     * Audio (music, sfx, mute).
     *
     * @return the audio system
     */
    Audio audio();

    /**
     * The logical viewport size in pixels (the value configured with
     * {@link EngineConfig.Builder#logicalViewport(int, int)}). Reused instance.
     *
     * @return logical viewport size, e.g. {@code (640, 360)}
     */
    Vec2 viewport();
}
