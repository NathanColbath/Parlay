package io.stratixops.engine.imgui.widget;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiMouseButton;
import io.stratixops.engine.imgui.theme.Icon;
import io.stratixops.engine.imgui.theme.Theme;
import java.util.List;

/**
 * Breadcrumb bar for the Project panel (design system section 3.4): a root
 * folder mark, clickable path segments with chevron separators, an overflow
 * {@code …} popup when the path does not fit, and a drop-target state so a drag
 * can move an asset onto a segment.
 *
 * <p>The overflow decision is a pure function ({@link #overflow}) so it is
 * unit-testable; the drop target styling is driven by the caller, which owns the
 * drag session (STR-35).
 */
public final class Breadcrumb {

    private Breadcrumb() {
    }

    /** One path segment: a label plus the navigation target it resolves to. */
    public record Segment(String label, String path) {
    }

    /** Result of a breadcrumb frame. {@code activated} is null when nothing was clicked. */
    public record Result(Segment activated) {
    }

    /** Where an overflowing path should split, and whether an ellipsis is shown. */
    public record Cut(int firstTail, boolean ellipsis) {
    }

    private static final String CHEVRON = "\u203a";
    private static final String ELLIPSIS = "\u2026";

    /**
     * Computes the overflow split: always keep the first segment, optionally an
     * ellipsis, then as many trailing segments as fit. Pure so it never depends
     * on the current ImGui frame.
     *
     * @param segmentWidths each segment's pixel width, root first
     * @param available     the bar's usable width
     * @param chevronWidth  the separator width between two visible elements
     * @param ellipsisWidth the {@code …} element width
     */
    public static Cut overflow(float[] segmentWidths, float available, float chevronWidth,
            float ellipsisWidth) {
        int n = segmentWidths.length;
        if (n == 0) {
            return new Cut(0, false);
        }
        if (n == 1) {
            return new Cut(1, false);
        }
        float all = segmentWidths[0];
        for (int i = 1; i < n; i++) {
            all += chevronWidth + segmentWidths[i];
        }
        if (all <= available) {
            return new Cut(1, false);
        }
        float used = segmentWidths[0] + chevronWidth + ellipsisWidth;
        int firstTail = n;
        for (int i = n - 1; i >= 1; i--) {
            float add = chevronWidth + segmentWidths[i];
            if (used + add > available) {
                break;
            }
            used += add;
            firstTail = i;
        }
        return new Cut(firstTail, true);
    }

    /**
     * Renders the breadcrumb at the current cursor over {@code available} px.
     *
     * @param activeIndex the current folder segment (styled primary)
     * @param dropIndex   the segment currently under a drag, or {@code -1}
     */
    public static Result render(Theme t, List<Segment> segments, int activeIndex,
            float available, int dropIndex) {
        if (segments.isEmpty()) {
            return new Result(null);
        }
        float height = t.size.SIZE_BREADCRUMB_H;
        float chevronWidth = ImGui.calcTextSizeX(CHEVRON) + t.space.SPACE_1 * 2f;
        float ellipsisWidth = ImGui.calcTextSizeX(ELLIPSIS) + t.space.SPACE_2;

        float[] widths = new float[segments.size()];
        for (int i = 0; i < segments.size(); i++) {
            float width = ImGui.calcTextSizeX(segments.get(i).label()) + t.space.SPACE_2 * 2f;
            if (i == 0) {
                width += t.size.SIZE_ICON_MD;
            }
            widths[i] = width;
        }
        Cut cut = overflow(widths, available, chevronWidth, ellipsisWidth);

        ImVec2 origin = new ImVec2(ImGui.getCursorScreenPos());
        ImGui.dummy(available, height);
        ImDrawList draw = ImGui.getWindowDrawList();

        Segment activated = null;
        Segment root = segments.get(0);
        if (segment(t, draw, root, 0, origin.x, origin.y, widths[0], height, true,
                activeIndex == 0, dropIndex == 0)) {
            activated = root;
        }
        float x = origin.x + widths[0];

        int next = 1;
        if (cut.ellipsis()) {
            x = chevron(t, draw, x, origin.y, height);
            ImGui.setCursorScreenPos(x, origin.y);
            ImGui.invisibleButton("##bc-overflow", ellipsisWidth, height);
            draw.addText(x + t.space.SPACE_1, Ui.textBaseline(origin.y, height),
                    Ui.u32(t.color.TEXT_MUTED), ELLIPSIS);
            if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
                ImGui.openPopup("##bc-overflow-popup");
            }
            if (ImGui.beginPopup("##bc-overflow-popup")) {
                for (int i = 1; i < cut.firstTail(); i++) {
                    if (ImGui.menuItem(segments.get(i).label())) {
                        activated = segments.get(i);
                    }
                }
                ImGui.endPopup();
            }
            x += ellipsisWidth;
            next = Math.max(1, cut.firstTail());
        }

        for (int i = next; i < segments.size(); i++) {
            x = chevron(t, draw, x, origin.y, height);
            if (segment(t, draw, segments.get(i), i, x, origin.y, widths[i], height, false,
                    i == activeIndex, i == dropIndex)) {
                activated = segments.get(i);
            }
            x += widths[i];
        }

        ImGui.setCursorScreenPos(origin.x, origin.y + height);
        return new Result(activated);
    }

    private static float chevron(Theme t, ImDrawList draw, float x, float y, float height) {
        draw.addText(x + t.space.SPACE_1, Ui.textBaseline(y, height), Ui.u32(t.color.TEXT_MUTED),
                CHEVRON);
        return x + ImGui.calcTextSizeX(CHEVRON) + t.space.SPACE_1 * 2f;
    }

    private static boolean segment(Theme t, ImDrawList draw, Segment segment, int index, float x,
            float y, float width, float height, boolean root, boolean active, boolean dropTarget) {
        ImGui.setCursorScreenPos(x, y);
        ImGui.invisibleButton("##bc-seg-" + index + "-" + segment.path(), width, height);
        boolean hovered = ImGui.isItemHovered();
        int color;
        if (dropTarget) {
            draw.addRectFilled(x, y, x + width, y + height, Ui.u32(t.color.ACCENT_SUBTLE),
                    t.radius.RADIUS_SM);
            draw.addRect(x, y, x + width, y + height, Ui.u32(t.color.ACCENT_DEFAULT),
                    t.radius.RADIUS_SM, 1f);
            color = t.color.TEXT_PRIMARY;
        } else if (active) {
            color = t.color.TEXT_PRIMARY;
        } else {
            color = hovered ? t.color.TEXT_SECONDARY : t.color.TEXT_MUTED;
        }
        float textX = x + t.space.SPACE_1;
        if (root) {
            Ui.drawIcon(t, draw, textX, Ui.textBaseline(y, height), color, Icon.PANEL_PROJECT);
            textX += t.size.SIZE_ICON_MD;
        }
        draw.addText(textX, Ui.textBaseline(y, height), Ui.u32(color), segment.label());
        if (hovered) {
            ImGui.setTooltip(segment.path().isEmpty() ? segment.label() : segment.path());
        }
        return ImGui.isItemClicked(ImGuiMouseButton.Left);
    }
}
