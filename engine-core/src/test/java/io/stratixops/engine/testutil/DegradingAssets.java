package io.stratixops.engine.testutil;

import io.stratixops.engine.Assets;
import io.stratixops.engine.Font;
import io.stratixops.engine.Sprite;
import io.stratixops.engine.TextureAtlas;

/**
 * Headless {@link Assets} double modelling the ADR-0001 runtime-content policy:
 * a missing or corrupt path degrades to a non-null placeholder (a magenta
 * stand-in sprite, an empty atlas, the built-in font) instead of throwing, so
 * gameplay never crashes.
 */
public final class DegradingAssets implements Assets {

    private final Sprite placeholder = new Sprite() {
        @Override
        public float width() {
            return 1f;
        }

        @Override
        public float height() {
            return 1f;
        }
    };

    private final Font font = () -> 8f;

    private final TextureAtlas emptyAtlas = name -> placeholder;

    @Override
    public Sprite sprite(String path) {
        return placeholder;
    }

    @Override
    public TextureAtlas atlas(String path) {
        return emptyAtlas;
    }

    @Override
    public Font font(String path) {
        return font;
    }
}
