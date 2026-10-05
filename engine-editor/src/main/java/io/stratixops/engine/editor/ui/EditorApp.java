package io.stratixops.engine.editor.ui;

import io.stratixops.engine.editor.DockLayout;
import io.stratixops.engine.editor.EditorState;
import io.stratixops.engine.editor.gl.GlViewportRenderer;
import io.stratixops.engine.editor.view.ViewportRenderer;
import io.stratixops.engine.imgui.glue.ImGuiLayer;
import io.stratixops.engine.render.DesktopAssets;
import io.stratixops.engine.render.GlRenderer;
import io.stratixops.engine.render.WindowContext;
import io.stratixops.engine.render.platform.GlfwWindow;
import io.stratixops.engine.scene.Scene;
import io.stratixops.engine.serialize.SceneIO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The editor application host (STR-40): owns the shared GLFW/OpenGL window, the
 * ImGui layer bound to that same context (ADR-0005 section 3), and the editor
 * frame loop. The frame order is {@code pollEvents -> newFrame -> panels ->
 * render -> swap}, with ImGui as the final overlay pass.
 *
 * <p>It is constructible with any {@link WindowContext}, so tests can drive the
 * UI headlessly through an ImGui mock; the GL layer stays in
 * {@link ImGuiLayer} and is excluded from the headless coverage gate.
 */
public final class EditorApp implements AutoCloseable {

    private static final float FIXED_FRAME_SECONDS = 1f / 60f;

    private final EditorState state;
    private final ThemeProvider themes = new ThemeProvider();
    private final EditorLayoutStore layouts;
    private final WindowContext window;
    private final ViewportRenderer viewport;
    private final ImGuiLayer layer = new ImGuiLayer();
    private final EditorUi ui;
    private boolean running;

    /**
     * @param state      the headless editor state
     * @param window     the shared window/context
     * @param layoutFile where to persist the docking layout, or {@code null}
     */
    public EditorApp(EditorState state, WindowContext window, Path layoutFile) {
        this(state, window, layoutFile, null);
    }

    /**
     * @param state      the headless editor state
     * @param window     the shared window/context
     * @param layoutFile where to persist the docking layout, or {@code null}
     * @param viewport   the runtime Game-view target, or {@code null} headless
     */
    public EditorApp(EditorState state, WindowContext window, Path layoutFile,
                     ViewportRenderer viewport) {
        this.state = state;
        this.window = window;
        this.viewport = viewport;
        this.layouts = new EditorLayoutStore(layoutFile);
        this.ui = new EditorUi(state, themes, layouts, viewport);
        state.setLayout(layouts.load(state.layout()));
    }

    /** Runs the editor loop until the window closes or a quit is requested. */
    public void run() {
        layer.init(window.handle(), ui.context().theme(), 1f);
        ui.context().onQuit = this::requestStop;
        ui.context().onThemeChanged = () -> layer.setTheme(ui.context().theme());
        ui.context().onSaveScene = this::saveScene;
        ui.context().onOpenScene = this::openScene;
        state.console().info("StratixOps editor started (layout "
                + (layouts.file() == null ? "disabled" : "persisted") + ")");

        running = true;
        while (running && !window.shouldClose()) {
            window.pollEvents();
            state.play().update(FIXED_FRAME_SECONDS);
            renderRuntime();
            layer.newFrame();
            ui.render();
            layer.render();
            window.swapBuffers();
        }
        close();
    }

    private void renderRuntime() {
        if (viewport == null) {
            return;
        }
        Scene runtime = state.play().runtimeScene();
        if (runtime != null) {
            viewport.renderRuntime(runtime);
        }
    }

    /** Requests a clean shutdown at the end of the current frame. */
    public void requestStop() {
        running = false;
    }

    /** @return the shared editor context. */
    EditorContext context() {
        return ui.context();
    }

    private void saveScene() {
        Path root = state.projectRoot();
        if (root == null) {
            state.console().warn("No project root set; scene was not saved");
            return;
        }
        Path directory = root.resolve("scenes");
        Path file = directory.resolve(state.scene().name() + ".scene");
        try {
            Files.createDirectories(directory);
            new SceneIO(state.registry()).save(state.scene(), file);
            state.markClean();
            state.console().info("Saved " + file);
        } catch (IOException | RuntimeException e) {
            state.console().error("Could not save " + file, e);
        }
    }

    private void openScene() {
        Path root = state.projectRoot();
        if (root == null) {
            state.console().warn("No project root set; scene was not opened");
            return;
        }
        Path file = root.resolve("scenes").resolve(state.scene().name() + ".scene");
        if (!Files.exists(file)) {
            state.console().warn("No saved scene at " + file);
            return;
        }
        try {
            state.openScene(file);
            state.console().info("Opened " + file);
        } catch (RuntimeException e) {
            state.console().error("Could not open " + file, e);
        }
    }

    @Override
    public void close() {
        layer.close();
    }

    /**
     * Launches the editor against a real GLFW window.
     *
     * @param args optional project root as the first argument
     */
    public static void main(String[] args) {
        Path root = Path.of(args.length > 0 ? args[0] : System.getProperty("user.dir", "."));
        var registry = io.stratixops.engine.inspect.ComponentRegistry.withBuiltins();
        EditorState state = new EditorState(registry);
        state.setProjectRoot(root);
        Path layoutFile = root.resolve(".stratixops").resolve("editor-layout.txt");
        try (GlfwWindow window = new GlfwWindow("StratixOps Editor", 1280, 800, true, true);
                DesktopAssets assets = new DesktopAssets();
                GlRenderer renderer = new GlRenderer(assets,
                        GlViewportRenderer.LOGICAL_WIDTH, GlViewportRenderer.LOGICAL_HEIGHT);
                GlViewportRenderer viewport = new GlViewportRenderer(renderer);
                EditorApp app = new EditorApp(state, window, layoutFile, viewport)) {
            app.run();
        }
    }
}
