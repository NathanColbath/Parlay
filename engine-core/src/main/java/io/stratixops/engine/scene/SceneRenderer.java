package io.stratixops.engine.scene;

import io.stratixops.engine.Renderer;
import io.stratixops.engine.Sprite;
import io.stratixops.engine.math.Vec2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The single scene submission path (ADR-0008). Both the runtime Game view and
 * the editor Scene view call {@link #render(Scene, Renderer)}, so they cannot
 * drift. It uses only the frozen {@link Renderer} contract from
 * {@code engine-api} and is unit-testable with a fake renderer.
 *
 * <p>Draw order is: clear, push the main camera (if any), then every active
 * {@link SpriteRenderer} sorted by layer, then {@code sortingOrder}, then
 * hierarchy order; finally pop the camera for screen-space drawing.
 */
public final class SceneRenderer {

    /**
     * Submits a scene for one frame.
     *
     * @param scene    the scene to draw
     * @param renderer the target renderer
     */
    public void render(Scene scene, Renderer renderer) {
        renderer.clear(scene.background());

        Camera camera = findMainCamera(scene);
        if (camera != null && camera.gameObject() != null) {
            Vec2 p = camera.gameObject().transform().worldPosition();
            camera.camera2D().position().set(p);
            renderer.pushCamera(camera.camera2D());
        }

        List<SpriteRenderer> drawables = new ArrayList<>();
        for (GameObject root : new ArrayList<>(scene.gameObjects())) {
            collect(root, drawables);
        }
        drawables.sort(Comparator
                .comparingInt((SpriteRenderer s) -> s.gameObject().layer())
                .thenComparingInt(SpriteRenderer::sortingOrder));

        for (SpriteRenderer spriteRenderer : drawables) {
            Sprite sprite = spriteRenderer.spriteHandle();
            if (sprite == null) {
                continue;
            }
            Vec2 position = spriteRenderer.gameObject().transform().worldPosition();
            renderer.draw(sprite,
                    position.x + spriteRenderer.offset().x,
                    position.y + spriteRenderer.offset().y);
        }

        if (camera != null && camera.gameObject() != null) {
            renderer.popCamera();
        }
    }

    private void collect(GameObject object, List<SpriteRenderer> out) {
        if (!object.activeInHierarchy()) {
            return;
        }
        SpriteRenderer spriteRenderer = object.getComponent(SpriteRenderer.class);
        if (spriteRenderer != null && spriteRenderer.enabled()) {
            out.add(spriteRenderer);
        }
        for (GameObject child : object.children()) {
            collect(child, out);
        }
    }

    private Camera findMainCamera(Scene scene) {
        List<Camera> cameras = new ArrayList<>();
        for (GameObject root : scene.gameObjects()) {
            collectCameras(root, cameras);
        }
        Camera first = null;
        for (Camera camera : cameras) {
            if (camera.main()) {
                return camera;
            }
            if (first == null) {
                first = camera;
            }
        }
        return first;
    }

    private void collectCameras(GameObject object, List<Camera> out) {
        if (!object.activeInHierarchy()) {
            return;
        }
        Camera camera = object.getComponent(Camera.class);
        if (camera != null && camera.enabled()) {
            out.add(camera);
        }
        for (GameObject child : object.children()) {
            collectCameras(child, out);
        }
    }
}
