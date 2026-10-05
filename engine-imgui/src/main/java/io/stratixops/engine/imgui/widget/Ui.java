package io.stratixops.engine.imgui.widget;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImVec2;
import io.stratixops.engine.imgui.theme.Icon;
import io.stratixops.engine.imgui.theme.Rgba;
import io.stratixops.engine.imgui.theme.Shadow;
import io.stratixops.engine.imgui.theme.Theme;
import java.util.List;
import java.util.Locale;

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

    /**
     * Fills the viewport with a scrim on the background draw list (behind all
     * windows). Used by modal overlays such as the command palette. The scrim
     * stays visible under reduce-motion; only the open/close transition snaps
     * (design system 3.5, G&TD W4).
     */
    public static void scrim(int packedColor) {
        ImGuiIO io = ImGui.getIO();
        float[] c = rgba(packedColor);
        ImGui.getBackgroundDrawList().addRectFilled(0f, 0f, io.getDisplaySizeX(), io.getDisplaySizeY(),
                ImGui.getColorU32(c[0], c[1], c[2], c[3]));
    }

    /**
     * @return a vertically centered text baseline inside a row, snapped to the
     *         physical pixel grid so fractional DPI scales do not blur glyphs
     *         (design system section 5.4).
     */
    public static float textBaseline(float rowTop, float rowHeight) {
        return Math.round(rowTop + (rowHeight - ImGui.getTextLineHeight()) * 0.5f);
    }

    /** @return the current cursor position as a mutable copy. */
    public static ImVec2 cursor() {
        return new ImVec2(ImGui.getCursorScreenPos());
    }

    /**
     * @return the Lucide glyph for a semantic icon as a single-character string
     *         in the font atlas's Private Use Area. {@code ?} is returned only
     *         for a null icon; an unmapped icon resolves to Lucide's
     *         {@code circle} placeholder rather than a missing glyph.
     */
    public static String iconGlyph(Icon icon) {
        return icon == null ? "?" : String.valueOf((char) icon.codepoint());
    }

    /** Draws a Lucide icon glyph at {@code (x, textY)} on the current font. */
    public static void drawIcon(Theme t, ImDrawList draw, float x, float textY, int packed,
            Icon icon) {
        draw.addText(x, Math.round(textY), u32(packed), iconGlyph(icon));
    }

    /**
     * Draws a right-aligned {@code KeyHint} (design system section 3): keycaps
     * with {@code radius.xs}, {@code border.default} and {@code text.muted}.
     *
     * @param rightX right edge the hint is aligned to
     * @param textY  top of the row text
     * @return the left edge consumed by the hint
     */
    public static float keyHint(Theme t, ImDrawList draw, float rightX, float textY, String hint) {
        if (hint == null || hint.isBlank()) {
            return rightX;
        }
        float pad = t.space.SPACE_1;
        float capHeight = ImGui.getTextLineHeight() + pad;
        float capY = textY - pad * 0.5f;
        float x = rightX;
        String[] keys = hint.split("\\+");
        for (int i = keys.length - 1; i >= 0; i--) {
            String key = keys[i].trim();
            float width = ImGui.calcTextSizeX(key) + pad * 2f;
            x -= width;
            draw.addRect(x, capY, x + width, capY + capHeight, u32(t.color.BORDER_DEFAULT),
                    t.radius.RADIUS_XS, 1f);
            draw.addText(x + pad, textY, u32(t.color.TEXT_MUTED), key);
            x -= pad;
        }
        return x;
    }

    /**
     * Resolves a type-ahead buffer to the first matching label (prefix first,
     * then substring). Pure so the behavior is testable without input.
     *
     * @return the matching index, or {@code -1}
     */
    public static int typeAheadIndex(List<String> labels, String buffer) {
        if (labels == null || buffer == null || buffer.isEmpty()) {
            return -1;
        }
        String needle = buffer.toLowerCase(Locale.ROOT);
        for (int i = 0; i < labels.size(); i++) {
            String label = labels.get(i);
            if (label != null && label.toLowerCase(Locale.ROOT).startsWith(needle)) {
                return i;
            }
        }
        for (int i = 0; i < labels.size(); i++) {
            String label = labels.get(i);
            if (label != null && label.toLowerCase(Locale.ROOT).contains(needle)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Paints a token-driven drop shadow behind a floating surface. ImGui has no
     * shadow model, so the {@link Shadow} blur is approximated with concentric
     * rounded fills on the background draw list.
     */
    public static void elevation(Theme t, Shadow shadow, float x, float y, float w, float h,
            float rounding) {
        if (shadow == null || shadow.isNone()) {
            return;
        }
        ImDrawList bg = ImGui.getBackgroundDrawList();
        int baseAlpha = Rgba.alpha(shadow.rgba());
        int steps = Math.max(1, shadow.blur());
        for (int i = steps; i >= 1; i--) {
            int alpha = Math.round(baseAlpha * (1f - (float) i / (steps + 1f)));
            if (alpha <= 0) {
                continue;
            }
            int color = Rgba.pack(Rgba.red(shadow.rgba()), Rgba.green(shadow.rgba()),
                    Rgba.blue(shadow.rgba()), alpha);
            bg.addRectFilled(x + shadow.dx() - i, y + shadow.dy() - i,
                    x + shadow.dx() + w + i, y + shadow.dy() + h + i, u32(color), rounding + i);
        }
    }
}
