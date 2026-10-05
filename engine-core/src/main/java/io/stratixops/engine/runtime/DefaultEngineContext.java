package io.stratixops.engine.runtime;

import io.stratixops.engine.Assets;
import io.stratixops.engine.EngineContext;
import io.stratixops.engine.audio.Audio;
import io.stratixops.engine.input.Input;
import io.stratixops.engine.math.Vec2;
import java.util.Objects;

/**
 * Default {@link EngineContext}. Assembled by the backend bootstrap
 * ({@code engine-desktop}) from the render module's {@code Assets} and core's
 * input/audio implementations; the game receives it through
 * {@code GameLifecycle.onCreate}.
 *
 * <p>The returned {@link #viewport()} is a single reused instance, matching the
 * ownership contract on {@link EngineContext}.
 */
public final class DefaultEngineContext implements EngineContext {

    private final Assets assets;
    private final Input input;
    private final Audio audio;
    private final Vec2 viewport;

    /**
     * Creates a context.
     *
     * @param assets   asset cache
     * @param input    input system
     * @param audio    audio system
     * @param viewport logical viewport size; a copy is retained
     */
    public DefaultEngineContext(Assets assets, Input input, Audio audio, Vec2 viewport) {
        this.assets = Objects.requireNonNull(assets, "assets");
        this.input = Objects.requireNonNull(input, "input");
        this.audio = Objects.requireNonNull(audio, "audio");
        Objects.requireNonNull(viewport, "viewport");
        this.viewport = new Vec2(viewport.x, viewport.y);
    }

    @Override
    public Assets assets() {
        return assets;
    }

    @Override
    public Input input() {
        return input;
    }

    @Override
    public Audio audio() {
        return audio;
    }

    @Override
    public Vec2 viewport() {
        return viewport;
    }
}
