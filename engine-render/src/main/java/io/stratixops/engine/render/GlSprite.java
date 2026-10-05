package io.stratixops.engine.render;

import io.stratixops.engine.Sprite;

/**
 * A {@link Sprite} backed by a GL texture and an optional sub-region. Holds the
 * texture coordinates (already half-texel inset for atlas regions) so the
 * renderer does no per-draw math.
 */
final class GlSprite implements Sprite {

    private final GlTexture texture;
    private final float width;
    private final float height;
    private final float u0;
    private final float v0;
    private final float u1;
    private final float v1;

    private GlSprite(GlTexture texture, float width, float height,
                     float u0, float v0, float u1, float v1) {
        this.texture = texture;
        this.width = width;
        this.height = height;
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
    }

    /**
     * Creates a sprite covering a whole texture.
     *
     * @param texture backing texture
     * @return the sprite
     */
    static GlSprite whole(GlTexture texture) {
        return new GlSprite(texture, texture.width(), texture.height(), 0f, 0f, 1f, 1f);
    }

    /**
     * Creates a sprite for an atlas region.
     *
     * @param texture backing texture
     * @param region  region (provides size and inset UVs)
     * @return the sprite
     */
    static GlSprite ofRegion(GlTexture texture, AtlasRegion region) {
        return new GlSprite(texture, region.width(), region.height(),
            region.u0(), region.v0(), region.u1(), region.v1());
    }

    GlTexture texture() {
        return texture;
    }

    float u0() {
        return u0;
    }

    float v0() {
        return v0;
    }

    float u1() {
        return u1;
    }

    float v1() {
        return v1;
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
