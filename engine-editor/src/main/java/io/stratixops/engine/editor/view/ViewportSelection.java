package io.stratixops.engine.editor.view;

import io.stratixops.engine.scene.GameObject;
import io.stratixops.engine.scene.Scene;

import java.util.ArrayList;
import java.util.List;

/**
 * Marquee hit-testing for the Scene view (STR-41): maps a screen rectangle to
 * world space through the editor camera and returns every object whose bounds
 * overlap it. Pure and headless-testable.
 */
public final class ViewportSelection {

    private ViewportSelection() {
    }

    /**
     * Objects whose bounds intersect the marquee, in scene depth-first order.
     *
     * @param scene     the scene to search
     * @param worldRect the marquee in world space
     * @return the hit objects (never {@code null})
     */
    public static List<GameObject> objectsIn(Scene scene, ViewRect worldRect) {
        List<GameObject> hits = new ArrayList<>();
        for (GameObject root : scene.gameObjects()) {
            collect(root, worldRect, hits);
        }
        return hits;
    }

    /**
     * Maps a viewport-local screen rectangle to world space and returns the
     * objects inside it.
     *
     * @param scene   the scene to search
     * @param camera  the editor camera
     * @param x0      first screen x
     * @param y0      first screen y
     * @param x1      second screen x
     * @param y1      second screen y
     * @return the hit objects
     */
    public static List<GameObject> objectsIn(Scene scene, EditorCamera camera,
                                             float x0, float y0, float x1, float y1) {
        float minX = Math.min(x0, x1);
        float minY = Math.min(y0, y1);
        float maxX = Math.max(x0, x1);
        float maxY = Math.max(y0, y1);
        return objectsIn(scene, new ViewRect(camera.screenToWorldX(minX), camera.screenToWorldY(minY),
                camera.screenToWorldX(maxX), camera.screenToWorldY(maxY)));
    }

    private static void collect(GameObject object, ViewRect worldRect, List<GameObject> out) {
        if (worldRect.intersects(SceneBounds.of(object))) {
            out.add(object);
        }
        for (GameObject child : object.children()) {
            collect(child, worldRect, out);
        }
    }
}
