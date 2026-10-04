package io.stratixops.engine.audio;

/**
 * A looping music track. Only one track plays at a time; starting a new one
 * replaces the current track.
 */
public interface Music {

    /**
     * Starts (or restarts) this track as a loop.
     *
     * @return this track, for chaining
     */
    Music loop();

    /**
     * Stops playback.
     */
    void stop();

    /**
     * Sets the track volume.
     *
     * @param volume 0..1
     */
    void setVolume(float volume);
}
