package io.stratixops.engine.render;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class RenderTargetLayoutTest {

    private static final float EPS = 1e-4f;

    @Test
    void exactAspectFillsAvailableArea() {
        RenderTargetLayout.Fit fit = RenderTargetLayout.fit(640, 360, 1280f, 720f);
        assertThat(fit.scale()).isCloseTo(2f, within(EPS));
        assertThat(fit.x()).isCloseTo(0f, within(EPS));
        assertThat(fit.y()).isCloseTo(0f, within(EPS));
        assertThat(fit.width()).isCloseTo(1280f, within(EPS));
        assertThat(fit.height()).isCloseTo(720f, within(EPS));
    }

    @Test
    void preservesAspectAndCentersWhenWider() {
        RenderTargetLayout.Fit fit = RenderTargetLayout.fit(640, 360, 1280f, 640f);
        // Height is the limiting axis: scale = 640 / 360.
        float expectedScale = 640f / 360f;
        assertThat(fit.scale()).isCloseTo(expectedScale, within(EPS));
        assertThat(fit.height()).isCloseTo(640f, within(EPS));
        assertThat(fit.width()).isCloseTo(640f * expectedScale, within(EPS));
        assertThat(fit.width() / fit.height()).isCloseTo(640f / 360f, within(EPS));
        assertThat(fit.x()).isCloseTo((1280f - fit.width()) / 2f, within(EPS));
        assertThat(fit.y()).isCloseTo(0f, within(EPS));
    }

    @Test
    void preservesAspectAndCentersWhenTaller() {
        RenderTargetLayout.Fit fit = RenderTargetLayout.fit(640, 360, 500f, 720f);
        // Width is the limiting axis.
        float expectedScale = 500f / 640f;
        assertThat(fit.scale()).isCloseTo(expectedScale, within(EPS));
        assertThat(fit.width()).isCloseTo(500f, within(EPS));
        assertThat(fit.height()).isCloseTo(360f * expectedScale, within(EPS));
        assertThat(fit.x()).isCloseTo(0f, within(EPS));
        assertThat(fit.y()).isCloseTo((720f - fit.height()) / 2f, within(EPS));
    }

    @Test
    void squareTargetKeepsSquarePixels() {
        RenderTargetLayout.Fit fit = RenderTargetLayout.fit(100, 100, 300f, 150f);
        assertThat(fit.scale()).isCloseTo(1.5f, within(EPS));
        assertThat(fit.width()).isCloseTo(fit.height(), within(EPS));
    }

    @Test
    void nonPositiveAvailableAreaYieldsEmptyFit() {
        RenderTargetLayout.Fit fit = RenderTargetLayout.fit(640, 360, 0f, 720f);
        assertThat(fit.scale()).isZero();
        assertThat(fit.width()).isZero();
        assertThat(fit.height()).isZero();
        assertThat(fit.x()).isZero();
        assertThat(fit.y()).isZero();
    }

    @Test
    void rejectsNonPositiveTarget() {
        assertThatIllegalArgumentException().isThrownBy(() -> RenderTargetLayout.fit(0, 360, 640f, 360f));
        assertThatIllegalArgumentException().isThrownBy(() -> RenderTargetLayout.fit(640, -1, 640f, 360f));
    }
}
