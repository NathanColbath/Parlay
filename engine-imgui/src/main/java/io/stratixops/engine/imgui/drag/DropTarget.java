package io.stratixops.engine.imgui.drag;

import io.stratixops.engine.imgui.theme.Theme;

/**
 * A candidate drop target registered for the current frame.
 *
 * @param id   stable id of the hovered item (row, cell, breadcrumb segment)
 * @param zone resolution of the pointer inside the item
 * @param x    target rect top-left x in screen space
 * @param y    target rect top-left y in screen space
 * @param w    target rect width
 * @param h    target rect height
 */
public record DropTarget(String id, DropZone zone, float x, float y, float w, float h) {

    /** @return an empty target used when the pointer is over nothing. */
    public static DropTarget none() {
        return new DropTarget(null, DropZone.NONE, 0f, 0f, 0f, 0f);
    }

    /** A target centered on a row {@code h} tall. */
    public static DropTarget row(String id, DropZone zone, float x, float y, float w, float h) {
        return new DropTarget(id, zone, x, y, w, h);
    }

    /** @return the target rect as a {@code [x, y, w, h]} array. */
    public float[] rect() {
        return new float[] {x, y, w, h};
    }

    /** @return the y of a reorder insertion line for this target and theme. */
    public float insertionY(Theme theme) {
        if (zone == DropZone.AFTER) {
            return y + h - DragConfig.INSERTION_THICKNESS;
        }
        return y;
    }
}
