package io.stratixops.engine.imgui.widget;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import io.stratixops.engine.imgui.theme.Rgba;
import io.stratixops.engine.imgui.theme.Theme;

/**
 * Shared drawing primitives for the widget kit.
 *
 * <p>ImGui has no style slot for the design system's custom marks — the 2 px
 * selection bar, focus ring, drop line, elevation shadows — so they are drawn
 * here against the window draw list. Every helper reads {@link Theme}; no
 * literal color or size is used anywhere in the kit.
 */
public final class Ui {

    private Ui() {
    }

    /** @return packed color as ImGui's normalized RGBA float order. */
    public static float[] rgba(int packed) {
        return Rgba.toFloats(packed);
    }

    /** @return packed color as an ImGui draw-list color. */
    public static int u32(int packed) {
        float[] c = rgba(packed);
        return ImGui.getColorU32(c[0], c[1], c[2], c[3]);
    }

    /** Pushes one ImGui color slot from a packed token color. */
    public static void pushColor(int imguiCol, int packed) {
        float[] c = rgba(packed);
        ImGui.pushStyleColor(imguiCol, c[0], c[1], c[2], c[3]);
    }

    public static void popColor() {
        ImGui.popStyleColor();
    }

    /** Draws the 2 px accent selection bar on the leading edge of a row. */
    public static void selectionBar(Theme t, float x, float y, float height) {
        ImGui.getWindowDrawList().addRectFilled(x, y, x + 2f, y + height, u32(t.color.ACCENT_DEFAULT));
    }

    /** Draws the 2 px keyboard focus ring with a 1 px gap (design system 1.7). */
    public static void focusRing(Theme t, float x, float y, float w, float h) {
        ImGui.getWindowDrawList().addRect(x, y, x + w, y + h, u32(t.color.BORDER_FOCUS),
                t.radius.RADIUS_SM, 2f);
    }

    /** Draws the 2 px drop insertion line used by reordering drag targets. */
    public static void dropLine(Theme t, float x, float y, float w) {
        ImGui.getWindowDrawList().addRectFilled(x, y, x + w, y + 2f, u32(t.color.ACCENT_DEFAULT));
    }

    /** Fills a rounded selection surface. */
    public static void selectionFill(Theme t, float x, float y, float w, float h) {
        ImGui.getWindowDrawList().addRectFilled(x, y, x + w, y + h,
                u32(t.color.SURFACE_SELECTED), t.radius.RADIUS_SM);
    }

    /** Truncates text with an ellipsis so it never clips mid-glyph. */
    public static String ellipsize(String text, float maxWidth) {
        if (text == null || maxWidth <= 0f || ImGui.calcTextSize(text).x <= maxWidth) {
            return text;
        }
        String ellipsis = "\u2026";
        float ellipsisWidth = ImGui.calcTextSize(ellipsis).x;
        int end = text.length();
        while (end > 0 && ImGui.calcTextSize(text.substring(0, end)).x + ellipsisWidth > maxWidth) {
            end--;
        }
        return text.substring(0, end) + ellipsis;
    }

    /** @return a vertically centered text baseline inside a row. */
    public static float textBaseline(float rowTop, float rowHeight) {
        return rowTop + (rowHeight - ImGui.getTextLineHeight()) * 0.5f;
    }

    /** @return the current cursor position as a mutable copy. */
    public static ImVec2 cursor() {
        return new ImVec2(ImGui.getCursorScreenPos());
    }
}
