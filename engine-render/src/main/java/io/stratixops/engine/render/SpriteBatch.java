package io.stratixops.engine.render;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;

/**
 * Accumulates quads into reusable GL buffers and flushes them with one draw
 * call per texture. The public draw order is the call order (F3 / AC3.1); a
 * flush is forced only when the texture changes, the batch is full, or the frame
 * ends (F3 / AC3.2).
 *
 * <p>The geometry rules live in {@link QuadBatch} (headless-testable); this
 * class is the thin GL wrapper.
 */
final class SpriteBatch implements AutoCloseable {

    private static final int STRIDE_FLOATS = QuadBatch.FLOATS_PER_VERTEX;
    private static final int BYTES_PER_FLOAT = Float.BYTES;

    private final QuadBatch batch;
    private final ShaderProgram shader;
    private final int projectionLocation;
    private final int samplerLocation;
    private final int vao;
    private final int vbo;
    private final int ebo;
    private final FloatBuffer vertexUpload;
    private final IntBuffer indexUpload;
    private final float[] projection = new float[16];
    private final float[] xy = new float[8];
    private final float[] uv = new float[8];
    private int currentTexture;
    private int drawCalls;

    SpriteBatch(int maxQuads, ShaderProgram shader) {
        this.batch = new QuadBatch(maxQuads);
        this.shader = shader;
        this.projectionLocation = shader.uniformLocation("uProjection");
        this.samplerLocation = shader.uniformLocation("uTexture");
        this.vertexUpload = BufferUtils.createFloatBuffer(batch.vertexArray().length);
        this.indexUpload = BufferUtils.createIntBuffer(batch.indexArray().length);

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, (long) batch.vertexArray().length * BYTES_PER_FLOAT, GL_DYNAMIC_DRAW);

        ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, (long) batch.indexArray().length * Integer.BYTES, GL_DYNAMIC_DRAW);

        int stride = STRIDE_FLOATS * BYTES_PER_FLOAT;
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0L);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 2L * BYTES_PER_FLOAT);
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 4, GL_FLOAT, false, stride, 4L * BYTES_PER_FLOAT);

        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    /**
     * Starts a frame with the given world-to-clip matrix.
     *
     * @param projectionMatrix column-major 4x4 matrix
     */
    void begin(float[] projectionMatrix) {
        System.arraycopy(projectionMatrix, 0, projection, 0, 16);
        batch.reset();
        currentTexture = 0;
        drawCalls = 0;
    }

    /**
     * Sets the active projection without clearing buffered geometry (used when
     * the camera changes mid-frame; the caller has already flushed).
     *
     * @param projectionMatrix column-major 4x4 matrix
     */
    void setProjection(float[] projectionMatrix) {
        System.arraycopy(projectionMatrix, 0, projection, 0, 16);
    }

    /**
     * Queues a textured quad centered on {@code (x, y)}.
     *
     * @param textureId GL texture handle
     * @param x         center x in the current space
     * @param y         center y in the current space
     * @param width     quad width in pixels
     * @param height    quad height in pixels
     * @param u0        left texture coordinate
     * @param v0        top texture coordinate
     * @param u1        right texture coordinate
     * @param v1        bottom texture coordinate
     */
    void drawSprite(int textureId, float x, float y, float width, float height,
                    float u0, float v0, float u1, float v1) {
        float left = x - width / 2f;
        float right = x + width / 2f;
        float top = y - height / 2f;
        float bottom = y + height / 2f;
        setQuad(left, top, right, bottom, u0, v0, u1, v1);
        add(textureId, Colors.WHITE);
    }

    /**
     * Queues an untextured solid rectangle (uses the supplied 1x1 white
     * texture).
     *
     * @param whiteTextureId GL handle of a 1x1 opaque white texture
     * @param x              left edge
     * @param y              top edge
     * @param width          width
     * @param height         height
     * @param rgba           packed fill color
     */
    void fillRect(int whiteTextureId, float x, float y, float width, float height, int rgba) {
        setQuad(x, y, x + width, y + height, 0.5f, 0.5f, 0.5f, 0.5f);
        add(whiteTextureId, rgba);
    }

    /**
     * Queues a glyph quad from a texture atlas region at a top-left origin.
     *
     * @param textureId GL texture handle
     * @param x         left edge
     * @param y         top edge
     * @param width     glyph width
     * @param height    glyph height
     * @param u0        left texture coordinate
     * @param v0        top texture coordinate
     * @param u1        right texture coordinate
     * @param v1        bottom texture coordinate
     * @param rgba      packed color
     */
    void drawGlyph(int textureId, float x, float y, float width, float height,
                   float u0, float v0, float u1, float v1, int rgba) {
        setQuad(x, y, x + width, y + height, u0, v0, u1, v1);
        add(textureId, rgba);
    }

    /**
     * Ends the frame: flushes any buffered geometry.
     */
    void end() {
        flush();
    }

    /**
     * Uploads and draws the buffered quads, resetting the batch. No-op if empty.
     */
    void flush() {
        int vertexCount = batch.vertexCount();
        if (vertexCount == 0) {
            return;
        }
        int indexCount = batch.indexCount();

        vertexUpload.clear();
        vertexUpload.put(batch.vertexArray(), 0, vertexCount * STRIDE_FLOATS);
        vertexUpload.flip();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0L, vertexUpload);

        indexUpload.clear();
        indexUpload.put(batch.indexArray(), 0, indexCount);
        indexUpload.flip();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0L, indexUpload);

        shader.bind();
        shader.setUniformMatrix4(projectionLocation, projection);
        shader.setUniformInt(samplerLocation, 0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, currentTexture);

        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0L);
        glBindVertexArray(0);

        drawCalls++;
        batch.reset();
    }

    /** @return the number of GL draw calls issued since {@link #begin(float[])} */
    int drawCalls() {
        return drawCalls;
    }

    private void add(int textureId, int rgba) {
        if (textureId != currentTexture) {
            flush();
            currentTexture = textureId;
        }
        if (!batch.addQuad(xy, uv, rgba)) {
            flush();
            batch.addQuad(xy, uv, rgba);
        }
    }

    private void setQuad(float left, float top, float right, float bottom,
                         float u0, float v0, float u1, float v1) {
        xy[0] = left;
        xy[1] = top;
        xy[2] = right;
        xy[3] = top;
        xy[4] = right;
        xy[5] = bottom;
        xy[6] = left;
        xy[7] = bottom;

        uv[0] = u0;
        uv[1] = v0;
        uv[2] = u1;
        uv[3] = v0;
        uv[4] = u1;
        uv[5] = v1;
        uv[6] = u0;
        uv[7] = v1;
    }

    @Override
    public void close() {
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        glDeleteVertexArrays(vao);
    }
}
