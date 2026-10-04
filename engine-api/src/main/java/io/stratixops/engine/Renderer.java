package io.stratixops.engine;

/**
 * Immediate-mode 2D renderer. World drawing is bracketed by
 * {@link #pushCamera(Camera2D)} / {@link #popCamera()}; anything drawn after
 * the pop is screen-space, which is how HUDs stay camera-independent.
 *
 * <p>Angles are radians. Colors are packed {@code 0xRRGGBBAA} ints.
 */
public interface Renderer {

    /**
     * Clears the frame.
     *
     * @param rgba packed color, e.g. {@code 0x1B1B2FFF}
     */
    void clear(int rgba);

    /**
     * Draws a sprite. The sprite is centered on the given position by default.
     *
     * @param sprite sprite to draw
     * @param x      world x in pixels
     * @param y      world y in pixels
     */
    void draw(Sprite sprite, float x, float y);

    /**
     * Draws text with the built-in font. The position is the top-left corner of
     * the first glyph.
     *
     * @param text text to draw
     * @param x    screen x in pixels
     * @param y    screen y in pixels
     */
    void drawText(String text, float x, float y);

    /**
     * Pushes a camera transform; subsequent world draws use it.
     *
     * @param camera camera to apply
     */
    void pushCamera(Camera2D camera);

    /**
     * Pops the current camera transform, returning to screen space.
     */
    void popCamera();
}
