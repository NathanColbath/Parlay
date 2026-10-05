package io.stratixops.engine.render;

/**
 * Computes how a fixed-size {@link RenderTarget} should be scaled and centered
 * inside an available rectangle (for example an ImGui panel's content region)
 * while preserving its aspect ratio, so the presented image is never distorted
 * (ADR-0005 §4).
 *
 * <p>Pure and headless-testable; no GL context is required.
 */
public final class RenderTargetLayout {

    private RenderTargetLayout() {
    }

    /**
     * The centered, aspect-preserving placement of a target inside an available
     * rectangle, in the available rectangle's units (typically ImGui points).
     */
    public static final class Fit {

        private final float x;
        private final float y;
        private final float width;
        private final float height;
        private final float scale;

        Fit(float x, float y, float width, float height, float scale) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.scale = scale;
        }

        /** @return left edge inside the available rectangle */
        public float x() {
            return x;
        }

        /** @return top edge inside the available rectangle */
        public float y() {
            return y;
        }

        /** @return presented width */
        public float width() {
            return width;
        }

        /** @return presented height */
        public float height() {
            return height;
        }

        /** @return the uniform scale applied to the target (0 when no room) */
        public float scale() {
            return scale;
        }
    }

    /**
     * Computes the largest uniform scale at which a target fits inside an
     * available rectangle, centered, with the aspect ratio preserved.
     *
     * @param targetWidth     target width in pixels, positive
     * @param targetHeight    target height in pixels, positive
     * @param availableWidth  available width; non-positive yields an empty fit
     * @param availableHeight available height; non-positive yields an empty fit
     * @return the centered placement
     */
    public static Fit fit(int targetWidth, int targetHeight, float availableWidth, float availableHeight) {
        if (targetWidth <= 0 || targetHeight <= 0) {
            throw new IllegalArgumentException(
                "render target size must be positive but was " + targetWidth + "x" + targetHeight);
        }
        if (availableWidth <= 0f || availableHeight <= 0f) {
            return new Fit(0f, 0f, 0f, 0f, 0f);
        }
        float scale = Math.min(availableWidth / targetWidth, availableHeight / targetHeight);
        float width = targetWidth * scale;
        float height = targetHeight * scale;
        float x = (availableWidth - width) / 2f;
        float y = (availableHeight - height) / 2f;
        return new Fit(x, y, width, height, scale);
    }
}
