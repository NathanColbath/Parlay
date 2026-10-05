package io.stratixops.engine.spi;

import io.stratixops.engine.EngineConfig;
import io.stratixops.engine.GameLifecycle;

/**
 * Internal SPI: a complete engine backend.
 *
 * <p>This is not part of the developer-facing API (ADR-0004). {@code Engine.run}
 * resolves exactly one implementation through {@link java.util.ServiceLoader};
 * the desktop backend ({@code engine-desktop}) registers
 * {@code DesktopEngineRuntime} in
 * {@code META-INF/services/io.stratixops.engine.spi.EngineRuntime}. A future web
 * backend can be added by registering another provider without changing game
 * code.
 */
public interface EngineRuntime {

    /**
     * Owns the window/context, the game loop, and orderly shutdown.
     *
     * @param game   the game lifecycle
     * @param config engine configuration
     */
    void run(GameLifecycle game, EngineConfig config);
}
