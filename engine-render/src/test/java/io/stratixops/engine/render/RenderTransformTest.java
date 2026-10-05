package io.stratixops.engine.render;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import io.stratixops.engine.math.Vec2;
import org.junit.jupiter.api.Test;

class RenderTransformTest {

    private static final float EPS = 1e-5f;

    @Test
    void cameraCenterMapsToNdcOrigin() {
        float[] m = new float[16];
        RenderTransform.ortho(320f, 180f, 640f, 360f, m);
        assertThat(m[0] * 320f + m[12]).isCloseTo(0f, within(EPS));
        assertThat(m[5] * 180f + m[13]).isCloseTo(0f, within(EPS));
    }

    @Test
    void topLeftWorldMapsToTopLeftNdc() {
        float[] m = new float[16];
        RenderTransform.ortho(320f, 180f, 640f, 360f, m);
        assertThat(m[0] * 0f + m[12]).isCloseTo(-1f, within(EPS));
        assertThat(m[5] * 0f + m[13]).isCloseTo(1f, within(EPS));
    }

    @Test
    void bottomRightWorldMapsToBottomRightNdc() {
        float[] m = new float[16];
        RenderTransform.ortho(320f, 180f, 640f, 360f, m);
        assertThat(m[0] * 640f + m[12]).isCloseTo(1f, within(EPS));
        assertThat(m[5] * 360f + m[13]).isCloseTo(-1f, within(EPS));
    }

    @Test
    void worldToNdcAgreesWithMatrix() {
        float[] m = new float[16];
        RenderTransform.ortho(100f, 50f, 640f, 360f, m);
        Vec2 out = new Vec2();
        RenderTransform.worldToNdc(140f, 90f, 100f, 50f, 640f, 360f, out);
        assertThat(out.x).isCloseTo(m[0] * 140f + m[12], within(EPS));
        assertThat(out.y).isCloseTo(m[5] * 90f + m[13], within(EPS));
    }

    @Test
    void cameraCenteredOnOriginMapsOriginToNdcCenter() {
        Vec2 out = new Vec2();
        RenderTransform.worldToNdc(0f, 0f, 0f, 0f, 640f, 360f, out);
        assertThat(out.x).isCloseTo(0f, within(EPS));
        assertThat(out.y).isCloseTo(0f, within(EPS));
    }

    @Test
    void topLeftOfViewportMapsToNdcCorner() {
        Vec2 out = new Vec2();
        RenderTransform.worldToNdc(0f, 0f, 320f, 180f, 640f, 360f, out);
        assertThat(out.x).isCloseTo(-1f, within(EPS));
        assertThat(out.y).isCloseTo(1f, within(EPS));
    }
}
