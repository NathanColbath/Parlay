package io.stratixops.engine.render;

/**
 * The shared desktop window and its OpenGL context.
 *
 * <p>Owned by {@code engine-render} (ADR-0004) and exposed so {@code engine-imgui}
 * can bind the Dear ImGui GLFW/OpenGL3 backends to the <em>same</em> window and
 * context the scene renderer draws into, rather than creating a second one
 * (ADR-0005). This is a rendering concern; no ImGui, scene or platform type
 * appears here.
 *
 * <p>The native window handle is deliberately opaque: it is a {@code long} so a
 * consumer can pass it to a native binding without this module leaking the
 * binding's type into its API.
 */
public interface WindowContext {

    /**
     * @return the opaque native window handle (a GLFW {@code GLFWwindow*}),
     *         valid while the context is open
     */
    long handle();

    /** @return the current framebuffer width in pixels */
    int width();

    /** @return the current framebuffer height in pixels */
    int height();

    /** @return true once the user has requested the window close */
    boolean shouldClose();

    /** Requests closure on the next {@link #shouldClose()} check. */
    void requestClose();

    /** Processes pending window and input events. Call once per frame. */
    void pollEvents();

    /** Swaps the front and back buffers. */
    void swapBuffers();
}
