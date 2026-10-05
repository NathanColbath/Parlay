package io.stratixops.engine.imgui.drag;

/**
 * Resolves a pointer position into a {@link DropZone} for a target rectangle,
 * applying the 8 px snap tolerance from design system 3.8 so a drop is never
 * ambiguous near a zone boundary.
 */
public final class DropZones {

    private DropZones() {
    }

    /**
     * @param pointerY    pointer y in screen space
     * @param targetY     target top y
     * @param targetH     target height
     * @param acceptsInto whether the target can be reparented into
     * @param actionable  whether the target accepts the payload at all
     * @return the resolved zone; {@link DropZone#NONE} when the pointer is outside
     */
    public static DropZone resolve(float pointerY, float targetY, float targetH,
            boolean acceptsInto, boolean actionable) {
        return resolve(pointerY, targetY, targetH, acceptsInto, actionable, DragConfig.SNAP_DISTANCE);
    }

    /** Snap-tolerance variant used by tests. */
    public static DropZone resolve(float pointerY, float targetY, float targetH,
            boolean acceptsInto, boolean actionable, float snap) {
        if (pointerY < targetY || pointerY > targetY + targetH) {
            return DropZone.NONE;
        }
        if (!actionable) {
            return DropZone.INVALID;
        }
        float bottom = targetY + targetH;
        // Snap tolerance only applies when positive; with zero snap the row
        // falls back to the halfway reorder split so a drop is never ambiguous.
        if (snap > 0f) {
            if (pointerY - targetY <= snap) {
                return DropZone.BEFORE;
            }
            if (bottom - pointerY <= snap) {
                return DropZone.AFTER;
            }
            if (acceptsInto) {
                return DropZone.INTO;
            }
        }
        // No reparenting (or no snap tolerance): split the row in half.
        return pointerY < targetY + targetH * 0.5f ? DropZone.BEFORE : DropZone.AFTER;
    }

    /** @return the index a reorder should insert at, clamped to {@code [0, size]}. */
    public static int insertionIndex(DropZone zone, int index, int size) {
        int at = zone == DropZone.AFTER ? index + 1 : index;
        return Math.max(0, Math.min(at, size));
    }
}
