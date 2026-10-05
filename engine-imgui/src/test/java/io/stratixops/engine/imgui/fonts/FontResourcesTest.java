package io.stratixops.engine.imgui.fonts;

import static org.assertj.core.api.Assertions.assertThat;

import io.stratixops.engine.imgui.glue.FontAtlas;
import io.stratixops.engine.imgui.theme.Icon;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * STR-36: the embedded faces are real and complete. Parses the shipped TTFs so
 * a missing glyph or a dropped asset fails the build instead of silently
 * falling back to ImGui's built-in font.
 */
class FontResourcesTest {

    private static final String DIR = "/io/stratixops/engine/imgui/fonts/";

    @Test
    void everyDesignSystemFaceIsPackaged() {
        for (String resource : FontAtlas.FONT_RESOURCES) {
            byte[] data = read(resource);
            assertThat(data).as(resource).isNotNull();
            assertThat(data.length).as(resource + " size").isGreaterThan(1000);
            assertThat(isSfnt(data)).as(resource + " has an sfnt header").isTrue();
        }
    }

    @Test
    void lucideSubsetCarriesEveryIconCodepoint() {
        TrueTypeCmap cmap = TrueTypeCmap.parse(read(FontAtlas.ICON_FONT));
        for (Icon icon : Icon.values()) {
            assertThat(cmap.contains(icon.codepoint()))
                    .as("lucide.ttf glyph for %s (U+%04X)", icon.name(), icon.codepoint())
                    .isTrue();
        }
    }

    @Test
    void uiFaceCoversEveryCharacterTheDemoDraws() {
        TrueTypeCmap cmap = TrueTypeCmap.parse(read(FontAtlas.UI_REGULAR));
        int[] required = {
            0x00B7, // middle dot
            0x2014, // em dash
            0x2026, // ellipsis
            0x203A, // single right angle quote (breadcrumb)
            0x2197, // north-east arrow (link modifier)
            0x25B2, // up triangle (sort)
            0x25BC, // down triangle (sort)
        };
        for (int codepoint : required) {
            assertThat(cmap.contains(codepoint))
                    .as("Inter-Regular covers U+%04X", codepoint)
                    .isTrue();
        }
    }

    @Test
    void licenseNoticesShipWithTheFonts() {
        Map<String, String> notices = Map.of(
                DIR + "LICENSE-Inter.txt", "SIL OPEN FONT LICENSE",
                DIR + "LICENSE-JetBrainsMono.txt", "SIL OPEN FONT LICENSE",
                DIR + "LICENSE-Lucide.txt", "ISC License");
        notices.forEach((resource, marker) -> {
            byte[] data = read(resource);
            assertThat(data).as(resource).isNotNull();
            assertThat(new String(data, StandardCharsets.UTF_8))
                    .as(resource + " notice text")
                    .contains(marker);
        });
    }

    private static boolean isSfnt(byte[] data) {
        int magic = ((data[0] & 0xFF) << 24) | ((data[1] & 0xFF) << 16)
                | ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
        return magic == 0x00010000 || magic == 0x74727565 || magic == 0x4F54544F;
    }

    private static byte[] read(String resource) {
        try (InputStream in = FontResourcesTest.class.getResourceAsStream(resource)) {
            return in == null ? null : in.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("cannot read " + resource, e);
        }
    }
}
