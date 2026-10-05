package io.stratixops.engine.imgui.drag;

/**
 * Keyboard modifier that changes what a drop does (design system 3.8):
 * plain move/reparent, {@code Ctrl} copy, {@code Alt} create reference/link.
 */
public enum DragModifier {

    MOVE(null),
    COPY("+"),
    LINK("\u2197");

    private final String badge;

    DragModifier(String badge) {
        this.badge = badge;
    }

    /** @return the cursor glyph for the modifier, or {@code null} for plain move. */
    public String badge() {
        return badge;
    }

    /** @return whether a modifier badge should be drawn on the ghost. */
    public boolean hasBadge() {
        return badge != null;
    }

    /** @return the modifier implied by the current key state. */
    public static DragModifier of(boolean ctrl, boolean alt) {
        if (alt) {
            return LINK;
        }
        if (ctrl) {
            return COPY;
        }
        return MOVE;
    }
}
