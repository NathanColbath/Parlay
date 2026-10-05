package io.stratixops.engine.imgui.widget;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiMouseButton;
import io.stratixops.engine.imgui.theme.Theme;

/**
 * Project filter chip row (design system section 3.4): {@code All · Scene ·
 * Prefab · Script · Image · Audio · Font} pills. Selected chips use
 * {@code accent.subtle} fill with {@code accent.text}; neutral chips use
 * {@code bg.panel} with a {@code border.default} outline.
 */
public final class FilterChips {

    private FilterChips() {
    }

    /**
     * Renders the chip row at the current cursor.
     *
     * @return the criterion selected this frame (the current one if none clicked)
     */
    public static AssetFilter.Criterion render(Theme t, AssetFilter.Criterion current) {
        AssetFilter.Criterion next = current;
        float height = t.size.SIZE_CHIP_H;
        ImDrawList draw = ImGui.getWindowDrawList();
        AssetFilter.Criterion[] criteria = AssetFilter.Criterion.values();
        for (int i = 0; i < criteria.length; i++) {
            AssetFilter.Criterion criterion = criteria[i];
            float width = ImGui.calcTextSizeX(criterion.label()) + t.space.SPACE_2 * 2f;
            if (i > 0) {
                ImGui.sameLine(0f, t.space.SPACE_1);
            }
            ImVec2 min = new ImVec2(ImGui.getCursorScreenPos());
            ImGui.invisibleButton("##asset-chip-" + criterion.name(), width, height);
            boolean hovered = ImGui.isItemHovered();
            boolean selected = criterion == current;
            float x1 = min.x + width;
            float y1 = min.y + height;

            int fill;
            int border;
            int text;
            if (selected) {
                fill = t.color.ACCENT_SUBTLE;
                border = t.color.ACCENT_DEFAULT;
                text = t.color.ACCENT_TEXT;
            } else if (hovered) {
                fill = t.color.SURFACE_HOVER;
                border = t.color.BORDER_DEFAULT;
                text = t.color.TEXT_PRIMARY;
            } else {
                fill = t.color.BG_PANEL;
                border = t.color.BORDER_DEFAULT;
                text = t.color.TEXT_SECONDARY;
            }
            draw.addRectFilled(min.x, min.y, x1, y1, Ui.u32(fill), t.radius.RADIUS_PILL);
            draw.addRect(min.x, min.y, x1, y1, Ui.u32(border), t.radius.RADIUS_PILL, 1f);
            draw.addText(min.x + t.space.SPACE_2, Ui.textBaseline(min.y, height), Ui.u32(text),
                    criterion.label());

            if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
                next = criterion;
            }
        }
        ImGui.spacing();
        return next;
    }
}
