package io.stratixops.engine.render.platform;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_DEPTH_BITS;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_SAMPLES;
import static org.lwjgl.glfw.GLFW.GLFW_STENCIL_BITS;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.system.MemoryStack.stackPush;

import io.stratixops.engine.render.WindowContext;
import java.nio.IntBuffer;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

/**
 * A GLFW 3 window with an OpenGL 3.3 core context.
 *
 * <p>Creation makes the context current and initializes LWJGL's GL bindings, so
 * {@link io.stratixops.engine.render.DesktopAssets} and
 * {@link io.stratixops.engine.render.GlRenderer} can be constructed immediately
 * afterwards. Mouse callbacks, gamepads and multi-window support are out of
 * scope for the MVP.
 */
public final class GlfwWindow implements WindowContext, AutoCloseable {

    private final long handle;
    private final GLFWErrorCallback errorCallback;
    private final GLFWFramebufferSizeCallbackI framebufferCallback;
    private int width;
    private int height;
    private boolean closed;

    /**
     * Creates and shows a window.
     *
     * @param title     window title
     * @param width     initial framebuffer width, positive
     * @param height    initial framebuffer height, positive
     * @param resizable whether the user may resize the window
     * @param vsync     whether to enable vertical sync
     */
    public GlfwWindow(String title, int width, int height, boolean resizable, boolean vsync) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("window size must be positive but was " + width + "x" + height);
        }
        this.errorCallback = GLFWErrorCallback.createPrint(System.err);
        errorCallback.set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, 0);
        glfwWindowHint(GLFW_DEPTH_BITS, 0);
        glfwWindowHint(GLFW_STENCIL_BITS, 0);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE);

        handle = glfwCreateWindow(width, height, title, 0L, 0L);
        if (handle == 0L) {
            glfwTerminate();
            throw new IllegalStateException("Unable to create the GLFW window (OpenGL 3.3 core may be unsupported)");
        }

        glfwMakeContextCurrent(handle);
        glfwSwapInterval(vsync ? 1 : 0);
        GL.createCapabilities();

        try (MemoryStack stack = stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            glfwGetFramebufferSize(handle, w, h);
            this.width = w.get(0);
            this.height = h.get(0);
        }

        framebufferCallback = (window, newWidth, newHeight) -> {
            this.width = newWidth;
            this.height = newHeight;
        };
        glfwSetFramebufferSizeCallback(handle, framebufferCallback);
        glfwShowWindow(handle);
    }

    /** @return the native GLFW window handle */
    @Override
    public long handle() {
        return handle;
    }

    /** @return the current framebuffer width in pixels */
    @Override
    public int width() {
        return width;
    }

    /** @return the current framebuffer height in pixels */
    @Override
    public int height() {
        return height;
    }

    /** @return true once the user has requested the window close */
    @Override
    public boolean shouldClose() {
        return glfwWindowShouldClose(handle);
    }

    /** Requests closure on the next {@link #shouldClose()} check. */
    @Override
    public void requestClose() {
        glfwSetWindowShouldClose(handle, true);
    }

    /** Processes pending window/input events. Call once per frame. */
    @Override
    public void pollEvents() {
        glfwPollEvents();
    }

    /** Swaps the front and back buffers. */
    @Override
    public void swapBuffers() {
        glfwSwapBuffers(handle);
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        glfwFreeCallbacks(handle);
        glfwDestroyWindow(handle);
        glfwTerminate();
        errorCallback.free();
    }
}
