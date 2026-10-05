package io.stratixops.engine.audio;

import io.stratixops.engine.math.Mathf;
import io.stratixops.engine.spi.AudioDevice;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Default {@link Audio} implementation over the backend {@link AudioDevice} SPI.
 *
 * <p>Owns the behavior the device does not: path-keyed caching of music and
 * effects, the SFX voice pool (default cap {@value #DEFAULT_VOICE_CAP}; the
 * oldest voice is stolen first), the single-active-music rule, and master mute.
 * When no device is available every operation degrades to a no-op and one
 * warning is logged, so gameplay never crashes (AC6.4).
 *
 * <p>Volume is applied when a clip or track starts. The MVP {@code AudioDevice}
 * SPI has no "set volume of a playing voice" call, so {@code setVolume} on an
 * already-playing handle takes effect on the next start.
 *
 * <p>Single-threaded, like the rest of the runtime; OpenAL's worker threads are
 * encapsulated by the device.
 */
public final class DefaultAudio implements Audio {

    /** Default maximum number of simultaneously playing SFX voices. */
    public static final int DEFAULT_VOICE_CAP = 16;

    private static final Logger LOG = Logger.getLogger("io.stratixops.engine.audio");

    private final AudioDevice device;
    private final int voiceCap;

    private final Map<String, DefaultSfx> sfxCache = new HashMap<>();
    private final Map<String, DefaultMusic> musicCache = new HashMap<>();
    private final Deque<Integer> voiceQueue = new ArrayDeque<>();

    private DefaultMusic currentMusic;
    private boolean muted;
    private boolean warnedNoDevice;

    /**
     * Creates audio over the given device with the default voice cap.
     *
     * @param device backend audio device
     */
    public DefaultAudio(AudioDevice device) {
        this(device, DEFAULT_VOICE_CAP);
    }

    /**
     * Creates audio over the given device.
     *
     * @param device   backend audio device
     * @param voiceCap maximum simultaneous one-shot voices; must be &gt;= 1
     * @throws IllegalArgumentException if {@code voiceCap < 1}
     */
    public DefaultAudio(AudioDevice device, int voiceCap) {
        this.device = Objects.requireNonNull(device, "device");
        if (voiceCap < 1) {
            throw new IllegalArgumentException("voiceCap must be >= 1 but was " + voiceCap);
        }
        this.voiceCap = voiceCap;
    }

    @Override
    public Music music(String path, float volume) {
        Objects.requireNonNull(path, "path");
        DefaultMusic track = musicCache.get(path);
        if (track == null) {
            int handle = 0;
            if (available()) {
                handle = device.openMusic(path);
            } else {
                warnNoDeviceOnce();
            }
            track = new DefaultMusic(this, handle);
            musicCache.put(path, track);
        }
        track.setVolume(volume);
        return track;
    }

    @Override
    public Sfx sfx(String path) {
        Objects.requireNonNull(path, "path");
        DefaultSfx effect = sfxCache.get(path);
        if (effect == null) {
            int clip = 0;
            if (available()) {
                clip = device.loadClip(path);
            } else {
                warnNoDeviceOnce();
            }
            effect = new DefaultSfx(this, clip);
            sfxCache.put(path, effect);
        }
        return effect;
    }

    @Override
    public void toggleMute() {
        setMuted(!muted);
    }

    @Override
    public void setMuted(boolean muted) {
        this.muted = muted;
        if (available()) {
            device.setMasterGain(muted ? 0f : 1f);
        }
    }

    @Override
    public boolean isMuted() {
        return muted;
    }

    /** Releases the underlying device. */
    public void dispose() {
        if (available()) {
            device.dispose();
        }
    }

    // ---- used by the handles -------------------------------------------------

    boolean playEffect(DefaultSfx effect) {
        if (!available() || effect.clip() <= 0) {
            return false;
        }
        while (voiceQueue.size() >= voiceCap) {
            Integer oldest = voiceQueue.pollFirst();
            if (oldest != null && oldest > 0) {
                // The SPI stops every instance of a clip, so this steals the
                // oldest clip rather than one voice; documented approximation.
                device.stopClip(oldest);
            }
        }
        device.playClip(effect.clip(), effect.volume());
        voiceQueue.addLast(effect.clip());
        return true;
    }

    void stopEffect(DefaultSfx effect) {
        voiceQueue.removeIf(handle -> handle == effect.clip());
        if (available() && effect.clip() > 0) {
            device.stopClip(effect.clip());
        }
    }

    void startMusic(DefaultMusic track) {
        if (currentMusic != null && currentMusic != track && available()) {
            device.stopMusic();
        }
        currentMusic = track;
        if (!available()) {
            warnNoDeviceOnce();
            return;
        }
        if (track.handle() > 0) {
            device.playMusic(track.handle(), true, track.volume());
        }
    }

    void stopMusic(DefaultMusic track) {
        if (currentMusic == track) {
            if (available() && track.handle() > 0) {
                device.stopMusic();
            }
            currentMusic = null;
        }
    }

    private boolean available() {
        return device.isAvailable();
    }

    private void warnNoDeviceOnce() {
        if (!warnedNoDevice) {
            warnedNoDevice = true;
            LOG.warning("No audio device; running muted");
        }
    }

    private static float clampVolume(float volume) {
        return Mathf.clamp(volume, 0f, 1f);
    }

    // Handle implementations ---------------------------------------------------

    static final class DefaultMusic implements Music {
        private final DefaultAudio audio;
        private final int handle;
        private float volume = 1f;

        DefaultMusic(DefaultAudio audio, int handle) {
            this.audio = audio;
            this.handle = handle;
        }

        int handle() {
            return handle;
        }

        float volume() {
            return volume;
        }

        @Override
        public Music loop() {
            audio.startMusic(this);
            return this;
        }

        @Override
        public void stop() {
            audio.stopMusic(this);
        }

        @Override
        public void setVolume(float volume) {
            this.volume = clampVolume(volume);
        }
    }

    static final class DefaultSfx implements Sfx {
        private final DefaultAudio audio;
        private final int clip;
        private float volume = 1f;

        DefaultSfx(DefaultAudio audio, int clip) {
            this.audio = audio;
            this.clip = clip;
        }

        int clip() {
            return clip;
        }

        float volume() {
            return volume;
        }

        @Override
        public void play() {
            audio.playEffect(this);
        }

        @Override
        public void stop() {
            audio.stopEffect(this);
        }

        @Override
        public void setVolume(float volume) {
            this.volume = clampVolume(volume);
        }
    }
}
