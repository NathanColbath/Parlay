package io.stratixops.engine.editor.view;

/**
 * An axis-aligned rectangle in world space (STR-41). Used for object bounds,
 * selection framing, marquee hit-testing and the visible viewport region. Pure
 * and free of ImGui/GL types so it is unit-testable headless.
 *
 * @param minX left edge
 * @param minY top edge
 * @param maxX right edge
 * @param maxY bottom edge
 */
public record ViewRect(float minX, float minY, float maxX, float maxY) {

    /**
     * Creates a rectangle from a center and half extents.
     *
     * @param centerX center x
     * @param centerY center y
     * @param halfW   half width
     * @param halfH   half height
     * @return the rectangle
     */
    public static ViewRect centered(float centerX, float centerY, float halfW, float halfH) {
        return new ViewRect(centerX - halfW, centerY - halfH, centerX + halfW, centerY + halfH);
    }

    /** @return the rectangle width (never negative) */
    public float width() {
        return maxX - minX;
    }

    /** @return the rectangle height (never negative) */
    public float height() {
        return maxY - minY;
    }

    /** @return the center x */
    public float centerX() {
        return (minX + maxX) * 0.5f;
    }

    /** @return the center y */
    public float centerY() {
        return (minY + maxY) * 0.5f;
    }

    /**
     * @param x world x
     * @param y world y
     * @return true when the point is inside (inclusive)
     */
    public boolean contains(float x, float y) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    /**
     * @param other the other rectangle
     * @return true when the two rectangles overlap
     */
    public boolean intersects(ViewRect other) {
        return maxX >= other.minX && minX <= other.maxX
                && maxY >= other.minY && minY <= other.maxY;
    }

    /**
     * The smallest rectangle containing both.
     *
     * @param other the other rectangle
     * @return the union
     */
    public ViewRect union(ViewRect other) {
        return new ViewRect(Math.min(minX, other.minX), Math.min(minY, other.minY),
                Math.max(maxX, other.maxX), Math.max(maxY, other.maxY));
    }
}
