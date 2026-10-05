package io.stratixops.engine.testutil;

import io.stratixops.engine.Sprite;

/**
 * A headless {@link Sprite} handle for tests.
 */
public final class TestSprite implements Sprite {

    private final float width;
    private final float height;

    /**
     * @param width  sprite width
     * @param height sprite height
     */
    public TestSprite(float width, float height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public float width() {
        return width;
    }

    @Override
    public float height() {
        return height;
    }
}
