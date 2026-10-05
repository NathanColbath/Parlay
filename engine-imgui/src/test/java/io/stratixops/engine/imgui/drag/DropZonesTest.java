package io.stratixops.engine.imgui.drag;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Pointer-to-zone resolution and the 8 px snap tolerance (design system 3.8). */
class DropZonesTest {

    private static final float ROW_Y = 100f;
    private static final float ROW_H = 22f;

    @Test
    void pointerOutsideTheRowIsNoTarget() {
        assertThat(DropZones.resolve(50f, ROW_Y, ROW_H, true, true)).isEqualTo(DropZone.NONE);
        assertThat(DropZones.resolve(200f, ROW_Y, ROW_H, true, true)).isEqualTo(DropZone.NONE);
    }

    @Test
    void nearEdgesSnapToInsertionZones() {
        assertThat(DropZones.resolve(ROW_Y + 3f, ROW_Y, ROW_H, true, true))
                .isEqualTo(DropZone.BEFORE);
        assertThat(DropZones.resolve(ROW_Y + ROW_H - 3f, ROW_Y, ROW_H, true, true))
                .isEqualTo(DropZone.AFTER);
    }

    @Test
    void middleOfARowThatAcceptsChildrenIsInto() {
        assertThat(DropZones.resolve(ROW_Y + ROW_H * 0.5f, ROW_Y, ROW_H, true, true))
                .isEqualTo(DropZone.INTO);
    }

    @Test
    void nonContainerSplitsTheRowInHalf() {
        assertThat(DropZones.resolve(ROW_Y + 4f, ROW_Y, ROW_H, false, true))
                .isEqualTo(DropZone.BEFORE);
        assertThat(DropZones.resolve(ROW_Y + ROW_H - 1f, ROW_Y, ROW_H, false, true))
                .isEqualTo(DropZone.AFTER);
    }

    @Test
    void inactionableTargetIsInvalid() {
        assertThat(DropZones.resolve(ROW_Y + ROW_H * 0.5f, ROW_Y, ROW_H, true, false))
                .isEqualTo(DropZone.INVALID);
    }

    @Test
    void zeroSnapUsesTheHalfwaySplit() {
        assertThat(DropZones.resolve(ROW_Y + 1f, ROW_Y, ROW_H, true, true, 0f))
                .isEqualTo(DropZone.BEFORE);
        assertThat(DropZones.resolve(ROW_Y + ROW_H - 1f, ROW_Y, ROW_H, true, true, 0f))
                .isEqualTo(DropZone.AFTER);
    }

    @Test
    void insertionIndexClampsToTheListBounds() {
        assertThat(DropZones.insertionIndex(DropZone.BEFORE, 2, 5)).isEqualTo(2);
        assertThat(DropZones.insertionIndex(DropZone.AFTER, 2, 5)).isEqualTo(3);
        assertThat(DropZones.insertionIndex(DropZone.AFTER, 5, 5)).isEqualTo(5);
        assertThat(DropZones.insertionIndex(DropZone.BEFORE, 0, 5)).isEqualTo(0);
    }

    @Test
    void dropTargetExposesRectAndInsertionY() {
        DropTarget before = DropTarget.row("a", DropZone.BEFORE, 1f, 2f, 3f, 4f);
        assertThat(before.rect()).containsExactly(1f, 2f, 3f, 4f);
        assertThat(before.insertionY(null)).isEqualTo(2f);

        DropTarget after = DropTarget.row("a", DropZone.AFTER, 1f, 2f, 3f, 4f);
        assertThat(after.insertionY(null)).isEqualTo(2f + 4f - DragConfig.INSERTION_THICKNESS);
    }
}
