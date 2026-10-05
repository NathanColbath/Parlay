package io.stratixops.engine.render;

/**
 * CPU-side accumulation of textured/colored quads into an interleaved vertex
 * array and a static index pattern.
 *
 * <p>Layout per vertex: {@code x, y, u, v, r, g, b, a} (8 floats). Corners are
 * emitted in order top-left, top-right, bottom-right, bottom-left; the index
 * pattern {@code 0,1,2, 0,2,3} forms the two triangles. Draw order is preserved:
 * quads appear in the buffer in the order they were added.
 *
 * <p>This class performs no GL calls and does no allocation after construction,
 * so the batching rules can be unit-tested headless.
 */
public final class QuadBatch {

    /** Floats per vertex: x, y, u, v, r, g, b, a. */
    public static final int FLOATS_PER_VERTEX = 8;
    /** Vertices per quad. */
    public static final int VERTICES_PER_QUAD = 4;
    /** Indices per quad (two triangles). */
    public static final int INDICES_PER_QUAD = 6;

    private final float[] vertices;
    private final int[] indices;
    private int quadCount;

    /**
     * Creates a batch with a fixed quad capacity.
     *
     * @param maxQuads maximum quads held before the caller must flush; must be
     *                 positive
     */
    public QuadBatch(int maxQuads) {
        if (maxQuads <= 0) {
            throw new IllegalArgumentException("maxQuads must be > 0 but was " + maxQuads);
        }
        this.vertices = new float[maxQuads * VERTICES_PER_QUAD * FLOATS_PER_VERTEX];
        this.indices = new int[maxQuads * INDICES_PER_QUAD];
        for (int q = 0; q < maxQuads; q++) {
            int base = q * VERTICES_PER_QUAD;
            int i = q * INDICES_PER_QUAD;
            indices[i] = base;
            indices[i + 1] = base + 1;
            indices[i + 2] = base + 2;
            indices[i + 3] = base;
            indices[i + 4] = base + 2;
            indices[i + 5] = base + 3;
        }
    }

    /**
     * Appends a colored quad.
     *
     * @param xy   eight floats: {@code x0,y0, x1,y1, x2,y2, x3,y3}
     * @param uv   eight floats: {@code u0,v0, u1,v1, u2,v2, u3,v3}
     * @param rgba packed {@code 0xRRGGBBAA} vertex color
     * @return {@code true} if added, {@code false} if the batch is full (caller
     *         should flush and retry)
     */
    public boolean addQuad(float[] xy, float[] uv, int rgba) {
        if (quadCount >= capacityQuads()) {
            return false;
        }
        float r = ((rgba >>> 24) & 0xFF) / 255f;
        float g = ((rgba >>> 16) & 0xFF) / 255f;
        float b = ((rgba >>> 8) & 0xFF) / 255f;
        float a = (rgba & 0xFF) / 255f;
        int v = quadCount * VERTICES_PER_QUAD * FLOATS_PER_VERTEX;
        for (int corner = 0; corner < VERTICES_PER_QUAD; corner++) {
            int c = corner * 2;
            vertices[v] = xy[c];
            vertices[v + 1] = xy[c + 1];
            vertices[v + 2] = uv[c];
            vertices[v + 3] = uv[c + 1];
            vertices[v + 4] = r;
            vertices[v + 5] = g;
            vertices[v + 6] = b;
            vertices[v + 7] = a;
            v += FLOATS_PER_VERTEX;
        }
        quadCount++;
        return true;
    }

    /**
     * @return the maximum number of quads this batch can hold
     */
    public int capacityQuads() {
        return vertices.length / (VERTICES_PER_QUAD * FLOATS_PER_VERTEX);
    }

    /**
     * @return the number of quads currently accumulated
     */
    public int quadCount() {
        return quadCount;
    }

    /**
     * @return the number of vertices currently accumulated
     */
    public int vertexCount() {
        return quadCount * VERTICES_PER_QUAD;
    }

    /**
     * @return the number of indices currently accumulated
     */
    public int indexCount() {
        return quadCount * INDICES_PER_QUAD;
    }

    /**
     * @return {@code true} if no quads are buffered
     */
    public boolean isEmpty() {
        return quadCount == 0;
    }

    /**
     * Resets the batch for the next flush. The backing arrays are reused.
     */
    public void reset() {
        quadCount = 0;
    }

    /**
     * Interleaved vertex data; only the first {@link #vertexCount()} vertices are
     * meaningful.
     *
     * @return the backing vertex array (reused)
     */
    public float[] vertexArray() {
        return vertices;
    }

    /**
     * Index data; only the first {@link #indexCount()} indices are meaningful.
     *
     * @return the backing index array (reused)
     */
    public int[] indexArray() {
        return indices;
    }
}
