package io.stratixops.engine.editor.view;

import io.stratixops.engine.math.Vec2;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class GizmoMathTest {

    private static final float EPS = 1e-3f;

    @Test
    void moveSnapsToTheGrid() {
        assertThat(GizmoMath.snapMove(7f, true)).isEqualTo(0f);
        assertThat(GizmoMath.snapMove(9f, true)).isEqualTo(16f);
        assertThat(GizmoMath.snapMove(-25f, true)).isEqualTo(-32f);
        assertThat(GizmoMath.snapMove(7f, false)).isEqualTo(7f);
    }

    @Test
    void moveSnapsAVector() {
        Vec2 snapped = GizmoMath.snapMove(new Vec2(17f, 15f), true);
        assertThat(snapped.x).isEqualTo(16f);
        assertThat(snapped.y).isEqualTo(16f);
    }

    @Test
    void rotationSnapsToFifteenDegrees() {
        float radians = (float) Math.toRadians(20f);
        assertThat(GizmoMath.snapRotation(radians, true))
                .isCloseTo((float) Math.toRadians(15f), within(EPS));
        assertThat(GizmoMath.snapRotation(radians, false)).isCloseTo(radians, within(EPS));
    }

    @Test
    void scaleSnapsToTenths() {
        assertThat(GizmoMath.snapScale(1.23f, true)).isCloseTo(1.2f, within(EPS));
        assertThat(GizmoMath.snapScale(1.27f, true)).isCloseTo(1.3f, within(EPS));
        assertThat(GizmoMath.snapScale(1.23f, false)).isCloseTo(1.23f, within(EPS));
    }
}
