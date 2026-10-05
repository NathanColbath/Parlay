package io.stratixops.engine.imgui.glue;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import io.stratixops.engine.imgui.theme.Theme;

/**
 * Owns the ImGui frame lifecycle against the shared GL window/context
 * (ADR-0005 sections 3 and 4). Call order per frame:
 *
 * <pre>
 * layer.newFrame();   // glfw.newFrame + ImGui.newFrame
 * ... build panels ...
 * layer.render();     // ImGui.render + backend draw
 * </pre>
 *
 * <p>ImGui runs as the final overlay pass on the same context that
 * engine-render owns. This class is GL/native glue and is excluded from the
 * headless coverage gate; the headless path lives in {@code HeadlessUi}.
 */
public final class ImGuiLayer implements AutoCloseable {

    private final ImGuiImplGlfw glfwBackend = new ImGuiImplGlfw();
    private final ImGuiImplGl3 gl3Backend = new ImGuiImplGl3();
    private Theme theme;
    private boolean initialized;

    /**
     * Initializes ImGui and both backends for an existing GLFW window.
     *
     * @param glfwWindowHandle native GLFW window handle from engine-render
     * @param theme            the active theme (dark by default)
     * @param dpiScale         monitor content scale (1.0/1.25/1.5/2.0)
     */
    public void init(long glfwWindowHandle, Theme theme, float dpiScale) {
        this.theme = theme;

        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);

        FontAtlas.load(io.getFonts(), theme, dpiScale);
        ImGuiStyleMapper.apply(theme, ImGui.getStyle());

        glfwBackend.init(glfwWindowHandle, true);
        gl3Backend.init("#version 330 core");
        initialized = true;
    }

    /** Swaps the theme's style in place (dark/light toggle). */
    public void setTheme(Theme theme) {
        this.theme = theme;
        ImGuiStyleMapper.apply(theme, ImGui.getStyle());
    }

    public Theme theme() {
        return theme;
    }

    /** Starts a frame: polls GLFW and begins ImGui. */
    public void newFrame() {
        glfwBackend.newFrame();
        ImGui.newFrame();
    }

    /** Ends a frame: renders ImGui and issues the backend draw pass. */
    public void render() {
        ImGui.render();
        gl3Backend.renderDrawData(ImGui.getDrawData());
    }

    @Override
    public void close() {
        if (!initialized) {
            return;
        }
        gl3Backend.shutdown();
        glfwBackend.shutdown();
        ImGui.destroyContext();
        initialized = false;
    }
}
