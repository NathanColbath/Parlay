package io.stratixops.engine.scene;

import io.stratixops.engine.inspect.SerializeField;
import io.stratixops.engine.inspect.Tooltip;
import io.stratixops.engine.math.Vec2;

/**
 * An axis-aligned box used for overlap <em>queries</em> only (no simulation).
 * Gameplay code calls {@link #overlaps(Collider2D)} / {@link #contains(Vec2)};
 * the engine does not run a physics step.
 *
 * <p>The world box is the transform world position plus {@link #offset()},
 * with size scaled by the transform world scale.
 */
public final class Collider2D extends Component {

    @SerializeField
    @Tooltip("Centre offset from the object position, in pixels.")
    private final Vec2 offset = new Vec2();

    @SerializeField
    private final Vec2 size = new Vec2(16f, 16f);

    @SerializeField
    private boolean trigger;

    /** Creates a 16x16 trigger-capable box. */
    public Collider2D() {
    }

    /**
     * Centre offset from the object position. Mutable, reused instance.
     *
     * @return the offset
     */
    public Vec2 offset() {
        return offset;
    }

    /**
     * Box size in local pixels. Mutable, reused instance.
     *
     * @return the size
     */
    public Vec2 size() {
        return size;
    }

    /**
     * @return true when the collider is a trigger
     */
    public boolean trigger() {
        return trigger;
    }

    /**
     * Sets the trigger flag.
     *
     * @param trigger true for a trigger
     */
    public void setTrigger(boolean trigger) {
        this.trigger = trigger;
    }

    /**
     * Tests whether this box overlaps another box (touching edges do not
     * count).
     *
     * @param other the other collider
     * @return true on overlap
     */
    public boolean overlaps(Collider2D other) {
        Vec2 aMin = min();
        Vec2 aMax = max();
        Vec2 bMin = other.min();
        Vec2 bMax = other.max();
        return aMin.x < bMax.x && aMax.x > bMin.x && aMin.y < bMax.y && aMax.y > bMin.y;
    }

    /**
     * Tests whether a world point is inside this box.
     *
     * @param point world position
     * @return true when inside
     */
    public boolean contains(Vec2 point) {
        Vec2 min = min();
        Vec2 max = max();
        return point.x >= min.x && point.x <= max.x && point.y >= min.y && point.y <= max.y;
    }

    private Vec2 min() {
        Vec2 c = center();
        Vec2 h = halfSize();
        return new Vec2(c.x - h.x, c.y - h.y);
    }

    private Vec2 max() {
        Vec2 c = center();
        Vec2 h = halfSize();
        return new Vec2(c.x + h.x, c.y + h.y);
    }

    private Vec2 center() {
        Transform t = gameObject() == null ? null : gameObject().transform();
        Vec2 p = t == null ? new Vec2() : t.worldPosition();
        return new Vec2(p.x + offset.x, p.y + offset.y);
    }

    private Vec2 halfSize() {
        Transform t = gameObject() == null ? null : gameObject().transform();
        float sx = t == null ? 1f : Math.abs(t.worldScale().x);
        float sy = t == null ? 1f : Math.abs(t.worldScale().y);
        return new Vec2(size.x * sx * 0.5f, size.y * sy * 0.5f);
    }
}
