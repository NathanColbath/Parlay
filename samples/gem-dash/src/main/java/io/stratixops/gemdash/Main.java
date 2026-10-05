package io.stratixops.gemdash;

import io.stratixops.engine.Engine;
import io.stratixops.engine.EngineConfig;

/**
 * Entry point for the Gem Dash sample ({@code io.stratixops:gem-dash-sample}).
 *
 * <p>Launched by the onboarding template's {@code runSample} task
 * (see {@code docs/GETTING_STARTED.md}) and packaged as a runnable distribution
 * ({@code Main-Class: io.stratixops.gemdash.Main}).
 */
public final class Main {

    private Main() {
    }

    /**
     * Boots Gem Dash at the first level.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        Engine.run(new GemDashGame(0), EngineConfig.builder()
            .title("Gem Dash")
            .windowSize(1280, 720)
            .logicalViewport(640, 360)
            .build());
    }
}
