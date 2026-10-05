package io.stratixops.engine.editor.ui;

import io.stratixops.engine.editor.DockLayout;
import io.stratixops.engine.editor.EditorState;
import io.stratixops.engine.imgui.glue.ImGuiLayer;
import io.stratixops.engine.render.WindowContext;
import io.stratixops.engine.render.platform.GlfwWindow;
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
    private final ImGuiLayer layer = new ImGuiLayer();
    private final EditorUi ui;
    private boolean running;

    /**
     * @param state      the headless editor state
     * @param window     the shared window/context
     * @param layoutFile where to persist the docking layout, or {@code null}
     */
    public EditorApp(EditorState state, WindowContext window, Path layoutFile) {
        this.state = state;
        this.window = window;
        this.layouts = new EditorLayoutStore(layoutFile);
        this.ui = new EditorUi(state, themes, layouts);
        state.setLayout(layouts.load(state.layout()));
    }

    /** Runs the editor loop until the window closes or a quit is requested. */
    public void run() {
        layer.init(window.handle(), ui.context().theme(), 1f);
        ui.context().onQuit = this::requestStop;
        ui.context().onThemeChanged = () -> layer.setTheme(ui.context().theme());
        ui.context().onSaveScene = this::saveScene;
        state.console().info("StratixOps editor started (layout "
                + (layouts.file() == null ? "disabled" : "persisted") + ")");

        running = true;
        while (running && !window.shouldClose()) {
            window.pollEvents();
            state.play().update(FIXED_FRAME_SECONDS);
            layer.newFrame();
            ui.render();
            layer.render();
            window.swapBuffers();
        }
        close();
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
                EditorApp app = new EditorApp(state, window, layoutFile)) {
            app.run();
        }
    }
}
