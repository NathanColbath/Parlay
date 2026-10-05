package io.stratixops.engine.render;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class LogicalViewportLayoutTest {

    @Test
    void exactMultipleFillsWindow() {
        LogicalViewportLayout.Rect rect = LogicalViewportLayout.compute(1280, 720, 640, 360);
        assertThat(rect.scale()).isEqualTo(2);
        assertThat(rect.x()).isZero();
        assertThat(rect.y()).isZero();
        assertThat(rect.width()).isEqualTo(1280);
        assertThat(rect.height()).isEqualTo(720);
    }

    @Test
    void letterboxesAndCenters() {
        LogicalViewportLayout.Rect rect = LogicalViewportLayout.compute(800, 600, 640, 360);
        assertThat(rect.scale()).isEqualTo(1);
        assertThat(rect.width()).isEqualTo(640);
        assertThat(rect.height()).isEqualTo(360);
        assertThat(rect.x()).isEqualTo(80);
        assertThat(rect.y()).isEqualTo(120);
    }

    @Test
    void scaleIsNeverBelowOne() {
        LogicalViewportLayout.Rect rect = LogicalViewportLayout.compute(320, 180, 640, 360);
        assertThat(rect.scale()).isEqualTo(1);
        assertThat(rect.width()).isEqualTo(640);
        assertThat(rect.x()).isEqualTo(-160);
    }

    @Test
    void picksSmallestAxisScale() {
        LogicalViewportLayout.Rect rect = LogicalViewportLayout.compute(1920, 1000, 640, 360);
        assertThat(rect.scale()).isEqualTo(2);
        assertThat(rect.width()).isEqualTo(1280);
        assertThat(rect.height()).isEqualTo(720);
    }

    @Test
    void rejectsNonPositiveWindow() {
        assertThatIllegalArgumentException().isThrownBy(() -> LogicalViewportLayout.compute(0, 720, 640, 360));
    }

    @Test
    void rejectsNonPositiveLogical() {
        assertThatIllegalArgumentException().isThrownBy(() -> LogicalViewportLayout.compute(1280, 720, 0, 360));
    }
}
