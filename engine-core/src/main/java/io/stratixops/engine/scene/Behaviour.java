package io.stratixops.engine.scene;

/**
 * Base class for user gameplay scripts (ADR-0007).
 *
 * <p>Extend this, add fields, and attach the behaviour to a {@link GameObject}.
 * Fields that are {@code public}/{@code @SerializeField} are serialized and
 * shown in the Inspector automatically. Every {@code Behaviour} must have a
 * public no-argument constructor; this is required so the loader can
 * reconstruct it and is validated at registration time.
 */
public abstract class Behaviour extends Component {

    /**
     * Convenience accessor for the sibling {@link Transform}.
     *
     * @return the owning object's transform, or {@code null} if detached
     */
    public final Transform transform() {
        GameObject owner = gameObject();
        return owner == null ? null : owner.transform();
    }
}
