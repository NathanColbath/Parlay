package io.stratixops.engine;

/**
 * Entry point. {@code Engine.run(...)} owns window creation, the game loop,
 * fixed-step timing with render interpolation, and orderly shutdown.
 */
public final class Engine {

    private Engine() {
    }

    /**
     * Runs a game until its window is closed.
     *
     * @param game   the {@link GameLifecycle} implementation
     * @param config engine configuration
     */
    public static void run(GameLifecycle game, EngineConfig config) {
        throw new UnsupportedOperationException(
            "Engine runtime is provided by the core/render modules (STR-2/STR-5/STR-6)."
                + " This module is the API contract only. Requested title: " + config.title());
    }
}
