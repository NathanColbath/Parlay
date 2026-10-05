package io.stratixops.engine.editor.view;

import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.SpriteRenderer;
import io.stratixops.engine.Sprite;
import io.stratixops.engine.math.Vec2;

import java.util.List;

/**
 * Computes world-space bounds for editor objects (STR-41). Bounds come from the
 * object's world position and scale; when a {@link SpriteRenderer} has a
 * resolved sprite its pixel size is used, otherwise a fixed editor cell size is
 * assumed so an object is always visible and selectable even before assets are
 * resolved.
 *
 * <p>Rotation is ignored: the engine's {@code Transform} derives an
 * axis-aligned world transform (translation + scale), so an AABB is exact for
 * the supported 2D case. Pure and headless-testable.
 */
public final class SceneBounds {

    /** Fallback object size in world pixels when no sprite is resolved. */
    public static final float DEFAULT_SIZE = 32f;

    private SceneBounds() {
    }

    /**
     * The world bounds of a single object (ignoring children).
     *
     * @param object the object
     * @return its bounds
     */
    public static ViewRect of(GameObject object) {
        Vec2 position = object.transform().worldPosition();
        Vec2 scale = object.transform().worldScale();
        float width = DEFAULT_SIZE;
        float height = DEFAULT_SIZE;
        SpriteRenderer spriteRenderer = object.getComponent(SpriteRenderer.class);
        if (spriteRenderer != null) {
            Sprite sprite = spriteRenderer.spriteHandle();
            if (sprite != null) {
                width = sprite.width();
                height = sprite.height();
            }
        }
        width *= Math.abs(scale.x);
        height *= Math.abs(scale.y);
        return ViewRect.centered(position.x, position.y, width * 0.5f, height * 0.5f);
    }

    /**
     * The world bounds of an object and all of its descendants.
     *
     * @param object the subtree root
     * @return the union of the subtree
     */
    public static ViewRect subtree(GameObject object) {
        ViewRect bounds = of(object);
        for (GameObject child : object.children()) {
            bounds = bounds.union(subtree(child));
        }
        return bounds;
    }

    /**
     * The union of the subtrees of several objects.
     *
     * @param objects the objects
     * @return the union, or {@code null} when the list is empty
     */
    public static ViewRect union(List<GameObject> objects) {
        ViewRect bounds = null;
        for (GameObject object : objects) {
            if (object == null) {
                continue;
            }
            ViewRect objectBounds = subtree(object);
            bounds = bounds == null ? objectBounds : bounds.union(objectBounds);
        }
        return bounds;
    }
}
