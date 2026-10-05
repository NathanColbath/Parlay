package io.stratixops.engine.imgui.widget;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Design system 3.4: list column sorting and the folder-first rule. */
class AssetSortTest {

    private static final AssetItem FOLDER = AssetItem.folder("zzz-folder", "zzz-folder");
    private static final AssetItem IMAGE_A =
            new AssetItem("a", "a.png", AssetKind.IMAGE, 100L, 3L, AssetStatus.READY);
    private static final AssetItem IMAGE_B =
            new AssetItem("b", "b.png", AssetKind.IMAGE, 200L, 2L, AssetStatus.READY);
    private static final AssetItem SCRIPT_C =
            new AssetItem("c", "c.java", AssetKind.SCRIPT, 50L, 1L, AssetStatus.READY);

    private static final List<AssetItem> ITEMS = List.of(IMAGE_B, SCRIPT_C, FOLDER, IMAGE_A);

    private static List<String> ids(List<AssetItem> items) {
        return items.stream().map(AssetItem::id).toList();
    }

    @Test
    void defaultsToNameAscendingWithFoldersFirst() {
        AssetSort sort = new AssetSort();
        assertThat(sort.column()).isEqualTo(AssetSort.Column.NAME);
        assertThat(sort.direction()).isEqualTo(AssetSort.Direction.ASC);
        assertThat(ids(sort.sort(ITEMS))).containsExactly("zzz-folder", "a", "b", "c");
    }

    @Test
    void togglingSameColumnFlipsDirection() {
        AssetSort sort = new AssetSort();
        sort.toggle(AssetSort.Column.NAME);
        assertThat(sort.direction()).isEqualTo(AssetSort.Direction.DESC);
        assertThat(ids(sort.sort(ITEMS))).containsExactly("zzz-folder", "c", "b", "a");
    }

    @Test
    void togglingNewColumnStartsAscending() {
        AssetSort sort = new AssetSort();
        sort.toggle(AssetSort.Column.SIZE);
        assertThat(sort.direction()).isEqualTo(AssetSort.Direction.ASC);
        assertThat(ids(sort.sort(ITEMS))).containsExactly("zzz-folder", "c", "a", "b");
    }

    @Test
    void sortsByModifiedDescending() {
        AssetSort sort = new AssetSort();
        sort.toggle(AssetSort.Column.MODIFIED);
        sort.toggle(AssetSort.Column.MODIFIED);
        assertThat(ids(sort.sort(ITEMS))).containsExactly("zzz-folder", "a", "b", "c");
    }

    @Test
    void sortsByTypeThenName() {
        AssetSort sort = new AssetSort();
        sort.toggle(AssetSort.Column.TYPE);
        assertThat(ids(sort.sort(ITEMS))).containsExactly("zzz-folder", "a", "b", "c");
    }

    @Test
    void comparatorNeverMutatesTheInput() {
        AssetSort sort = new AssetSort();
        sort.sort(ITEMS);
        assertThat(ids(ITEMS)).containsExactly("b", "c", "zzz-folder", "a");
    }
}
