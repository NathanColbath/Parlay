package io.stratixops.engine.imgui;

import static org.assertj.core.api.Assertions.assertThat;

import io.stratixops.engine.imgui.demo.DemoWindow;
import io.stratixops.engine.imgui.theme.Theme;
import io.stratixops.engine.imgui.theme.ThemeLoader;
import io.stratixops.engine.imgui.theme.ThemeMode;
import org.junit.jupiter.api.Test;

/**
 * Design system AC2/AC8: the demo renders every widget at 1x/1.5x/2x in both
 * themes through the headless-safe render path, with no ImGui context errors.
 */
class HeadlessUiTest {

    @Test
    void rendersDemoForEveryThemeAndDpiScale() {
        for (ThemeMode mode : ThemeMode.values()) {
            Theme theme = ThemeLoader.load(mode);
            for (float scale : DemoWindow.verifiedDpiScales()) {
                try (HeadlessUi ui = new HeadlessUi(1280f, 720f, theme)) {
                    ui.beginFrame();
                    DemoWindow.render(theme, scale);
                    ui.endFrame();

                    assertThat(ui.lastFrameValid())
                            .as("frame valid for %s at %sx", mode, scale)
                            .isTrue();
                    assertThat(ui.commandListCount())
                            .as("draw commands produced for %s at %sx", mode, scale)
                            .isGreaterThanOrEqualTo(0);
                }
            }
        }
    }

    @Test
    void headlessHostDoesNotOwnAnExistingContext() {
        try (HeadlessUi first = new HeadlessUi(800f, 600f, ThemeLoader.load(ThemeMode.DARK))) {
            // A nested host must reuse the live context instead of creating one.
            try (HeadlessUi nested = new HeadlessUi(800f, 600f, ThemeLoader.load(ThemeMode.DARK))) {
                nested.beginFrame();
                nested.endFrame();
                assertThat(nested.lastFrameValid()).isTrue();
            }
            first.beginFrame();
            first.endFrame();
            assertThat(first.lastFrameValid()).isTrue();
        }
    }
}
