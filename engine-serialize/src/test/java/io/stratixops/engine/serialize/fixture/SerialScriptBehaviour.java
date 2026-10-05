package io.stratixops.engine.serialize.fixture;

import io.stratixops.engine.inspect.Color;
import io.stratixops.engine.inspect.NonSerialized;
import io.stratixops.engine.inspect.SerializeField;
import io.stratixops.engine.math.Vec2;
import io.stratixops.engine.scene.AssetRef;
import io.stratixops.engine.scene.Behaviour;
import io.stratixops.engine.scene.ObjectRef;
import io.stratixops.engine.scene.PrefabRef;

import java.util.ArrayList;
import java.util.List;

/**
 * A user behaviour exercising every supported property kind for the
 * serialization round-trip tests.
 */
public class SerialScriptBehaviour extends Behaviour {

    /** A sample enum. */
    public enum Mode {
        /** Waiting. */
        IDLE,
        /** Moving. */
        RUN
    }

    /** A private opted-in string. */
    @SerializeField
    private String label = "hello";

    /** A public float. */
    public float speed = 1.5f;

    /** A private packed color. */
    @SerializeField
    @Color
    private int color = 0x0A0B0C0D;

    /** A public long. */
    public long ticks = 9L;

    /** A public double. */
    public double scale = 2.25d;

    /** A public boolean. */
    public boolean active = true;

    /** A public vector. */
    public Vec2 offset = new Vec2(1f, 2f);

    /** A public enum. */
    public Mode mode = Mode.RUN;

    /** A private asset reference. */
    @SerializeField
    private AssetRef sprite;

    /** A private object reference. */
    @SerializeField
    private ObjectRef target;

    /** A private prefab reference. */
    @SerializeField
    private PrefabRef prefab;

    /** A private list of asset references. */
    @SerializeField
    private final List<AssetRef> path = new ArrayList<>();

    /** A field excluded from serialization. */
    @NonSerialized
    public String runtimeOnly = "temp";

    /** A private field never serialized. */
    private String hidden = "hidden";

    /**
     * Sets the sprite reference.
     *
     * @param sprite the reference
     */
    public void setSprite(AssetRef sprite) {
        this.sprite = sprite;
    }

    /**
     * Sets the target reference.
     *
     * @param target the reference
     */
    public void setTarget(ObjectRef target) {
        this.target = target;
    }

    /**
     * Sets the prefab reference.
     *
     * @param prefab the reference
     */
    public void setPrefab(PrefabRef prefab) {
        this.prefab = prefab;
    }

    /**
     * Sets the label.
     *
     * @param label the label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the label
     */
    public String label() {
        return label;
    }

    /**
     * @return the color
     */
    public int color() {
        return color;
    }

    /**
     * @return the sprite reference
     */
    public AssetRef sprite() {
        return sprite;
    }

    /**
     * @return the target reference
     */
    public ObjectRef target() {
        return target;
    }

    /**
     * @return the prefab reference
     */
    public PrefabRef prefab() {
        return prefab;
    }

    /**
     * @return the path list
     */
    public List<AssetRef> path() {
        return path;
    }
}
