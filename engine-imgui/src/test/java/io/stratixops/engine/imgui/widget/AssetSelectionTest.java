package io.stratixops.engine.imgui.widget;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Design system 3.4: multi-select pointer and keyboard model. */
class AssetSelectionTest {

    private static final List<AssetItem> ITEMS = List.of(
            AssetItem.of("a", "a", AssetKind.IMAGE),
            AssetItem.of("b", "b", AssetKind.IMAGE),
            AssetItem.of("c", "c", AssetKind.IMAGE),
            AssetItem.of("d", "d", AssetKind.IMAGE));

    @Test
    void plainClickSelectsOnlyTheTarget() {
        AssetSelection selection = new AssetSelection();
        selection.click(ITEMS, "b", false, false);
        assertThat(selection.ids()).containsExactly("b");
        assertThat(selection.cursor()).isEqualTo("b");
        assertThat(selection.anchor()).isEqualTo("b");
        assertThat(selection.size()).isEqualTo(1);
        assertThat(selection.isEmpty()).isFalse();
    }

    @Test
    void ctrlClickTogglesMembership() {
        AssetSelection selection = new AssetSelection();
        selection.setSingle("a");
        selection.click(ITEMS, "c", true, false);
        assertThat(selection.ids()).containsExactly("a", "c");
        assertThat(selection.cursor()).isEqualTo("c");
        selection.click(ITEMS, "a", true, false);
        assertThat(selection.ids()).containsExactly("c");
    }

    @Test
    void shiftClickExtendsForwardFromAnchor() {
        AssetSelection selection = new AssetSelection();
        selection.setSingle("b");
        selection.click(ITEMS, "d", false, true);
        assertThat(selection.ids()).containsExactly("b", "c", "d");
        assertThat(selection.anchor()).isEqualTo("b");
        assertThat(selection.cursor()).isEqualTo("d");
    }

    @Test
    void shiftClickExtendsBackwardsFromAnchor() {
        AssetSelection selection = new AssetSelection();
        selection.setSingle("d");
        selection.click(ITEMS, "b", false, true);
        assertThat(selection.ids()).containsExactly("b", "c", "d");
        assertThat(selection.anchor()).isEqualTo("d");
    }

    @Test
    void selectAllThenShiftRangeCollapsesToTheRange() {
        AssetSelection selection = new AssetSelection();
        selection.selectAll(ITEMS);
        assertThat(selection.size()).isEqualTo(4);
        assertThat(selection.cursor()).isEqualTo("d");
        selection.click(ITEMS, "b", false, true);
        assertThat(selection.ids()).containsExactly("a", "b");
    }

    @Test
    void arrowMoveWithoutShiftCollapsesSelection() {
        AssetSelection selection = new AssetSelection();
        selection.setSingle("a");
        assertThat(selection.move(ITEMS, 1, false)).isEqualTo("b");
        assertThat(selection.ids()).containsExactly("b");
    }

    @Test
    void arrowMoveWithShiftExtendsTheRange() {
        AssetSelection selection = new AssetSelection();
        selection.setSingle("a");
        selection.move(ITEMS, 1, true);
        assertThat(selection.ids()).containsExactly("a", "b");
        selection.move(ITEMS, 1, true);
        assertThat(selection.ids()).containsExactly("a", "b", "c");
        assertThat(selection.anchor()).isEqualTo("a");
    }

    @Test
    void moveClampsAtTheEdges() {
        AssetSelection selection = new AssetSelection();
        selection.setSingle("d");
        assertThat(selection.move(ITEMS, 5, false)).isEqualTo("d");
        selection.setSingle("a");
        assertThat(selection.move(ITEMS, -5, false)).isEqualTo("a");
    }

    @Test
    void moveWithNoCursorStartsAtFirstOrLastForShift() {
        AssetSelection selection = new AssetSelection();
        assertThat(selection.move(ITEMS, 0, false)).isEqualTo("a");
        AssetSelection shifted = new AssetSelection();
        assertThat(shifted.move(ITEMS, 0, true)).isEqualTo("d");
    }

    @Test
    void moveOnEmptyListReturnsNull() {
        assertThat(new AssetSelection().move(List.of(), 1, false)).isNull();
    }

    @Test
    void rangeToUnknownIdFallsBackToSingle() {
        AssetSelection selection = new AssetSelection();
        selection.rangeTo(ITEMS, "missing");
        assertThat(selection.ids()).containsExactly("missing");
    }

    @Test
    void clearAndToggleManageCursorAndAnchor() {
        AssetSelection selection = new AssetSelection();
        selection.setSingle("a");
        selection.toggle("a");
        assertThat(selection.isEmpty()).isTrue();
        assertThat(selection.cursor()).isEqualTo("a");
        selection.clear();
        assertThat(selection.cursor()).isNull();
        assertThat(selection.anchor()).isNull();
    }

    @Test
    void containsIgnoresNull() {
        assertThat(new AssetSelection().contains(null)).isFalse();
    }
}
