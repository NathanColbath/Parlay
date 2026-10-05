package io.stratixops.engine.imgui.widget;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiMouseButton;
import io.stratixops.engine.imgui.drag.DragAndDrop;
import io.stratixops.engine.imgui.drag.DragPayload;
import io.stratixops.engine.imgui.theme.Icon;
import io.stratixops.engine.imgui.theme.Theme;
import java.util.List;

/**
 * Tree row (design system section 3.1) shared by the Hierarchy and Project
 * trees. Renders indent, disclosure, type icon, label and state (hover,
 * selected, focused, drop target) and reports activation.
 *
 * <p>Interaction for this phase: single click selects, double click toggles
 * expansion. Full keyboard roving focus and inline rename land with the
 * hierarchy panel work in engine-editor.
 *
 * <p>The drag-aware overload registers the row as a source and a drop target on
 * a shared {@link DragAndDrop}; the ghost, insertion line, drop-into fill and
 * invalid border are drawn by the drag overlay (design system 3.8).
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

    /** Renders one row without drag-and-drop. */
    public static Action row(Theme t, String id, String label, Icon icon, int depth,
            boolean hasChildren, boolean expanded, boolean selected, boolean focused,
            boolean dropTarget) {
        return render(t, null, null, null, id, label, icon, depth, hasChildren, expanded,
                selected, focused, dropTarget, false);
    }

    /**
     * Renders one row wired into the shared drag-and-drop system.
     *
     * @param drag        the panel's drag controller
     * @param dragPanel   source panel id stored in the payload
     * @param dragIds     ids to drag when this row initiates the gesture
     * @param acceptsInto whether this row can be reparented into
     */
    public static Action row(Theme t, DragAndDrop drag, String dragPanel, List<String> dragIds,
            String id, String label, Icon icon, int depth, boolean hasChildren, boolean expanded,
            boolean selected, boolean focused, boolean acceptsInto) {
        return render(t, drag, dragPanel, dragIds, id, label, icon, depth, hasChildren, expanded,
                selected, focused, false, acceptsInto);
    }

    private static Action render(Theme t, DragAndDrop drag, String dragPanel, List<String> dragIds,
            String id, String label, Icon icon, int depth, boolean hasChildren, boolean expanded,
            boolean selected, boolean focused, boolean dropTarget, boolean acceptsInto) {

        ImDrawList draw = ImGui.getWindowDrawList();
        float width = Math.max(ImGui.getContentRegionAvailX(), 1f);
        float height = t.size.SIZE_ROW_H;

        ImGui.invisibleButton("##row-" + id, width, height);
        boolean hovered = ImGui.isItemHovered();
        ImVec2 min = ImGui.getItemRectMin();
        ImVec2 max = ImGui.getItemRectMax();

        if (drag != null) {
            drag.source(new DragPayload(dragPanel, dragIds), hovered, min.x, min.y, width, height);
            drag.target(id, min.x, min.y, width, height, acceptsInto, true);
        }

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
            Ui.drawIcon(t, draw, x, baseline, t.color.TEXT_MUTED,
                    expanded ? Icon.CHEVRON_DOWN : Icon.CHEVRON_RIGHT);
        }
        x += t.size.SIZE_ICON_MD;

        // Icon glyph is drawn from the Lucide subset merged into the UI face
        // (design system section 1.6); the codepoint is fixed in Icon.
        Ui.drawIcon(t, draw, x, baseline, t.color.TEXT_SECONDARY, icon);
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
