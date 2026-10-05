package io.stratixops.engine.editor.command;

import io.stratixops.engine.editor.fixture.TestBehaviour;
import io.stratixops.engine.inspect.ComponentDescriptor;
import io.stratixops.engine.inspect.ComponentRegistry;
import io.stratixops.engine.math.Vec2;
import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Guid;
import io.stratixops.engine.scene.Scene;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EditCommandsTest {

    private static ComponentRegistry registry() {
        ComponentRegistry registry = ComponentRegistry.withBuiltins();
        registry.register(TestBehaviour.class);
        return registry;
    }

    @Test
    void createAddsAndUndoes() {
        Scene scene = new Scene("Level");
        CommandStack stack = new CommandStack();
        GameObject object = new GameObject(Guid.random(), "New");

        stack.execute(EditCommands.create(scene, object, null));
        assertThat(scene.gameObjects()).containsExactly(object);

        stack.undo();
        assertThat(scene.gameObjects()).isEmpty();

        stack.redo();
        assertThat(scene.gameObjects()).containsExactly(object);
    }

    @Test
    void createUnderParentUsesChildList() {
        Scene scene = new Scene("Level");
        CommandStack stack = new CommandStack();
        GameObject parent = scene.add(new GameObject(Guid.random(), "Parent"));
        GameObject child = new GameObject(Guid.random(), "Child");

        stack.execute(EditCommands.create(scene, child, parent));

        assertThat(scene.gameObjects()).containsExactly(parent);
        assertThat(parent.children()).containsExactly(child);
        assertThat(child.parent()).isEqualTo(parent);
    }

    @Test
    void deleteAndUndoRestoreOrder() {
        Scene scene = new Scene("Level");
        GameObject a = scene.add(new GameObject(Guid.random(), "A"));
        GameObject b = scene.add(new GameObject(Guid.random(), "B"));
        GameObject c = scene.add(new GameObject(Guid.random(), "C"));
        CommandStack stack = new CommandStack();

        stack.execute(EditCommands.delete(scene, List.of(b)));
        assertThat(scene.gameObjects()).containsExactly(a, c);

        stack.undo();
        assertThat(scene.gameObjects()).containsExactly(a, b, c);
    }

    @Test
    void deleteSkipsDescendantsOfDeletedAncestor() {
        Scene scene = new Scene("Level");
        GameObject parent = scene.add(new GameObject(Guid.random(), "Parent"));
        GameObject child = new GameObject(Guid.random(), "Child");
        child.setParent(parent);
        CommandStack stack = new CommandStack();

        stack.execute(EditCommands.delete(scene, List.of(parent, child)));
        assertThat(scene.gameObjects()).isEmpty();

        stack.undo();
        assertThat(scene.gameObjects()).containsExactly(parent);
        assertThat(parent.children()).containsExactly(child);
    }

    @Test
    void renameIsReversible() {
        GameObject object = new GameObject(Guid.random(), "Old");
        CommandStack stack = new CommandStack();

        stack.execute(EditCommands.rename(object, "New"));
        assertThat(object.name()).isEqualTo("New");
        stack.undo();
        assertThat(object.name()).isEqualTo("Old");
    }

    @Test
    void setEnabledIsReversible() {
        TestBehaviour behaviour = new TestBehaviour();
        new GameObject(Guid.random(), "Host").addComponent(behaviour);
        CommandStack stack = new CommandStack();

        stack.execute(EditCommands.setEnabled(behaviour, false));
        assertThat(behaviour.enabled()).isFalse();
        stack.undo();
        assertThat(behaviour.enabled()).isTrue();
    }

    @Test
    void setFieldCopiesVec2AndIsReversible() {
        TestBehaviour behaviour = new TestBehaviour();
        new GameObject(Guid.random(), "Host").addComponent(behaviour);
        ComponentDescriptor descriptor = registry().descriptor(TestBehaviour.class);
        CommandStack stack = new CommandStack();

        stack.execute(EditCommands.setField(behaviour, descriptor.field("velocity"), new Vec2(5f, 6f)));
        assertThat(behaviour.velocity.x).isEqualTo(5f);
        assertThat(behaviour.velocity.y).isEqualTo(6f);

        stack.undo();
        assertThat(behaviour.velocity.x).isEqualTo(1f);
        assertThat(behaviour.velocity.y).isEqualTo(2f);
    }

    @Test
    void reparentRestoresOriginalPosition() {
        Scene scene = new Scene("Level");
        GameObject a = scene.add(new GameObject(Guid.random(), "A"));
        GameObject b = scene.add(new GameObject(Guid.random(), "B"));
        GameObject move = scene.add(new GameObject(Guid.random(), "Move"));
        CommandStack stack = new CommandStack();

        stack.execute(EditCommands.reparent(scene, move, b));
        assertThat(scene.gameObjects()).containsExactly(a, b);
        assertThat(b.children()).containsExactly(move);

        stack.undo();
        assertThat(scene.gameObjects()).containsExactly(a, b, move);
    }

    @Test
    void duplicateInsertsAfterSourceWithFreshGuid() {
        Scene scene = new Scene("Level");
        GameObject source = scene.add(new GameObject(Guid.random(), "Source"));
        source.addComponent(new TestBehaviour());
        CommandStack stack = new CommandStack();
        registry().descriptor(TestBehaviour.class);

        stack.execute(EditCommands.duplicate(scene, source, registry()));
        assertThat(scene.gameObjects()).hasSize(2);
        GameObject clone = scene.gameObjects().get(1);
        assertThat(clone.guid()).isNotEqualTo(source.guid());
        assertThat(clone.getComponent(TestBehaviour.class)).isNotNull();

        stack.undo();
        assertThat(scene.gameObjects()).containsExactly(source);
    }
}
