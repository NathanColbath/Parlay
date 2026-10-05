package io.stratixops.engine.imgui.drag;

/**
 * Frame-persistent drag state machine shared by every drag source and drop
 * target (design system 3.8). It owns no rendering and no ImGui types, so the
 * whole gesture — threshold, modifiers, auto-scroll, auto-expand, snap-back —
 * is unit-testable headlessly.
 *
 * <p>Lifecycle: {@code press} begins a pending drag; the first {@code move}
 * past {@link DragConfig#THRESHOLD} promotes it to {@link Phase#DRAGGING}; drop
 * targets are reported with {@link #hover(DropTarget)} each frame; {@link #release()}
 * yields the outcome and {@link #cancel(float)} snaps the ghost back.
 */
public final class DragSession {

    /** Drag lifecycle state. */
    public enum Phase {
        IDLE,
        PENDING,
        DRAGGING,
        SNAPPING_BACK
    }

    private Phase phase = Phase.IDLE;

    private float pressX;
    private float pressY;
    private float pointerX;
    private float pointerY;
    private float snapshotW;
    private float snapshotH;

    private DragPayload payload = new DragPayload("", java.util.List.of());
    private DragModifier modifier = DragModifier.MOVE;
    private DropTarget candidate;

    private String hoverFolderId;
    private float hoverFolderMs;
    private String pendingExpandId;

    private float snapFromX;
    private float snapFromY;
    private float snapElapsedMs;
    private float snapDurationMs;

    /** Clears the per-frame drop candidate. Call once at the start of a frame. */
    public void beginFrame() {
        candidate = null;
    }

    /** Records a press on a source of the given snapshot size. */
    public void press(float x, float y, float width, float height, DragPayload dragPayload) {
        pressX = x;
        pressY = y;
        pointerX = x;
        pointerY = y;
        snapshotW = Math.max(0f, width);
        snapshotH = Math.max(0f, height);
        payload = dragPayload == null ? new DragPayload("", java.util.List.of()) : dragPayload;
        modifier = DragModifier.MOVE;
        candidate = null;
        hoverFolderId = null;
        hoverFolderMs = 0f;
        pendingExpandId = null;
        phase = Phase.PENDING;
    }

    /** Moves the pointer; promotes a pending press to a drag past the threshold. */
    public void move(float x, float y) {
        pointerX = x;
        pointerY = y;
        if (phase == Phase.PENDING) {
            float dx = x - pressX;
            float dy = y - pressY;
            if (Math.hypot(dx, dy) >= DragConfig.THRESHOLD) {
                phase = Phase.DRAGGING;
            }
        }
    }

    /** Reports the target under the pointer for this frame. */
    public void hover(DropTarget target) {
        if (phase != Phase.DRAGGING) {
            return;
        }
        candidate = target == null || target.zone() == DropZone.NONE ? null : target;
    }

    /** Sets the active modifier. Ignored unless a drag is underway. */
    public void setModifier(DragModifier next) {
        if (phase == Phase.PENDING || phase == Phase.DRAGGING) {
            modifier = next == null ? DragModifier.MOVE : next;
        }
    }

    /**
     * Ends a drag, returning the outcome, or {@code null} for a click that never
     * crossed the threshold. A release outside any target is a no-op.
     */
    public DropResult release() {
        if (phase != Phase.DRAGGING) {
            reset();
            return null;
        }
        DropResult result;
        if (candidate == null) {
            result = new DropResult(null, DropZone.NONE, modifier, payload);
        } else {
            result = new DropResult(candidate.id(), candidate.zone(), modifier, payload);
        }
        reset();
        return result;
    }

    /**
     * Cancels the active drag and animates the ghost back to its origin
     * (design system 3.8 "Esc cancels; subtle snap-back").
     *
     * @param snapDurationMs animation length; {@code 0} snaps instantly
     */
    public void cancel(float snapDurationMs) {
        if (phase == Phase.PENDING) {
            reset();
            return;
        }
        if (phase != Phase.DRAGGING) {
            return;
        }
        snapFromX = ghostX();
        snapFromY = ghostY();
        snapElapsedMs = 0f;
        // The parameter shadows the field; assign the field explicitly so the
        // snap duration survives for advance()/progress().
        this.snapDurationMs = Math.max(0f, snapDurationMs);
        candidate = null;
        phase = Phase.SNAPPING_BACK;
        if (this.snapDurationMs == 0f) {
            reset();
        }
    }

    /** Advances the snap-back animation. */
    public void advance(float dtSeconds) {
        if (phase != Phase.SNAPPING_BACK) {
            return;
        }
        snapElapsedMs += Math.max(0f, dtSeconds) * 1000f;
        if (snapElapsedMs >= snapDurationMs) {
            reset();
        }
    }

    /**
     * Tracks dwell on a collapsed folder during a drag.
     *
     * @return {@code true} once the 500 ms auto-expand threshold has been reached
     */
    public boolean hoverFolder(String folderId, boolean collapsed, float dtSeconds) {
        if (phase != Phase.DRAGGING || !collapsed || folderId == null) {
            hoverFolderId = null;
            hoverFolderMs = 0f;
            return false;
        }
        if (!folderId.equals(hoverFolderId)) {
            hoverFolderId = folderId;
            hoverFolderMs = 0f;
        }
        hoverFolderMs += Math.max(0f, dtSeconds) * 1000f;
        if (hoverFolderMs >= DragConfig.AUTO_EXPAND_MS) {
            pendingExpandId = folderId;
        }
        return pendingExpandId != null;
    }

    /** @return and clears the folder that should be expanded this frame. */
    public String consumeAutoExpand() {
        String id = pendingExpandId;
        pendingExpandId = null;
        return id;
    }

    /**
     * @return auto-scroll delta in pixels for one axis: {@code -8} toward the
     *         start edge, {@code +8} toward the end edge, {@code 0} outside the band
     */
    public float autoScroll(float pointer, float min, float max) {
        if (phase != Phase.DRAGGING) {
            return 0f;
        }
        if (pointer - min < DragConfig.AUTO_SCROLL_BAND) {
            return -DragConfig.AUTO_SCROLL_SPEED;
        }
        if (max - pointer < DragConfig.AUTO_SCROLL_BAND) {
            return DragConfig.AUTO_SCROLL_SPEED;
        }
        return 0f;
    }

    /** @return the ghost's top-left x (offset while dragging, interpolated while snapping back). */
    public float ghostX() {
        if (phase == Phase.DRAGGING) {
            return pointerX + DragConfig.GHOST_OFFSET_X;
        }
        if (phase == Phase.SNAPPING_BACK) {
            return lerp(snapFromX, pressX, snapProgress());
        }
        return 0f;
    }

    /** @return the ghost's top-left y. */
    public float ghostY() {
        if (phase == Phase.DRAGGING) {
            return pointerY + DragConfig.GHOST_OFFSET_Y;
        }
        if (phase == Phase.SNAPPING_BACK) {
            return lerp(snapFromY, pressY, snapProgress());
        }
        return 0f;
    }

    /** @return snap-back progress in {@code [0, 1]}. */
    public float progress() {
        return snapProgress();
    }

    private float snapProgress() {
        if (snapDurationMs <= 0f) {
            return 1f;
        }
        return Math.min(1f, snapElapsedMs / snapDurationMs);
    }

    private static float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }

    private void reset() {
        phase = Phase.IDLE;
        candidate = null;
        hoverFolderId = null;
        hoverFolderMs = 0f;
        pendingExpandId = null;
        snapElapsedMs = 0f;
        snapDurationMs = 0f;
    }

    public Phase phase() {
        return phase;
    }

    public boolean isActive() {
        return phase != Phase.IDLE;
    }

    public boolean isPending() {
        return phase == Phase.PENDING;
    }

    public boolean isDragging() {
        return phase == Phase.DRAGGING;
    }

    public DragPayload payload() {
        return payload;
    }

    public DragModifier modifier() {
        return modifier;
    }

    /** @return the current drop candidate, or {@code null}. */
    public DropTarget candidate() {
        return candidate;
    }

    public float pointerX() {
        return pointerX;
    }

    public float pointerY() {
        return pointerY;
    }

    public float snapshotWidth() {
        return snapshotW;
    }

    public float snapshotHeight() {
        return snapshotH;
    }
}
