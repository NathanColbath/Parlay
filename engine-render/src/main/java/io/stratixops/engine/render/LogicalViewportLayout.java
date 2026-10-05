package io.stratixops.engine.render;

/**
 * Computes the integer-scaled, centered, letterboxed destination rectangle used
 * to present the logical framebuffer in a physical window (AC1.2).
 *
 * <p>Integer scaling keeps pixel art crisp; the scale never drops below 1 so
 * the image is at least visible when the window is smaller than the logical
 * viewport. This is pure and headless-testable.
 */
public final class LogicalViewportLayout {

    private LogicalViewportLayout() {
    }

    /**
     * An integer output rectangle in window pixels.
     */
    public static final class Rect {

        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final int scale;

        Rect(int x, int y, int width, int height, int scale) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.scale = scale;
        }

        /** @return left edge in window pixels */
        public int x() {
            return x;
        }

        /** @return top edge in window pixels */
        public int y() {
            return y;
        }

        /** @return width in window pixels */
        public int width() {
            return width;
        }

        /** @return height in window pixels */
        public int height() {
            return height;
        }

        /** @return the integer scale factor applied to the logical viewport */
        public int scale() {
            return scale;
        }
    }

    /**
     * Computes the presentation rectangle.
     *
     * @param windowWidth   physical window width, positive
     * @param windowHeight  physical window height, positive
     * @param logicalWidth  logical viewport width, positive
     * @param logicalHeight logical viewport height, positive
     * @return the centered, integer-scaled destination rectangle
     */
    public static Rect compute(int windowWidth, int windowHeight, int logicalWidth, int logicalHeight) {
        if (windowWidth <= 0 || windowHeight <= 0) {
            throw new IllegalArgumentException(
                "window size must be positive but was " + windowWidth + "x" + windowHeight);
        }
        if (logicalWidth <= 0 || logicalHeight <= 0) {
            throw new IllegalArgumentException(
                "logical viewport must be positive but was " + logicalWidth + "x" + logicalHeight);
        }
        int scale = Math.min(windowWidth / logicalWidth, windowHeight / logicalHeight);
        scale = Math.max(scale, 1);
        int width = logicalWidth * scale;
        int height = logicalHeight * scale;
        int x = (windowWidth - width) / 2;
        int y = (windowHeight - height) / 2;
        return new Rect(x, y, width, height, scale);
    }
}
