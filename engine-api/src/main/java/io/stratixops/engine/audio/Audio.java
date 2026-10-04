package io.stratixops.engine.audio;

/**
 * Master/sfx/music audio with mute. If no audio device is available the engine
 * degrades gracefully: calls become no-ops and a warning is logged once.
 */
public interface Audio {

    /**
     * Loads (or returns a cached) music track.
     *
     * @param path   classpath or filesystem path, e.g. {@code music/level1.ogg}
     * @param volume initial volume, 0..1
     * @return the track handle
     */
    Music music(String path, float volume);

    /**
     * Toggles mute for all output.
     */
    void toggleMute();

    /**
     * Sets mute explicitly.
     *
     * @param muted true to mute all output
     */
    void setMuted(boolean muted);

    /**
     * Whether output is currently muted.
     *
     * @return the mute state
     */
    boolean isMuted();
}
