package io.stratixops.engine;

/**
 * A drawable image handle. Owned and cached by {@link Assets}; the developer
 * never disposes it manually.
 */
public interface Sprite {

    /**
     * Sprite width in pixels.
     *
     * @return width
     */
    float width();

    /**
     * Sprite height in pixels.
     *
     * @return height
     */
    float height();
}
