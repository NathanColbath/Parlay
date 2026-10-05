package io.stratixops.engine.editor.ui;

import imgui.ImGui;
import io.stratixops.engine.editor.PanelKind;
import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Scene;

/**
 * The editor main menu bar (STR-40): File, Edit, GameObject, Play, View and
 * Window menus. Every mutating entry routes through {@code EditCommands} or the
 * host callbacks, so the GUI and the command palette share one implementation.
 */
final class MainMenuBar {

    private MainMenuBar() {
    }

    static void render(EditorContext ctx) {
        if (!ImGui.beginMainMenuBar()) {
            return;
        }
        fileMenu(ctx);
        editMenu(ctx);
        gameObjectMenu(ctx);
        playMenu(ctx);
        viewMenu(ctx);
        windowMenu(ctx);
        ImGui.endMainMenuBar();
    }

    private static void fileMenu(EditorContext ctx) {
        if (ImGui.beginMenu("File")) {
            if (ImGui.menuItem("New Scene")) {
                ctx.state.openScene(new Scene("Untitled"));
            }
            if (ImGui.menuItem("Open Scene", "Ctrl+O")) {
                ctx.onOpenScene.run();
            }
            if (ImGui.menuItem("Save Scene", "Ctrl+S")) {
                ctx.onSaveScene.run();
            }
            ImGui.separator();
            boolean persistent = ctx.layouts.file() != null;
            if (ImGui.menuItem("Save Layout", "", false, persistent)) {
                String error = ctx.layouts.save(ctx.state.layout());
                if (error != null) {
                    ctx.log(io.stratixops.engine.editor.console.LogLevel.WARNING, error);
                }
            }
            if (ImGui.menuItem("Load Layout", "", false, persistent)) {
                ctx.state.setLayout(ctx.layouts.load(io.stratixops.engine.editor.DockLayout.defaultLayout()));
                ctx.onRequestDockRebuild.run();
            }
            if (ImGui.menuItem("Reset Layout")) {
                ctx.state.setLayout(io.stratixops.engine.editor.DockLayout.defaultLayout());
                ctx.onRequestDockRebuild.run();
            }
            ImGui.separator();
            if (ImGui.menuItem("Exit")) {
                ctx.onQuit.run();
            }
            ImGui.endMenu();
        }
    }

    private static void editMenu(EditorContext ctx) {
        if (ImGui.beginMenu("Edit")) {
            String undo = ctx.state.commands().undoName();
            String redo = ctx.state.commands().redoName();
            if (ImGui.menuItem(undo == null ? "Undo" : "Undo " + undo, "Ctrl+Z",
                    false, ctx.state.commands().canUndo())) {
                ctx.state.undo();
            }
            if (ImGui.menuItem(redo == null ? "Redo" : "Redo " + redo, "Ctrl+Y",
                    false, ctx.state.commands().canRedo())) {
                ctx.state.redo();
            }
            ImGui.separator();
            boolean hasSelection = !ctx.state.selection().isEmpty();
            if (ImGui.menuItem("Copy", "Ctrl+C", false, hasSelection)) {
                int count = ctx.controller.copySelection();
                ctx.log(io.stratixops.engine.editor.console.LogLevel.INFO,
                        "Copied " + count + (count == 1 ? " object" : " objects"));
            }
            if (ImGui.menuItem("Paste", "Ctrl+V", false, !ctx.controller.clipboardEmpty())) {
                int count = ctx.controller.clipboardSize();
                ctx.controller.paste();
                ctx.log(io.stratixops.engine.editor.console.LogLevel.INFO,
                        "Pasted " + count + (count == 1 ? " object" : " objects"));
            }
            ImGui.separator();
            if (ImGui.menuItem("Duplicate", "Ctrl+D", false, hasSelection)) {
                ctx.controller.duplicateSelection();
            }
            if (ImGui.menuItem("Delete", "Del", false, hasSelection)) {
                ctx.controller.deleteSelection();
            }
            ImGui.endMenu();
        }
    }

    private static void gameObjectMenu(EditorContext ctx) {
        if (ImGui.beginMenu("GameObject")) {
            if (ImGui.menuItem("Create Empty")) {
                ctx.controller.createGameObject("GameObject");
            }
            GameObject primary = ctx.state.selection().primary();
            if (ImGui.menuItem("Create Empty Child", "", false, primary != null)) {
                ctx.controller.createChild("GameObject");
            }
            ImGui.endMenu();
        }
    }

    private static void playMenu(EditorContext ctx) {
        if (ImGui.beginMenu("Play")) {
            if (ImGui.menuItem("Play", "", false, ctx.state.play().isStopped())) {
                ctx.state.play().play(ctx.state.scene(), ctx.state.registry());
            }
            if (ImGui.menuItem("Pause", "", false, ctx.state.play().isPlaying())) {
                ctx.state.play().pause();
            }
            if (ImGui.menuItem("Resume", "", false, ctx.state.play().isPaused())) {
                ctx.state.play().resume();
            }
            if (ImGui.menuItem("Step", "", false, ctx.state.play().isPaused())) {
                ctx.state.play().step();
            }
            if (ImGui.menuItem("Stop", "", false, !ctx.state.play().isStopped())) {
                ctx.state.play().stop();
            }
            ImGui.endMenu();
        }
    }

    private static void viewMenu(EditorContext ctx) {
        if (ImGui.beginMenu("View")) {
            if (ImGui.menuItem("Toggle Theme")) {
                ctx.toggleTheme();
            }
            ImGui.endMenu();
        }
    }

    private static void windowMenu(EditorContext ctx) {
        if (ImGui.beginMenu("Window")) {
            for (PanelKind kind : PanelKind.values()) {
                boolean visible = ctx.state.layout().isVisible(kind);
                if (ImGui.menuItem(kind.title(), "", visible, true)) {
                    EditorUi.togglePanel(ctx, kind);
                }
            }
            ImGui.separator();
            if (ImGui.menuItem("Reset Layout")) {
                ctx.state.setLayout(io.stratixops.engine.editor.DockLayout.defaultLayout());
                ctx.onRequestDockRebuild.run();
            }
            ImGui.endMenu();
        }
    }
}
