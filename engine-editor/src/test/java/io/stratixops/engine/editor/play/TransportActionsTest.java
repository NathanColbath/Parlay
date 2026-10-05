package io.stratixops.engine.editor.play;

import io.stratixops.engine.editor.EditorState;
import io.stratixops.engine.editor.fixture.TestBehaviour;
import io.stratixops.engine.inspect.ComponentRegistry;
import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Guid;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransportActionsTest {

    private static ComponentRegistry registry() {
        ComponentRegistry registry = ComponentRegistry.withBuiltins();
        registry.register(TestBehaviour.class);
        return registry;
    }

    @Test
    void playRunsOnACopyAndStopRestoresEditorSceneExactly() {
        EditorState state = new EditorState(registry());
        GameObject editor = state.scene().add(new GameObject(Guid.random(), "Player"));
        editor.transform().localPosition().set(5f, 5f);

        TransportActions.play(state);
        assertThat(state.play().isPlaying()).isTrue();

        GameObject runtime = state.play().runtimeScene().gameObjects().get(0);
        runtime.setName("Mutated");
        runtime.transform().localPosition().set(99f, 99f);

        TransportActions.stop(state);

        assertThat(state.play().isStopped()).isTrue();
        assertThat(editor.name()).isEqualTo("Player");
        assertThat(editor.transform().localPosition().x).isEqualTo(5f);
        assertThat(editor.transform().localPosition().y).isEqualTo(5f);
    }

    @Test
    void pauseAndStepOnlyRunWhenPaused() {
        EditorState state = new EditorState(registry());
        GameObject editor = state.scene().add(new GameObject(Guid.random(), "Player"));
        editor.addComponent(new TestBehaviour());

        TransportActions.pause(state);
        assertThat(state.play().isStopped()).isTrue();

        TransportActions.play(state);
        TransportActions.pause(state);
        assertThat(state.play().isPaused()).isTrue();

        TransportActions.step(state);
        TestBehaviour runtime = state.play().runtimeScene().gameObjects().get(0)
                .getComponent(TestBehaviour.class);
        assertThat(runtime.fixedUpdates).isEqualTo(1);

        TransportActions.step(state);
        assertThat(runtime.fixedUpdates).isEqualTo(2);

        TransportActions.play(state);
        assertThat(state.play().isPlaying()).isTrue();
    }

    @Test
    void toggleFlipsBetweenPlayAndPause() {
        EditorState state = new EditorState(registry());
        state.scene().add(new GameObject(Guid.random(), "Player"));

        TransportActions.toggle(state);
        assertThat(state.play().isPlaying()).isTrue();

        TransportActions.toggle(state);
        assertThat(state.play().isPaused()).isTrue();

        TransportActions.toggle(state);
        assertThat(state.play().isPlaying()).isTrue();
    }

    @Test
    void stopWhenAlreadyStoppedIsANoOp() {
        EditorState state = new EditorState(registry());
        TransportActions.stop(state);
        assertThat(state.play().isStopped()).isTrue();
    }
}
