package io.stratixops.engine;

/**
 * Asset loading and caching. Assets are requested by path and decoded once;
 * requesting the same path twice returns the cached handle.
 *
 * <h2>Missing-asset policy</h2>
 * <ul>
 *   <li><strong>Registration / config errors fail fast:</strong> an invalid
 *       configuration value or a bad action-map definition throws
 *       {@link IllegalArgumentException} naming the offending file/key/value.</li>
 *   <li><strong>Runtime content loads degrade:</strong> a missing sprite
 *       resolves to a magenta placeholder and logs a clear, actionable message
 *       with the path; a missing sound is silent. Neither crashes the game.</li>
 * </ul>
 */
public interface Assets {

    /**
     * Loads (or returns a cached) sprite.
     *
     * @param path classpath or filesystem path, e.g. {@code images/logo.png}
     * @return the sprite handle, or a magenta placeholder if the asset is
     *         missing (logged)
     */
    Sprite sprite(String path);

    /**
     * Loads (or returns a cached) texture atlas.
     *
     * @param path path to the atlas metadata, e.g. {@code atlas.json}
     * @return the atlas handle
     */
    TextureAtlas atlas(String path);

    /**
     * Loads (or returns a cached) bitmap font.
     *
     * @param path path to the font, e.g. {@code font.pixel}
     * @return the font handle
     */
    Font font(String path);
}
