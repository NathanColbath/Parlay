package io.stratixops.hello;

import io.stratixops.engine.Engine;
import io.stratixops.engine.EngineConfig;

/** Entry point. {@code gradlew run} (Windows) / {@code ./gradlew run} (macOS, Linux). */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Engine.run(new MyGame(), EngineConfig.builder()
            .title("Hello Engine")
            // windowSize is the physical window; logicalViewport is the fixed
            // 640x360 game resolution, integer-scaled into the window.
            .windowSize(1280, 720)
            .logicalViewport(640, 360)
            .build());
    }
}
