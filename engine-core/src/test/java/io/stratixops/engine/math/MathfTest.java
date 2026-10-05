package io.stratixops.engine.math;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class MathfTest {

    @Test
    void clampFloatBoundsBothEnds() {
        assertThat(Mathf.clamp(5f, 0f, 10f)).isEqualTo(5f);
        assertThat(Mathf.clamp(-1f, 0f, 10f)).isEqualTo(0f);
        assertThat(Mathf.clamp(11f, 0f, 10f)).isEqualTo(10f);
    }

    @Test
    void clampIntBoundsBothEnds() {
        assertThat(Mathf.clamp(5, 0, 10)).isEqualTo(5);
        assertThat(Mathf.clamp(-1, 0, 10)).isEqualTo(0);
        assertThat(Mathf.clamp(11, 0, 10)).isEqualTo(10);
    }

    @Test
    void clampRejectsInvertedRange() {
        assertThatIllegalArgumentException().isThrownBy(() -> Mathf.clamp(1f, 2f, 1f));
        assertThatIllegalArgumentException().isThrownBy(() -> Mathf.clamp(1, 2, 1));
    }

    @Test
    void lerpInterpolatesAndExtrapolates() {
        assertThat(Mathf.lerp(0f, 10f, 0f)).isEqualTo(0f);
        assertThat(Mathf.lerp(0f, 10f, 0.5f)).isEqualTo(5f);
        assertThat(Mathf.lerp(0f, 10f, 1f)).isEqualTo(10f);
        assertThat(Mathf.lerp(0f, 10f, 2f)).isEqualTo(20f);
    }

    @Test
    void approachStepsTowardTargetWithoutOvershooting() {
        assertThat(Mathf.approach(0f, 10f, 3f)).isEqualTo(3f);
        assertThat(Mathf.approach(9f, 10f, 3f)).isEqualTo(10f);
        assertThat(Mathf.approach(10f, 0f, 3f)).isEqualTo(7f);
        assertThat(Mathf.approach(1f, 0f, 3f)).isEqualTo(0f);
        assertThat(Mathf.approach(5f, 5f, 3f)).isEqualTo(5f);
    }

    @Test
    void approachRejectsNegativeDelta() {
        assertThatIllegalArgumentException().isThrownBy(() -> Mathf.approach(0f, 1f, -1f));
    }
}
