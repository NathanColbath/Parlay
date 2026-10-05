package io.stratixops.engine.editor.ui;

import imgui.flag.ImGuiDir;
import imgui.internal.ImGui;
import imgui.internal.flag.ImGuiDockNodeFlags;
import imgui.type.ImInt;
import io.stratixops.engine.editor.DockLayout;
import io.stratixops.engine.editor.PanelKind;

/**
 * Builds the editor dockspace from the headless {@link DockLayout} model
 * (STR-40). The ImGui dock builder is invoked once per layout change, then the
 * panels dock themselves by window title as they are created; user
 * rearrangements are preserved until the next explicit rebuild.
 *
 * <p>GL/ImGui host glue, excluded from the headless coverage gate (ADR-0008
 * section 7).
 */
final class DockSpaceLayout {

    private boolean rebuildRequested = true;

    /** Requests a dock rebuild on the next frame. */
    void requestRebuild() {
        rebuildRequested = true;
    }

    /**
     * Draws the full-window dockspace and, when requested, applies the layout.
     *
     * @param layout the layout to apply on a rebuild
     */
    void render(DockLayout layout) {
        int dockspace = ImGui.dockSpaceOverViewport(ImGuiDockNodeFlags.PassthruCentralNode);
        if (rebuildRequested) {
            build(dockspace, layout);
            rebuildRequested = false;
        }
    }

    private void build(int dockspace, DockLayout layout) {
        ImGui.dockBuilderRemoveNode(dockspace);
        ImGui.dockBuilderAddNode(dockspace,
                ImGuiDockNodeFlags.DockSpace | ImGuiDockNodeFlags.PassthruCentralNode);

        ImInt atDirection = new ImInt();
        ImInt opposite = new ImInt();
        int central = dockspace;
        int left = 0;
        int right = 0;
        int bottom = 0;
        if (!layout.tabs(DockLayout.Area.LEFT).isEmpty()) {
            ImGui.dockBuilderSplitNode(central, ImGuiDir.Left, 0.20f, atDirection, opposite);
            left = atDirection.get();
            central = opposite.get();
        }
        if (!layout.tabs(DockLayout.Area.RIGHT).isEmpty()) {
            ImGui.dockBuilderSplitNode(central, ImGuiDir.Right, 0.24f, atDirection, opposite);
            right = atDirection.get();
            central = opposite.get();
        }
        if (!layout.tabs(DockLayout.Area.BOTTOM).isEmpty()) {
            ImGui.dockBuilderSplitNode(central, ImGuiDir.Down, 0.30f, atDirection, opposite);
            bottom = atDirection.get();
            central = opposite.get();
        }
        dock(layout, DockLayout.Area.LEFT, left);
        dock(layout, DockLayout.Area.RIGHT, right);
        dock(layout, DockLayout.Area.BOTTOM, bottom);
        dock(layout, DockLayout.Area.CENTER, central);
        ImGui.dockBuilderFinish(dockspace);
    }

    private void dock(DockLayout layout, DockLayout.Area area, int node) {
        if (node == 0) {
            return;
        }
        for (PanelKind kind : layout.tabs(area)) {
            ImGui.dockBuilderDockWindow(kind.title(), node);
        }
    }
}
