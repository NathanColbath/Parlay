package io.stratixops.engine.imgui.drag;

/** Where a pointer is currently aimed inside a drop target (design system 3.1/3.8). */
public enum DropZone {

    /** Pointer is not over a target. */
    NONE,
    /** Reorder: drop above this item. */
    BEFORE,
    /** Reorder: drop below this item. */
    AFTER,
    /** Reparent: drop inside this item. */
    INTO,
    /** Target refuses the payload; shown with the no-drop state. */
    INVALID;

    /** @return whether this zone reorders rather than reparents. */
    public boolean isInsertion() {
        return this == BEFORE || this == AFTER;
    }

    /** @return whether this zone reparents into the target. */
    public boolean isInto() {
        return this == INTO;
    }

    /** @return whether the target is highlighted for a valid drop. */
    public boolean isActive() {
        return this != NONE;
    }
}
