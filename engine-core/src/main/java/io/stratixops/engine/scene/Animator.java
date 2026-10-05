package io.stratixops.engine.scene;

import io.stratixops.engine.Sprite;
import io.stratixops.engine.inspect.NonSerialized;
import io.stratixops.engine.inspect.Range;
import io.stratixops.engine.inspect.SerializeField;
import io.stratixops.engine.inspect.Tooltip;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple frame-based animation over an ordered list of sprite assets.
 *
 * <p>Frame stepping is frame-rate independent: {@link #update(float)}
 * accumulates elapsed time and advances the cursor. Rendering is done by the
 * owner's {@link SpriteRenderer}; the animator only chooses the current frame.
 */
public final class Animator extends Component {

    @SerializeField
    @Tooltip("Ordered sprite asset frames (by GUID).")
    private final List<AssetRef> frames = new ArrayList<>();

    @SerializeField
    @Range(min = 0.001f, max = 120f)
    private float framesPerSecond = 8f;

    @SerializeField
    private boolean loop = true;

    @SerializeField
    private boolean playing = true;

    @NonSerialized
    private final List<Sprite> resolvedFrames = new ArrayList<>();

    @NonSerialized
    private int currentFrame;

    @NonSerialized
    private float accumulator;

    /** Creates a playing animator that stays on the first frame if empty. */
    public Animator() {
    }

    /**
     * Ordered frame asset references. Mutable list.
     *
     * @return the frames
     */
    public List<AssetRef> frames() {
        return frames;
    }

    /**
     * @return playback rate in frames per second
     */
    public float framesPerSecond() {
        return framesPerSecond;
    }

    /**
     * Sets the playback rate.
     *
     * @param framesPerSecond frames per second; must be positive
     */
    public void setFramesPerSecond(float framesPerSecond) {
        if (framesPerSecond <= 0f) {
            throw new IllegalArgumentException("framesPerSecond must be positive");
        }
        this.framesPerSecond = framesPerSecond;
    }

    /**
     * @return true when playback wraps at the end
     */
    public boolean loop() {
        return loop;
    }

    /**
     * Sets looping.
     *
     * @param loop true to wrap
     */
    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    /**
     * @return true when the animation is advancing
     */
    public boolean playing() {
        return playing;
    }

    /**
     * Starts or pauses playback.
     *
     * @param playing true to play
     */
    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    /**
     * @return index of the currently displayed frame
     */
    public int currentFrame() {
        return currentFrame;
    }

    /**
     * Selects the current frame index.
     *
     * @param index frame index, clamped to {@code [0, size)}
     */
    public void setCurrentFrame(int index) {
        if (frames.isEmpty()) {
            currentFrame = 0;
        } else {
            currentFrame = Math.max(0, Math.min(index, frames.size() - 1));
        }
    }

    @Override
    public void update(float dt) {
        clampFrame();
        if (!playing || frames.size() < 2) {
            return;
        }
        accumulator += dt * framesPerSecond;
        while (accumulator >= 1f) {
            accumulator -= 1f;
            if (currentFrame + 1 < frames.size()) {
                currentFrame++;
            } else if (loop) {
                currentFrame = 0;
            } else {
                playing = false;
                break;
            }
        }
    }

    private void clampFrame() {
        if (frames.isEmpty()) {
            currentFrame = 0;
        } else if (currentFrame >= frames.size()) {
            currentFrame = frames.size() - 1;
        }
    }

    /**
     * The sprite asset reference for the current frame.
     *
     * @return the current frame reference, or {@code null} when empty
     */
    public AssetRef currentFrameAsset() {
        if (frames.isEmpty()) {
            return null;
        }
        return frames.get(Math.min(currentFrame, frames.size() - 1));
    }

    /**
     * Resolved runtime sprite handles, parallel to {@link #frames()}.
     *
     * @return the resolved handles
     */
    public List<Sprite> resolvedFrames() {
        return resolvedFrames;
    }
}
