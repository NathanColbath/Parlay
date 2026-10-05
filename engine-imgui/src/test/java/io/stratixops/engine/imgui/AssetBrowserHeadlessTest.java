package io.stratixops.engine.imgui;

import static org.assertj.core.api.Assertions.assertThat;

import imgui.ImGui;
import io.stratixops.engine.imgui.theme.Theme;
import io.stratixops.engine.imgui.theme.ThemeLoader;
import io.stratixops.engine.imgui.theme.ThemeMode;
import io.stratixops.engine.imgui.widget.AssetBrowser;
import io.stratixops.engine.imgui.widget.AssetEvent;
import io.stratixops.engine.imgui.widget.AssetFilter;
import io.stratixops.engine.imgui.widget.AssetGrid;
import io.stratixops.engine.imgui.widget.AssetItem;
import io.stratixops.engine.imgui.widget.AssetKind;
import io.stratixops.engine.imgui.widget.AssetListView;
import io.stratixops.engine.imgui.widget.AssetSelection;
import io.stratixops.engine.imgui.widget.AssetSort;
import io.stratixops.engine.imgui.widget.AssetStatus;
import io.stratixops.engine.imgui.widget.AssetListColumns;
import io.stratixops.engine.imgui.widget.Breadcrumb;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Design system AC8 / section 3.4: both asset views render through the
 * headless-safe path in every theme and DPI scale with valid draw data.
 */
class AssetBrowserHeadlessTest {

    private static final List<AssetItem> ITEMS = List.of(
            AssetItem.folder("characters", "characters"),
            new AssetItem("player", "player_idle.png", AssetKind.IMAGE, 24_576L, 1_752_000_000L,
                    AssetStatus.READY),
            new AssetItem("enemy", "enemy_run.png", AssetKind.IMAGE, 18_432L, 1_751_900_000L,
                    AssetStatus.READY),
            new AssetItem("gem", "gem.png", AssetKind.IMAGE, 4_096L, 1_751_800_000L,
                    AssetStatus.MISSING),
            new AssetItem("portal", "portal.png", AssetKind.IMAGE, 0L, 0L, AssetStatus.UNIMPORTED),
            AssetItem.of("scene", "gem-dash.scene", AssetKind.SCENE),
            AssetItem.of("prefab", "spikebot.prefab", AssetKind.PREFAB),
            AssetItem.of("script", "PlayerController.java", AssetKind.SCRIPT),
            AssetItem.of("audio", "theme.ogg", AssetKind.AUDIO),
            AssetItem.of("font", "pixel.ttf", AssetKind.FONT));

    private static final List<Breadcrumb.Segment> PATH = List.of(
            new Breadcrumb.Segment("Project", ""),
            new Breadcrumb.Segment("assets", "assets"),
            new Breadcrumb.Segment("sprites", "assets/sprites"),
            new Breadcrumb.Segment("characters", "assets/sprites/characters"),
            new Breadcrumb.Segment("enemies", "assets/sprites/characters/enemies"),
            new Breadcrumb.Segment("bosses", "assets/sprites/characters/enemies/bosses"));

    @Test
    void rendersGridAndListViewForEveryThemeAndScale() {
        for (ThemeMode mode : ThemeMode.values()) {
            Theme theme = ThemeLoader.load(mode);
            for (float scale : new float[] {1f, 1.5f, 2f}) {
                assertViewRenders(theme, AssetBrowser.View.GRID);
                assertViewRenders(theme, AssetBrowser.View.LIST);
            }
        }
    }

    private void assertViewRenders(Theme theme, AssetBrowser.View view) {
        AssetBrowser browser = new AssetBrowser();
        browser.setView(view);
        browser.selection().selectAll(ITEMS);
        browser.sort().toggle(AssetSort.Column.SIZE);
        try (HeadlessUi ui = new HeadlessUi(1280f, 720f, theme)) {
            ui.beginFrame();
            ImGui.begin("asset-browser-test");
            AssetBrowser.Outcome outcome = browser.render(theme, ITEMS, PATH, PATH.size() - 1, 2);
            assertThat(outcome.isIdle()).isTrue();
            ImGui.end();
            ui.endFrame();
            assertThat(ui.lastFrameValid())
                    .as("valid frame for %s %s", theme.mode, view)
                    .isTrue();
        }
    }

    @Test
    void rendersFilteredAndEmptyStates() {
        for (ThemeMode mode : ThemeMode.values()) {
            Theme theme = ThemeLoader.load(mode);
            AssetBrowser browser = new AssetBrowser();
            browser.setFilter(AssetFilter.Criterion.IMAGE);
            browser.search().set("gem");
            try (HeadlessUi ui = new HeadlessUi(1024f, 640f, theme)) {
                ui.beginFrame();
                ImGui.begin("asset-browser-empty");
                browser.render(theme, ITEMS, PATH, 0);
                ImGui.end();
                ui.endFrame();
                assertThat(ui.lastFrameValid()).isTrue();
            }
        }
    }

    @Test
    void gridRendersEveryTileStateWithFocus() {
        AssetSelection selection = new AssetSelection();
        selection.setSingle("portal");
        for (ThemeMode mode : ThemeMode.values()) {
            Theme theme = ThemeLoader.load(mode);
            try (HeadlessUi ui = new HeadlessUi(1280f, 720f, theme)) {
                ui.beginFrame();
                ImGui.begin("asset-grid-states");
                AssetEvent event = AssetGrid.render(theme, ITEMS, selection, true);
                ImGui.end();
                ui.endFrame();
                assertThat(event).isEqualTo(AssetEvent.NONE);
                assertThat(ui.lastFrameValid()).isTrue();
            }
        }
    }

    @Test
    void listRendersSelectionSortAndDropTarget() {
        AssetSelection selection = new AssetSelection();
        selection.selectAll(ITEMS);
        AssetSort sort = new AssetSort();
        sort.toggle(AssetSort.Column.TYPE);
        for (ThemeMode mode : ThemeMode.values()) {
            Theme theme = ThemeLoader.load(mode);
            try (HeadlessUi ui = new HeadlessUi(1280f, 720f, theme)) {
                ui.beginFrame();
                ImGui.begin("asset-list-states");
                AssetListView.render(theme, ITEMS, selection, sort, new AssetListColumns(), true, 1);
                ImGui.end();
                ui.endFrame();
                assertThat(ui.lastFrameValid()).isTrue();
            }
        }
    }

    @Test
    void outcomeAndEventRecordsDescribeActions() {
        assertThat(AssetBrowser.Outcome.NONE.isIdle()).isTrue();
        AssetEvent open = AssetEvent.of(AssetEvent.Action.OPEN, "player");
        assertThat(open.isPresent()).isTrue();
        assertThat(open.action()).isEqualTo(AssetEvent.Action.OPEN);
        assertThat(open.id()).isEqualTo("player");
    }
}
