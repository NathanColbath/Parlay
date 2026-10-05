package io.stratixops.engine.scene;

import io.stratixops.engine.Sprite;
import io.stratixops.engine.inspect.Color;
import io.stratixops.engine.inspect.Header;
import io.stratixops.engine.inspect.NonSerialized;
import io.stratixops.engine.inspect.Order;
import io.stratixops.engine.inspect.SerializeField;
import io.stratixops.engine.inspect.Tooltip;
import io.stratixops.engine.math.Vec2;

/**
 * Draws a sprite at the object's world position. Submitted by
 * {@link SceneRenderer} in ascending {@code (layer, sortingOrder)} order.
 */
public final class SpriteRenderer extends Component {

    @SerializeField
    @Header("Sprite")
    @Tooltip("Sprite asset to draw (by GUID).")
    private AssetRef sprite;

    @SerializeField
    @Order(1)
    private final Vec2 offset = new Vec2();

    @SerializeField
    @Order(2)
    @Color
    private int tint = 0xFFFFFFFF;

    @SerializeField
    @Order(3)
    @Tooltip("Draw order within the same layer; lower draws first.")
    private int sortingOrder;

    @SerializeField
    @Order(4)
    private boolean flipX;

    @SerializeField
    @Order(5)
    private boolean flipY;

    @NonSerialized
    private Sprite resolved;

    /** Creates an empty sprite renderer. */
    public SpriteRenderer() {
    }

    /**
     * @return the sprite asset reference (may be {@code null})
     */
    public AssetRef sprite() {
        return sprite;
    }

    /**
     * Sets the sprite asset reference.
     *
     * @param sprite asset reference, or {@code null} to clear
     */
    public void setSprite(AssetRef sprite) {
        this.sprite = sprite;
    }

    /**
     * Drawing offset added to the world position, in pixels. Mutable.
     *
     * @return the offset
     */
    public Vec2 offset() {
        return offset;
    }

    /**
     * @return the packed {@code 0xRRGGBBAA} tint
     */
    public int tint() {
        return tint;
    }

    /**
     * Sets the packed tint.
     *
     * @param tint packed {@code 0xRRGGBBAA}
     */
    public void setTint(int tint) {
        this.tint = tint;
    }

    /**
     * @return the draw order within the object's layer
     */
    public int sortingOrder() {
        return sortingOrder;
    }

    /**
     * Sets the draw order within the object's layer.
     *
     * @param sortingOrder draw order; lower draws first
     */
    public void setSortingOrder(int sortingOrder) {
        this.sortingOrder = sortingOrder;
    }

    /**
     * @return true when the sprite is mirrored horizontally
     */
    public boolean flipX() {
        return flipX;
    }

    /**
     * Sets horizontal mirroring.
     *
     * @param flipX true to mirror
     */
    public void setFlipX(boolean flipX) {
        this.flipX = flipX;
    }

    /**
     * @return true when the sprite is mirrored vertically
     */
    public boolean flipY() {
        return flipY;
    }

    /**
     * Sets vertical mirroring.
     *
     * @param flipY true to mirror
     */
    public void setFlipY(boolean flipY) {
        this.flipY = flipY;
    }

    /**
     * The resolved sprite handle, if the asset database resolved it.
     *
     * @return the sprite, or {@code null}
     */
    public Sprite spriteHandle() {
        return resolved;
    }

    /**
     * Stores the runtime sprite handle resolved from {@link #sprite()}.
     *
     * @param resolved the resolved sprite, or {@code null}
     */
    public void setSpriteHandle(Sprite resolved) {
        this.resolved = resolved;
    }
}
