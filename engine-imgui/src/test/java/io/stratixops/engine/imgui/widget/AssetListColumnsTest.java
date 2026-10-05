package io.stratixops.engine.imgui.widget;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Design system 3.4: list column widths, boundaries and resizing. */
class AssetListColumnsTest {

    @Test
    void startsAtDocumentedDefaults() {
        AssetListColumns columns = new AssetListColumns();
        assertThat(columns.typeWidth()).isEqualTo(80f);
        assertThat(columns.sizeWidth()).isEqualTo(80f);
        assertThat(columns.modifiedWidth()).isEqualTo(140f);
    }

    @Test
    void nameColumnTakesTheRemainder() {
        AssetListColumns columns = new AssetListColumns();
        assertThat(columns.nameWidth(600f)).isEqualTo(600f - 80f - 80f - 140f);
    }

    @Test
    void nameColumnNeverCollapsesBelowTheMinimum() {
        AssetListColumns columns = new AssetListColumns();
        assertThat(columns.nameWidth(100f)).isEqualTo(AssetListColumns.MIN_WIDTH);
    }

    @Test
    void columnStartsFollowTheFlexibleNameWidth() {
        AssetListColumns columns = new AssetListColumns();
        float[] starts = columns.columnStarts(600f);
        assertThat(starts).containsExactly(0f, 300f, 380f, 460f);
    }

    @Test
    void handleHitTestsColumnBoundaries() {
        AssetListColumns columns = new AssetListColumns();
        float available = 600f;
        float[] starts = columns.columnStarts(available);
        assertThat(columns.handleAt(starts[1], available)).isZero();
        assertThat(columns.handleAt(starts[2], available)).isEqualTo(1);
        assertThat(columns.handleAt(starts[3], available)).isEqualTo(2);
        assertThat(columns.handleAt(10f, available)).isEqualTo(-1);
    }

    @Test
    void resizeAdjustsTheHandleColumnAndClamps() {
        AssetListColumns columns = new AssetListColumns();
        columns.resize(0, 40f);
        assertThat(columns.typeWidth()).isEqualTo(120f);
        columns.resize(1, -100f);
        assertThat(columns.sizeWidth()).isEqualTo(AssetListColumns.MIN_WIDTH);
        columns.resize(2, 1000f);
        assertThat(columns.modifiedWidth()).isEqualTo(AssetListColumns.MAX_WIDTH);
        columns.resize(9, 50f);
        assertThat(columns.modifiedWidth()).isEqualTo(AssetListColumns.MAX_WIDTH);
    }

    @Test
    void resetRestoresDefaults() {
        AssetListColumns columns = new AssetListColumns();
        columns.resize(0, 40f);
        columns.reset();
        assertThat(columns.typeWidth()).isEqualTo(80f);
    }
}
