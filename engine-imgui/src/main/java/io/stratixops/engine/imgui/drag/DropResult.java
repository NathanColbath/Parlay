package io.stratixops.engine.imgui.drag;

/**
 * The outcome of a completed drag. {@code targetId} is {@code null} for a
 * cancelled or out-of-target drop (a no-op with snap-back).
 *
 * @param targetId id of the target the drop landed on
 * @param zone     how the payload lands on the target
 * @param modifier move, copy or link
 * @param payload  the dragged selection
 */
public record DropResult(String targetId, DropZone zone, DragModifier modifier, DragPayload payload) {

    /** @return whether the drop landed on an accepting target. */
    public boolean accepted() {
        return targetId != null && zone != DropZone.NONE && zone != DropZone.INVALID;
    }
}
