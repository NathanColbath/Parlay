package io.stratixops.engine.scene;

import io.stratixops.engine.testutil.RecordingComponent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ComponentLifecycleTest {

    @Test
    void awakeEnableStartUpdateOrder() {
        Scene scene = new Scene("lifecycle");
        GameObject object = scene.add(new GameObject());
        RecordingComponent component = object.addComponent(new RecordingComponent());

        scene.update(0.016f);
        assertThat(component.calls).containsExactly("awake", "onEnable", "start", "update");
        assertThat(component.lastDt).isEqualTo(0.016f);

        scene.update(0.016f);
        assertThat(component.calls).containsExactly(
                "awake", "onEnable", "start", "update", "update");
    }

    @Test
    void fixedAndLateUpdateOnlyWhenEnabled() {
        Scene scene = new Scene("lifecycle");
        GameObject object = scene.add(new GameObject());
        RecordingComponent component = object.addComponent(new RecordingComponent());

        scene.fixedUpdate(0.02f);
        scene.lateUpdate(0.02f);
        assertThat(component.calls).contains("fixedUpdate", "lateUpdate");

        component.setEnabled(false);
        assertThat(component.calls).contains("onDisable");
        component.calls.clear();

        scene.fixedUpdate(0.02f);
        scene.lateUpdate(0.02f);
        scene.update(0.02f);
        assertThat(component.calls).isEmpty();

        component.setEnabled(true);
        assertThat(component.calls).containsExactly("onEnable");
    }

    @Test
    void inactiveObjectsDoNotRun() {
        Scene scene = new Scene("lifecycle");
        GameObject object = scene.add(new GameObject());
        object.setActive(false);
        RecordingComponent component = object.addComponent(new RecordingComponent());
        scene.update(0.016f);
        assertThat(component.calls).isEmpty();
    }

    @Test
    void destroyFiresDisableAndDestroyOnce() {
        GameObject object = new GameObject();
        RecordingComponent component = object.addComponent(new RecordingComponent());
        object.driveUpdate(0.016f);
        component.calls.clear();

        object.removeComponent(component);
        assertThat(component.calls).containsExactly("onDisable", "onDestroy");

        component.calls.clear();
        object.driveDestroy();
        assertThat(component.calls).isEmpty();
    }

    @Test
    void behaviourExposesTransform() {
        GameObject object = new GameObject();
        TransformProbe behaviour = object.addComponent(new TransformProbe());
        assertThat(behaviour.transform()).isSameAs(object.transform());
        assertThat(new TransformProbe().transform()).isNull();
    }

    /** A behaviour that can be attached without extra state. */
    public static final class TransformProbe extends Behaviour {
    }
}
