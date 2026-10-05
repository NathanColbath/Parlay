package io.stratixops.engine.editor.play;

import io.stratixops.engine.inspect.ComponentRegistry;
import io.stratixops.engine.scene.Scene;

/**
 * Drives Play/Pause/Step for the editor (STR-23, ADR-0008 section 4).
 *
 * <p>Entering Play deep-copies the editor scene and runs the copy, so the
 * editor scene is never mutated: exit-Play is an exact restore by construction.
 * Pause stops updates but keeps the scene present for rendering; Step advances
 * exactly one fixed tick (1/60 s, ADR-0003).
 */
public final class PlayController {

    /** The runtime fixed timestep in seconds (ADR-0003). */
    public static final float FIXED_STEP = 1f / 60f;

    private static final float MAX_FRAME = 0.25f;

    private final float fixedStep;
    private PlayState state = PlayState.STOPPED;
    private Scene runtimeScene;
    private float accumulator;
    private float elapsed;

    /**
     * Creates a controller with the engine's 1/60 s fixed step.
     */
    public PlayController() {
        this(FIXED_STEP);
    }

    /**
     * Creates a controller with an explicit fixed step (tests).
     *
     * @param fixedStep the fixed step in seconds
     */
    public PlayController(float fixedStep) {
        if (!(fixedStep > 0f)) {
            throw new IllegalArgumentException("fixedStep must be > 0");
        }
        this.fixedStep = fixedStep;
    }

    /**
     * @return the current transport state
     */
    public PlayState state() {
        return state;
    }

    /**
     * @return true while running
     */
    public boolean isPlaying() {
        return state == PlayState.PLAYING;
    }

    /**
     * @return true while paused
     */
    public boolean isPaused() {
        return state == PlayState.PAUSED;
    }

    /**
     * @return true when no runtime scene is instantiated
     */
    public boolean isStopped() {
        return state == PlayState.STOPPED;
    }

    /**
     * The runtime scene, present whenever Play has been entered.
     *
     * @return the runtime scene, or {@code null}
     */
    public Scene runtimeScene() {
        return runtimeScene;
    }

    /**
     * @return wall-clock seconds accumulated since Play was entered
     */
    public float elapsed() {
        return elapsed;
    }

    /**
     * Enters Play, instantiating an independent copy of the editor scene.
     *
     * @param editScene the editor scene to snapshot
     * @param registry  the component registry (user behaviours included)
     */
    public void play(Scene editScene, ComponentRegistry registry) {
        if (state != PlayState.STOPPED) {
            stop();
        }
        runtimeScene = SceneCloner.deepCopy(editScene, registry, false);
        accumulator = 0f;
        elapsed = 0f;
        state = PlayState.PLAYING;
    }

    /**
     * Pauses the runtime.
     */
    public void pause() {
        if (state == PlayState.PLAYING) {
            state = PlayState.PAUSED;
        }
    }

    /**
     * Resumes a paused runtime.
     */
    public void resume() {
        if (state == PlayState.PAUSED) {
            state = PlayState.PLAYING;
        }
    }

    /**
     * Advances exactly one fixed tick while paused.
     */
    public void step() {
        if (state != PlayState.PAUSED || runtimeScene == null) {
            return;
        }
        runtimeScene.fixedUpdate(fixedStep);
        runtimeScene.update(fixedStep);
        runtimeScene.lateUpdate(fixedStep);
        elapsed += fixedStep;
    }

    /**
     * Stops the runtime and disposes the copy.
     */
    public void stop() {
        if (runtimeScene != null) {
            runtimeScene.dispose();
            runtimeScene = null;
        }
        accumulator = 0f;
        elapsed = 0f;
        state = PlayState.STOPPED;
    }

    /**
     * Advances the runtime for one frame.
     *
     * @param frameSeconds elapsed real time since the last frame
     */
    public void update(float frameSeconds) {
        if (state != PlayState.PLAYING || runtimeScene == null) {
            return;
        }
        float frame = Math.max(0f, Math.min(frameSeconds, MAX_FRAME));
        accumulator += frame;
        while (accumulator >= fixedStep) {
            runtimeScene.fixedUpdate(fixedStep);
            accumulator -= fixedStep;
        }
        runtimeScene.update(frame);
        runtimeScene.lateUpdate(frame);
        elapsed += frame;
    }
}
