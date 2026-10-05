package io.stratixops.engine.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import io.stratixops.engine.Assets;
import io.stratixops.engine.audio.Audio;
import io.stratixops.engine.audio.DefaultAudio;
import io.stratixops.engine.input.DefaultInput;
import io.stratixops.engine.input.Input;
import io.stratixops.engine.math.Vec2;
import io.stratixops.engine.testutil.FakeAssets;
import io.stratixops.engine.testutil.FakeAudioDevice;
import io.stratixops.engine.testutil.FakeInputDevice;
import org.junit.jupiter.api.Test;

class DefaultEngineContextTest {

    @Test
    void exposesTheProvidedSystems() {
        Assets assets = new FakeAssets();
        Input input = new DefaultInput(new FakeInputDevice());
        Audio audio = new DefaultAudio(new FakeAudioDevice());

        DefaultEngineContext ctx = new DefaultEngineContext(assets, input, audio, new Vec2(640f, 360f));

        assertThat(ctx.assets()).isSameAs(assets);
        assertThat(ctx.input()).isSameAs(input);
        assertThat(ctx.audio()).isSameAs(audio);
    }

    @Test
    void viewportIsCopiedDefensively() {
        Vec2 source = new Vec2(640f, 360f);
        DefaultEngineContext ctx = new DefaultEngineContext(
            new FakeAssets(), new DefaultInput(new FakeInputDevice()),
            new DefaultAudio(new FakeAudioDevice()), source);

        source.set(1f, 2f);

        assertThat(ctx.viewport().x).isEqualTo(640f);
        assertThat(ctx.viewport().y).isEqualTo(360f);
    }

    @Test
    void viewportIsAReusedInstance() {
        DefaultEngineContext ctx = new DefaultEngineContext(
            new FakeAssets(), new DefaultInput(new FakeInputDevice()),
            new DefaultAudio(new FakeAudioDevice()), new Vec2(320f, 180f));

        assertThat(ctx.viewport()).isSameAs(ctx.viewport());
    }

    @Test
    void rejectsNullDependencies() {
        assertThatNullPointerException().isThrownBy(() -> new DefaultEngineContext(
            null, new DefaultInput(new FakeInputDevice()),
            new DefaultAudio(new FakeAudioDevice()), new Vec2(1f, 1f)));
        assertThatNullPointerException().isThrownBy(() -> new DefaultEngineContext(
            new FakeAssets(), null,
            new DefaultAudio(new FakeAudioDevice()), new Vec2(1f, 1f)));
        assertThatNullPointerException().isThrownBy(() -> new DefaultEngineContext(
            new FakeAssets(), new DefaultInput(new FakeInputDevice()),
            null, new Vec2(1f, 1f)));
        assertThatNullPointerException().isThrownBy(() -> new DefaultEngineContext(
            new FakeAssets(), new DefaultInput(new FakeInputDevice()),
            new DefaultAudio(new FakeAudioDevice()), null));
    }
}
