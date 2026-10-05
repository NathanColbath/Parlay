package io.stratixops.engine.editor.view;

import io.stratixops.engine.math.Vec2;

/**
 * Snapping rules for the transform gizmos (STR-41). Kept pure so the exact
 * snapped value can be unit-tested headless. Angles are radians, matching the
 * engine convention.
 */
public final class GizmoMath {

    /** Move snap step in world pixels. */
    public static final float MOVE_SNAP = 16f;
    /** Rotate snap step in degrees. */
    public static final float ROTATE_SNAP_DEGREES = 15f;
    /** Scale snap step (a factor of 0.1). */
    public static final float SCALE_SNAP = 0.1f;

    private GizmoMath() {
    }

    /**
     * Snaps a translation component to the move grid when snapping is enabled.
     *
     * @param value the raw world coordinate
     * @param snap  whether snapping is enabled
     * @return the snapped coordinate
     */
    public static float snapMove(float value, boolean snap) {
        return snap ? Math.round(value / MOVE_SNAP) * MOVE_SNAP : value;
    }

    /**
     * Snaps a translation to the move grid when snapping is enabled.
     *
     * @param value the raw world position
     * @param snap  whether snapping is enabled
     * @return a fresh snapped position
     */
    public static Vec2 snapMove(Vec2 value, boolean snap) {
        return new Vec2(snapMove(value.x, snap), snapMove(value.y, snap));
    }

    /**
     * Snaps a rotation to the rotate grid when snapping is enabled.
     *
     * @param radians the raw rotation
     * @param snap    whether snapping is enabled
     * @return the snapped rotation in radians
     */
    public static float snapRotation(float radians, boolean snap) {
        if (!snap) {
            return radians;
        }
        float step = (float) Math.toRadians(ROTATE_SNAP_DEGREES);
        return Math.round(radians / step) * step;
    }

    /**
     * Snaps a scale component to the scale grid when snapping is enabled.
     *
     * @param value the raw scale
     * @param snap  whether snapping is enabled
     * @return the snapped scale
     */
    public static float snapScale(float value, boolean snap) {
        return snap ? Math.round(value / SCALE_SNAP) * SCALE_SNAP : value;
    }

    /**
     * Snaps a scale to the scale grid when snapping is enabled.
     *
     * @param value the raw scale
     * @param snap  whether snapping is enabled
     * @return a fresh snapped scale
     */
    public static Vec2 snapScale(Vec2 value, boolean snap) {
        return new Vec2(snapScale(value.x, snap), snapScale(value.y, snap));
    }
}
