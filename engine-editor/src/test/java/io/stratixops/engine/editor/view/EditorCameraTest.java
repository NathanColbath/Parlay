package io.stratixops.engine.editor.view;

import io.stratixops.engine.math.Vec2;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class EditorCameraTest {

    private static final float EPS = 1e-3f;

    @Test
    void centreMapsToViewportCentre() {
        EditorCamera camera = new EditorCamera();
        camera.setViewport(800f, 600f);
        camera.centerOn(100f, 50f);

        Vec2 world = camera.screenToWorld(400f, 300f);
        assertThat(world.x).isCloseTo(100f, within(EPS));
        assertThat(world.y).isCloseTo(50f, within(EPS));

        Vec2 screen = camera.worldToScreen(100f, 50f);
        assertThat(screen.x).isCloseTo(400f, within(EPS));
        assertThat(screen.y).isCloseTo(300f, within(EPS));
    }

    @Test
    void panMovesCameraByScreenDeltaOverZoom() {
        EditorCamera camera = new EditorCamera();
        camera.setViewport(800f, 600f);
        camera.setZoom(2f);

        camera.panScreen(40f, -20f);

        assertThat(camera.position().x).isCloseTo(-20f, within(EPS));
        assertThat(camera.position().y).isCloseTo(10f, within(EPS));
    }

    @Test
    void zoomKeepsAnchorWorldPointFixed() {
        EditorCamera camera = new EditorCamera();
        camera.setViewport(800f, 600f);
        camera.centerOn(0f, 0f);

        Vec2 before = camera.screenToWorld(200f, 150f);
        camera.zoomBy(2f, 200f, 150f);
        Vec2 after = camera.screenToWorld(200f, 150f);

        assertThat(camera.zoom()).isCloseTo(2f, within(EPS));
        assertThat(after.x).isCloseTo(before.x, within(EPS));
        assertThat(after.y).isCloseTo(before.y, within(EPS));
        assertThat(camera.worldToScreen(before.x, before.y).x).isCloseTo(200f, within(EPS));
    }

    @Test
    void zoomIsClamped() {
        EditorCamera camera = new EditorCamera();
        camera.setZoom(0.0001f);
        assertThat(camera.zoom()).isEqualTo(EditorCamera.MIN_ZOOM);
        camera.setZoom(100000f);
        assertThat(camera.zoom()).isEqualTo(EditorCamera.MAX_ZOOM);
    }

    @Test
    void frameCentresAndFitsBoundsWithPadding() {
        EditorCamera camera = new EditorCamera();
        camera.setViewport(800f, 600f);

        camera.frame(new ViewRect(0f, 0f, 100f, 50f), 0.1f);

        assertThat(camera.position().x).isCloseTo(50f, within(EPS));
        assertThat(camera.position().y).isCloseTo(25f, within(EPS));
        // Width 100 * 1.2 = 120 -> 800/120 = 6.667; height 50 * 1.2 = 60 -> 10.
        assertThat(camera.zoom()).isCloseTo(800f / 120f, within(EPS));
    }

    @Test
    void visibleBoundsTracksPanAndZoom() {
        EditorCamera camera = new EditorCamera();
        camera.setViewport(200f, 100f);
        camera.centerOn(0f, 0f);

        ViewRect visible = camera.visibleBounds();
        assertThat(visible.minX()).isCloseTo(-100f, within(EPS));
        assertThat(visible.maxX()).isCloseTo(100f, within(EPS));
        assertThat(visible.minY()).isCloseTo(-50f, within(EPS));
        assertThat(visible.maxY()).isCloseTo(50f, within(EPS));
    }
}
