package io.stratixops.engine.spi;

/**
 * Internal SPI: a raw audio backend.
 *
 * <p>This is not part of the developer-facing API (ADR-0004). A backend
 * implements it (for example {@code OpenAlAudioDevice} in {@code engine-render});
 * the core runtime consumes it to build {@link io.stratixops.engine.audio.Audio}
 * semantics such as handle caching, the master mute, and the voice cap.
 *
 * <h2>Contract</h2>
 * <ul>
 *   <li>Handle {@code 0} always means "invalid"; a failed load returns {@code 0}
 *       rather than throwing, so content problems never crash the game. A
 *       missing path is logged by the implementation.</li>
 *   <li>All methods are no-ops when {@link #isAvailable()} is {@code false}, and
 *       a single warning is logged when the device could not be opened.</li>
 *   <li>Calls are made on the main (GL) thread; the backend may use its own
 *       internal threads, but no thread-safety is required of callers.</li>
 * </ul>
 */
public interface AudioDevice {

    /**
     * Whether audio output could be opened.
     *
     * @return true if the device is usable
     */
    boolean isAvailable();

    /**
     * Sets the master output gain.
     *
     * @param gain linear gain, {@code 0..1}
     */
    void setMasterGain(float gain);

    /**
     * Loads (or returns a cached) short sound effect, fully decoded in memory.
     *
     * @param path classpath or filesystem path
     * @return a positive handle, or {@code 0} if the clip is missing/undecodable
     */
    int loadClip(String path);

    /**
     * Plays a clip once from the beginning, overlapping any current playback.
     *
     * @param clip   handle from {@link #loadClip(String)}
     * @param volume per-clip volume, {@code 0..1}
     */
    void playClip(int clip, float volume);

    /**
     * Stops all currently playing instances of a clip.
     *
     * @param clip handle from {@link #loadClip(String)}
     */
    void stopClip(int clip);

    /**
     * Opens a long streamed/looping track.
     *
     * @param path classpath or filesystem path
     * @return a positive handle, or {@code 0} if the track is missing/undecodable
     */
    int openMusic(String path);

    /**
     * Plays a track, replacing any currently playing track.
     *
     * @param music  handle from {@link #openMusic(String)}
     * @param loop   whether to loop
     * @param volume track volume, {@code 0..1}
     */
    void playMusic(int music, boolean loop, float volume);

    /**
     * Stops the current music track.
     */
    void stopMusic();

    /**
     * Releases the device. No-op if already disposed or unavailable.
     */
    void dispose();
}
