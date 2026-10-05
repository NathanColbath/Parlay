package io.stratixops.engine.render;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

import io.stratixops.engine.Camera2D;
import io.stratixops.engine.Renderer;
import io.stratixops.engine.Sprite;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * The OpenGL 3.3 immediate-mode {@link Renderer}.
 *
 * <p>The scene is drawn into a fixed logical framebuffer; {@link #present(int, int)}
 * integer-scales and letterboxes it into the window. Draw order is call order.
 * World draws are bracketed by {@link #pushCamera(Camera2D)}/{@link #popCamera()};
 * after a pop, draws are screen-space (logical-viewport pixels).
 *
 * <p>The runtime drives the frame: {@link #beginFrame()} before
 * {@code GameLifecycle.onRender}, then {@link #present(int, int)}.
 *
 * <p>The editor renders the scene into an offscreen {@link RenderTarget} created
 * with {@link #createRenderTarget(int, int)} and presents the resulting GL
 * texture through ImGui (ADR-0005). The default logical viewport and every
 * render target share this renderer's single shader, batch and camera stack, so
 * there is exactly one submission path (ADR-0008).
 */
public final class GlRenderer implements Renderer, AutoCloseable {

    private static final int MAX_QUADS = 8192;

    private static final String VERTEX_SHADER =
        "#version 330 core\n"
            + "layout(location = 0) in vec2 aPosition;\n"
            + "layout(location = 1) in vec2 aTexCoord;\n"
            + "layout(location = 2) in vec4 aColor;\n"
            + "uniform mat4 uProjection;\n"
            + "out vec2 vTexCoord;\n"
            + "out vec4 vColor;\n"
            + "void main() {\n"
            + "    gl_Position = uProjection * vec4(aPosition, 0.0, 1.0);\n"
            + "    vTexCoord = aTexCoord;\n"
            + "    vColor = aColor;\n"
            + "}\n";

    private static final String FRAGMENT_SHADER =
        "#version 330 core\n"
            + "in vec2 vTexCoord;\n"
            + "in vec4 vColor;\n"
            + "uniform sampler2D uTexture;\n"
            + "out vec4 fragColor;\n"
            + "void main() {\n"
            + "    fragColor = texture(uTexture, vTexCoord) * vColor;\n"
            + "}\n";

    private final int logicalWidth;
    private final int logicalHeight;
    private final BitmapFont font;
    private final ShaderProgram shader;
    private final SpriteBatch batch;
    private final GlTexture white;
    private final LogicalViewport viewport;
    private final Deque<Camera2D> cameras = new ArrayDeque<>();
    private final float[] projection = new float[16];
    private final float[] clearColor = new float[4];
    private int activeWidth;
    private int activeHeight;

    /**
     * Creates the renderer. Requires a current GL 3.3 core context.
     *
     * @param assets        the desktop asset cache (provides the built-in font)
     * @param logicalWidth  logical viewport width, e.g. 640
     * @param logicalHeight logical viewport height, e.g. 360
     */
    public GlRenderer(DesktopAssets assets, int logicalWidth, int logicalHeight) {
        this.logicalWidth = logicalWidth;
        this.logicalHeight = logicalHeight;
        this.font = assets.builtInFont();
        this.shader = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        this.batch = new SpriteBatch(MAX_QUADS, shader);
        this.white = GlTexture.white();
        this.viewport = new LogicalViewport(logicalWidth, logicalHeight);
    }

    /** @return the logical viewport width this renderer was created with */
    public int logicalWidth() {
        return logicalWidth;
    }

    /** @return the logical viewport height this renderer was created with */
    public int logicalHeight() {
        return logicalHeight;
    }

    /**
     * Creates an offscreen render target that draws through this renderer. Use
     * {@code target.begin()}/{@code target.end()} around scene rendering, then
     * present {@code target.textureId()} with ImGui. Requires a current GL
     * context.
     *
     * @param width  target width in pixels, positive
     * @param height target height in pixels, positive
     * @return the new render target
     */
    public RenderTarget createRenderTarget(int width, int height) {
        return new RenderTarget(this, width, height);
    }

    /**
     * Starts a frame: binds the logical framebuffer, sets blend state, and
     * resets the camera stack to screen space.
     */
    public void beginFrame() {
        viewport.bind();
        beginPass(logicalWidth, logicalHeight);
    }

    /**
     * Finishes the frame and presents it to the window.
     *
     * @param windowWidth  physical window width
     * @param windowHeight physical window height
     */
    public void present(int windowWidth, int windowHeight) {
        batch.end();
        viewport.present(windowWidth, windowHeight);
    }

    /**
     * Binds an offscreen render target and resets the pass state. Called by
     * {@link RenderTarget#begin()}.
     *
     * @param target the target to draw into
     */
    void beginTarget(RenderTarget target) {
        target.framebuffer().bind();
        beginPass(target.width(), target.height());
    }

    /**
     * Flushes and unbinds the current offscreen render target. Called by
     * {@link RenderTarget#end()}.
     */
    void endTarget() {
        batch.end();
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private void beginPass(int viewportWidth, int viewportHeight) {
        activeWidth = viewportWidth;
        activeHeight = viewportHeight;
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);
        cameras.clear();
        screenProjection();
        batch.begin(projection);
    }

    @Override
    public void clear(int rgba) {
        Colors.unpack(rgba, clearColor);
        glClearColor(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);
        glClear(GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void draw(Sprite sprite, float x, float y) {
        if (sprite instanceof GlSprite glSprite) {
            batch.drawSprite(glSprite.texture().id(), x, y, glSprite.width(), glSprite.height(),
                glSprite.u0(), glSprite.v0(), glSprite.u1(), glSprite.v1());
        }
    }

    @Override
    public void fillRect(float x, float y, float w, float h, int rgba) {
        batch.fillRect(white.id(), x, y, w, h, rgba);
    }

    @Override
    public void drawText(String text, float x, float y) {
        font.draw(text, x, y, batch, Colors.WHITE);
    }

    @Override
    public void pushCamera(Camera2D camera) {
        batch.flush();
        cameras.push(camera);
        updateProjection();
    }

    @Override
    public void popCamera() {
        batch.flush();
        if (!cameras.isEmpty()) {
            cameras.pop();
        }
        updateProjection();
    }

    private void screenProjection() {
        RenderTransform.ortho(activeWidth / 2f, activeHeight / 2f, activeWidth, activeHeight, projection);
    }

    private void updateProjection() {
        if (cameras.isEmpty()) {
            screenProjection();
        } else {
            Camera2D camera = cameras.peek();
            RenderTransform.ortho(camera.position().x, camera.position().y,
                activeWidth, activeHeight, projection);
        }
        batch.setProjection(projection);
    }

    /**
     * @return the number of GL draw calls issued in the last frame (flush count)
     */
    public int drawCallsLastFrame() {
        return batch.drawCalls();
    }

    @Override
    public void close() {
        batch.close();
        shader.dispose();
        white.close();
        viewport.close();
    }
}
