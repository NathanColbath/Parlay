package io.stratixops.engine.imgui.drag;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Gesture behaviour of the shared drag state (design system 3.8): threshold,
 * modifiers, auto-scroll, auto-expand, snapping and Esc cancel with snap-back.
 */
class DragSessionTest {

    private static final DragPayload PAYLOAD = DragPayload.single("hierarchy", "player");

    @Test
    void pressBelowThresholdIsAClickNotADrag() {
        DragSession session = new DragSession();
        session.press(10f, 10f, 100f, 22f, PAYLOAD);
        assertThat(session.isPending()).isTrue();

        session.move(12f, 11f);
        assertThat(session.isDragging()).isFalse();

        assertThat(session.release()).isNull();
        assertThat(session.phase()).isEqualTo(DragSession.Phase.IDLE);
    }

    @Test
    void movePastThresholdStartsDrag() {
        DragSession session = new DragSession();
        session.press(10f, 10f, 100f, 22f, PAYLOAD);
        session.move(15f, 10f);
        assertThat(session.isDragging()).isTrue();
    }

    @Test
    void releaseReportsTargetZoneAndModifier() {
        DragSession session = new DragSession();
        drag(session);
        session.setModifier(DragModifier.COPY);
        session.hover(new DropTarget("row-b", DropZone.AFTER, 0f, 30f, 200f, 22f));

        DropResult result = session.release();
        assertThat(result).isNotNull();
        assertThat(result.targetId()).isEqualTo("row-b");
        assertThat(result.zone()).isEqualTo(DropZone.AFTER);
        assertThat(result.modifier()).isEqualTo(DragModifier.COPY);
        assertThat(result.payload()).isEqualTo(PAYLOAD);
        assertThat(result.accepted()).isTrue();
        assertThat(session.phase()).isEqualTo(DragSession.Phase.IDLE);
    }

    @Test
    void releaseOutsideAnyTargetIsANoOp() {
        DragSession session = new DragSession();
        drag(session);
        DropResult result = session.release();
        assertThat(result).isNotNull();
        assertThat(result.targetId()).isNull();
        assertThat(result.zone()).isEqualTo(DropZone.NONE);
        assertThat(result.accepted()).isFalse();
    }

    @Test
    void hoverIgnoredWhileOnlyPending() {
        DragSession session = new DragSession();
        session.press(10f, 10f, 100f, 22f, PAYLOAD);
        session.hover(new DropTarget("row-b", DropZone.INTO, 0f, 0f, 100f, 22f));
        assertThat(session.candidate()).isNull();
    }

    @Test
    void modifiersMapFromKeyState() {
        assertThat(DragModifier.of(false, false)).isEqualTo(DragModifier.MOVE);
        assertThat(DragModifier.of(true, false)).isEqualTo(DragModifier.COPY);
        assertThat(DragModifier.of(true, true)).isEqualTo(DragModifier.LINK);
        assertThat(DragModifier.MOVE.hasBadge()).isFalse();
        assertThat(DragModifier.COPY.badge()).isEqualTo("+");
        assertThat(DragModifier.LINK.badge()).isNotEmpty();
    }

    @Test
    void escapeCancelAnimatesBackToOrigin() {
        DragSession session = new DragSession();
        drag(session);
        session.move(100f, 120f);
        assertThat(session.ghostX()).isEqualTo(112f);
        assertThat(session.ghostY()).isEqualTo(132f);

        session.cancel(120f);
        assertThat(session.phase()).isEqualTo(DragSession.Phase.SNAPPING_BACK);
        assertThat(session.candidate()).isNull();

        session.advance(0.06f);
        assertThat(session.progress()).isBetween(0.4f, 0.6f);
        assertThat(session.ghostX()).isBetween(10f, 112f);

        session.advance(0.2f);
        assertThat(session.phase()).isEqualTo(DragSession.Phase.IDLE);
    }

    @Test
    void instantCancelResetsImmediately() {
        DragSession session = new DragSession();
        drag(session);
        session.cancel(0f);
        assertThat(session.isActive()).isFalse();
    }

    @Test
    void cancelDuringPendingJustResets() {
        DragSession session = new DragSession();
        session.press(1f, 1f, 10f, 10f, PAYLOAD);
        session.cancel(120f);
        assertThat(session.phase()).isEqualTo(DragSession.Phase.IDLE);
    }

    @Test
    void autoScrollActivatesInsideTheEightPixelBand() {
        DragSession session = new DragSession();
        drag(session);
        assertThat(session.autoScroll(4f, 0f, 100f)).isEqualTo(-8f);
        assertThat(session.autoScroll(97f, 0f, 100f)).isEqualTo(8f);
        assertThat(session.autoScroll(50f, 0f, 100f)).isZero();
    }

    @Test
    void autoScrollIsInactiveWhenNotDragging() {
        DragSession session = new DragSession();
        assertThat(session.autoScroll(1f, 0f, 100f)).isZero();
    }

    @Test
    void collapsedFolderAutoExpandsAfterFiveHundredMilliseconds() {
        DragSession session = new DragSession();
        drag(session);

        assertThat(session.hoverFolder("assets", true, 0.2f)).isFalse();
        assertThat(session.hoverFolder("assets", true, 0.2f)).isFalse();
        assertThat(session.hoverFolder("assets", true, 0.2f)).isTrue();
        assertThat(session.consumeAutoExpand()).isEqualTo("assets");
        assertThat(session.consumeAutoExpand()).isNull();
    }

    @Test
    void expandedOrMissingFolderNeverAutoExpands() {
        DragSession session = new DragSession();
        drag(session);
        assertThat(session.hoverFolder("assets", false, 1f)).isFalse();
        assertThat(session.hoverFolder(null, true, 1f)).isFalse();
    }

    @Test
    void hoveringANewFolderResetsTheDwell() {
        DragSession session = new DragSession();
        drag(session);
        session.hoverFolder("a", true, 0.4f);
        assertThat(session.hoverFolder("b", true, 0.2f)).isFalse();
        assertThat(session.hoverFolder("b", true, 0.4f)).isTrue();
        assertThat(session.consumeAutoExpand()).isEqualTo("b");
    }

    @Test
    void beginFrameClearsTheCandidate() {
        DragSession session = new DragSession();
        drag(session);
        session.hover(new DropTarget("row", DropZone.INTO, 0f, 0f, 10f, 10f));
        assertThat(session.candidate()).isNotNull();
        session.beginFrame();
        assertThat(session.candidate()).isNull();
    }

    private static void drag(DragSession session) {
        session.press(0f, 0f, 100f, 22f, PAYLOAD);
        session.move(10f, 10f);
        assertThat(session.isDragging()).isTrue();
    }
}
