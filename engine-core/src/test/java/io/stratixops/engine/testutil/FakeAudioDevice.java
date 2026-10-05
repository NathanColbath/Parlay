package io.stratixops.engine.testutil;

import io.stratixops.engine.spi.AudioDevice;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Headless {@link AudioDevice} fake that records calls and can simulate a
 * missing asset or an absent device.
 */
public final class FakeAudioDevice implements AudioDevice {

    private boolean available = true;
    private float masterGain = 1f;
    private int nextHandle = 1;

    private final Set<String> missing = new HashSet<>();

    public final List<String> loadedClips = new ArrayList<>();
    public final List<String> openedMusic = new ArrayList<>();
    public final List<Integer> playedClips = new ArrayList<>();
    public final List<Float> playedClipVolumes = new ArrayList<>();
    public final List<Integer> stoppedClips = new ArrayList<>();
    public final List<Integer> musicHandles = new ArrayList<>();
    public final List<Float> musicVolumes = new ArrayList<>();
    public final List<Boolean> musicLoops = new ArrayList<>();

    public boolean disposed;

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setMissing(String path) {
        missing.add(path);
    }

    public float masterGain() {
        return masterGain;
    }

    public int playMusicCalls() {
        return musicHandles.size();
    }

    public int stopMusicCalls;

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void setMasterGain(float gain) {
        this.masterGain = gain;
    }

    @Override
    public int loadClip(String path) {
        loadedClips.add(path);
        if (!available || missing.contains(path)) {
            return 0;
        }
        return nextHandle++;
    }

    @Override
    public void playClip(int clip, float volume) {
        playedClips.add(clip);
        playedClipVolumes.add(volume);
    }

    @Override
    public void stopClip(int clip) {
        stoppedClips.add(clip);
    }

    @Override
    public int openMusic(String path) {
        openedMusic.add(path);
        if (!available || missing.contains(path)) {
            return 0;
        }
        return nextHandle++;
    }

    @Override
    public void playMusic(int music, boolean loop, float volume) {
        musicHandles.add(music);
        musicLoops.add(loop);
        musicVolumes.add(volume);
    }

    @Override
    public void stopMusic() {
        stopMusicCalls++;
    }

    @Override
    public void dispose() {
        disposed = true;
    }
}
