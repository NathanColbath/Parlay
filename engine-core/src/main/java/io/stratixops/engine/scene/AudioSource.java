package io.stratixops.engine.scene;

import io.stratixops.engine.inspect.Range;
import io.stratixops.engine.inspect.SerializeField;
import io.stratixops.engine.inspect.Tooltip;

/**
 * Describes a clip to play from this object. The component is pure state in
 * {@code engine-core}; the runtime/editor binds it to the audio device.
 */
public final class AudioSource extends Component {

    @SerializeField
    @Tooltip("Audio asset to play (by GUID).")
    private AssetRef clip;

    @SerializeField
    @Range(min = 0f, max = 1f)
    private float volume = 1f;

    @SerializeField
    private boolean loop;

    @SerializeField
    private boolean playOnAwake;

    /** Creates an unbound audio source. */
    public AudioSource() {
    }

    /**
     * @return the clip asset reference (may be {@code null})
     */
    public AssetRef clip() {
        return clip;
    }

    /**
     * Sets the clip asset reference.
     *
     * @param clip asset reference, or {@code null} to clear
     */
    public void setClip(AssetRef clip) {
        this.clip = clip;
    }

    /**
     * @return linear volume in {@code [0,1]}
     */
    public float volume() {
        return volume;
    }

    /**
     * Sets the volume, clamped to {@code [0,1]}.
     *
     * @param volume linear volume
     */
    public void setVolume(float volume) {
        this.volume = Math.max(0f, Math.min(1f, volume));
    }

    /**
     * @return true when the clip loops
     */
    public boolean loop() {
        return loop;
    }

    /**
     * Sets looping.
     *
     * @param loop true to loop
     */
    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    /**
     * @return true when the clip should play on {@code awake}
     */
    public boolean playOnAwake() {
        return playOnAwake;
    }

    /**
     * Sets whether the clip plays on awake.
     *
     * @param playOnAwake true to play on awake
     */
    public void setPlayOnAwake(boolean playOnAwake) {
        this.playOnAwake = playOnAwake;
    }
}
