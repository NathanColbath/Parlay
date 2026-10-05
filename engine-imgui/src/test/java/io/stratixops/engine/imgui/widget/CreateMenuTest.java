package io.stratixops.engine.imgui.widget;

import static org.assertj.core.api.Assertions.assertThat;

import io.stratixops.engine.imgui.HeadlessUi;
import io.stratixops.engine.imgui.theme.Icon;
import io.stratixops.engine.imgui.theme.Theme;
import io.stratixops.engine.imgui.theme.ThemeLoader;
import io.stratixops.engine.imgui.theme.ThemeMode;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Design system 3.3: searchable create menu ordering, filtering and rendering. */
class CreateMenuTest {

    private static final List<CreateMenu.Item> ITEMS = List.of(
            CreateMenu.Item.quick("empty", "Create Empty", Icon.ADD),
            CreateMenu.Item.of("empty-child", "Create Empty Child", "Create", Icon.CREATE_CHILD),
            CreateMenu.Item.action("sprite", "Sprite Renderer", "Rendering",
                    Icon.SPRITE_RENDERER, "Ctrl+1"),
            CreateMenu.Item.disabled("animator", "Animator", "Animation", Icon.ANIMATOR,
                    "No clip assigned"),
            CreateMenu.Item.of("camera", "Camera", "Rendering", Icon.CAMERA));

    private static List<String> labels(List<CreateMenu.Option> options) {
        return options.stream()
                .filter(option -> option.item() != null)
                .map(option -> option.item().label())
                .toList();
    }

    @Test
    void emptyQueryPinsQuickActionsThenGroupsThenRecents() {
        List<CreateMenu.Option> options = CreateMenu.buildOptions(ITEMS, "",
                List.of("camera"));

        assertThat(labels(options)).startsWith("Create Empty");
        assertThat(labels(options)).endsWith("Camera");
        assertThat(options).anyMatch(option -> "Recent".equals(option.header()));
        assertThat(options).anyMatch(option -> "Create".equals(option.header()));
    }

    @Test
    void fuzzyQueryMatchesNameInCaseInsensitiveOrder() {
        List<CreateMenu.Option> options = CreateMenu.buildOptions(ITEMS, "SPR", List.of());

        assertThat(labels(options)).containsExactly("Sprite Renderer");
        CreateMenu.Option row = options.stream()
                .filter(option -> option.item() != null)
                .findFirst()
                .orElseThrow();
        assertThat(row.match()).isNotNull();
        assertThat(row.match().positions()).containsExactly(0, 1, 2);
    }

    @Test
    void queryMatchesCategoryWhenNameDoesNot() {
        List<CreateMenu.Option> options = CreateMenu.buildOptions(ITEMS, "anim", List.of());

        assertThat(labels(options)).contains("Animator");
        assertThat(options.get(0).match()).isNull();
    }

    @Test
    void noMatchProducesNoRows() {
        assertThat(CreateMenu.buildOptions(ITEMS, "zzzz", List.of())).isEmpty();
    }

    @Test
    void rendersOpenMenuAndProducesDrawData() {
        Theme theme = ThemeLoader.load(ThemeMode.DARK);
        CreateMenu.State state = new CreateMenu.State();
        state.openAt(40f, 60f);

        try (HeadlessUi ui = new HeadlessUi(1280f, 720f, theme)) {
            ui.beginFrame();
            String activated = CreateMenu.render(theme, state, ITEMS);
            ui.endFrame();

            assertThat(activated).isNull();
            assertThat(ui.lastFrameValid()).isTrue();
            assertThat(ui.commandListCount()).isGreaterThan(0);
        }
    }

    @Test
    void closedMenuRendersNothing() {
        Theme theme = ThemeLoader.load(ThemeMode.LIGHT);
        CreateMenu.State state = new CreateMenu.State();

        try (HeadlessUi ui = new HeadlessUi(800f, 600f, theme)) {
            ui.beginFrame();
            String activated = CreateMenu.render(theme, state, ITEMS);
            ui.endFrame();

            assertThat(activated).isNull();
            assertThat(ui.lastFrameValid()).isTrue();
        }
    }
}
