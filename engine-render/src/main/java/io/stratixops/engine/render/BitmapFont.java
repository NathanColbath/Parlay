package io.stratixops.engine.render;

import io.stratixops.engine.Font;
import io.stratixops.engine.render.font.BuiltInFontData;

/**
 * The built-in fixed-cell bitmap font. Glyph data is embedded (no user file is
 * required for {@code Assets.font("font.pixel")}) and rasterized into a nearest
 * -filtered texture at startup.
 */
final class BitmapFont implements Font {

    private final GlTexture texture;
    private final int cell;
    private final int columns;
    private final int firstChar;
    private final int lastChar;
    private final int advance;

    private BitmapFont(GlTexture texture, int cell, int columns, int firstChar, int lastChar, int advance) {
        this.texture = texture;
        this.cell = cell;
        this.columns = columns;
        this.firstChar = firstChar;
        this.lastChar = lastChar;
        this.advance = advance;
    }

    /**
     * Builds the built-in font. Requires a current GL context.
     *
     * @return the built-in font
     */
    static BitmapFont builtIn() {
        int[] pixels = BuiltInFontData.rasterize();
        GlTexture texture = GlTexture.fromPixels(
            pixels, BuiltInFontData.TEXTURE_WIDTH, BuiltInFontData.TEXTURE_HEIGHT, true);
        return new BitmapFont(texture, BuiltInFontData.CELL, BuiltInFontData.COLUMNS,
            BuiltInFontData.FIRST_CHAR, BuiltInFontData.LAST_CHAR, BuiltInFontData.ADVANCE);
    }

    @Override
    public float lineHeight() {
        return cell;
    }

    /**
     * Measures a single line.
     *
     * @param text text to measure
     * @return width in pixels
     */
    float textWidth(String text) {
        return text.length() * (float) advance;
    }

    GlTexture texture() {
        return texture;
    }

    /**
     * Queues the glyphs of the text. The origin is the top-left of the first
     * glyph; {@code '\n'} starts a new line one {@link #lineHeight()} down.
     *
     * @param text  text to draw
     * @param x     left edge
     * @param y     top edge
     * @param batch destination batch
     * @param rgba  packed text color
     */
    void draw(String text, float x, float y, SpriteBatch batch, int rgba) {
        float penX = x;
        float penY = y;
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if (character == '\n') {
                penX = x;
                penY += cell;
                continue;
            }
            if (character < firstChar || character > lastChar) {
                penX += advance;
                continue;
            }
            int index = character - firstChar;
            int col = index % columns;
            int row = index / columns;
            AtlasRegion region = new AtlasRegion(
                col * cell, row * cell, cell, cell, texture.width(), texture.height());
            batch.drawGlyph(texture.id(), penX, penY, cell, cell,
                region.u0(), region.v0(), region.u1(), region.v1(), rgba);
            penX += advance;
        }
    }

    void dispose() {
        texture.close();
    }
}
