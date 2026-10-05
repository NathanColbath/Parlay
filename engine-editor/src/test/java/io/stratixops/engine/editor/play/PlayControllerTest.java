package io.stratixops.engine.editor.play;

import io.stratixops.engine.editor.fixture.TestBehaviour;
import io.stratixops.engine.inspect.ComponentRegistry;
import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Guid;
import io.stratixops.engine.scene.Scene;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlayControllerTest {

    private static ComponentRegistry registry() {
        ComponentRegistry registry = ComponentRegistry.withBuiltins();
        registry.register(TestBehaviour.class);
        return registry;
    }

    @Test
    void playRunsOnACopyLeavingEditorUntouched() {
        Scene editor = new Scene("Level");
        GameObject object = editor.add(new GameObject(Guid.random(), "Player"));
        object.addComponent(new TestBehaviour());

        PlayController play = new PlayController();
        play.play(editor, registry());

        assertThat(play.state()).isEqualTo(PlayState.PLAYING);
        assertThat(play.runtimeScene()).isNotSameAs(editor);

        play.runtimeScene().gameObjects().get(0).setName("Mutated");
        play.stop();

        assertThat(object.name()).isEqualTo("Player");
        assertThat(play.runtimeScene()).isNull();
        assertThat(play.state()).isEqualTo(PlayState.STOPPED);
    }

    @Test
    void updateAccumulatesFixedSteps() {
        Scene editor = sceneWithBehaviour();
        PlayController play = new PlayController();
        play.play(editor, registry());

        play.update(0.06f);

        TestBehaviour runtime = play.runtimeScene().gameObjects().get(0)
                .getComponent(TestBehaviour.class);
        assertThat(runtime.fixedUpdates).isEqualTo(3);
        assertThat(runtime.updates).isEqualTo(1);
    }

    @Test
    void pauseFreezesAndStepAdvancesExactlyOneTick() {
        Scene editor = sceneWithBehaviour();
        PlayController play = new PlayController();
        play.play(editor, registry());
        play.pause();

        assertThat(play.state()).isEqualTo(PlayState.PAUSED);
        play.update(1.0f);
        TestBehaviour runtime = play.runtimeScene().gameObjects().get(0)
                .getComponent(TestBehaviour.class);
        assertThat(runtime.fixedUpdates).isZero();

        play.step();
        assertThat(runtime.fixedUpdates).isEqualTo(1);
        assertThat(runtime.updates).isEqualTo(1);

        play.resume();
        assertThat(play.state()).isEqualTo(PlayState.PLAYING);
    }

    @Test
    void enteringPlayAgainRestartsRuntime() {
        Scene editor = sceneWithBehaviour();
        PlayController play = new PlayController();
        play.play(editor, registry());
        Scene first = play.runtimeScene();

        play.play(editor, registry());

        assertThat(play.state()).isEqualTo(PlayState.PLAYING);
        assertThat(play.runtimeScene()).isNotSameAs(first);
    }

    private static Scene sceneWithBehaviour() {
        Scene scene = new Scene("Level");
        GameObject object = scene.add(new GameObject(Guid.random(), "Player"));
        object.addComponent(new TestBehaviour());
        return scene;
    }
}
