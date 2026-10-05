package io.stratixops.engine.audio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import io.stratixops.engine.testutil.FakeAudioDevice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Audio state machine and no-device degradation (F6, AC6.4). */
class DefaultAudioTest {

    private FakeAudioDevice device;
    private DefaultAudio audio;

    @BeforeEach
    void setUp() {
        device = new FakeAudioDevice();
        audio = new DefaultAudio(device);
    }

    @Test
    void sfxHandlesAreCachedAndLoadedOnce() {
        Sfx first = audio.sfx("sounds/pickup.wav");
        Sfx second = audio.sfx("sounds/pickup.wav");

        assertThat(first).isSameAs(second);
        assertThat(device.loadedClips).containsExactly("sounds/pickup.wav");
    }

    @Test
    void playingSfxSendsClipAndVolume() {
        Sfx sfx = audio.sfx("a.wav");
        sfx.setVolume(0.25f);
        sfx.play();

        assertThat(device.playedClips).containsExactly(1);
        assertThat(device.playedClipVolumes).containsExactly(0.25f);
    }

    @Test
    void voiceCapStealsTheOldestClip() {
        audio = new DefaultAudio(device, 2);
        Sfx a = audio.sfx("a.wav");
        Sfx b = audio.sfx("b.wav");
        Sfx c = audio.sfx("c.wav");

        a.play();
        b.play();
        c.play();

        assertThat(device.playedClips).containsExactly(1, 2, 3);
        assertThat(device.stoppedClips).containsExactly(1);
    }

    @Test
    void stopStopsAllInstancesOfTheClip() {
        Sfx sfx = audio.sfx("a.wav");
        sfx.play();
        sfx.stop();

        assertThat(device.stoppedClips).containsExactly(1);
    }

    @Test
    void muteZeroesAndRestoresTheMasterGain() {
        audio.setMuted(true);
        assertThat(audio.isMuted()).isTrue();
        assertThat(device.masterGain()).isZero();

        audio.toggleMute();
        assertThat(audio.isMuted()).isFalse();
        assertThat(device.masterGain()).isEqualTo(1f);
    }

    @Test
    void missingClipPlaysSilently() {
        device.setMissing("missing.wav");
        Sfx sfx = audio.sfx("missing.wav");
        sfx.play();

        assertThat(device.loadedClips).containsExactly("missing.wav");
        assertThat(device.playedClips).isEmpty();
    }

    @Test
    void musicIsCachedAndLooped() {
        Music first = audio.music("level1.ogg", 0.5f);
        Music second = audio.music("level1.ogg", 0.5f);
        first.loop();

        assertThat(first).isSameAs(second);
        assertThat(device.openedMusic).containsExactly("level1.ogg");
        assertThat(device.playMusicCalls()).isEqualTo(1);
        assertThat(device.musicHandles).containsExactly(1);
        assertThat(device.musicLoops).containsExactly(true);
        assertThat(device.musicVolumes).containsExactly(0.5f);
    }

    @Test
    void startingAnotherTrackReplacesTheCurrentOne() {
        audio.music("a.ogg", 1f).loop();
        audio.music("b.ogg", 1f).loop();

        assertThat(device.stopMusicCalls).isEqualTo(1);
        assertThat(device.playMusicCalls()).isEqualTo(2);
    }

    @Test
    void stoppingMusicStopsTheDevice() {
        Music music = audio.music("a.ogg", 1f);
        music.loop();
        music.stop();

        assertThat(device.stopMusicCalls).isEqualTo(1);
    }

    @Test
    void volumeIsClamped() {
        Music music = audio.music("a.ogg", 5f);
        music.loop();

        assertThat(device.musicVolumes).containsExactly(1f);
    }

    @Test
    void voiceCapMustBePositive() {
        assertThatIllegalArgumentException().isThrownBy(() -> new DefaultAudio(device, 0));
    }

    @Test
    void noDeviceDegradesToNoOps() {
        device.setAvailable(false);

        Sfx sfx = audio.sfx("a.wav");
        sfx.play();
        audio.music("a.ogg", 1f).loop();
        audio.setMuted(true);

        assertThat(device.playedClips).isEmpty();
        assertThat(device.musicHandles).isEmpty();
        assertThat(audio.isMuted()).isTrue();
    }

    @Test
    void disposeReleasesTheDevice() {
        audio.dispose();
        assertThat(device.disposed).isTrue();
    }
}
