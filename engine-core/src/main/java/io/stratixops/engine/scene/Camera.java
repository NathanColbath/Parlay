package io.stratixops.engine.scene;

import io.stratixops.engine.Camera2D;
import io.stratixops.engine.inspect.Color;
import io.stratixops.engine.inspect.NonSerialized;
import io.stratixops.engine.inspect.SerializeField;
import io.stratixops.engine.math.Vec2;

/**
 * A scene camera component. The object's world position becomes the
 * {@link Camera2D} position when {@link SceneRenderer} submits the scene.
 *
 * <p>The main camera is the enabled {@code main} camera on the highest active
 * object; if none is marked main, the first enabled camera is used.
 */
public final class Camera extends Component {

    @SerializeField
    @Color
    private int backgroundColor = 0x1B1B2FFF;

    @SerializeField
    private boolean main = true;

    @SerializeField
    private final Vec2 viewport = new Vec2(640f, 360f);

    @NonSerialized
    private Camera2D camera2D;

    /** Creates a main camera with a 640x360 logical viewport. */
    public Camera() {
    }

    /**
     * @return the packed background color
     */
    public int backgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color.
     *
     * @param backgroundColor packed {@code 0xRRGGBBAA}
     */
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * @return true when this is the main camera
     */
    public boolean main() {
        return main;
    }

    /**
     * Sets whether this is the main camera.
     *
     * @param main true to mark main
     */
    public void setMain(boolean main) {
        this.main = main;
    }

    /**
     * Logical viewport size. Mutable, reused instance.
     *
     * @return the viewport size in pixels
     */
    public Vec2 viewport() {
        return viewport;
    }

    /**
     * The backing {@link Camera2D}, created on first use.
     *
     * @return the camera
     */
    public Camera2D camera2D() {
        if (camera2D == null) {
            camera2D = new Camera2D(viewport);
        }
        return camera2D;
    }
}
