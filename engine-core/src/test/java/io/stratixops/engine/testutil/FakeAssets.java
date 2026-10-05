package io.stratixops.engine.testutil;

import io.stratixops.engine.Assets;
import io.stratixops.engine.Font;
import io.stratixops.engine.Sprite;
import io.stratixops.engine.TextureAtlas;

/** Minimal {@link Assets} fake returning fixed-size handles. */
public final class FakeAssets implements Assets {

    private final Sprite sprite = new Sprite() {
        @Override
        public float width() {
            return 16f;
        }

        @Override
        public float height() {
            return 16f;
        }
    };

    private final Font font = () -> 8f;

    private final TextureAtlas atlas = new TextureAtlas() {
        @Override
        public Sprite region(String name) {
            return sprite;
        }
    };

    @Override
    public Sprite sprite(String path) {
        return sprite;
    }

    @Override
    public TextureAtlas atlas(String path) {
        return atlas;
    }

    @Override
    public Font font(String path) {
        return font;
    }
}
