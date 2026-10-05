package io.stratixops.engine.editor;

import io.stratixops.engine.editor.fixture.TestBehaviour;
import io.stratixops.engine.inspect.ComponentRegistry;
import io.stratixops.engine.math.Vec2;
import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Scene;
import io.stratixops.engine.scene.SpriteRenderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EditorControllerTest {

    private static ComponentRegistry registry() {
        ComponentRegistry registry = ComponentRegistry.withBuiltins();
        registry.register(TestBehaviour.class);
        return registry;
    }

    private static EditorController controller() {
        return new EditorController(new EditorState(registry()));
    }

    @Test
    void createRootAndChild() {
        EditorController controller = controller();

        GameObject root = controller.createGameObject("Player");
        assertThat(controller.state().scene().gameObjects()).containsExactly(root);
        assertThat(controller.state().selection().primary()).isEqualTo(root);

        GameObject child = controller.createChild("Weapon");
        assertThat(child.parent()).isEqualTo(root);
        assertThat(root.children()).containsExactly(child);
        assertThat(controller.state().scene().gameObjects()).containsExactly(root);
    }

    @Test
    void createGeneratesUniqueNames() {
        EditorController controller = controller();
        controller.createGameObject("Enemy");
        GameObject second = controller.createGameObject("Enemy");
        GameObject third = controller.createGameObject("Enemy");

        assertThat(second.name()).isEqualTo("Enemy (1)");
        assertThat(third.name()).isEqualTo("Enemy (2)");
    }

    @Test
    void deleteSelectionAndUndo() {
        EditorController controller = controller();
        GameObject a = controller.createGameObject("A");
        controller.createGameObject("B");
        controller.state().selection().select(a);

        assertThat(controller.deleteSelection()).isTrue();
        assertThat(controller.state().selection().isEmpty()).isTrue();
        assertThat(controller.state().scene().gameObjects()).hasSize(1);

        controller.state().undo();
        assertThat(controller.state().scene().gameObjects()).contains(a);
    }

    @Test
    void duplicateSelectsNewCopies() {
        EditorController controller = controller();
        GameObject source = controller.createGameObject("Source");
        controller.state().selection().select(source);

        assertThat(controller.duplicateSelection()).isTrue();
        assertThat(controller.state().scene().gameObjects()).hasSize(2);
        GameObject copy = controller.state().selection().primary();
        assertThat(copy).isNotSameAs(source);
        assertThat(copy.guid()).isNotEqualTo(source.guid());
    }

    @Test
    void copyPasteCreatesIndependentObjects() {
        EditorController controller = controller();
        GameObject source = controller.createGameObject("Source");
        source.addComponent(new TestBehaviour());
        controller.state().selection().select(source);
        assertThat(controller.copySelection()).isEqualTo(1);

        controller.state().selection().clear();
        assertThat(controller.paste()).isTrue();

        assertThat(controller.state().scene().gameObjects()).hasSize(2);
        GameObject pasted = controller.state().scene().gameObjects().get(1);
        assertThat(pasted.guid()).isNotEqualTo(source.guid());
        assertThat(pasted.getComponent(TestBehaviour.class)).isNotNull();
    }

    @Test
    void pasteWithEmptyClipboardIsNoOp() {
        EditorController controller = controller();
        assertThat(controller.clipboardEmpty()).isTrue();
        assertThat(controller.paste()).isFalse();
    }

    @Test
    void addComponentIsUndoable() {
        EditorController controller = controller();
        GameObject object = controller.createGameObject("Frame");
        SpriteRenderer sprite = controller.addComponent(object, SpriteRenderer.class);

        assertThat(object.getComponent(SpriteRenderer.class)).isSameAs(sprite);
        assertThat(object.components()).contains(sprite);

        controller.state().undo();
        assertThat(object.components()).doesNotContain(sprite);
    }

    @Test
    void addUnregisteredComponentFailsFast() {
        EditorController controller = controller();
        GameObject object = controller.createGameObject("X");
        assertThatThrownBy(() -> controller.addComponent(object, Unregistered.class))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void saveAndReloadRoundTripsSceneState(@TempDir Path dir) {
        EditorController controller = controller();
        GameObject root = controller.createGameObject("Player");
        root.transform().localPosition().set(12f, -4f);
        TestBehaviour behaviour = root.addComponent(new TestBehaviour());
        behaviour.velocity.set(3f, 7f);

        Path file = dir.resolve("level.json");
        controller.state().saveScene(file);
        assertThat(controller.state().isDirty()).isFalse();

        controller.state().openScene(file);
        Scene loaded = controller.state().scene();
        GameObject loadedRoot = loaded.gameObjects().get(0);
        assertThat(loadedRoot.name()).isEqualTo("Player");
        assertThat(loadedRoot.transform().localPosition().x).isEqualTo(12f);
        assertThat(loadedRoot.getComponent(TestBehaviour.class).velocity.x).isEqualTo(3f);
    }

    @Test
    void reparentAndRename() {
        EditorController controller = controller();
        GameObject a = controller.createGameObject("A");
        GameObject b = controller.createGameObject("B");

        assertThat(controller.reparent(b, a)).isTrue();
        assertThat(b.parent()).isEqualTo(a);
        assertThat(controller.state().scene().gameObjects()).containsExactly(a);

        assertThat(controller.rename(a, "Renamed")).isTrue();
        assertThat(a.name()).isEqualTo("Renamed");
        assertThat(controller.rename(a, "Renamed")).isFalse();
    }

    @Test
    void setFieldThroughControllerIsUndoable() {
        EditorController controller = controller();
        GameObject object = controller.createGameObject("Player");
        TestBehaviour behaviour = object.addComponent(new TestBehaviour());
        var field = controller.state().registry().descriptor(TestBehaviour.class).field("velocity");

        controller.setField(behaviour, field, new Vec2(9f, 9f));
        assertThat(behaviour.velocity.x).isEqualTo(9f);

        controller.state().undo();
        assertThat(behaviour.velocity.x).isEqualTo(1f);
    }

    static final class Unregistered extends io.stratixops.engine.scene.Component {
    }
}
