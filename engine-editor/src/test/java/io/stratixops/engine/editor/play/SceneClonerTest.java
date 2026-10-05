package io.stratixops.engine.editor.play;

import io.stratixops.engine.editor.fixture.TestBehaviour;
import io.stratixops.engine.inspect.ComponentRegistry;
import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Guid;
import io.stratixops.engine.scene.ObjectRef;
import io.stratixops.engine.scene.Scene;
import io.stratixops.engine.scene.UnsupportedComponent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SceneClonerTest {

    static ComponentRegistry registry() {
        ComponentRegistry registry = ComponentRegistry.withBuiltins();
        registry.register(TestBehaviour.class);
        return registry;
    }

    @Test
    void deepCopyIsIndependentAndPreservesHierarchy() {
        Scene source = new Scene("Level");
        GameObject parent = source.add(new GameObject(Guid.random(), "Parent"));
        GameObject child = new GameObject(Guid.random(), "Child");
        child.setParent(parent);
        TestBehaviour behaviour = parent.addComponent(new TestBehaviour());

        Scene copy = SceneCloner.deepCopy(source, registry(), false);

        GameObject copiedParent = copy.gameObjects().get(0);
        assertThat(copiedParent).isNotSameAs(parent);
        assertThat(copiedParent.guid()).isEqualTo(parent.guid());
        assertThat(copiedParent.children()).hasSize(1);
        assertThat(copiedParent.children().get(0).parent()).isEqualTo(copiedParent);

        TestBehaviour copiedBehaviour = copiedParent.getComponent(TestBehaviour.class);
        assertThat(copiedBehaviour).isNotSameAs(behaviour);
        behaviour.velocity.set(9f, 9f);
        assertThat(copiedBehaviour.velocity.x).isEqualTo(1f);
    }

    @Test
    void freshGuidsRemapObjectReferences() {
        Scene source = new Scene("Level");
        GameObject a = source.add(new GameObject(Guid.random(), "A"));
        GameObject b = source.add(new GameObject(Guid.random(), "B"));
        TestBehaviour behaviour = a.addComponent(new TestBehaviour());
        behaviour.target = new ObjectRef(b.guid());

        Scene copy = SceneCloner.deepCopy(source, registry(), true);

        GameObject copiedA = copy.gameObjects().get(0);
        GameObject copiedB = copy.gameObjects().get(1);
        assertThat(copiedA.guid()).isNotEqualTo(a.guid());
        assertThat(copiedA.getComponent(TestBehaviour.class).target.guid())
                .isEqualTo(copiedB.guid());
    }

    @Test
    void preservesUnsupportedComponents() {
        Scene source = new Scene("Level");
        GameObject object = source.add(new GameObject(Guid.random(), "A"));
        object.addComponent(new UnsupportedComponent("custom/thing", "{\"v\":1}"));

        Scene copy = SceneCloner.deepCopy(source, registry(), false);

        UnsupportedComponent preserved = copy.gameObjects().get(0).getComponent(UnsupportedComponent.class);
        assertThat(preserved).isNotNull();
        assertThat(preserved.typeId()).isEqualTo("custom/thing");
        assertThat(preserved.rawJson()).isEqualTo("{\"v\":1}");
    }

    @Test
    void duplicateGivesFreshGuidsAndOffsetFreeSubtree() {
        Scene source = new Scene("Level");
        GameObject root = source.add(new GameObject(Guid.random(), "Root"));
        GameObject child = new GameObject(Guid.random(), "Child");
        child.setParent(root);
        root.addComponent(new TestBehaviour());

        GameObject copy = SceneCloner.duplicate(root, registry());

        assertThat(copy).isNotSameAs(root);
        assertThat(copy.guid()).isNotEqualTo(root.guid());
        assertThat(copy.parent()).isNull();
        assertThat(copy.children()).hasSize(1);
        assertThat(copy.children().get(0).parent()).isEqualTo(copy);
        assertThat(copy.getComponent(TestBehaviour.class)).isNotNull();
    }
}
