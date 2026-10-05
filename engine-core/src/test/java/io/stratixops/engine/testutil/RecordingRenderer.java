package io.stratixops.engine.testutil;

import io.stratixops.engine.Camera2D;
import io.stratixops.engine.Renderer;
import io.stratixops.engine.Sprite;

import java.util.ArrayList;
import java.util.List;

/**
 * A fake {@link Renderer} that records the submission sequence, so
 * {@code SceneRenderer} can be tested headless.
 */
public final class RecordingRenderer implements Renderer {

    /** The last clear color, or {@code -1} when never cleared. */
    public int clearColor = -1;
    /** Ordered event log. */
    public final List<String> events = new ArrayList<>();
    /** Drawn sprites, in order. */
    public final List<Sprite> sprites = new ArrayList<>();
    /** Draw positions, parallel to {@link #sprites}. */
    public final List<float[]> positions = new ArrayList<>();
    /** The number of camera pushes. */
    public int pushes;
    /** The number of camera pops. */
    public int pops;
    /** The last pushed camera. */
    public Camera2D camera;

    @Override
    public void clear(int rgba) {
        this.clearColor = rgba;
        events.add("clear");
    }

    @Override
    public void draw(Sprite sprite, float x, float y) {
        sprites.add(sprite);
        positions.add(new float[]{x, y});
        events.add("draw");
    }

    @Override
    public void fillRect(float x, float y, float w, float h, int rgba) {
        events.add("fillRect");
    }

    @Override
    public void drawText(String text, float x, float y) {
        events.add("drawText");
    }

    @Override
    public void pushCamera(Camera2D pushed) {
        pushes++;
        camera = pushed;
        events.add("push");
    }

    @Override
    public void popCamera() {
        pops++;
        events.add("pop");
    }
}
