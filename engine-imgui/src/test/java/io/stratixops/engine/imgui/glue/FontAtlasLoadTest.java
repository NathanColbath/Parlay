package io.stratixops.engine.imgui.glue;

import static org.assertj.core.api.Assertions.assertThat;

import imgui.ImFont;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.type.ImInt;
import io.stratixops.engine.imgui.theme.Icon;
import io.stratixops.engine.imgui.theme.Theme;
import io.stratixops.engine.imgui.theme.ThemeLoader;
import io.stratixops.engine.imgui.theme.ThemeMode;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

/**
 * STR-36: the real Inter / JetBrains Mono / Lucide faces load into ImGui's font
 * atlas and the atlas builds and renders, proving a packaged build never falls
 * back to the built-in font. Per-glyph completeness is pinned against the
 * shipped TTF cmap in {@code FontResourcesTest}. CPU-only (ImGui core), so it
 * runs headlessly.
 */
class FontAtlasLoadTest {

    @Test
    void designSystemFacesLoadWithoutFallbackAndBuildAnAtlas() {
        ImGui.createContext();
        try {
            ImGuiIO io = ImGui.getIO();
            io.setIniFilename(null);
            io.setDisplaySize(1280f, 720f);
            io.setDeltaTime(1f / 60f);
            Theme theme = ThemeLoader.load(ThemeMode.DARK);

            boolean loaded = FontAtlas.load(io.getFonts(), theme, 1.25f);
            assertThat(loaded).as("design-system faces present, no fallback").isTrue();

            ImInt width = new ImInt();
            ImInt height = new ImInt();
            ByteBuffer pixels = io.getFonts().getTexDataAsRGBA32(width, height);
            assertThat(pixels).as("atlas texture built").isNotNull();
            assertThat(width.get()).isGreaterThan(0);
            assertThat(height.get()).isGreaterThan(0);

            ImGui.newFrame();
            ImFont font = ImGui.getFont();
            assertThat(font).as("default font").isNotNull();
            assertThat(font.isLoaded()).as("font rasterized").isTrue();

            // The merged Lucide glyphs are addressable from the default face.
            float iconWidth = font.calcTextSizeAX(16f, Float.MAX_VALUE, 0f,
                    String.valueOf((char) Icon.PLAY.codepoint()));
            assertThat(iconWidth).as("Lucide glyph resolves in the default face")
                    .isGreaterThan(0f);
            for (Icon icon : Icon.values()) {
                font.calcTextSizeAX(16f, Float.MAX_VALUE, 0f, String.valueOf((char) icon.codepoint()));
            }

            ImGui.render();
            assertThat(ImGui.getDrawData().getValid()).as("frame produces draw data").isTrue();
        } finally {
            ImGui.destroyContext();
        }
    }
}
