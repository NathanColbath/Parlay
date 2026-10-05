package io.stratixops.engine.spi;

import io.stratixops.engine.input.Key;

/**
 * Internal SPI: raw keyboard state for one frame.
 *
 * <p>This is not part of the developer-facing API (ADR-0004). A backend
 * implements it (for example {@code GlfwInputDevice} in {@code engine-render});
 * the core runtime consumes it to build the higher-level
 * {@link io.stratixops.engine.input.Input} semantics.
 *
 * <h2>Contract</h2>
 * <ul>
 *   <li>{@link #poll()} is called exactly once per frame, before any query, and
 *       moves the buffered press/release edges to the current frame.</li>
 *   <li>{@link #isKeyDown(Key)} reflects the physical state sampled during the
 *       most recent {@link #poll()}.</li>
 *   <li>{@link #wasKeyPressed(Key)} / {@link #wasKeyReleased(Key)} are true only
 *       for the single frame in which the edge occurred. An edge that happens
 *       between polls is buffered, never lost.</li>
 * </ul>
 */
public interface InputDevice {

    /**
     * Samples the device and advances the per-frame edge state. Must be called
     * once per frame before any query method.
     */
    void poll();

    /**
     * Whether the key is currently held.
     *
     * @param key physical key
     * @return true while held
     */
    boolean isKeyDown(Key key);

    /**
     * Whether the key went down since the previous {@link #poll()}.
     *
     * @param key physical key
     * @return true on the frame of the press
     */
    boolean wasKeyPressed(Key key);

    /**
     * Whether the key was released since the previous {@link #poll()}.
     *
     * @param key physical key
     * @return true on the frame of the release
     */
    boolean wasKeyReleased(Key key);
}
