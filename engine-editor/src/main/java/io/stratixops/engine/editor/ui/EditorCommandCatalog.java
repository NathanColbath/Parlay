package io.stratixops.engine.editor.ui;

import io.stratixops.engine.editor.DockLayout;
import io.stratixops.engine.editor.PanelKind;
import io.stratixops.engine.scene.GameObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the command palette catalogue from the live editor state (STR-40).
 * Every mutating entry routes through the {@code EditorController}, so palette
 * actions are undoable exactly like the panel and menu equivalents.
 */
final class EditorCommandCatalog {

    private EditorCommandCatalog() {
    }

    static List<PaletteCommand> commands(EditorContext ctx) {
        List<PaletteCommand> commands = new ArrayList<>();
        commands.addAll(gameObjectCommands(ctx));
        commands.addAll(editCommands(ctx));
        commands.addAll(viewCommands(ctx));
        commands.addAll(playCommands(ctx));
        return commands;
    }

    private static List<PaletteCommand> gameObjectCommands(EditorContext ctx) {
        List<PaletteCommand> list = new ArrayList<>();
        GameObject primary = ctx.state.selection().primary();
        list.add(new PaletteCommand("go.create", "Create Empty GameObject", "GameObject", true,
                () -> create(ctx, null)));
        list.add(new PaletteCommand("go.create-child", "Create Child GameObject", "GameObject",
                primary != null, () -> create(ctx, primary)));
        list.add(new PaletteCommand("go.duplicate", "Duplicate Selection", "GameObject",
                primary != null, () -> ctx.controller.duplicateSelection()));
        list.add(new PaletteCommand("go.delete", "Delete Selection", "GameObject",
                !ctx.state.selection().isEmpty(), () -> ctx.controller.deleteSelection()));
        list.add(new PaletteCommand("go.copy", "Copy Selection", "GameObject",
                !ctx.state.selection().isEmpty(), () -> ctx.controller.copySelection()));
        list.add(new PaletteCommand("go.paste", "Paste", "GameObject",
                !ctx.controller.clipboardEmpty(), () -> ctx.controller.paste()));
        return list;
    }

    private static List<PaletteCommand> editCommands(EditorContext ctx) {
        List<PaletteCommand> list = new ArrayList<>();
        list.add(new PaletteCommand("edit.undo", "Undo", "Edit", ctx.state.commands().canUndo(),
                () -> ctx.state.undo()));
        list.add(new PaletteCommand("edit.redo", "Redo", "Edit", ctx.state.commands().canRedo(),
                () -> ctx.state.redo()));
        list.add(new PaletteCommand("file.open", "Open Scene", "File", true,
                () -> ctx.onOpenScene.run()));
        list.add(new PaletteCommand("file.save", "Save Scene", "File", true,
                () -> ctx.onSaveScene.run()));
        return list;
    }

    private static List<PaletteCommand> viewCommands(EditorContext ctx) {
        List<PaletteCommand> list = new ArrayList<>();
        for (PanelKind kind : PanelKind.values()) {
            boolean visible = ctx.state.layout().isVisible(kind);
            list.add(new PaletteCommand("view." + kind.name().toLowerCase(),
                    (visible ? "Close " : "Open ") + kind.title() + " Panel", "View", true,
                    () -> EditorUi.togglePanel(ctx, kind)));
        }
        list.add(new PaletteCommand("view.reset-layout", "Reset Layout", "View", true,
                () -> {
                    ctx.state.setLayout(DockLayout.defaultLayout());
                    ctx.onRequestDockRebuild.run();
                }));
        list.add(new PaletteCommand("view.save-layout", "Save Layout", "View",
                ctx.layouts.file() != null, () -> {
                    String error = ctx.layouts.save(ctx.state.layout());
                    if (error != null) {
                        ctx.log(io.stratixops.engine.editor.console.LogLevel.WARNING, error);
                    }
                }));
        list.add(new PaletteCommand("view.load-layout", "Load Layout", "View",
                ctx.layouts.file() != null, () -> {
                    ctx.state.setLayout(ctx.layouts.load(DockLayout.defaultLayout()));
                    ctx.onRequestDockRebuild.run();
                }));
        list.add(new PaletteCommand("view.theme", "Toggle Theme", "View", true, ctx::toggleTheme));
        return list;
    }

    private static List<PaletteCommand> playCommands(EditorContext ctx) {
        List<PaletteCommand> list = new ArrayList<>();
        boolean stopped = ctx.state.play().isStopped();
        boolean playing = ctx.state.play().isPlaying();
        boolean paused = ctx.state.play().isPaused();
        list.add(new PaletteCommand("play.play", "Play", "Play", stopped,
                () -> ctx.state.play().play(ctx.state.scene(), ctx.state.registry())));
        list.add(new PaletteCommand("play.pause", "Pause", "Play", playing,
                () -> ctx.state.play().pause()));
        list.add(new PaletteCommand("play.resume", "Resume", "Play", paused,
                () -> ctx.state.play().resume()));
        list.add(new PaletteCommand("play.step", "Step", "Play", paused,
                () -> ctx.state.play().step()));
        list.add(new PaletteCommand("play.stop", "Stop", "Play", !stopped,
                () -> ctx.state.play().stop()));
        return list;
    }

    static void create(EditorContext ctx, GameObject parent) {
        createObject(ctx, parent);
    }

    static GameObject createObject(EditorContext ctx, GameObject parent) {
        if (parent == null) {
            return ctx.controller.createGameObject("GameObject");
        }
        ctx.state.selection().select(parent);
        return ctx.controller.createChild("GameObject");
    }

    /**
     * Groups commands by category, preserving first-seen order.
     *
     * @param commands the commands
     * @return group name to commands
     */
    static Map<String, List<PaletteCommand>> group(List<PaletteCommand> commands) {
        Map<String, List<PaletteCommand>> groups = new LinkedHashMap<>();
        for (PaletteCommand command : commands) {
            groups.computeIfAbsent(command.group(), key -> new ArrayList<>()).add(command);
        }
        return groups;
    }
}
