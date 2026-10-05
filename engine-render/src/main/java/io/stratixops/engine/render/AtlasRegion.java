package io.stratixops.engine.render;

/**
 * A pixel rectangle inside a texture atlas plus the texture dimensions needed to
 * derive sample coordinates.
 *
 * <p>UV edges are inset by half a texel ({@link #INSET}) so that linear/nearest
 * sampling at region borders never bleeds a neighbouring sprite (AC3.3). This is
 * pure geometry and headless-testable.
 */
public final class AtlasRegion {

    /** Half-texel inset applied to each edge of a region. */
    public static final float INSET = 0.5f;

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int imageWidth;
    private final int imageHeight;

    /**
     * Creates a region.
     *
     * @param x           left edge in the atlas image, pixels
     * @param y           top edge in the atlas image, pixels
     * @param width       region width, pixels (positive)
     * @param height      region height, pixels (positive)
     * @param imageWidth  atlas image width, pixels (positive)
     * @param imageHeight atlas image height, pixels (positive)
     */
    public AtlasRegion(int x, int y, int width, int height, int imageWidth, int imageHeight) {
        if (imageWidth <= 0 || imageHeight <= 0) {
            throw new IllegalArgumentException(
                "atlas image size must be positive but was " + imageWidth + "x" + imageHeight);
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                "atlas region size must be positive but was " + width + "x" + height);
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    /** @return region left edge in atlas pixels */
    public int x() {
        return x;
    }

    /** @return region top edge in atlas pixels */
    public int y() {
        return y;
    }

    /** @return region width in pixels */
    public int width() {
        return width;
    }

    /** @return region height in pixels */
    public int height() {
        return height;
    }

    /** @return left texture coordinate, inset by half a texel */
    public float u0() {
        return (x + INSET) / imageWidth;
    }

    /** @return top texture coordinate, inset by half a texel */
    public float v0() {
        return (y + INSET) / imageHeight;
    }

    /** @return right texture coordinate, inset by half a texel */
    public float u1() {
        return (x + width - INSET) / imageWidth;
    }

    /** @return bottom texture coordinate, inset by half a texel */
    public float v1() {
        return (y + height - INSET) / imageHeight;
    }
}
