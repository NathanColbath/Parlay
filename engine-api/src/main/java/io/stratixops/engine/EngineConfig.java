package io.stratixops.engine;

/**
 * Immutable engine configuration with sane defaults. Build it with
 * {@link #builder()}.
 *
 * <pre>{@code
 * EngineConfig.builder()
 *     .title("Hello Engine")
 *     .windowSize(1280, 720)       // physical window, default 1280x720
 *     .logicalViewport(640, 360)   // logical render target, default 640x360
 *     .build();
 * }</pre>
 *
 * <p>The logical viewport is rendered at its own fixed resolution and then
 * integer-scaled and letterboxed into the physical window, so pixel art stays
 * crisp and aspect-correct at any window size.
 */
public final class EngineConfig {

    /** Default physical window width. */
    public static final int DEFAULT_WINDOW_WIDTH = 1280;
    /** Default physical window height. */
    public static final int DEFAULT_WINDOW_HEIGHT = 720;
    /** Default logical viewport width. */
    public static final int DEFAULT_LOGICAL_WIDTH = 640;
    /** Default logical viewport height. */
    public static final int DEFAULT_LOGICAL_HEIGHT = 360;
    /** Default window title. */
    public static final String DEFAULT_TITLE = "Stratix Engine";

    private final String title;
    private final int windowWidth;
    private final int windowHeight;
    private final int logicalWidth;
    private final int logicalHeight;
    private final boolean resizable;
    private final boolean vsync;

    private EngineConfig(Builder builder) {
        this.title = builder.title;
        this.windowWidth = builder.windowWidth;
        this.windowHeight = builder.windowHeight;
        this.logicalWidth = builder.logicalWidth;
        this.logicalHeight = builder.logicalHeight;
        this.resizable = builder.resizable;
        this.vsync = builder.vsync;
    }

    /**
     * Window title.
     *
     * @return title
     */
    public String title() {
        return title;
    }

    /**
     * Physical window width in pixels.
     *
     * @return window width
     */
    public int windowWidth() {
        return windowWidth;
    }

    /**
     * Physical window height in pixels.
     *
     * @return window height
     */
    public int windowHeight() {
        return windowHeight;
    }

    /**
     * Logical viewport width in pixels.
     *
     * @return logical width
     */
    public int logicalWidth() {
        return logicalWidth;
    }

    /**
     * Logical viewport height in pixels.
     *
     * @return logical height
     */
    public int logicalHeight() {
        return logicalHeight;
    }

    /**
     * Whether the window is resizable.
     *
     * @return resizable flag
     */
    public boolean resizable() {
        return resizable;
    }

    /**
     * Whether vsync is enabled.
     *
     * @return vsync flag
     */
    public boolean vsync() {
        return vsync;
    }

    /**
     * Starts a configuration builder.
     *
     * @return a fresh builder pre-filled with defaults
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Fluent builder for {@link EngineConfig}. */
    public static final class Builder {

        private String title = DEFAULT_TITLE;
        private int windowWidth = DEFAULT_WINDOW_WIDTH;
        private int windowHeight = DEFAULT_WINDOW_HEIGHT;
        private int logicalWidth = DEFAULT_LOGICAL_WIDTH;
        private int logicalHeight = DEFAULT_LOGICAL_HEIGHT;
        private boolean resizable = true;
        private boolean vsync = true;

        private Builder() {
        }

        /**
         * Sets the window title.
         *
         * @param title window title
         * @return this builder
         */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /**
         * Sets the physical window size. This is independent of the logical
         * viewport; the viewport is integer-scaled to fit.
         *
         * @param width  window width in pixels
         * @param height window height in pixels
         * @return this builder
         */
        public Builder windowSize(int width, int height) {
            this.windowWidth = requirePositive("windowSize.width", width);
            this.windowHeight = requirePositive("windowSize.height", height);
            return this;
        }

        /**
         * Sets the fixed logical viewport (render target). Defaults to 640x360.
         *
         * @param width  logical width in pixels
         * @param height logical height in pixels
         * @return this builder
         */
        public Builder logicalViewport(int width, int height) {
            this.logicalWidth = requirePositive("logicalViewport.width", width);
            this.logicalHeight = requirePositive("logicalViewport.height", height);
            return this;
        }

        /**
         * Sets whether the window can be resized.
         *
         * @param resizable true to allow resizing
         * @return this builder
         */
        public Builder resizable(boolean resizable) {
            this.resizable = resizable;
            return this;
        }

        /**
         * Enables or disables vsync.
         *
         * @param vsync true to enable vsync
         * @return this builder
         */
        public Builder vsync(boolean vsync) {
            this.vsync = vsync;
            return this;
        }

        /**
         * Validates and builds the configuration.
         *
         * @return the immutable configuration
         * @throws IllegalArgumentException if a dimension is not positive
         */
        public EngineConfig build() {
            return new EngineConfig(this);
        }

        private static int requirePositive(String name, int value) {
            if (value <= 0) {
                throw new IllegalArgumentException(name + " must be > 0 but was " + value);
            }
            return value;
        }
    }
}
