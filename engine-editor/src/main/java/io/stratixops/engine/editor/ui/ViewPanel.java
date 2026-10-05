package io.stratixops.engine.editor.ui;

import imgui.ImGui;
import imgui.ImVec2;
import io.stratixops.engine.editor.PanelKind;
import io.stratixops.engine.editor.play.PlayState;
import io.stratixops.engine.imgui.theme.Theme;
import io.stratixops.engine.imgui.widget.Ui;

/**
 * Scene and Game view panels (STR-40). The world is rendered offscreen to an
 * `engine-render` {@code RenderTarget} and presented here as an ImGui image
 * (ADR-0005 section 4); until the editor/STR-29 hand-off supplies a texture the
 * panel renders a themed placeholder, so the OOB layout always shows all six
 * panels.
 */
final class ViewPanel {

    private final PanelKind kind;
    private int textureId;

    ViewPanel(PanelKind kind) {
        this.kind = kind;
    }

    /**
     * Supplies the offscreen color texture to present.
     *
     * @param textureId the GL texture id, or {@code 0} for the placeholder
     */
    void setTextureId(int textureId) {
        this.textureId = textureId;
    }

    void render(EditorContext ctx) {
        Theme theme = ctx.theme();
        if (!ImGui.begin(kind.title())) {
            ImGui.end();
            return;
        }
        if (ImGui.isWindowFocused()) {
            ctx.focusedPanel = kind;
        }
        float width = Math.max(ImGui.getContentRegionAvailX(), 1f);
        float height = Math.max(ImGui.getContentRegionAvailY(), 1f);
        if (textureId != 0) {
            ImGui.image(textureId, width, height);
        } else {
            ImVec2 origin = ImGui.getCursorScreenPos();
            ImGui.getWindowDrawList().addRectFilled(origin.x, origin.y, origin.x + width,
                    origin.y + height, Ui.u32(theme.color.BG_PANEL));
            ImGui.getWindowDrawList().addRect(origin.x, origin.y, origin.x + width,
                    origin.y + height, Ui.u32(theme.color.BORDER_SUBTLE));
            String label = kind == PanelKind.GAME ? "Game view" : "Scene view";
            String state = kind == PanelKind.GAME ? playLabel(ctx) : "editor camera";
            float textX = origin.x + (width - ImGui.calcTextSizeX(label)) * 0.5f;
            float textY = origin.y + (height - ImGui.getTextLineHeight()) * 0.5f;
            ImGui.getWindowDrawList().addText(textX, textY, Ui.u32(theme.color.TEXT_MUTED), label);
            Ui.pushColor(imgui.flag.ImGuiCol.Text, theme.color.TEXT_DISABLED);
            ImGui.setCursorScreenPos(origin.x, textY + ImGui.getTextLineHeightWithSpacing());
            ImGui.textUnformatted("  " + state + " \u00b7 offscreen RenderTarget pending");
            Ui.popColor();
        }
        ImGui.end();
    }

    private static String playLabel(EditorContext ctx) {
        PlayState state = ctx.state.play().state();
        return switch (state) {
            case STOPPED -> "stopped";
            case PLAYING -> "playing";
            case PAUSED -> "paused";
        };
    }
}
