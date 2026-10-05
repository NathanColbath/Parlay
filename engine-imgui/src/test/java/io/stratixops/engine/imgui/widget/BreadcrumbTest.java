package io.stratixops.engine.imgui.widget;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Design system 3.4: breadcrumb overflow collapses the middle. */
class BreadcrumbTest {

    private static final float CHEVRON = 10f;
    private static final float ELLIPSIS = 20f;

    @Test
    void emptyPathHasNoCut() {
        assertThat(Breadcrumb.overflow(new float[0], 100f, CHEVRON, ELLIPSIS).ellipsis()).isFalse();
    }

    @Test
    void singleSegmentNeverOverflows() {
        Breadcrumb.Cut cut = Breadcrumb.overflow(new float[] {50f}, 10f, CHEVRON, ELLIPSIS);
        assertThat(cut.firstTail()).isEqualTo(1);
        assertThat(cut.ellipsis()).isFalse();
    }

    @Test
    void fittingPathShowsEverySegment() {
        Breadcrumb.Cut cut = Breadcrumb.overflow(new float[] {50f, 50f, 50f}, 1000f, CHEVRON,
                ELLIPSIS);
        assertThat(cut.firstTail()).isEqualTo(1);
        assertThat(cut.ellipsis()).isFalse();
    }

    @Test
    void overflowKeepsRootEllipsisAndTheTailThatFits() {
        Breadcrumb.Cut cut = Breadcrumb.overflow(new float[] {50f, 50f, 50f}, 140f, CHEVRON,
                ELLIPSIS);
        assertThat(cut.ellipsis()).isTrue();
        assertThat(cut.firstTail()).isEqualTo(2);
    }

    @Test
    void veryNarrowBarShowsRootAndEllipsisOnly() {
        Breadcrumb.Cut cut = Breadcrumb.overflow(new float[] {50f, 50f, 50f}, 100f, CHEVRON,
                ELLIPSIS);
        assertThat(cut.ellipsis()).isTrue();
        assertThat(cut.firstTail()).isEqualTo(3);
    }

    @Test
    void segmentAndResultCarryTheirValues() {
        Breadcrumb.Segment segment = new Breadcrumb.Segment("sprites", "assets/sprites");
        assertThat(segment.label()).isEqualTo("sprites");
        assertThat(segment.path()).isEqualTo("assets/sprites");
        assertThat(new Breadcrumb.Result(segment).activated()).isEqualTo(segment);
    }
}
