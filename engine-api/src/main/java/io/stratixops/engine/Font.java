package io.stratixops.engine;

/**
 * A bitmap font used with {@link Renderer#drawText(String, float, float)}.
 */
public interface Font {

    /**
     * Line height in pixels.
     *
     * @return line height
     */
    float lineHeight();
}
