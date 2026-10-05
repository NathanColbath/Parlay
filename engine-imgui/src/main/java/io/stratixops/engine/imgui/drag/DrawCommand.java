package io.stratixops.engine.imgui.drag;

/**
 * One resolved drawing operation produced by {@link DragVisuals}.
 *
 * <p>Keeping the drag visuals as data lets the exact commands (colors,
 * geometry, thickness) be asserted in a headless test without a GPU, while the
 * same list is replayed onto ImGui's foreground draw list by
 * {@link DragOverlay}.
 *
 * @param kind      what the command represents
 * @param x         top-left x (or text x for text commands)
 * @param y         top-left y (or text y for text commands)
 * @param w         width; unused by text commands
 * @param h         height; unused by text commands
 * @param color     packed {@code 0xRRGGBBAA}, read from {@code Theme}
 * @param rounding  corner radius in logical pixels
 * @param thickness stroke width in logical pixels; {@code 0} for fills
 * @param text      label for text commands, otherwise {@code null}
 */
public record DrawCommand(Kind kind, float x, float y, float w, float h, int color,
        float rounding, float thickness, String text) {

    /** The individual operations a drag frame is composed of. */
    public enum Kind {
        SHADOW,
        GHOST_FILL,
        GHOST_BORDER,
        BADGE_FILL,
        BADGE_BORDER,
        BADGE_TEXT,
        MODIFIER_FILL,
        MODIFIER_TEXT,
        INSERTION_LINE,
        DROP_FILL,
        DROP_BORDER,
        INVALID_BORDER
    }

    public static DrawCommand fill(Kind kind, float x, float y, float w, float h, int color,
            float rounding) {
        return new DrawCommand(kind, x, y, w, h, color, rounding, 0f, null);
    }

    public static DrawCommand stroke(Kind kind, float x, float y, float w, float h, int color,
            float rounding, float thickness) {
        return new DrawCommand(kind, x, y, w, h, color, rounding, thickness, null);
    }

    public static DrawCommand text(Kind kind, float x, float y, int color, String text) {
        return new DrawCommand(kind, x, y, 0f, 0f, color, 0f, 0f, text);
    }

    public boolean isFill() {
        return kind == Kind.SHADOW || kind == Kind.GHOST_FILL || kind == Kind.BADGE_FILL
                || kind == Kind.MODIFIER_FILL || kind == Kind.INSERTION_LINE || kind == Kind.DROP_FILL;
    }

    public boolean isStroke() {
        return kind == Kind.GHOST_BORDER || kind == Kind.BADGE_BORDER || kind == Kind.DROP_BORDER
                || kind == Kind.INVALID_BORDER;
    }

    public boolean isText() {
        return kind == Kind.BADGE_TEXT || kind == Kind.MODIFIER_TEXT;
    }
}
