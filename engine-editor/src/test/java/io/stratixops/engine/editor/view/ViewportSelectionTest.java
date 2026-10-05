package io.stratixops.engine.editor.view;

import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Guid;
import io.stratixops.engine.scene.Scene;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ViewportSelectionTest {

    @Test
    void marqueeSelectsIntersectingObjects() {
        Scene scene = new Scene("Level");
        GameObject near = scene.add(new GameObject(Guid.random(), "Near"));
        GameObject far = scene.add(new GameObject(Guid.random(), "Far"));
        far.transform().localPosition().set(1000f, 1000f);

        List<GameObject> hits = ViewportSelection.objectsIn(scene, new ViewRect(-32f, -32f, 32f, 32f));

        assertThat(hits).containsExactly(near);
    }

    @Test
    void marqueeDescendsIntoChildren() {
        Scene scene = new Scene("Level");
        GameObject parent = scene.add(new GameObject(Guid.random(), "Parent"));
        GameObject child = new GameObject(Guid.random(), "Child");
        child.setParent(parent);
        child.transform().localPosition().set(10f, 10f);

        List<GameObject> hits = ViewportSelection.objectsIn(scene, new ViewRect(-64f, -64f, 64f, 64f));

        assertThat(hits).containsExactly(parent, child);
    }

    @Test
    void screenMarqueeMapsThroughCamera() {
        Scene scene = new Scene("Level");
        GameObject centre = scene.add(new GameObject(Guid.random(), "Centre"));
        scene.add(new GameObject(Guid.random(), "Away")).transform().localPosition().set(500f, 500f);

        EditorCamera camera = new EditorCamera();
        camera.setViewport(200f, 200f);

        List<GameObject> hits = ViewportSelection.objectsIn(scene, camera, 0f, 0f, 200f, 200f);

        assertThat(hits).containsExactly(centre);
    }
}
