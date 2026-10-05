package io.stratixops.engine.scene;

import io.stratixops.engine.inspect.SerializeField;
import io.stratixops.engine.math.Vec2;

/**
 * Every {@link GameObject} has exactly one {@code Transform}. It stores the
 * local TRS and derives the world transform by composing its parents.
 *
 * <p>Angles are radians. The derivation is axis-aligned (translation + scale),
 * which is all the 2D runtime needs; rotation is carried for completeness.
 */
public final class Transform extends Component {

    @SerializeField
    private final Vec2 localPosition = new Vec2();

    @SerializeField
    private final Vec2 localScale = new Vec2(1f, 1f);

    private final Vec2 worldPosition = new Vec2();
    private final Vec2 worldScale = new Vec2(1f, 1f);

    @SerializeField
    private float localRotation;

    /**
     * Local (parent-relative) position in pixels. Mutable, reused instance.
     *
     * @return the local position
     */
    public Vec2 localPosition() {
        return localPosition;
    }

    /**
     * Local scale. Mutable, reused instance.
     *
     * @return the local scale
     */
    public Vec2 localScale() {
        return localScale;
    }

    /**
     * Local rotation in radians.
     *
     * @return the local rotation
     */
    public float localRotation() {
        return localRotation;
    }

    /**
     * Sets the local rotation.
     *
     * @param radians rotation in radians
     */
    public void setLocalRotation(float radians) {
        this.localRotation = radians;
    }

    /**
     * Derived world position. Recomputed on demand; reused instance.
     *
     * @return the world position in pixels
     */
    public Vec2 worldPosition() {
        GameObject parent = parentObject();
        if (parent == null) {
            worldPosition.set(localPosition);
        } else {
            Transform pt = parent.transform();
            Vec2 pw = pt.worldPosition();
            Vec2 ps = pt.worldScale();
            worldPosition.set(pw.x + localPosition.x * ps.x, pw.y + localPosition.y * ps.y);
        }
        return worldPosition;
    }

    /**
     * Derived world scale. Recomputed on demand; reused instance.
     *
     * @return the world scale
     */
    public Vec2 worldScale() {
        GameObject parent = parentObject();
        if (parent == null) {
            worldScale.set(localScale);
        } else {
            Vec2 ps = parent.transform().worldScale();
            worldScale.set(ps.x * localScale.x, ps.y * localScale.y);
        }
        return worldScale;
    }

    /**
     * Derived world rotation in radians.
     *
     * @return the world rotation
     */
    public float worldRotation() {
        GameObject parent = parentObject();
        return parent == null ? localRotation : parent.transform().worldRotation() + localRotation;
    }

    private GameObject parentObject() {
        GameObject owner = gameObject();
        return owner == null ? null : owner.parent();
    }
}
