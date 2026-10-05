package io.stratixops.engine.audio;

/**
 * A cached one-shot sound effect. Handles are owned by {@link Audio} and must
 * not be disposed by the game. Several effects may play concurrently; the
 * engine caps the number of active voices.
 */
public interface Sfx {

    /**
     * Plays the effect once from the beginning.
     */
    void play();

    /**
     * Stops all currently playing instances of this effect.
     */
    void stop();

    /**
     * Sets the per-effect volume.
     *
     * @param volume volume in {@code 0..1}
     */
    void setVolume(float volume);
}
