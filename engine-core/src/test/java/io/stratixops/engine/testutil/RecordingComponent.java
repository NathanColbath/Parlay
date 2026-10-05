package io.stratixops.engine.testutil;

import io.stratixops.engine.scene.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * A component that records its lifecycle callbacks for assertions.
 */
public final class RecordingComponent extends Component {

    /** Ordered lifecycle events. */
    public final List<String> calls = new ArrayList<>();
    /** The last {@code update} delta. */
    public float lastDt;

    @Override
    public void awake() {
        calls.add("awake");
    }

    @Override
    public void onEnable() {
        calls.add("onEnable");
    }

    @Override
    public void start() {
        calls.add("start");
    }

    @Override
    public void update(float dt) {
        calls.add("update");
        lastDt = dt;
    }

    @Override
    public void lateUpdate(float dt) {
        calls.add("lateUpdate");
    }

    @Override
    public void fixedUpdate(float dt) {
        calls.add("fixedUpdate");
    }

    @Override
    public void onDisable() {
        calls.add("onDisable");
    }

    @Override
    public void onDestroy() {
        calls.add("onDestroy");
    }
}
