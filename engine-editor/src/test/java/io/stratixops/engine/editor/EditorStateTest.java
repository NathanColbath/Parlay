package io.stratixops.engine.editor;

import io.stratixops.engine.editor.command.EditCommands;
import io.stratixops.engine.editor.fixture.TestBehaviour;
import io.stratixops.engine.inspect.ComponentRegistry;
import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Guid;
import io.stratixops.engine.scene.Scene;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EditorStateTest {

    private static ComponentRegistry registry() {
        ComponentRegistry registry = ComponentRegistry.withBuiltins();
        registry.register(TestBehaviour.class);
        return registry;
    }

    @Test
    void startsCleanWithDefaultLayout() {
        EditorState state = new EditorState(registry());
        assertThat(state.scene().name()).isEqualTo("Untitled");
        assertThat(state.layout()).isEqualTo(DockLayout.defaultLayout());
        assertThat(state.isDirty()).isFalse();
        assertThat(state.selection().isEmpty()).isTrue();
    }

    @Test
    void executeMarksDirtyAndUndoRedoWork() {
        EditorState state = new EditorState(registry());
        GameObject object = new GameObject(Guid.random(), "New");

        state.execute(EditCommands.create(state.scene(), object, null));
        assertThat(state.isDirty()).isTrue();
        assertThat(state.scene().gameObjects()).containsExactly(object);

        state.undo();
        assertThat(state.scene().gameObjects()).isEmpty();
        state.redo();
        assertThat(state.scene().gameObjects()).containsExactly(object);
    }

    @Test
    void openSceneResetsHistorySelectionAndDirty() {
        EditorState state = new EditorState(registry());
        state.execute(EditCommands.create(state.scene(), new GameObject(Guid.random(), "New"), null));
        state.selection().select(new GameObject(Guid.random(), "X"));

        state.openScene(new Scene("Loaded"));

        assertThat(state.scene().name()).isEqualTo("Loaded");
        assertThat(state.isDirty()).isFalse();
        assertThat(state.selection().isEmpty()).isTrue();
        assertThat(state.commands().canUndo()).isFalse();
    }

    @Test
    void undoOnEmptyHistoryIsANoOp() {
        EditorState state = new EditorState(registry());
        assertThat(state.undo()).isFalse();
        assertThat(state.redo()).isFalse();
    }
}
