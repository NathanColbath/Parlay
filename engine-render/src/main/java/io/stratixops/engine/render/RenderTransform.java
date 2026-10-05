package io.stratixops.engine.render;

import io.stratixops.engine.math.Vec2;

/**
 * Builds the world-to-clip transform for the 2D renderer.
 *
 * <p>Coordinate model (frozen by the API contract): world origin is top-left,
 * {@code +x} is right, {@code +y} is down, units are logical pixels. The camera
 * position is the world point shown at the center of the logical viewport.
 * Clip space is the usual OpenGL NDC ({@code -1..1}, {@code +y} up).
 *
 * <p>This class is pure so the camera mapping can be unit-tested headless; the
 * renderer feeds the matrix to the vertex shader.
 */
public final class RenderTransform {

    private RenderTransform() {
    }

    /**
     * Writes a column-major 4x4 orthographic matrix mapping world space to
     * clip space.
     *
     * @param cameraX  camera center x in world pixels
     * @param cameraY  camera center y in world pixels
     * @param viewportW logical viewport width in pixels
     * @param viewportH logical viewport height in pixels
     * @param out       destination, at least 16 elements (column-major)
     */
    public static void ortho(float cameraX, float cameraY, float viewportW, float viewportH, float[] out) {
        // World x in [cameraX - w/2, cameraX + w/2] -> NDC [-1, 1].
        float sx = 2f / viewportW;
        float tx = -2f * cameraX / viewportW;
        // World y is down, NDC y is up: world in [cameraY - h/2, ...] -> [1, -1].
        float sy = -2f / viewportH;
        float ty = 2f * cameraY / viewportH;

        out[0] = sx;
        out[1] = 0f;
        out[2] = 0f;
        out[3] = 0f;

        out[4] = 0f;
        out[5] = sy;
        out[6] = 0f;
        out[7] = 0f;

        out[8] = 0f;
        out[9] = 0f;
        out[10] = -1f;
        out[11] = 0f;

        out[12] = tx;
        out[13] = ty;
        out[14] = 0f;
        out[15] = 1f;
    }

    /**
     * Maps a world point to normalized device coordinates. Pure convenience used
     * by tests and picking; the GPU path uses the matrix instead.
     *
     * @param worldX   world x in pixels
     * @param worldY   world y in pixels
     * @param cameraX  camera center x in world pixels
     * @param cameraY  camera center y in world pixels
     * @param viewportW logical viewport width
     * @param viewportH logical viewport height
     * @param out      destination vector, set to the NDC x/y
     */
    public static void worldToNdc(float worldX, float worldY, float cameraX, float cameraY,
                                  float viewportW, float viewportH, Vec2 out) {
        float ndcX = (worldX - cameraX) * (2f / viewportW);
        float ndcY = -(worldY - cameraY) * (2f / viewportH);
        out.set(ndcX, ndcY);
    }
}
