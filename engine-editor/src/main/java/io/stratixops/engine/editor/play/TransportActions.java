package io.stratixops.engine.editor.play;

import io.stratixops.engine.editor.EditorState;

/**
 * Headless Play/Pause/Step/Stop actions (STR-41) shared by the transport
 * toolbar, the Play menu and the keyboard shortcuts, so every entry point runs
 * the exact same transition. Exiting Play is an exact restore by construction
 * because the runtime runs on a deep copy and the editor scene is never mutated
 * (see {@link PlayController}).
 *
 * <p>Kept free of ImGui so it can be unit-tested headless.
 */
public final class TransportActions {

    private TransportActions() {
    }

    /**
     * Enters Play from Stopped, or resumes from Paused. Does nothing while
     * already playing.
     *
     * @param state the editor state
     */
    public static void play(EditorState state) {
        PlayController play = state.play();
        if (play.isStopped()) {
            play.play(state.scene(), state.registry());
        } else if (play.isPaused()) {
            play.resume();
        }
    }

    /**
     * Pauses a running Play mode. Does nothing otherwise.
     *
     * @param state the editor state
     */
    public static void pause(EditorState state) {
        if (state.play().isPlaying()) {
            state.play().pause();
        }
    }

    /**
     * Advances exactly one fixed tick while paused. Does nothing otherwise.
     *
     * @param state the editor state
     */
    public static void step(EditorState state) {
        if (state.play().isPaused()) {
            state.play().step();
        }
    }

    /**
     * Stops Play and restores the editor scene. Does nothing when stopped.
     *
     * @param state the editor state
     */
    public static void stop(EditorState state) {
        if (!state.play().isStopped()) {
            state.play().stop();
        }
    }

    /**
     * Space-style toggle: Play/Resume when not playing, Pause while playing.
     *
     * @param state the editor state
     */
    public static void toggle(EditorState state) {
        PlayController play = state.play();
        if (play.isStopped()) {
            play.play(state.scene(), state.registry());
        } else if (play.isPlaying()) {
            play.pause();
        } else {
            play.resume();
        }
    }
}
