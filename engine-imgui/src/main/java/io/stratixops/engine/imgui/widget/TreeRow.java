package io.stratixops.engine.imgui.widget;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiMouseButton;
import io.stratixops.engine.imgui.theme.Icon;
import io.stratixops.engine.imgui.theme.Theme;

/**
 * Tree row (design system section 3.1) shared by the Hierarchy and Project
 * trees. Renders indent, disclosure, type icon, label and state (hover,
 * selected, focused, drop target) and reports activation.
 *
 * <p>Interaction for this phase: single click selects, double click toggles
 * expansion. Full keyboard roving focus and inline rename land with the
 * hierarchy panel work in engine-editor.
 */
public final class TreeRow {

    private TreeRow() {
    }

    /** Result of rendering a tree row. */
    public enum Action {
        NONE,
        SELECTED,
        TOGGLED
    }

    /**
     * Renders one row.
     *
     * @param id          stable widget id (the object/asset guid)
     * @param label       display label
     * @param icon        semantic type icon
     * @param depth       tree depth for indentation
     * @param hasChildren whether a disclosure chevron is shown
     * @param expanded    current expansion state
     * @param selected    primary-selection state
     * @param focused     keyboard focus state
     * @param dropTarget  whether a drag is hovering this row
     */
    public static Action row(Theme t, String id, String label, Icon icon, int depth,
            boolean hasChildren, boolean expanded, boolean selected, boolean focused,
            boolean dropTarget) {

        ImDrawList draw = ImGui.getWindowDrawList();
        float width = Math.max(ImGui.getContentRegionAvailX(), 1f);
        float height = t.size.SIZE_ROW_H;

        ImGui.invisibleButton("##row-" + id, width, height);
        boolean hovered = ImGui.isItemHovered();
        ImVec2 min = ImGui.getItemRectMin();
        ImVec2 max = ImGui.getItemRectMax();

        if (dropTarget) {
            draw.addRectFilled(min.x, min.y, max.x, max.y, Ui.u32(t.color.ACCENT_SUBTLE),
                    t.radius.RADIUS_SM);
            draw.addRect(min.x, min.y, max.x, max.y, Ui.u32(t.color.ACCENT_DEFAULT),
                    t.radius.RADIUS_SM, 1f);
        } else if (selected) {
            draw.addRectFilled(min.x, min.y, max.x, max.y, Ui.u32(t.color.SURFACE_SELECTED),
                    t.radius.RADIUS_SM);
            draw.addRectFilled(min.x, min.y, min.x + 2f, max.y, Ui.u32(t.color.ACCENT_DEFAULT));
        } else if (hovered) {
            draw.addRectFilled(min.x, min.y, max.x, max.y, Ui.u32(t.color.SURFACE_HOVER),
                    t.radius.RADIUS_SM);
        }
        if (focused) {
            draw.addRect(min.x, min.y, max.x, max.y, Ui.u32(t.color.BORDER_FOCUS),
                    t.radius.RADIUS_SM, 2f);
        }

        float baseline = Ui.textBaseline(min.y, height);
        float x = min.x + t.space.SPACE_2 + depth * t.size.SIZE_INDENT_PER_LEVEL;

        if (hasChildren) {
            String chevron = expanded ? "\u25be" : "\u25b8";
            draw.addText(x, baseline, Ui.u32(t.color.TEXT_MUTED), chevron);
        }
        x += t.size.SIZE_ICON_MD;

        // Icon glyph is drawn from the Lucide atlas once the font asset lands;
        // the semantic name is reserved here so the layout is final.
        draw.addText(x, baseline, Ui.u32(t.color.TEXT_SECONDARY), "[" + icon.lucide().charAt(0) + "]");
        x += t.size.SIZE_ICON_MD + t.space.SPACE_1;

        int labelColor = selected ? t.color.TEXT_PRIMARY : t.color.TEXT_SECONDARY;
        float labelRoom = max.x - x - t.space.SPACE_2;
        draw.addText(x, baseline, Ui.u32(labelColor), Ui.ellipsize(label, labelRoom));

        if (hovered) {
            ImGui.setTooltip(label);
        }

        if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
            if (hasChildren && ImGui.isMouseDoubleClicked(ImGuiMouseButton.Left)) {
                return Action.TOGGLED;
            }
            return Action.SELECTED;
        }
        return Action.NONE;
    }
}
