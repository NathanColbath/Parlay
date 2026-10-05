package io.stratixops.engine.render;

import static org.assertj.core.api.Assertions.assertThat;

import io.stratixops.engine.render.font.BuiltInFontData;
import org.junit.jupiter.api.Test;

class BuiltInFontDataTest {

    @Test
    void textureHoldsThePrintableAsciiRange() {
        assertThat(BuiltInFontData.TEXTURE_WIDTH).isEqualTo(128);
        assertThat(BuiltInFontData.TEXTURE_HEIGHT).isEqualTo(48);
        assertThat(BuiltInFontData.rasterize()).hasSize(128 * 48);
    }

    @Test
    void spaceGlyphIsEmpty() {
        byte[] space = BuiltInFontData.glyph(' ');
        assertThat(space).hasSize(8);
        assertThat(space).containsOnly((byte) 0);
    }

    @Test
    void unsupportedCharactersReturnNull() {
        assertThat(BuiltInFontData.glyph(0x1F)).isNull();
        assertThat(BuiltInFontData.glyph(0x7F)).isNull();
    }

    @Test
    void rasterizesKnownGlyphPixels() {
        int[] pixels = BuiltInFontData.rasterize();
        int index = 'A' - BuiltInFontData.FIRST_CHAR;
        int col = index % BuiltInFontData.COLUMNS;
        int row = index / BuiltInFontData.COLUMNS;
        int cellX = col * BuiltInFontData.CELL;
        int cellY = row * BuiltInFontData.CELL;
        int bit2 = pixels[(cellY) * BuiltInFontData.TEXTURE_WIDTH + cellX + 2];
        assertThat(bit2).isEqualTo(0xFFFFFFFF);
        assertThat(pixels[0]).isZero();
    }

    @Test
    void rasterizeIsDeterministic() {
        assertThat(BuiltInFontData.rasterize()).isEqualTo(BuiltInFontData.rasterize());
    }
}
