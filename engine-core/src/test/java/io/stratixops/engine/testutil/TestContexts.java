package io.stratixops.engine.testutil;

import io.stratixops.engine.EngineContext;
import io.stratixops.engine.audio.DefaultAudio;
import io.stratixops.engine.input.DefaultInput;
import io.stratixops.engine.math.Vec2;
import io.stratixops.engine.runtime.DefaultEngineContext;

/** Builds an {@link EngineContext} backed entirely by headless fakes. */
public final class TestContexts {

    private TestContexts() {
    }

    public static EngineContext create() {
        return new DefaultEngineContext(
            new FakeAssets(),
            new DefaultInput(new FakeInputDevice()),
            new DefaultAudio(new FakeAudioDevice()),
            new Vec2(320f, 180f));
    }
}
