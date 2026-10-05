package io.stratixops.engine.editor.view;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ViewGridTest {

    @Test
    void baseSpacingAtUnitZoom() {
        assertThat(ViewGrid.spacing(1f)).isEqualTo(32f);
    }

    @Test
    void spacingDoublesWhenZoomedFarOut() {
        // 32 * 0.1 = 3.2 < 8 -> 64 * 0.1 = 6.4 < 8 -> 128 * 0.1 = 12.8 >= 8.
        assertThat(ViewGrid.spacing(0.1f)).isEqualTo(128f);
    }

    @Test
    void verticalLinesIncludeNearestLinesInsideRange() {
        List<Float> lines = ViewGrid.verticalLines(-40f, 40f, 32f);
        assertThat(lines).containsExactly(-32f, 0f, 32f);
    }

    @Test
    void majorEveryFifthLine() {
        assertThat(ViewGrid.isMajor(0f, 32f)).isTrue();
        assertThat(ViewGrid.isMajor(32f, 32f)).isFalse();
        assertThat(ViewGrid.isMajor(160f, 32f)).isTrue();
        assertThat(ViewGrid.isMajor(-160f, 32f)).isTrue();
    }

    @Test
    void noLinesForDegenerateSpacing() {
        assertThat(ViewGrid.verticalLines(0f, 100f, 0f)).isEmpty();
    }
}
