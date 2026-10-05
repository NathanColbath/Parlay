package io.stratixops.engine.render;

/**
 * Conversions for the engine's packed {@code 0xRRGGBBAA} color format. Pure and
 * headless-testable.
 */
public final class Colors {

    /** Opaque white, used as the neutral vertex color for textured draws. */
    public static final int WHITE = 0xFFFFFFFF;

    private Colors() {
    }

    /**
     * Unpacks a packed {@code 0xRRGGBBAA} color into normalized floats.
     *
     * @param rgba packed color
     * @param out  destination array, must hold at least four elements
     */
    public static void unpack(int rgba, float[] out) {
        out[0] = ((rgba >>> 24) & 0xFF) / 255f;
        out[1] = ((rgba >>> 16) & 0xFF) / 255f;
        out[2] = ((rgba >>> 8) & 0xFF) / 255f;
        out[3] = (rgba & 0xFF) / 255f;
    }

    /**
     * Unpacks a packed {@code 0xRRGGBBAA} color into a new four-element array.
     *
     * @param rgba packed color
     * @return {@code {r, g, b, a}} in {@code 0..1}
     */
    public static float[] unpack(int rgba) {
        float[] out = new float[4];
        unpack(rgba, out);
        return out;
    }
}
