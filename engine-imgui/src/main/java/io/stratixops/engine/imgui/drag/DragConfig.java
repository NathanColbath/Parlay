package io.stratixops.engine.imgui.drag;

/**
 * Interaction constants for the drag-and-drop system (design system 3.8).
 *
 * <p>These are gestures and timings rather than visual tokens, so they do not
 * live in {@code tokens.json}. Every <em>visual</em> value (color, radius,
 * elevation, spacing) is still read from {@link io.stratixops.engine.imgui.theme.Theme}.
 */
public final class DragConfig {

    /** Movement in logical pixels before a press becomes a drag. */
    public static final float THRESHOLD = 4f;

    /** Ghost is drawn this far down/right of the cursor (design system 3.8). */
    public static final float GHOST_OFFSET_X = 12f;
    public static final float GHOST_OFFSET_Y = 12f;

    /** Opacity applied to the whole dragged snapshot. */
    public static final float GHOST_OPACITY = 0.5f;

    /** Reorder insertion line thickness in logical pixels. */
    public static final float INSERTION_THICKNESS = 2f;

    /** Distance from a zone boundary at which the drop snaps to that zone. */
    public static final float SNAP_DISTANCE = 8f;

    /** Auto-scroll activates within this distance of a viewport edge. */
    public static final float AUTO_SCROLL_BAND = 8f;

    /** Auto-scroll distance per frame while in the edge band. */
    public static final float AUTO_SCROLL_SPEED = 8f;

    /** Dwell time before a hovered collapsed folder auto-expands. */
    public static final float AUTO_EXPAND_MS = 500f;

    private DragConfig() {
    }
}
