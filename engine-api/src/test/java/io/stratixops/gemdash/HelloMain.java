package io.stratixops.gemdash;

import io.stratixops.engine.Engine;
import io.stratixops.engine.EngineConfig;

/** Entry point for the section 2 example. */
public final class HelloMain {

    private HelloMain() {
    }

    /**
     * Boots the hello-sprite game.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        Engine.run(new HelloEngine(), EngineConfig.builder()
            .title("Hello Engine")
            .windowSize(1280, 720)       // physical window (default)
            .logicalViewport(640, 360)   // logical render target (default)
            .build());
    }
}
