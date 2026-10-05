package io.stratixops.engine.editor.fixture;

import io.stratixops.engine.inspect.Header;
import io.stratixops.engine.inspect.SerializeField;
import io.stratixops.engine.math.Vec2;
import io.stratixops.engine.scene.Behaviour;
import io.stratixops.engine.scene.ObjectRef;

/**
 * A user-style {@link Behaviour} exercised by the editor tests.
 */
public class TestBehaviour extends Behaviour {

    @SerializeField
    public int health = 3;

    @SerializeField
    public float speed = 12.5f;

    @Header("Motion")
    @SerializeField
    public Vec2 velocity = new Vec2(1f, 2f);

    @SerializeField
    public ObjectRef target = new ObjectRef(null);

    @SerializeField
    public String label = "behaviour";

    public Object unsupported = new Object();

    public int updates;

    public int fixedUpdates;

    public boolean scratch;

    @Override
    public void fixedUpdate(float dt) {
        fixedUpdates++;
    }

    @Override
    public void update(float dt) {
        updates++;
    }
}
