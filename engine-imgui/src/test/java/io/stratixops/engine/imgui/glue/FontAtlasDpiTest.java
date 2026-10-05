package io.stratixops.engine.imgui.glue;

import static org.assertj.core.api.Assertions.assertThat;

import io.stratixops.engine.imgui.theme.Theme;
import io.stratixops.engine.imgui.theme.ThemeLoader;
import io.stratixops.engine.imgui.theme.ThemeMode;
import org.junit.jupiter.api.Test;

/**
 * STR-36 / design system 5.4: the atlas is rasterized on the supported DPI grid
 * (including the fractional 1.25x target) at whole-pixel sizes so line height
 * and baseline stay on the physical pixel grid.
 */
class FontAtlasDpiTest {

    @Test
    void supportedScalesIncludeTheFractionalTargets() {
        assertThat(FontAtlas.DPI_SCALES).containsExactly(1.0f, 1.25f, 1.5f, 2.0f);
    }

    @Test
    void nearestScaleSnapsToTheSupportedGrid() {
        assertThat(FontAtlas.nearestScale(1.0f)).isEqualTo(1.0f);
        assertThat(FontAtlas.nearestScale(1.1f)).isEqualTo(1.0f);
        assertThat(FontAtlas.nearestScale(1.25f)).isEqualTo(1.25f);
        assertThat(FontAtlas.nearestScale(1.3f)).isEqualTo(1.25f);
        assertThat(FontAtlas.nearestScale(1.5f)).isEqualTo(1.5f);
        assertThat(FontAtlas.nearestScale(2.0f)).isEqualTo(2.0f);
        assertThat(FontAtlas.nearestScale(2.5f)).isEqualTo(2.0f);
    }

    @Test
    void rasterizedPixelSizesAreWholePixels() {
        Theme theme = ThemeLoader.load(ThemeMode.DARK);
        float[] sizes = {
            theme.typography.TYPE_MD.size(),
            theme.typography.TYPE_LG.size(),
            theme.typography.TYPE_MONO.size(),
            theme.icon.ICON_MD
        };
        for (float scale : FontAtlas.DPI_SCALES) {
            for (float size : sizes) {
                int snapped = FontAtlas.snapPixels(size * scale);
                assertThat(snapped).as("size=%s scale=%s", size, scale)
                        .isGreaterThanOrEqualTo(1);
                assertThat((float) snapped).as("size=%s scale=%s", size, scale)
                        .isEqualTo(Math.round(size * scale));
            }
        }
    }

    @Test
    void snapNeverCollapsesToZero() {
        assertThat(FontAtlas.snapPixels(0f)).isEqualTo(1);
        assertThat(FontAtlas.snapPixels(0.4f)).isEqualTo(1);
    }
}
