package io.stratixops.engine.imgui.widget;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;
import imgui.type.ImString;
import io.stratixops.engine.imgui.theme.Theme;
import java.util.List;

/**
 * Command palette (design system section 3.5): a centered search field over a
 * grouped result list. The editor supplies filtered commands and runs the chosen
 * one; the kit owns the surface. Call only while {@code open}.
 */
public final class CommandPalette {

    private static final float MAX_HEIGHT = 400f;
    /** Prefix marking a result string as a non-selectable group header. */
    public static final char GROUP = '\u0000';

    private CommandPalette() {
    }

    /**
     * Renders the palette overlay.
     *
     * @param query    search buffer, mutated by typing
     * @param results  already-filtered labels; a {@link #GROUP}-prefixed entry is
     *                 rendered as a category header
     * @param selected index of the highlighted row
     * @return the activated index, or {@code -1}
     */
    public static int render(Theme t, ImString query, List<String> results, ImInt selected) {
        ImGuiIO io = ImGui.getIO();
        float width = t.size.SIZE_PALETTE_W;
        ImGui.setNextWindowPos((io.getDisplaySizeX() - width) * 0.5f, io.getDisplaySizeY() * 0.12f,
                ImGuiCond.Always);
        ImGui.setNextWindowSize(width, MAX_HEIGHT, ImGuiCond.Always);

        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, t.radius.RADIUS_LG);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, t.space.SPACE_2, t.space.SPACE_2);
        Ui.pushColor(ImGuiCol.FrameBg, t.color.BG_ELEVATED);

        int flags = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize
                | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoSavedSettings
                | ImGuiWindowFlags.NoScrollbar;

        int activated = -1;
        if (ImGui.begin("##command-palette", flags)) {
            ImGui.setNextItemWidth(-1f);
            ImGui.inputTextWithHint("##palette-search", "Search commands\u2026", query, 0);
            ImGui.separator();
            for (int i = 0; i < results.size(); i++) {
                String label = results.get(i);
                if (label.charAt(0) == GROUP) {
                    Ui.pushColor(ImGuiCol.Text, t.color.TEXT_MUTED);
                    ImGui.textUnformatted(label.substring(1).toUpperCase());
                    Ui.popColor();
                    continue;
                }
                boolean isSelected = i == selected.get();
                if (ImGui.selectable(label, isSelected)) {
                    selected.set(i);
                    activated = i;
                }
                if (isSelected) {
                    ImGui.setItemDefaultFocus();
                }
            }
        }
        ImGui.end();
        Ui.popColor();
        ImGui.popStyleVar(2);
        return activated;
    }
}
