package io.stratixops.engine.imgui.widget;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Design system 3.4: grid wrap geometry stays deterministic. */
class AssetGridLayoutTest {

    private static final float TILE = 96f;
    private static final float GAP = 8f;
    private static final AssetGridLayout LAYOUT = new AssetGridLayout(TILE, GAP, 0f);

    @Test
    void columnsWrapToTheAvailableWidth() {
        assertThat(LAYOUT.columns(96f)).isEqualTo(1);
        assertThat(LAYOUT.columns(50f)).isEqualTo(1);
        assertThat(LAYOUT.columns(200f)).isEqualTo(2);
        assertThat(LAYOUT.columns(500f)).isEqualTo(4);
    }

    @Test
    void tilePositionsAdvanceByTilePlusGap() {
        assertThat(LAYOUT.tileX(0, 3, 10f)).isEqualTo(10f);
        assertThat(LAYOUT.tileX(1, 3, 10f)).isEqualTo(10f + TILE + GAP);
        assertThat(LAYOUT.tileY(0, 3, 5f)).isEqualTo(5f);
        assertThat(LAYOUT.tileY(3, 3, 5f)).isEqualTo(5f + LAYOUT.cellHeight() + GAP);
    }

    @Test
    void hitTestingMapsPointsToIndices() {
        assertThat(LAYOUT.indexAt(0f, 0f, 2)).isEqualTo(0);
        assertThat(LAYOUT.indexAt(120f, 0f, 2)).isEqualTo(1);
        assertThat(LAYOUT.indexAt(0f, 120f, 2)).isEqualTo(2);
        assertThat(LAYOUT.indexAt(120f, 120f, 2)).isEqualTo(3);
    }

    @Test
    void hitTestingRejectsGapsGuttersAndOutside() {
        assertThat(LAYOUT.indexAt(100f, 0f, 2)).isEqualTo(-1);
        assertThat(LAYOUT.indexAt(300f, 0f, 2)).isEqualTo(-1);
        assertThat(LAYOUT.indexAt(-1f, 0f, 2)).isEqualTo(-1);
        assertThat(LAYOUT.indexAt(0f, -1f, 2)).isEqualTo(-1);
        assertThat(LAYOUT.indexAt(0f, 0f, 0)).isEqualTo(-1);
    }

    @Test
    void contentHeightCountsRowsAndGaps() {
        assertThat(LAYOUT.contentHeight(0, 2)).isZero();
        assertThat(LAYOUT.contentHeight(2, 2)).isEqualTo(LAYOUT.cellHeight());
        assertThat(LAYOUT.contentHeight(5, 2))
                .isEqualTo(3 * LAYOUT.cellHeight() + 2 * GAP);
    }

    @Test
    void labelHeightIsAddedToCellHeight() {
        AssetGridLayout labelled = new AssetGridLayout(TILE, GAP, 20f);
        assertThat(labelled.cellHeight()).isEqualTo(TILE + GAP + 20f);
    }
}
