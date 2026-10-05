package io.stratixops.engine.render;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parsed atlas metadata: the image path and the named pixel rectangles inside
 * it. Immutable and backend-neutral.
 */
public final class AtlasMetadata {

    private final String image;
    private final Map<String, Rect> regions;

    /**
     * Creates metadata.
     *
     * @param image   atlas image path, relative to the metadata file
     * @param regions named rectangles
     */
    public AtlasMetadata(String image, Map<String, Rect> regions) {
        this.image = image;
        this.regions = Collections.unmodifiableMap(new LinkedHashMap<>(regions));
    }

    /** @return the atlas image path */
    public String image() {
        return image;
    }

    /** @return the named region rectangles, unmodifiable */
    public Map<String, Rect> regions() {
        return regions;
    }

    /**
     * Resolves a named rectangle against the decoded image size, producing
     * texture coordinates with half-texel insets.
     *
     * @param name        region name
     * @param imageWidth  decoded image width, pixels
     * @param imageHeight decoded image height, pixels
     * @return the resolved region, or {@code null} if the name is unknown
     */
    public AtlasRegion resolve(String name, int imageWidth, int imageHeight) {
        Rect rect = regions.get(name);
        if (rect == null) {
            return null;
        }
        return new AtlasRegion(rect.x, rect.y, rect.width, rect.height, imageWidth, imageHeight);
    }

    /** A pixel rectangle inside the atlas image. */
    public static final class Rect {

        private final int x;
        private final int y;
        private final int width;
        private final int height;

        /**
         * Creates a rectangle.
         *
         * @param x      left edge, pixels
         * @param y      top edge, pixels
         * @param width  width, pixels
         * @param height height, pixels
         */
        public Rect(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        /** @return left edge in pixels */
        public int x() {
            return x;
        }

        /** @return top edge in pixels */
        public int y() {
            return y;
        }

        /** @return width in pixels */
        public int width() {
            return width;
        }

        /** @return height in pixels */
        public int height() {
            return height;
        }
    }
}
