package io.stratixops.engine.editor.ui;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiKey;
import io.stratixops.engine.editor.DockLayout;
import io.stratixops.engine.editor.EditorState;
import io.stratixops.engine.editor.Focus;
import io.stratixops.engine.editor.PanelKind;
import io.stratixops.engine.editor.Shortcut;
import io.stratixops.engine.editor.ShortcutRouter;
import io.stratixops.engine.editor.command.EditCommands;
import io.stratixops.engine.editor.console.LogLevel;
import io.stratixops.engine.editor.play.TransportActions;
import io.stratixops.engine.editor.view.ViewportRenderer;
import io.stratixops.engine.scene.GameObject;

import java.nio.file.Path;

/**
 * Composes one editor frame (STR-40): the main menu, the dockspace, every
 * visible panel, the command palette, and keyboard-shortcut routing. All of it
 * is driven by the headless {@link EditorState} and lives behind the
 * {@link EditorApp} host, so it can be exercised against a mock ImGui context.
 */
public final class EditorUi {

    private final EditorContext ctx;
    private final DockSpaceLayout dockspace = new DockSpaceLayout();
    private final HierarchyPanel hierarchy = new HierarchyPanel();
    private final InspectorPanel inspector = new InspectorPanel();
    private final ProjectPanel project;
    private final ConsolePanel console = new ConsolePanel();
    private final SceneViewPanel sceneView;
    private final GameViewPanel gameView;

    /**
     * @param state        the headless editor state
     * @param themeProvider the resolved themes
     * @param layouts      the layout persistence store
     * @param viewport     the runtime Game-view target, or {@code null} headless
     */
    public EditorUi(EditorState state, ThemeProvider themeProvider, EditorLayoutStore layouts,
                    ViewportRenderer viewport) {
        this.ctx = new EditorContext(state, themeProvider, layouts);
        Path root = state.projectRoot();
        this.project = new ProjectPanel(root != null ? root : Path.of("."));
        this.sceneView = new SceneViewPanel(state.registry());
        this.gameView = new GameViewPanel(viewport);
        ctx.onRequestDockRebuild = dockspace::requestRebuild;
    }

    /** @return the shared editor context, for host callbacks. */
    public EditorContext context() {
        return ctx;
    }

    /** Renders one full editor frame. Call between ImGui {@code newFrame}/{@code render}. */
    public void render() {
        handleShortcuts();
        MainMenuBar.render(ctx);
        dockspace.render(ctx.state.layout());
        for (PanelKind kind : ctx.state.layout().panels()) {
            switch (kind) {
                case HIERARCHY -> hierarchy.render(ctx);
                case INSPECTOR -> inspector.render(ctx);
                case PROJECT -> project.render(ctx);
                case CONSOLE -> console.render(ctx);
                case SCENE -> sceneView.render(ctx);
                case GAME -> gameView.render(ctx);
            }
        }
        ctx.palette.render();
    }

    /**
     * Opens/closes a panel and requests a dock rebuild.
     *
     * @param ctx  the editor context
     * @param kind the panel
     */
    static void togglePanel(EditorContext ctx, PanelKind kind) {
        DockLayout layout = ctx.state.layout();
        if (layout.isVisible(kind)) {
            layout.close(kind);
        } else {
            layout.open(kind);
        }
        ctx.onRequestDockRebuild.run();
    }

    private void handleShortcuts() {
        ImGuiIO io = ImGui.getIO();
        ctx.textInput = io.getWantTextInput();
        if (ctx.palette.isOpen()) {
            return;
        }
        ShortcutRouter.Key key = pressedKey();
        if (key == null) {
            return;
        }
        Focus focus = io.getWantTextInput() ? Focus.TEXT : Focus.of(ctx.focusedPanel);
        Shortcut shortcut = ShortcutRouter.resolve(focus, io.getKeyCtrl(), io.getKeyShift(),
                io.getKeyAlt(), key);
        run(shortcut);
    }

    private void run(Shortcut shortcut) {
        EditorState state = ctx.state;
        switch (shortcut) {
            case NONE -> {
            }
            case OPEN_COMMAND_PALETTE -> ctx.palette.open();
            case UNDO -> state.undo();
            case REDO -> state.redo();
            case SAVE -> ctx.onSaveScene.run();
            case DELETE -> {
                if (!state.selection().isEmpty()) {
                    ctx.execute(EditCommands.delete(state.scene(), state.selection().objects()));
                    state.selection().clear();
                }
            }
            case DUPLICATE -> {
                GameObject primary = state.selection().primary();
                if (primary != null) {
                    ctx.execute(EditCommands.duplicate(state.scene(), primary, state.registry()));
                }
            }
            case RENAME -> hierarchy.beginRename(ctx, state.selection().primary());
            case SELECT_ALL_PANEL -> selectAll();
            case PLAY -> play();
            case PAUSE -> pause();
            case STEP -> step();
            case PLAY_TOGGLE -> togglePlay();
            case ACTIVATE -> {
                GameObject primary = state.selection().primary();
                ctx.log(LogLevel.INFO, primary == null ? "Nothing to activate"
                        : "Activated " + primary.name());
            }
        }
    }

    private void selectAll() {
        if (ctx.focusedPanel != PanelKind.HIERARCHY) {
            return;
        }
        for (GameObject root : ctx.state.scene().gameObjects()) {
            collect(root);
        }
    }

    private void collect(GameObject object) {
        ctx.state.selection().add(object);
        for (GameObject child : object.children()) {
            collect(child);
        }
    }

    private void togglePlay() {
        EditorState state = ctx.state;
        boolean wasStopped = state.play().isStopped();
        TransportActions.toggle(state);
        if (wasStopped) {
            ctx.log(LogLevel.INFO, "Entered Play mode");
        }
    }

    private void play() {
        if (ctx.state.play().isStopped()) {
            ctx.log(LogLevel.INFO, "Entered Play mode");
        }
        TransportActions.play(ctx.state);
    }

    private void pause() {
        if (ctx.state.play().isPlaying()) {
            TransportActions.pause(ctx.state);
            ctx.log(LogLevel.INFO, "Paused");
        }
    }

    private void step() {
        TransportActions.step(ctx.state);
    }

    private static ShortcutRouter.Key pressedKey() {
        if (ImGui.isKeyPressed(ImGuiKey.Enter)) {
            return ShortcutRouter.Key.ENTER;
        }
        if (ImGui.isKeyPressed(ImGuiKey.F2)) {
            return ShortcutRouter.Key.F2;
        }
        if (ImGui.isKeyPressed(ImGuiKey.Delete)) {
            return ShortcutRouter.Key.DELETE;
        }
        if (ImGui.isKeyPressed(ImGuiKey.A)) {
            return ShortcutRouter.Key.A;
        }
        if (ImGui.isKeyPressed(ImGuiKey.D)) {
            return ShortcutRouter.Key.D;
        }
        if (ImGui.isKeyPressed(ImGuiKey.Z)) {
            return ShortcutRouter.Key.Z;
        }
        if (ImGui.isKeyPressed(ImGuiKey.Y)) {
            return ShortcutRouter.Key.Y;
        }
        if (ImGui.isKeyPressed(ImGuiKey.S)) {
            return ShortcutRouter.Key.S;
        }
        if (ImGui.isKeyPressed(ImGuiKey.P)) {
            return ShortcutRouter.Key.P;
        }
        if (ImGui.isKeyPressed(ImGuiKey.Space)) {
            return ShortcutRouter.Key.SPACE;
        }
        return null;
    }
}
