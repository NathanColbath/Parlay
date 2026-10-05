package io.stratixops.engine.imgui.widget;

import static org.assertj.core.api.Assertions.assertThat;

import imgui.ImGui;
import imgui.flag.ImGuiKey;
import io.stratixops.engine.imgui.HeadlessUi;
import io.stratixops.engine.imgui.theme.Icon;
import io.stratixops.engine.imgui.theme.Theme;
import io.stratixops.engine.imgui.theme.ThemeLoader;
import io.stratixops.engine.imgui.theme.ThemeMode;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Design system 3.7: context menu rows, submenus, states and rendering. */
class ContextMenuTest {

    private static final List<ContextMenu.Entry> ENTRIES = List.of(
            ContextMenu.Entry.item("duplicate", "Duplicate", Icon.DUPLICATE, "Ctrl+D"),
            ContextMenu.Entry.submenu("add", "Add Component", Icon.ADD, List.of(
                    ContextMenu.Entry.item("sprite", "Sprite Renderer", Icon.SPRITE_RENDERER),
                    ContextMenu.Entry.separator(),
                    ContextMenu.Entry.item("audio", "AudioSource", Icon.AUDIO_SOURCE))),
            ContextMenu.Entry.separator(),
            ContextMenu.Entry.checkable("gizmos", "Show gizmos", Icon.SNAP, true),
            ContextMenu.Entry.radio("mode-move", "Move", Icon.GIZMO_MOVE, true),
            ContextMenu.Entry.radio("mode-rotate", "Rotate", Icon.GIZMO_ROTATE, false),
            ContextMenu.Entry.disabled("delete", "Delete", Icon.DELETE, "Locked by prefab"));

    @Test
    void selectableLabelsSkipSeparators() {
        assertThat(ContextMenu.selectableLabels(ENTRIES)).containsExactly(
                "Duplicate", "Add Component", "Show gizmos", "Move", "Rotate", "Delete");
    }

    @Test
    void rendersOpenMenuAndForcedSubmenu() {
        Theme theme = ThemeLoader.load(ThemeMode.DARK);
        ContextMenu.State state = new ContextMenu.State();
        state.openAt(40f, 60f);
        state.openSubmenu = 1;
        state.submenuVisible = true;
        state.submenuAnchorX = 240f;
        state.submenuAnchorY = 96f;
        state.parentLeft = 40f;

        try (HeadlessUi ui = new HeadlessUi(1280f, 720f, theme)) {
            ui.beginFrame();
            String activated = ContextMenu.render(theme, state, ENTRIES);
            ui.endFrame();

            assertThat(activated).isNull();
            assertThat(state.open).isTrue();
            assertThat(ui.lastFrameValid()).isTrue();
            assertThat(ui.commandListCount()).isGreaterThan(0);
        }
    }

    @Test
    void downArrowMovesSelection() {
        Theme theme = ThemeLoader.load(ThemeMode.DARK);
        ContextMenu.State state = new ContextMenu.State();
        state.openAt(40f, 60f);
        state.selected = 0;

        try (HeadlessUi ui = new HeadlessUi(800f, 600f, theme)) {
            ui.beginFrame();
            ContextMenu.render(theme, state, ENTRIES);
            ui.endFrame();
            assertThat(state.selected).isZero();

            ImGui.getIO().addKeyEvent(ImGuiKey.DownArrow, true);
            ui.beginFrame();
            ContextMenu.render(theme, state, ENTRIES);
            ui.endFrame();
            ImGui.getIO().addKeyEvent(ImGuiKey.DownArrow, false);

            assertThat(state.selected).isEqualTo(1);
        }
    }

    @Test
    void closedMenuRendersNothing() {
        Theme theme = ThemeLoader.load(ThemeMode.LIGHT);
        ContextMenu.State state = new ContextMenu.State();

        try (HeadlessUi ui = new HeadlessUi(800f, 600f, theme)) {
            ui.beginFrame();
            String activated = ContextMenu.render(theme, state, ENTRIES);
            ui.endFrame();

            assertThat(activated).isNull();
        }
    }

    @Test
    void typeAheadResolvesToFirstMatchingLabel() {
        List<String> labels = ContextMenu.selectableLabels(ENTRIES);

        assertThat(Ui.typeAheadIndex(labels, "ro")).isEqualTo(4);
        assertThat(Ui.typeAheadIndex(labels, "zz")).isEqualTo(-1);
    }
}
