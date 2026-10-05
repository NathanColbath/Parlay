package io.stratixops.engine.editor.ui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import io.stratixops.engine.editor.PanelKind;
import io.stratixops.engine.editor.play.PlayController;
import io.stratixops.engine.editor.view.ViewportRenderer;
import io.stratixops.engine.imgui.theme.Theme;
import io.stratixops.engine.imgui.widget.Ui;
import io.stratixops.engine.render.RenderTargetLayout;

/**
 * The Game view (STR-41). While Play is active it presents the offscreen
 * runtime {@link ViewportRenderer} target — the runtime scene submitted through
 * the shared {@code SceneRenderer} — as an {@code ImGui::Image}. The fixed
 * 640x360 target is letterboxed with {@link RenderTargetLayout} so the image is
 * never distorted, and bars fill the unused margin.
 */
final class GameViewPanel {

    private final ViewportRenderer viewport;

    /**
     * @param viewport the runtime target renderer, or {@code null} when no GL
     *                 context is available (headless)
     */
    GameViewPanel(ViewportRenderer viewport) {
        this.viewport = viewport;
    }

    void render(EditorContext ctx) {
        Theme theme = ctx.theme();
        if (!ImGui.begin(PanelKind.GAME.title())) {
            ImGui.end();
            return;
        }
        if (ImGui.isWindowFocused()) {
            ctx.focusedPanel = PanelKind.GAME;
        }
        TransportToolbar.render(ctx);
        ImGui.separator();

        float width = Math.max(ImGui.getContentRegionAvailX(), 1f);
        float height = Math.max(ImGui.getContentRegionAvailY(), 1f);
        PlayController play = ctx.state.play();

        if (viewport != null && viewport.isReady() && play.runtimeScene() != null) {
            present(theme, width, height);
        } else {
            placeholder(ctx, theme, width, height);
        }
        ImGui.end();
    }

    private void present(Theme theme, float width, float height) {
        ImVec2 origin = new ImVec2(ImGui.getCursorScreenPos());
        RenderTargetLayout.Fit fit = RenderTargetLayout.fit(viewport.targetWidth(),
                viewport.targetHeight(), width, height);
        ImGui.getWindowDrawList().addRectFilled(origin.x, origin.y, origin.x + width,
                origin.y + height, Ui.u32(theme.color.BG_CANVAS));
        if (fit.scale() > 0f) {
            ImGui.setCursorScreenPos(origin.x + fit.x(), origin.y + fit.y());
            ImGui.image(viewport.textureId(), fit.width(), fit.height());
        }
    }

    private void placeholder(EditorContext ctx, Theme theme, float width, float height) {
        ImVec2 origin = new ImVec2(ImGui.getCursorScreenPos());
        ImGui.getWindowDrawList().addRectFilled(origin.x, origin.y, origin.x + width,
                origin.y + height, Ui.u32(theme.color.BG_PANEL));
        ImGui.getWindowDrawList().addRect(origin.x, origin.y, origin.x + width,
                origin.y + height, Ui.u32(theme.color.BORDER_SUBTLE));
        String label = "Game view";
        String state = ctx.state.play().isStopped()
                ? "Press Play to run the scene" : "Runtime scene not rendered";
        float textX = origin.x + (width - ImGui.calcTextSizeX(label)) * 0.5f;
        float textY = origin.y + (height - ImGui.getTextLineHeight()) * 0.5f;
        ImGui.getWindowDrawList().addText(textX, textY, Ui.u32(theme.color.TEXT_MUTED), label);
        Ui.pushColor(ImGuiCol.Text, theme.color.TEXT_DISABLED);
        ImGui.setCursorScreenPos(origin.x, textY + ImGui.getTextLineHeightWithSpacing());
        ImGui.textUnformatted("  " + state);
        Ui.popColor();
    }
}
