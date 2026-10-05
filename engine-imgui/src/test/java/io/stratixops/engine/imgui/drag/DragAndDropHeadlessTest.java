package io.stratixops.engine.imgui.drag;

import static org.assertj.core.api.Assertions.assertThat;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiKey;
import io.stratixops.engine.imgui.HeadlessUi;
import io.stratixops.engine.imgui.theme.Theme;
import io.stratixops.engine.imgui.theme.ThemeLoader;
import io.stratixops.engine.imgui.theme.ThemeMode;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Drives {@link DragAndDrop} through the headless ImGui host and asserts both
 * the drop outcome and that the overlay renders without a display.
 */
class DragAndDropHeadlessTest {

    private static final Theme THEME = ThemeLoader.load(ThemeMode.DARK);

    @Test
    void dragAcrossRowsProducesAReorderDropResult() {
        try (HeadlessUi ui = new HeadlessUi(800f, 600f, THEME)) {
            DragAndDrop drag = new DragAndDrop();
            ImGuiIO io = ImGui.getIO();

            // Frame 1: press on row-a.
            io.addMousePosEvent(20f, 20f);
            io.addMouseButtonEvent(0, true);
            ui.beginFrame();
            drag.beginFrame(THEME);
            drag.source(DragPayload.single("hierarchy", "a"), true, 0f, 10f, 200f, 22f);
            assertThat(drag.endFrame(800f)).isNull();
            ui.endFrame();
            assertThat(drag.isActive()).isTrue();

            // Frame 2: move past the threshold and over row-b.
            io.addMousePosEvent(20f, 55f);
            ui.beginFrame();
            drag.beginFrame(THEME);
            drag.endFrame(800f);
            ui.endFrame();
            assertThat(drag.isDragging()).isTrue();

            // Frame 3: register the target and release.
            io.addMouseButtonEvent(0, false);
            ui.beginFrame();
            drag.beginFrame(THEME);
            drag.target("row-b", 0f, 44f, 200f, 22f, false, true);
            DropResult result = drag.endFrame(800f);
            ui.endFrame();

            assertThat(result).isNotNull();
            assertThat(result.targetId()).isEqualTo("row-b");
            assertThat(result.zone()).isEqualTo(DropZone.AFTER);
            assertThat(result.modifier()).isEqualTo(DragModifier.MOVE);
            assertThat(result.accepted()).isTrue();
            assertThat(ui.lastFrameValid()).isTrue();
        }
    }

    @Test
    void pressWithoutMovementReleasesAsAClick() {
        try (HeadlessUi ui = new HeadlessUi(800f, 600f, THEME)) {
            DragAndDrop drag = new DragAndDrop();
            ImGuiIO io = ImGui.getIO();

            io.addMousePosEvent(15f, 15f);
            io.addMouseButtonEvent(0, true);
            ui.beginFrame();
            drag.beginFrame(THEME);
            drag.source(DragPayload.single("hierarchy", "a"), true, 0f, 10f, 200f, 22f);
            assertThat(drag.endFrame(800f)).isNull();
            ui.endFrame();

            io.addMouseButtonEvent(0, false);
            ui.beginFrame();
            drag.beginFrame(THEME);
            assertThat(drag.endFrame(800f)).isNull();
            ui.endFrame();

            assertThat(drag.isActive()).isFalse();
            assertThat(ui.lastFrameValid()).isTrue();
        }
    }

    @Test
    void escapeCancelsTheDrag() {
        try (HeadlessUi ui = new HeadlessUi(800f, 600f, THEME)) {
            DragAndDrop drag = new DragAndDrop();
            ImGuiIO io = ImGui.getIO();

            io.addMousePosEvent(20f, 20f);
            io.addMouseButtonEvent(0, true);
            ui.beginFrame();
            drag.beginFrame(THEME);
            drag.source(DragPayload.single("hierarchy", "a"), true, 0f, 10f, 200f, 22f);
            drag.endFrame(800f);
            ui.endFrame();

            io.addMousePosEvent(40f, 60f);
            io.addKeyEvent(ImGuiKey.Escape, true);
            ui.beginFrame();
            drag.beginFrame(THEME);
            drag.endFrame(800f);
            ui.endFrame();

            assertThat(drag.isDragging()).isFalse();
            assertThat(drag.session().phase()).isEqualTo(DragSession.Phase.SNAPPING_BACK);
            assertThat(ui.lastFrameValid()).isTrue();
        }
    }

    @Test
    void overlayReplaysCommandsHeadlessly() {
        try (HeadlessUi ui = new HeadlessUi(800f, 600f, THEME)) {
            DragSession session = new DragSession();
            session.press(0f, 0f, 120f, 22f, new DragPayload("project", List.of("x", "y")));
            session.move(30f, 40f);
            session.hover(new DropTarget("folder", DropZone.INTO, 0f, 90f, 220f, 22f));

            ui.beginFrame();
            List<DrawCommand> commands = DragVisuals.build(THEME, session, 800f);
            DragOverlay.drawCommands(commands);
            ui.endFrame();

            assertThat(commands).isNotEmpty();
            assertThat(ui.lastFrameValid()).isTrue();
        }
    }
}
