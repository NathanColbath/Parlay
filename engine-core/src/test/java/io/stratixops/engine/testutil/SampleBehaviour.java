package io.stratixops.engine.testutil;

import io.stratixops.engine.inspect.Color;
import io.stratixops.engine.inspect.Header;
import io.stratixops.engine.inspect.NonSerialized;
import io.stratixops.engine.inspect.Order;
import io.stratixops.engine.inspect.Range;
import io.stratixops.engine.inspect.SerializeField;
import io.stratixops.engine.inspect.Tooltip;
import io.stratixops.engine.math.Vec2;
import io.stratixops.engine.scene.AssetRef;
import io.stratixops.engine.scene.Behaviour;
import io.stratixops.engine.scene.ObjectRef;
import io.stratixops.engine.scene.PrefabRef;

import java.util.ArrayList;
import java.util.List;

/**
 * A behaviour exercising every supported property kind and every exposure
 * rule, shared by core and serialization tests.
 */
public class SampleBehaviour extends Behaviour {

    /** A supported enum. */
    public enum Mood {
        /** Cheerful. */
        HAPPY,
        /** Gloomy. */
        SAD
    }

    /** A public field, serialized by the public convention. */
    public String publicLabel = "public";

    /** A private field opted in explicitly. */
    @SerializeField
    private String secretLabel = "secret";

    /** A public field opted out explicitly. */
    @NonSerialized
    public String ignoredLabel = "ignored";

    /** A private field that is never serialized. */
    private String hiddenLabel = "hidden";

    /** A constant, excluded because it is final. */
    public final int constant = 7;

    /** A static field, excluded. */
    public static String staticLabel = "static";

    /** A ranged float. */
    @SerializeField
    @Range(min = 0f, max = 10f)
    private float speed = 2.5f;

    /** A header/tooltip/order annotated int. */
    @SerializeField
    @Header("Combat")
    @Tooltip("How hard it hits")
    @Order(1)
    private int power = 3;

    /** A packed color. */
    @SerializeField
    @Color
    private int tint = 0x11223344;

    /** A long. */
    @SerializeField
    private long big = 42L;

    /** A double. */
    @SerializeField
    private double ratio = 1.5d;

    /** A boolean. */
    @SerializeField
    private boolean enabledFlag = true;

    /** A vector. */
    @SerializeField
    private final Vec2 point = new Vec2(4f, 5f);

    /** An enum. */
    @SerializeField
    private Mood mood = Mood.HAPPY;

    /** An asset reference. */
    @SerializeField
    private AssetRef sprite;

    /** An object reference. */
    @SerializeField
    private ObjectRef target;

    /** A prefab reference. */
    @SerializeField
    private PrefabRef prefab;

    /** A list of numbers. */
    @SerializeField
    private final List<Float> weights = new ArrayList<>();

    /** A list of enum values. */
    @SerializeField
    private final List<Mood> moods = new ArrayList<>();

    /** A field of an unsupported type (omitted from serialization). */
    @SerializeField
    private Runnable unsupported = () -> {
    };

    /** Creates a sample with a couple of list entries. */
    public SampleBehaviour() {
        weights.add(1.5f);
        weights.add(2.5f);
        moods.add(Mood.SAD);
    }
}
